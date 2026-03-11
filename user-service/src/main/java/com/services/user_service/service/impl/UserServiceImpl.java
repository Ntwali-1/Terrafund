package com.services.user_service.service.impl;

import com.services.user_service.dto.*;
import com.services.user_service.entity.User;
import com.services.user_service.entity.UserActivity;
import com.services.user_service.entity.UserRole;
import com.services.user_service.entity.VerificationToken;
import com.services.user_service.enums.Role;
import com.services.user_service.repository.UserRepository;
import com.services.user_service.repository.UserRoleRepository;
import com.services.user_service.repository.VerificationTokenRepository;
import com.services.user_service.security.JwtService;
import com.services.user_service.service.EmailService;
import com.services.user_service.service.UserActivityService;
import com.services.user_service.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserRoleRepository userRoleRepository;
    private final VerificationTokenRepository verificationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final UserActivityService activityService;
    private final EmailService emailService;

    @Autowired
    public UserServiceImpl(
            UserRepository userRepository,
            UserRoleRepository userRoleRepository,
            VerificationTokenRepository verificationTokenRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService,
            EmailService emailService,
            @Lazy UserActivityService activityService) {
        this.userRepository = userRepository;
        this.userRoleRepository = userRoleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.emailService = emailService;
        this.activityService = activityService;
    }

    @Override
    @Transactional
    public UserResponse signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email already registered");
        }

        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setFullName(request.getFullName());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setIsVerified(false);
        user.setIsActive(true);

        User savedUser = userRepository.save(user);

        // Generate 4-digit numeric OTP
        String token = String.format("%04d", new Random().nextInt(10000));
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(savedUser);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        // Log activity
        activityService.logActivity(
                savedUser.getId(),
                UserActivity.ActivityType.SIGNUP,
                "User signed up",
                "USER",
                savedUser.getId(),
                null,
                null
        );

        // Send OTP verification email
        emailService.sendVerificationEmail(savedUser.getEmail(), token);

        return mapToUserResponse(savedUser);
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new BadCredentialsException("Invalid email or password");
        }

        if (!user.getIsActive()) {
            throw new RuntimeException("Account is deactivated");
        }

        String token = jwtService.generateToken(user);
        user.setJwt(token);
        userRepository.save(user);

        // Log activity
        activityService.logActivity(
                user.getId(),
                UserActivity.ActivityType.LOGIN,
                "User logged in",
                "USER",
                user.getId(),
                null,
                null
        );

        Set<Role> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());

        return new LoginResponse(token, mapToUserResponse(user));
    }

    @Override
    @Transactional
    public UserResponse selectRole(Long userId, Role role) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if user already has this role
        if (userRoleRepository.existsByUserIdAndRole(userId, role)) {
            throw new RuntimeException("User already has this role");
        }

        // Add new role
        UserRole userRole = new UserRole();
        userRole.setRole(role);
        user.addRole(userRole);

        // Generate new JWT token with updated roles
        String newToken = jwtService.generateToken(user);
        user.setJwt(newToken);

        userRepository.save(user);

        // Refresh user to get updated roles
        user = userRepository.findById(userId).get();

        return mapToUserResponse(user);
    }

    @Override
    public UserResponse getCurrentUser(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return mapToUserResponse(user);
    }

    @Override
    @Transactional
    public UserResponse updateUser(Long userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }

        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }

        User updatedUser = userRepository.save(user);

        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public void verifyEmail(String token) {
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (verificationToken.isExpired()) {
            throw new RuntimeException("Verification token has expired");
        }

        User user = verificationToken.getUser();
        user.setIsVerified(true);
        userRepository.save(user);

        // Log activity
        activityService.logActivity(
                user.getId(),
                UserActivity.ActivityType.EMAIL_VERIFIED,
                "Email verified successfully",
                "USER",
                user.getId(),
                null,
                null
        );

        // Delete the used token
        verificationTokenRepository.delete(verificationToken);
    }

    @Override
    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getIsVerified()) {
            throw new RuntimeException("Email already verified");
        }

        // Delete old token if exists
        verificationTokenRepository.findByUserId(user.getId())
                .ifPresent(verificationTokenRepository::delete);

        // Create new 4-digit OTP
        String token = String.format("%04d", new Random().nextInt(10000));
        VerificationToken verificationToken = new VerificationToken();
        verificationToken.setToken(token);
        verificationToken.setUser(user);
        verificationToken.setExpiryDate(LocalDateTime.now().plusHours(24));
        verificationTokenRepository.save(verificationToken);

        // Send OTP verification email
        emailService.sendVerificationEmail(user.getEmail(), token);
    }

    private UserResponse mapToUserResponse(User user) {
        UserResponse response = new UserResponse();
        response.setId(user.getId());
        response.setEmail(user.getEmail());
        response.setFullName(user.getFullName());
        response.setPhoneNumber(user.getPhoneNumber());
        response.setIsVerified(user.getIsVerified());
        response.setJwtToken(user.getJwt());
        response.setIsActive(user.getIsActive());
        response.setCreatedAt(user.getCreatedAt());
        response.setUpdatedAt(user.getUpdatedAt());

        // Profile enhancements
        response.setProfilePictureUrl(user.getProfilePictureUrl());
        response.setBio(user.getBio());
        response.setDateOfBirth(user.getDateOfBirth());
        
        // Address
        response.setCountry(user.getCountry());
        response.setProvince(user.getProvince());
        response.setDistrict(user.getDistrict());
        response.setSector(user.getSector());
        response.setCell(user.getCell());
        response.setVillage(user.getVillage());
        response.setStreetAddress(user.getStreetAddress());
        
        // KYC
        response.setIdNumber(user.getIdNumber());
        response.setIdType(user.getIdType());
        response.setVerificationStatus(user.getVerificationStatus() != null ? 
                user.getVerificationStatus().name() : null);
        response.setVerifiedAt(user.getVerifiedAt());
        
        // Profile completion
        response.setProfileCompletionPercentage(user.getProfileCompletionPercentage());
        
        // Ratings
        response.setAverageRating(user.getAverageRating());
        response.setTotalRatings(user.getTotalRatings());

        Set<Role> roles = user.getRoles().stream()
                .map(UserRole::getRole)
                .collect(Collectors.toSet());
        response.setRoles(roles);

        return response;
    }
    
    @Override
    @Transactional
    public UserResponse updateProfile(Long userId, UpdateProfileRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (request.getFullName() != null && !request.getFullName().isBlank()) {
            user.setFullName(request.getFullName());
        }
        if (request.getPhoneNumber() != null) {
            user.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getProfilePictureUrl() != null) {
            user.setProfilePictureUrl(request.getProfilePictureUrl());
        }
        if (request.getBio() != null) {
            user.setBio(request.getBio());
        }
        if (request.getDateOfBirth() != null) {
            user.setDateOfBirth(request.getDateOfBirth());
        }
        
        // Address
        if (request.getCountry() != null) {
            user.setCountry(request.getCountry());
        }
        if (request.getProvince() != null) {
            user.setProvince(request.getProvince());
        }
        if (request.getDistrict() != null) {
            user.setDistrict(request.getDistrict());
        }
        if (request.getSector() != null) {
            user.setSector(request.getSector());
        }
        if (request.getCell() != null) {
            user.setCell(request.getCell());
        }
        if (request.getVillage() != null) {
            user.setVillage(request.getVillage());
        }
        if (request.getStreetAddress() != null) {
            user.setStreetAddress(request.getStreetAddress());
        }

        // Calculate profile completion
        user.calculateProfileCompletion();

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse submitKYC(Long userId, SubmitKYCRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setIdNumber(request.getIdNumber());
        user.setIdType(request.getIdType());
        user.setIdDocumentUrl(request.getIdDocumentUrl());
        user.setVerificationStatus(User.VerificationStatus.PENDING);

        // Calculate profile completion
        user.calculateProfileCompletion();

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse approveKYC(Long userId, Long adminId, String notes) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerificationStatus(User.VerificationStatus.VERIFIED);
        user.setVerifiedAt(LocalDateTime.now());
        user.setVerifiedBy(adminId);
        user.setVerificationNotes(notes);

        // Calculate profile completion
        user.calculateProfileCompletion();

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional
    public UserResponse rejectKYC(Long userId, Long adminId, String reason) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setVerificationStatus(User.VerificationStatus.REJECTED);
        user.setVerifiedBy(adminId);
        user.setVerificationNotes(reason);

        User updatedUser = userRepository.save(user);
        return mapToUserResponse(updatedUser);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return mapToUserResponse(user);
    }
}
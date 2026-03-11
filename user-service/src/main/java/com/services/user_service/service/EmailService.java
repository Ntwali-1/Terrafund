package com.services.user_service.service;

public interface EmailService {
    void sendVerificationEmail(String toEmail, String otpCode);
}

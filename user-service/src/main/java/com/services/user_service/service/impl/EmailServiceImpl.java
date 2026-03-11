package com.services.user_service.service.impl;

import com.services.user_service.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Override
    public void sendVerificationEmail(String toEmail, String otpCode) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Verify your TerraFund Account");

            String htmlContent = "<div style=\"font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; border: 1px solid #e0e0e0; border-radius: 10px; overflow: hidden;\">"
                    + "<div style=\"background-color: #11d421; padding: 20px; text-align: center;\">"
                    + "<h1 style=\"color: #ffffff; margin: 0;\">TerraFund</h1>"
                    + "</div>"
                    + "<div style=\"padding: 30px; background-color: #ffffff;\">"
                    + "<h2 style=\"color: #333333; margin-top: 0;\">Verify your email address</h2>"
                    + "<p style=\"color: #555555; line-height: 1.6;\">Thank you for registering with TerraFund. To complete your sign-up and start your sustainable investment journey, please use the verification code below:</p>"
                    + "<div style=\"text-align: center; margin: 30px 0;\">"
                    + "<span style=\"display: inline-block; padding: 15px 30px; font-size: 24px; font-weight: bold; letter-spacing: 5px; color: #11d421; border: 2px dashed #11d421; border-radius: 10px; background-color: #f0fdf4;\">"
                    + otpCode
                    + "</span>"
                    + "</div>"
                    + "<p style=\"color: #555555; line-height: 1.6; font-size: 14px;\">This code is valid for 24 hours. If you did not request this verification, please ignore this email.</p>"
                    + "</div>"
                    + "<div style=\"background-color: #f8fafc; padding: 15px; text-align: center; border-top: 1px solid #e0e0e0;\">"
                    + "<p style=\"color: #888888; font-size: 12px; margin: 0;\">© 2026 TerraFund Inc. All rights reserved.</p>"
                    + "</div>"
                    + "</div>";

            helper.setText(htmlContent, true);
            mailSender.send(message);

            System.out.println("OTP Email sent successfully to: " + toEmail);

        } catch (MessagingException e) {
            System.err.println("Failed to send OTP email: " + e.getMessage());
            throw new RuntimeException("Failed to send verification email");
        }
    }
}

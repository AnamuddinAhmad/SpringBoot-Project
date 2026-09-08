package com.auth.Auth.services;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;
    private final Logger logger = LoggerFactory.getLogger(MailService.class);

    @Value("${spring.mail.username}")
    private String fromAddress;

    public void sendOtpEmail(String toEmail, String otp) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "UTF-8");

            helper.setFrom(fromAddress, "Auth App");
            helper.setTo(toEmail);
            helper.setSubject("Your Auth App verification code");
            helper.setText(buildOtpEmailHtml(otp), true); // true = isHtml

            mailSender.send(mimeMessage);
        } catch (MessagingException | java.io.UnsupportedEncodingException e) {
            logger.error("Failed to send OTP email to {}", toEmail, e);
            throw new RuntimeException("Failed to send verification email.");
        }
    }

    private String buildOtpEmailHtml(String otp) {
        return """
                <!DOCTYPE html>
                <html>
                <head>
                  <meta charset="UTF-8" />
                  <meta name="viewport" content="width=device-width, initial-scale=1.0" />
                </head>
                <body style="margin:0; padding:0; background-color:#000000; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="background-color:#000000; padding: 40px 16px;">
                    <tr>
                      <td align="center">
                        <table role="presentation" width="100%%" cellpadding="0" cellspacing="0" style="max-width: 480px; background-color:#0a0a0a; border: 1px solid rgba(255,255,255,0.1); border-radius: 16px; overflow: hidden;">

                          <!-- Header / Logo -->
                          <tr>
                            <td style="padding: 32px 32px 0 32px;">
                              <table role="presentation" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td style="width:36px; height:36px; border-radius:10px; background: linear-gradient(135deg, #27272a, #000000); border: 1px solid rgba(255,255,255,0.12);">
                                    <table role="presentation" width="36" height="36" cellpadding="0" cellspacing="0">
                                      <tr>
                                        <td align="center" valign="middle">
                                          <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="#e4e4e7" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                                            <path d="M2 12C2 6.5 6.5 2 12 2a10 10 0 0 1 8 4" />
                                            <path d="M5 19.5C5.5 18 6 15 6 12c0-.7.12-1.37.34-2" />
                                            <path d="M17.29 21.02c.12-.6.43-2.3.5-3.02" />
                                            <path d="M12 10a2 2 0 0 0-2 2c0 1.02-.1 2.51-.26 4" />
                                            <path d="M8.65 22c.21-.66.45-1.32.57-2" />
                                            <path d="M14 13.12c0 2.38 0 6.38-1 8.88" />
                                            <path d="M2 16h.01" />
                                            <path d="M21.8 16c.2-2 .131-5.354 0-6" />
                                            <path d="M9 6.8a6 6 0 0 1 9 5.2c0 .47 0 1.17-.02 2" />
                                          </svg>
                                        </td>
                                      </tr>
                                    </table>
                                  </td>
                                  <td style="padding-left:10px; font-size:16px; font-weight:700; color:#ffffff;">Auth App</td>
                                </tr>
                              </table>
                            </td>
                          </tr>

                          <!-- Body -->
                          <tr>
                            <td style="padding: 32px;">
                              <h1 style="margin:0 0 12px 0; font-size: 22px; color:#ffffff; font-weight:700;">
                                Verify your email address
                              </h1>
                              <p style="margin:0 0 28px 0; font-size: 14px; line-height: 22px; color:#a1a1aa;">
                                Enter this code to finish setting up your account. For your security, it expires in 10 minutes.
                              </p>

                              <!-- OTP Box -->
                              <table role="presentation" width="100%%" cellpadding="0" cellspacing="0">
                                <tr>
                                  <td align="center" style="background-color: rgba(255,255,255,0.03); border: 1px solid rgba(255,255,255,0.1); border-radius: 12px; padding: 24px;">
                                    <span style="font-size: 36px; font-weight: 700; letter-spacing: 10px; color: #ffffff; font-family: 'Courier New', monospace;">
                                      %s
                                    </span>
                                  </td>
                                </tr>
                              </table>

                              <p style="margin: 28px 0 0 0; font-size: 13px; line-height: 20px; color:#71717a;">
                                Didn't request this code? You can safely ignore this email — your account remains secure and no changes will be made.
                              </p>
                            </td>
                          </tr>

                          <!-- Divider -->
                          <tr>
                            <td style="padding: 0 32px;">
                              <div style="height:1px; background-color: rgba(255,255,255,0.08);"></div>
                            </td>
                          </tr>

                          <!-- Footer -->
                          <tr>
                            <td style="padding: 24px 32px 32px 32px;">
                              <p style="margin:0; font-size: 12px; line-height: 18px; color:#52525b;">
                                This is an automated message from Auth App. Please don't reply to this email.
                              </p>
                            </td>
                          </tr>

                        </table>
                      </td>
                    </tr>
                  </table>
                </body>
                </html>
                """.formatted(otp);
    }
}
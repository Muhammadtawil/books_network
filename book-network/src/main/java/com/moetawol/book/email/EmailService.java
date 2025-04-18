package com.moetawol.book.email;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.springframework.mail.javamail.MimeMessageHelper.MULTIPART_MODE_MIXED;

@Service // Marks this class as a Spring service component
@Slf4j // Enables logging using the Slf4j logging API
@RequiredArgsConstructor // Lombok annotation to generate a constructor with required (final) fields
public class EmailService {

    private final JavaMailSender mailSender; // Injected Spring bean used to send emails
    private final SpringTemplateEngine templateEngine; // Used to process Thymeleaf templates

    @Async // Tells Spring to run this method asynchronously (in a separate thread)
    public void sendEmail(
            String to, // recipient email address
            String username, // recipient's name to be used in email
            EmailTemplateName emailTemplate, // enum or object defining the email template
            String confirmationUrl, // link for confirming email
            String activationCode, // optional activation code
            String subject // email subject line
    ) throws MessagingException {

        // Determine which template to use; default is "confirm-email"
        String templateName;
        if (emailTemplate == null) {
            templateName = "confirm-email";
        } else {
            templateName = emailTemplate.getName();
        }

        // Create a new email message
        MimeMessage mimeMessage = mailSender.createMimeMessage();

        // Set up helper to build the email with proper encoding and type
        MimeMessageHelper helper = new MimeMessageHelper(
                mimeMessage,
                MULTIPART_MODE_MIXED, // allow attachments or HTML content
                UTF_8.name() // use UTF-8 encoding
        );

        // Create variables to inject into the Thymeleaf template
        Map<String, Object> properties = new HashMap<>();
        properties.put("username", username);
        properties.put("confirmationUrl", confirmationUrl);
        properties.put("activation_code", activationCode);

        // Prepare Thymeleaf context with variables
        Context context = new Context();
        context.setVariables(properties);

        // Set sender email address
        helper.setFrom("contact@clickers.com");

        // Set recipient
        helper.setTo(to);

        // Set subject line
        helper.setSubject(subject);

        // Process the HTML template (Thymeleaf) with injected variables
        String template = templateEngine.process("activate_account", context);

        // Set the body of the email to the processed HTML
        helper.setText(template, true); // true = HTML content

        // Send the email
        mailSender.send(mimeMessage);
    }
}

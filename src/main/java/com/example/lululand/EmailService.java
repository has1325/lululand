package com.example.lululand;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    public void sendEmail(String toEmail, String subject, String content) throws IOException {

        Email from = new Email("보내는이메일@gmail.com"); // 본인 이메일
        Email to = new Email(toEmail);

        Content body = new Content("text/plain", content);

        Mail mail = new Mail(from, subject, to, body);

        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        System.out.println("메일 상태코드: " + response.getStatusCode());
    }
}
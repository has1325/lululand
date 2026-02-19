package com.example.lululand;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
public class EmailService {

    @Value("${SENDGRID_API_KEY}")
    private String sendGridApiKey;

    public void sendTempPasswordEmail(String toEmail, String username, String tempPassword) {

        Email from = new Email("no-reply@lululand.co.kr");
        Email to = new Email(toEmail);

        String subject = "[루루랜드] 임시 비밀번호 안내";

        String contentText =
                "안녕하세요 " + username + "님.\n\n" +
                "임시 비밀번호는 아래와 같습니다.\n\n" +
                tempPassword + "\n\n" +
                "로그인 후 반드시 비밀번호를 변경해주세요.";

        String contentHtml =
                "<div style='font-family:Arial;'>"
                + "<h2>임시 비밀번호 안내</h2>"
                + "<p>안녕하세요 <b>" + username + "</b>님.</p>"
                + "<p>임시 비밀번호는 아래와 같습니다.</p>"
                + "<h3 style='color:#ff4d6d;'>" + tempPassword + "</h3>"
                + "<p>로그인 후 반드시 비밀번호를 변경해주세요.</p>"
                + "</div>";

        Content textContent = new Content("text/plain", contentText);
        Content htmlContent = new Content("text/html", contentHtml);

        Mail mail = new Mail();
        mail.setFrom(from);
        mail.setSubject(subject);

        Personalization personalization = new Personalization();
        personalization.addTo(to);
        mail.addPersonalization(personalization);

        mail.addContent(textContent);
        mail.addContent(htmlContent);

        SendGrid sg = new SendGrid(sendGridApiKey);

        Request request = new Request();

        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());

            Response response = sg.api(request);

            System.out.println("메일 상태코드: " + response.getStatusCode());
            System.out.println("메일 응답: " + response.getBody());

            if (response.getStatusCode() != 202) {
                throw new RuntimeException("메일 발송 실패");
            }

        } catch (IOException ex) {
            throw new RuntimeException("SendGrid 오류 발생", ex);
        }
    }
}

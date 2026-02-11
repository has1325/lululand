package com.example.lululand;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import org.springframework.stereotype.Service;
import java.io.IOException;

@Service
public class EmailService {

    public void sendTempPasswordEmail(String toEmail, String username, String tempPassword) throws IOException {

        Email from = new Email("no-reply@lululand.co.kr"); // 반드시 SendGrid 인증된 이메일
        Email to = new Email(toEmail);

        String subject = "[루루랜드] 임시 비밀번호 안내";

        String contentText =
                "안녕하세요 " + username + "님.\n\n" +
                "임시 비밀번호는 아래와 같습니다.\n\n" +
                "👉 " + tempPassword + "\n\n" +
                "로그인 후 반드시 비밀번호를 변경해주세요.";

        Content content = new Content("text/plain", contentText);

        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(System.getenv("SENDGRID_API_KEY"));

        Request request = new Request();
        request.setMethod(Method.POST);
        request.setEndpoint("mail/send");
        request.setBody(mail.build());

        Response response = sg.api(request);

        System.out.println("메일 상태코드: " + response.getStatusCode());
        System.out.println("메일 응답: " + response.getBody());
    }
}

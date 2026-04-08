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

	@Value("${app.admin.email}")
	private String adminEmail;

	public void sendTempPasswordEmail(String toEmail, String username, String tempPassword) {

		Email from = new Email("no-reply@lululand.co.kr");
		Email to = new Email(toEmail);

		String subject = "[루루랜드] 임시 비밀번호 안내";

		String contentText = "안녕하세요 " + username + "님.\n\n" + "임시 비밀번호는 아래와 같습니다.\n\n" + tempPassword + "\n\n"
				+ "로그인 후 반드시 비밀번호를 변경해주세요.";

		String contentHtml = "<div style='font-family:Arial;'>" + "<h2>임시 비밀번호 안내</h2>" + "<p>안녕하세요 <b>" + username
				+ "</b>님.</p>" + "<p>임시 비밀번호는 아래와 같습니다.</p>" + "<h3 style='color:#ff4d6d;'>" + tempPassword + "</h3>"
				+ "<p>로그인 후 반드시 비밀번호를 변경해주세요.</p>" + "</div>";

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

	// ==============================
	// 🔔 관리자 알림 메일 (추가)
	// ==============================
	public void sendAdminConsultEmail(String name, String email, String jewelry, String message) {

		// 👉 관리자 메일 주소 (여기만 바꾸면 됨)
		Email to = new Email(adminEmail);

		Email from = new Email("no-reply@lululand.co.kr");

		String subject = "[루루랜드] 새로운 상담 신청 도착 🚨";

		String contentText = "새로운 상담 신청이 접수되었습니다.\n\n" + "이름: " + name + "\n" + "이메일: " + email + "\n" + "보석 종류: "
				+ jewelry + "\n\n" + "상담 내용:\n" + message;

		String contentHtml = "<div style='font-family:Arial;'>" + "<h2>🚨 새로운 상담 신청</h2>" + "<hr>" + "<p><b>이름:</b> "
				+ name + "</p>" + "<p><b>이메일:</b> " + email + "</p>" + "<p><b>보석 종류:</b> " + jewelry + "</p>" + "<hr>"
				+ "<p><b>상담 내용:</b><br>" + message + "</p>" + "</div>";

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

			System.out.println("관리자 메일 상태코드: " + response.getStatusCode());

			if (response.getStatusCode() != 202) {
				throw new RuntimeException("관리자 메일 발송 실패");
			}

		} catch (IOException ex) {
			throw new RuntimeException("SendGrid 오류", ex);
		}
	}

	// ==============================
	// 📩 고객 상담 접수 완료 메일 (추가)
	// ==============================
	public void sendConsultEmail(String toEmail, String name, String jewelry, String message) {

		Email from = new Email("no-reply@lululand.co.kr");
		Email to = new Email(toEmail);

		String subject = "[루루랜드] 상담 신청이 접수되었습니다 💎";

		String contentText = name + "님, 상담 신청이 정상적으로 접수되었습니다.\n\n" + "입력하신 내용:\n" + "보석 종류: " + jewelry + "\n"
				+ "상담 내용: " + message + "\n\n" + "빠른 시일 내에 답변드리겠습니다.\n" + "감사합니다.";

		String contentHtml = "<div style='font-family:Arial;'>" + "<h2>💎 상담 접수 완료</h2>" + "<p><b>" + name
				+ "</b>님, 상담 신청이 접수되었습니다.</p>" + "<hr>" + "<p><b>보석 종류:</b> " + jewelry + "</p>"
				+ "<p><b>상담 내용:</b><br>" + message + "</p>" + "<hr>" + "<p>빠른 시일 내에 답변드리겠습니다.</p>" + "<p>감사합니다 😊</p>"
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

			System.out.println("고객 메일 상태코드: " + response.getStatusCode());

			if (response.getStatusCode() != 202) {
				throw new RuntimeException("고객 메일 발송 실패");
			}

		} catch (IOException ex) {
			throw new RuntimeException("SendGrid 오류", ex);
		}
	}
}

package com.example.lululand;

import java.util.Map;
import java.util.Optional;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
@CrossOrigin(origins = "https://lululand.co.kr")
public class LululandController {

	private final LululandService lululandService;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;
	private final EmailService emailService;
	private final SurveyService surveyService;

	// === API 엔드포인트 ===
	@GetMapping("/api/hello")
	public String hello(Model model) {
		model.addAttribute("message", "Hello from Spring Boot!");
		return "hello"; // templates/hello.html 필요
	}

	// === 페이지 이동 ===
	@GetMapping("/index")
	public String index(HttpSession session, Model model) {
		Object loginUser = session.getAttribute("loginUser");
		model.addAttribute("loginUser", loginUser);
		return "index";
	}

	@GetMapping("/login")
	public String login() {
		return "login";
	}

	@GetMapping("/logout")
	public String logout(HttpSession session) {
		session.invalidate();
		return "redirect:/login";
	}

	@GetMapping("/signup")
	public String signupForm(Model model) {
		model.addAttribute("userDto", new UserCreateForm());
		return "signup";
	}

	@GetMapping("/mypage")
	public String mypage(HttpSession session, Model model, RedirectAttributes redirectAttributes) {
		Object loginUserObj = session.getAttribute("loginUser");

		if (loginUserObj == null) {
			redirectAttributes.addFlashAttribute("error", "로그인이 필요한 페이지입니다.");
			return "redirect:/login";
		}

		Lululand loginUser = (Lululand) loginUserObj;
		Lululand user = lululandService.findByEmail(loginUser.getEmail());

		if (user == null) {
			session.invalidate();
			redirectAttributes.addFlashAttribute("error", "사용자 정보를 찾을 수 없습니다. 다시 로그인해주세요.");
			return "redirect:/login";
		}

		model.addAttribute("user", user);
		return "mypage";
	}

	// === POST 요청 ===
	@PostMapping("/api/login")
	@ResponseBody
	public ResponseEntity<?> login(@RequestBody Map<String, String> loginData) {
		String email = loginData.get("email");
		String password = loginData.get("password");

		if (email == null || email.trim().isEmpty() || password == null || password.trim().isEmpty()) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", "이메일과 비밀번호를 모두 입력해 주세요."));
		}

		Lululand metalover = lululandService.findByEmail(email);

		if (metalover != null && passwordEncoder.matches(password, metalover.getPassword())) {
			// ✅ JWT 토큰 생성
			String token = jwtUtil.generateToken(email);

			return ResponseEntity
					.ok(Map.of("success", true, "token", token, "message", "로그인 성공", "user", metalover.getUsername()));
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
					.body(Map.of("success", false, "error", "아이디 또는 비밀번호가 올바르지 않습니다."));
		}
	}

	@PostMapping("/api/signup")
	@ResponseBody
	public ResponseEntity<Map<String, String>> apiSignup(@Valid @RequestBody UserCreateForm form) {

		// 1) 비밀번호 일치 여부 검사 (DTO 유효성은 @Valid로 처리)
		if (!form.getPassword1().equals(form.getPassword2())) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "비밀번호가 일치하지 않습니다."));
		}

		try {
			// 2) 서비스에게 생성 호출 (서비스 안에서 passwordEncoder.encode(...) 처리)
			lululandService.create(form.getUserid(), form.getEmail(), form.getPassword1(), form.getUsername(),
					form.getPhone());

			return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "회원가입 성공"));
		} catch (DataIntegrityViolationException e) {
			return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "이미 등록된 사용자입니다."));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "서버 에러: " + e.getMessage()));
		}
	}

	@PostMapping("/api/consult")
	@ResponseBody
	public ResponseEntity<?> submitConsult(@RequestBody Map<String, String> consultData,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		try {
			String name = consultData.get("name");
			String email = consultData.get("email");

			// ✅ 핵심 수정 (color → jewelry)
			String jewelry = consultData.get("jewelry");

			String message = consultData.get("message");

			// ✅ AI 상담 데이터 수신
			String gem = consultData.get("gem");
			String reform = consultData.get("reform");
			String condition = consultData.get("condition");
			String budget = consultData.get("budget");
			String style = consultData.get("style");

			// =========================
			// 1️⃣ JWT 사용자 자동 인식
			// =========================
			if ((email == null || email.isBlank()) && authHeader != null && authHeader.startsWith("Bearer ")) {
				String token = authHeader.substring(7);
				if (jwtUtil.validateToken(token)) {
					String userEmail = jwtUtil.extractUsername(token);
					Lululand user = lululandService.findByEmail(userEmail);
					if (user != null) {
						email = user.getEmail();
						name = user.getUsername();
					}
				}
			}

			// =========================
			// 2️⃣ AI 상담 자동 메시지 생성
			// =========================
			if (message == null || message.isBlank()) {
				if (gem != null || reform != null || condition != null || budget != null || style != null) {

					message = "AI 리폼 상담\n"
							+ "보석: " + safe(gem) + "\n"
							+ "리폼: " + safe(reform) + "\n"
							+ "상태: " + safe(condition) + "\n"
							+ "예산: " + safe(budget) + "\n"
							+ "스타일: " + safe(style);

					jewelry = gem; // 👉 보석 정보 저장
				}
			}

			// =========================
			// 3️⃣ 필수값 검증
			// =========================
			if (name == null || name.isBlank() ||
				email == null || email.isBlank() ||
				message == null || message.isBlank()) {

				return ResponseEntity.badRequest().body(Map.of("error", "상담 정보가 부족합니다."));
			}

			// =========================
			// 4️⃣ DB 저장
			// =========================
			lululandService.saveConsult(name, email, jewelry, message);

			// ✅ 고객에게 메일 발송
			emailService.sendConsultEmail(email, name, jewelry, message);
			// ✅ 관리자에게 알림 메일 발송 (🔥 추가 핵심)
			emailService.sendAdminConsultEmail(name, email, jewelry, message);
			return ResponseEntity.ok(Map.of("success", true, "message", "상담이 성공적으로 접수되었습니다."));

		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "서버 오류: " + e.getMessage()));
		}
	}

	// null 안전 처리용
	private String safe(String value) {
		return value == null ? "-" : value;
	}

	@PostMapping("/api/find-id")
	@ResponseBody
	public ResponseEntity<?> findId(@RequestBody Map<String, String> data) {

		String name = data.get("name");
		String phone = data.get("phone");

		if (name == null || phone == null || name.isBlank() || phone.isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("success", false, "error", "이름과 휴대폰 번호를 입력해주세요."));
		}

		// 하이픈 제거
		String normalizedPhone = phone.replaceAll("-", "");

		// 🔥 사용자 조회
		Lululand user = lululandService.findByNameAndPhone(name, normalizedPhone);

		if (user != null) {
			return ResponseEntity.ok(Map.of("success", true, "email", user.getEmail() // ✅ 이메일 반환
			));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND)
					.body(Map.of("success", false, "error", "일치하는 사용자를 찾을 수 없습니다."));
		}
	}

	@PostMapping("/api/find-password")
	public ResponseEntity<?> findPassword(@RequestBody Map<String, String> data) {

		String email = data.get("email");

		if (email == null || email.isBlank()) {
			return ResponseEntity.badRequest().body(Map.of("error", "이메일 입력 필요"));
		}

		email = email.trim().toLowerCase();

		Lululand user = lululandService.findByEmail(email);

		if (user == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "사용자 없음"));
		}

		try {
			// 1️⃣ 임시 비밀번호 생성
			String tempPassword = java.util.UUID.randomUUID().toString().substring(0, 8);

			// 2️⃣ DB 업데이트
			user.setPassword(passwordEncoder.encode(tempPassword));
			lululandService.updateUser(user);

			// 3️⃣ 이메일 발송 (⭐ 핵심)
			emailService.sendTempPasswordEmail(email, user.getUsername(), tempPassword);

			return ResponseEntity.ok(Map.of("success", true, "message", "임시 비밀번호 이메일 발송 완료"));

		} catch (Exception e) {
			e.printStackTrace();

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
		}
	}

	@PostMapping("/api/survey")
	@ResponseBody
	public ResponseEntity<?> submitSurvey(@RequestBody Map<String, String> data,
			@RequestHeader(value = "Authorization", required = false) String authHeader) {

		try {

			if (authHeader == null || !authHeader.startsWith("Bearer ")) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "로그인이 필요합니다."));
			}

			String token = authHeader.substring(7);

			if (!jwtUtil.validateToken(token)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "유효하지 않은 토큰입니다."));
			}

			String email = jwtUtil.extractUsername(token);

			Integer satisfaction = Integer.parseInt(data.get("satisfaction"));
			String service = data.get("service");
			String feedback = data.get("feedback");

			String coupon = surveyService.submitSurvey(email, satisfaction, service, feedback);

			return ResponseEntity.ok(Map.of("success", true, "coupon", coupon));

		} catch (RuntimeException e) {

			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", e.getMessage()));

		} catch (Exception e) {

			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "서버 오류: " + e.getMessage()));
		}
	}

	@GetMapping("/api/me")
	@ResponseBody
	public ResponseEntity<?> me(@RequestHeader(value = "Authorization", required = false) String authHeader) {
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "토큰이 제공되지 않았습니다."));
		}
		String token = authHeader.substring(7);
		try {
			if (!jwtUtil.validateToken(token)) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "유효하지 않은 토큰입니다."));
			}
			String email = jwtUtil.extractUsername(token); // jwtUtil에 따라 메서드명 맞춰주세요
			Lululand user = lululandService.findByEmail(email);
			if (user == null) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "사용자를 찾을 수 없습니다."));
			}
			return ResponseEntity.ok(Map.of("user", user.getUsername(), "email", user.getEmail()));
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(Map.of("error", "서버 에러: " + e.getMessage()));
		}
	}
}

package com.example.lululand;

import java.util.Map;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@Controller
public class LululandController {

	private final LululandService lululandService;
	private final PasswordEncoder passwordEncoder;
	private final JwtUtil jwtUtil;

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
	        String email = jwtUtil.extractUsername(token);
	        Lululand user = lululandService.findByEmail(email);
	        if (user == null) {
	            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of("error", "사용자를 찾을 수 없습니다."));
	        }

	        // ✅ 관리자 식별용 userid 포함
	        return ResponseEntity.ok(Map.of(
	            "user", user.getUsername(),
	            "email", user.getEmail(),
	            "userid", user.getUserid()
	        ));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "서버 에러: " + e.getMessage()));
	    }
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
			return ResponseEntity.badRequest().body(Map.of(
				"success", false,
				"error", "이메일과 비밀번호를 모두 입력해 주세요."
			));
		}

		Lululand metalover = lululandService.findByEmail(email);

		if (metalover != null && passwordEncoder.matches(password, metalover.getPassword())) {
			// ✅ JWT 토큰 생성
			String token = jwtUtil.generateToken(email);

			return ResponseEntity.ok(Map.of(
				"success", true,
				"token", token,
				"message", "로그인 성공",
				"user", metalover.getUsername()
			));
		} else {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of(
				"success", false,
				"error", "아이디 또는 비밀번호가 올바르지 않습니다."
			));
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
	        lululandService.create(form.getUserid(), form.getEmail(), form.getPassword1(), form.getUsername(), form.getPhone());

	        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("message", "회원가입 성공"));
	    } catch (DataIntegrityViolationException e) {
	        return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "이미 등록된 사용자입니다."));
	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
	                .body(Map.of("error", "서버 에러: " + e.getMessage()));
	    }
	}
}

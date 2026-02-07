package com.example.lululand;

import java.util.List;
import java.util.Optional;

import org.apache.catalina.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class LululandService {
	
	private final LululandRepository lululandRepository;
	private final PasswordEncoder passwordEncoder;
	private final ConsultRepository consultRepository;
	
	public Lululand create(String userid, String email, String password, String username, String phone) {
		Lululand lululand = new Lululand();
		lululand.setUserid(userid);
		lululand.setEmail(email);
		lululand.setPassword(passwordEncoder.encode(password));
		lululand.setUsername(username);
		lululand.setPhone(phone);
		this.lululandRepository.save(lululand);
		return lululand;
	}
	
	public List<Lululand> getAllMetalover() {
        return lululandRepository.findAll();
    }
	
	public String findUserId(String username, String email) {
		Optional<Lululand> lululandOptional = lululandRepository.findByUsernameAndEmail(username, email);
	    return lululandOptional.isPresent() ? lululandOptional.get().getUserid() : null;
    }
	
	public String findUserIdByNameAndPhone(String username, String phone) {
	    Optional<Lululand> user = lululandRepository.findByUsernameAndPhone(username, phone);
	    return user.map(Lululand::getUserid).orElse(null);
	}

	public Lululand getMetaloverByUseridAndUsernameAndEmail(String userid, String username, String email) {
		Optional<Lululand> lululand = this.lululandRepository.findByUseridAndUsernameAndEmail(userid, username, email);
		if(lululand.isPresent()) {
			return lululand.get();
		}else {
			throw new DataNotFoundException("Email not found!!");
		}
    }

	public Lululand findByEmail(String email) {
	    Optional<Lululand> lululandOptional = lululandRepository.findByEmail(email);
	    return lululandOptional.orElse(null);
	}
	
	public boolean existsByEmail(String email) {
	    return lululandRepository.existsByEmail(email);
	}
	
	public Lululand getMyInfo(String userId) {
        return lululandRepository.findByUserid(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }
	
	public void saveConsult(String name, String email, String color, String message) {
        Consult consult = new Consult();
        consult.setName(name);
        consult.setEmail(email);
        consult.setColor(color);
        consult.setMessage(message);

        consultRepository.save(consult); // DB에 저장
    }

	
}

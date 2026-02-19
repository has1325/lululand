package com.example.lululand;

import java.util.List;
import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional
public class LululandService {

    private final LululandRepository lululandRepository;
    private final PasswordEncoder passwordEncoder;
    private final ConsultRepository consultRepository;

    public Lululand create(String userid, String email, String password, String username, String phone) {

        Lululand lululand = new Lululand();
        lululand.setUserid(userid);
        lululand.setEmail(email.trim().toLowerCase());
        lululand.setPassword(passwordEncoder.encode(password));
        lululand.setUsername(username);

        String normalizedPhone = phone.replaceAll("-", "").trim();
        lululand.setPhone(normalizedPhone);

        lululandRepository.save(lululand);
        return lululand;
    }

    public List<Lululand> getAllMetalover() {
        return lululandRepository.findAll();
    }

    public String findUserId(String username, String email) {
        Optional<Lululand> user =
            lululandRepository.findByUsernameAndEmail(username, email.trim().toLowerCase());

        return user.map(Lululand::getUserid).orElse(null);
    }

    public String findUserIdByNameAndPhone(String username, String phone) {

        String normalizedPhone = phone.replaceAll("-", "").trim();
        String normalizedName = username.trim();

        Optional<Lululand> user =
            lululandRepository.findByUsernameAndPhone(normalizedName, normalizedPhone);

        return user.map(Lululand::getUserid).orElse(null);
    }
    
    public Lululand findByNameAndPhone(String username, String phone) {

        String normalizedPhone = phone.replaceAll("-", "").trim();
        String normalizedName = username.trim();

        return lululandRepository
            .findByUsernameAndPhone(normalizedName, normalizedPhone)
            .orElse(null);
    }

    public Lululand getMetaloverByUseridAndUsernameAndEmail(String userid, String username, String email) {
        return lululandRepository
            .findByUseridAndUsernameAndEmail(userid, username, email)
            .orElseThrow(() -> new DataNotFoundException("Email not found!!"));
    }

    public Lululand findByEmail(String email) {
        return lululandRepository
            .findByEmail(email.trim().toLowerCase())
            .orElse(null);
    }

    public boolean existsByEmail(String email) {
        return lululandRepository.existsByEmail(email.trim().toLowerCase());
    }

    public Lululand getMyInfo(String userId) {
        return lululandRepository.findByUserid(userId)
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
    }

    public void updateUser(Lululand user) {
        lululandRepository.saveAndFlush(user);
    }

    public void saveConsult(String name, String email, String color, String message) {
        Consult consult = new Consult();
        consult.setName(name);
        consult.setEmail(email);
        consult.setColor(color);
        consult.setMessage(message);

        consultRepository.save(consult);
    }
}

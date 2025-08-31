package com.example.lululand;

import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.transaction.Transactional;

@Service
public class PasswordSearchServiceImpl implements PasswordSearchService {

	private final LululandRepository lululandRepository;
    private final PasswordEncoder passwordEncoder;
    
    @Autowired
    public PasswordSearchServiceImpl(LululandRepository lululandRepository, PasswordEncoder passwordEncoder) {
        this.lululandRepository = lululandRepository;
        this.passwordEncoder = passwordEncoder;
    }
    
    @Override
    @Transactional
    public String searchPassword(String userid, String username, String email) {
        // Find user by username, name, and email
        var lululand = lululandRepository.findByUseridAndUsernameAndEmail(userid, username, email)
                .orElseThrow(() -> new RuntimeException("일치하는 사용자 정보를 찾을 수 없습니다."));

        // Generate temporary password
        String tempPassword = generateTempPassword();
        
        // Update user's password
        lululand.setPassword(passwordEncoder.encode(tempPassword));
        lululandRepository.save(lululand);

        return tempPassword;
    }

    private String generateTempPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!@#$%^&*";
        Random random = new Random();
        StringBuilder password = new StringBuilder();

        // Generate password with at least 8 characters
        int length = 8 + random.nextInt(5);
        
        // Ensure at least one character from each category
        password.append(chars.charAt(random.nextInt(26))); // Uppercase
        password.append(chars.charAt(26 + random.nextInt(26))); // Lowercase
        password.append(chars.charAt(52 + random.nextInt(10))); // Number
        password.append(chars.charAt(62 + random.nextInt(8))); // Special character

        // Add remaining characters
        for (int i = 4; i < length; i++) {
            password.append(chars.charAt(random.nextInt(chars.length())));
        }

        // Shuffle the password
        char[] passwordArray = password.toString().toCharArray();
        for (int i = passwordArray.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            char temp = passwordArray[index];
            passwordArray[index] = passwordArray[i];
            passwordArray[i] = temp;
        }

        return new String(passwordArray);
    }
    
}

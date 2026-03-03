package com.example.lululand;

import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class SurveyService {

    private final SurveyRepository surveyRepository;

    public String submitSurvey(String email, Integer satisfaction, String service, String feedback) {

        // 🔥 1인 1회 참여 제한
        if (surveyRepository.findByUserEmail(email).isPresent()) {
            throw new RuntimeException("이미 설문에 참여하셨습니다.");
        }

        // 🔥 랜덤 쿠폰 생성
        String coupon = generateCoupon();

        Survey survey = new Survey();
        survey.setUserEmail(email);
        survey.setSatisfaction(satisfaction);
        survey.setService(service);
        survey.setFeedback(feedback);
        survey.setCouponCode(coupon);
        survey.setCreatedAt(LocalDateTime.now());

        surveyRepository.save(survey);

        return coupon;
    }

    private String generateCoupon() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder sb = new StringBuilder("LULU-");
        Random random = new Random();

        for (int i = 0; i < 7; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }

        return sb.toString();
    }
}
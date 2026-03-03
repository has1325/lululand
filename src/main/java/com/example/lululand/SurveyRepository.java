package com.example.lululand;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SurveyRepository extends JpaRepository<Survey, Long> {

    Optional<Survey> findByUserEmail(String userEmail);
}
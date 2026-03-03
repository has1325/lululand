package com.example.lululand;

import java.time.LocalDateTime;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Survey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userEmail;   // 로그인 사용자 기준

    private Integer satisfaction;

    private String service;

    @Column(length = 1000)
    private String feedback;

    private String couponCode;

    private LocalDateTime createdAt;
}
package com.example.lululand;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "consult")
public class Consult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;        // 이름
    private String email;       // 이메일
    private String phone;       // 전화번호
    private String interest;    // 관심 보석/컬러
    @Column(length = 1000)
    private String message;     // 상담 내용

    // 회원 정보 연결 (선택 사항)
    @ManyToOne
    private Lululand user;      // 상담한 사용자 (회원 테이블과 연결)
}

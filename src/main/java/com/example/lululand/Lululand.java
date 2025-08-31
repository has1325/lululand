package com.example.lululand;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
public class Lululand {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(unique = true)
	private String userid;
	
	@Column(unique = true)
	private String email;
	
	private String password;
	
	@Column(unique = true)
	private String username;
	
	@Column(unique = true)
	private String phone;
	
}

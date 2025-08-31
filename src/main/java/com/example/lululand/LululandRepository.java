package com.example.lululand;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LululandRepository extends JpaRepository<Lululand, Long> {
	
	Optional<Lululand> findByUsername(String username);
	Optional<Lululand> findByUsernameAndEmail(String username, String email);
    Optional<Lululand> findByUseridAndUsernameAndEmail(String userid, String username, String email);
    Optional<Lululand> findByEmail(String email);
    Optional<Lululand> findByUserid(String userid);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
}

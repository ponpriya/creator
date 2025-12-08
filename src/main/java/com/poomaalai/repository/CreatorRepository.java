package com.poomaalai.repository;



import org.springframework.data.jpa.repository.JpaRepository;

import com.poomaalai.entity.Creator;

public interface CreatorRepository extends JpaRepository<Creator, Integer> {
    java.util.Optional<Creator> findByEmail(String email);   
}

package com.poomaalai.repository;


import com.poomaalai.entity.Creator;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CreatorRepository extends JpaRepository<Creator, Integer> {
    List<Creator> findAllByZipcode(String zipcode);

}

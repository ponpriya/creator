package com.poomaalai.repository;


import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.poomaalai.entity.Creator;
import com.poomaalai.entity.CreatorStore;

public interface CreatorStoreRepository extends JpaRepository<CreatorStore, Integer> {
    List<CreatorStore> findAllByZipcode(String zipcode);
    List<CreatorStore> findByOwner(Creator owner);
    List<CreatorStore> findByOwnerEmail(String email);

}

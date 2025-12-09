package com.poomaalai.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poomaalai.dto.CreatorStoreDto;
import com.poomaalai.entity.CreatorStore;
import com.poomaalai.repository.CreatorStoreRepository;

@Service
public class CreatorStoreService {

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @Autowired
    private ModelMapper mapper;


    public CreatorStoreDto getCreatorStoreById(int id) {
        Optional<CreatorStore> creatorStore = creatorStoreRepository.findById(id);
        CreatorStoreDto creatorStoreDto = mapper.map(creatorStore, CreatorStoreDto.class);
        return creatorStoreDto;
    }
   
    public List<CreatorStoreDto> searchByZipcode(String zipcode) {
        List<CreatorStore> creatorStores = creatorStoreRepository.findAllByZipcode(zipcode);    
        List<CreatorStoreDto> creatorStoreDtos = creatorStores.stream()
                .map(creatorStore -> mapper.map(creatorStore, CreatorStoreDto.class))
                .collect(Collectors.toList());

        return creatorStoreDtos;
    }

    public List<CreatorStoreDto> getAllCreatorStores() {
        List<CreatorStore> creatorStoreList= creatorStoreRepository.findAll();
        List<CreatorStoreDto> creatorStoreDtoList = creatorStoreList.stream()
                .map(creatorStore -> mapper.map(creatorStore, CreatorStoreDto.class))
                .collect(Collectors.toList());
        return creatorStoreDtoList;
    }

    public List<CreatorStoreDto> getCreatorStoresByOwnerEmail(String email) {
        List<CreatorStore> creatorStore = creatorStoreRepository.findByOwnerEmail(email);
        List<CreatorStoreDto> creatorStoreDtos = creatorStore.stream()
                .map(store -> mapper.map(store, CreatorStoreDto.class))
                .collect(Collectors.toList());
        return creatorStoreDtos;
    }
    public void addCreatorStore(CreatorStoreDto creatorStoreDto) {
        CreatorStore creatorStore = mapper.map(creatorStoreDto, CreatorStore.class);
        System.out.println("Saving Creator Store for owner: " + creatorStore.getOwner());
        creatorStoreRepository.save(creatorStore);
    }   

}
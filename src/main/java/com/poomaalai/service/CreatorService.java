package com.poomaalai.service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.poomaalai.dto.CreatorDto;
import com.poomaalai.dto.CreatorStoreDto;
import com.poomaalai.dto.RegisterCreatorDto;
import com.poomaalai.entity.Creator;
import com.poomaalai.entity.CreatorStore;
import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.repository.CreatorStoreRepository;


@Service
public class CreatorService  implements UserDetailsService {

    @Autowired
    private final CreatorRepository creatorRepository;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @Autowired
    private ModelMapper mapper;

    public CreatorService(CreatorRepository creatorRepository) {
        this.creatorRepository = creatorRepository;
    }

    public CreatorDto getCreatorById(int id) {
        Optional<Creator> creator = creatorRepository.findById(id);
        CreatorDto creatorDto= mapper.map(creator, CreatorDto.class);
        return creatorDto;
    }

    public Creator getCreatorByEmail(String email) {    
        return creatorRepository.findByEmail(email).orElse(null);
    }

    public List<CreatorDto> getAllCreators() {
        List<Creator> creators = creatorRepository.findAll();
        List<CreatorDto> creatorDtos = creators.stream()
                .map(creator -> mapper.map(creator, CreatorDto.class))
                .collect(Collectors.toList());
        return creatorDtos;
    }

    public List<CreatorStoreDto> getAllCreatorStoresByOwner(Creator owner) {
        List<CreatorStore> creatorStoreList = creatorStoreRepository.findByOwner(owner);
        List<CreatorStoreDto> creatorStoreDtos = creatorStoreList.stream()
                .map(store -> mapper.map(store, CreatorStoreDto.class))
                .collect(Collectors.toList());
        return creatorStoreDtos;
    }
    
    public List<CreatorStoreDto> getAllCreatorStoresByOwnerEmail(String email) {
        List<CreatorStore> creatorStoreList = creatorStoreRepository.findByOwnerEmail(email);
        List<CreatorStoreDto> creatorStoreDtos = creatorStoreList.stream()
                .map(store -> mapper.map(store, CreatorStoreDto.class))
                .collect(Collectors.toList());
        return creatorStoreDtos;
    }
    public void registerNewCreator(RegisterCreatorDto registerCreatorDto) {
        registerCreatorDto.setPassword(new BCryptPasswordEncoder().encode(registerCreatorDto.getPassword()));
        Creator creator = mapper.map(registerCreatorDto, Creator.class);
        creatorRepository.save(creator);
    }   

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Creator creator = creatorRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(creator.getEmail())
                .password(creator.getPassword()) // The stored password is already Bcrypt-hashed
                .authorities(Collections.emptyList())
                .build();
    }
}
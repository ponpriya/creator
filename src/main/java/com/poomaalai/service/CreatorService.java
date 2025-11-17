package com.poomaalai.service;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.dto.CreatorResponse;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class CreatorService {

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private ModelMapper mapper;


    public CreatorResponse getCreatorById(int id) {
        Optional<Creator> creator = creatorRepository.findById(id);
        CreatorResponse creatorResponse = mapper.map(creator, CreatorResponse.class);
        return creatorResponse;
    }
    /**
    public List<CreatorResponse> searchCreatorsByZipcode(String zipcode) {
        List<Creator> creatorsByZip = creatorRepository.findAllByZipcode(zipcode);
        List<CreatorResponse> creatorResponses = creatorsByZip.stream()
                .map(creator -> mapper.map(creator, CreatorResponse.class))
                .collect(Collectors.toList());
        return creatorResponses;
    }**/

    public List<Creator> searchCreatorsByZipcode(String zipcode) {
        List<Creator> creators = creatorRepository.findAllByZipcode(zipcode);    
        return creators;
    }

    public List<CreatorResponse> getAllCreators() {
        List<Creator> creators = creatorRepository.findAll();
        List<CreatorResponse> creatorResponses = creators.stream()
                .map(creator -> mapper.map(creator, CreatorResponse.class))
                .collect(Collectors.toList());
        return creatorResponses;
    }

}
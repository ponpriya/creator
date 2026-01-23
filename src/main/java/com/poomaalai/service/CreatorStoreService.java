package com.poomaalai.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.poomaalai.dto.CreatorStoreDto;
import com.poomaalai.entity.CreatorStore;
import com.poomaalai.repository.CreatorStoreRepository;

@Service
public class CreatorStoreService {

    private static final Logger logger = LoggerFactory.getLogger(CreatorStoreService.class);

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @Autowired
    private ModelMapper mapper;

    @Autowired
    private GeocodingService geocodingService;


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

    public List<CreatorStoreDto> searchByZipcodeAndRadius(String centerZipcode, int radiusMiles) {
        logger.info("Searching for stores within {} miles of zipcode: {}", radiusMiles, centerZipcode);
        
        // Get all stores
        List<CreatorStore> allStores = creatorStoreRepository.findAll();
        
        // Filter stores within the radius
        List<CreatorStoreDto> nearbyStores = allStores.stream()
                .filter(store -> {
                    if (store.getZipcode() == null || store.getZipcode().isEmpty()) {
                        return false;
                    }
                    double distance = calculateZipcodeDistance(centerZipcode, store.getZipcode());
                    return distance <= radiusMiles;
                })
                .map(store -> mapper.map(store, CreatorStoreDto.class))
                .collect(Collectors.toList());
        
        logger.debug("Found {} stores within radius", nearbyStores.size());
        return nearbyStores;
    }

    /**
     * Calculate approximate distance between two US zipcodes in miles.
     * This is a simplified calculation based on the zipcode prefix.
     * For production use, consider using a proper geocoding service or zipcode database.
     * 
     * @param zipcode1 First zipcode
     * @param zipcode2 Second zipcode
     * @return Approximate distance in miles
     */
    private double calculateZipcodeDistance(String zipcode1, String zipcode2) {
        if (zipcode1 == null || zipcode2 == null || 
            zipcode1.length() < 5 || zipcode2.length() < 5) {
            return Double.MAX_VALUE;
        }
        
        try {
            // Extract first 3 digits (sectional center facility)
            int prefix1 = Integer.parseInt(zipcode1.substring(0, 3));
            int prefix2 = Integer.parseInt(zipcode2.substring(0, 3));
            
            // Approximate: each zipcode prefix difference represents roughly 50-100 miles
            // This is a very rough estimation. For accurate results, use latitude/longitude
            int prefixDiff = Math.abs(prefix1 - prefix2);
            
            if (prefixDiff == 0) {
                // Same general area, check last 2 digits
                int suffix1 = Integer.parseInt(zipcode1.substring(3, 5));
                int suffix2 = Integer.parseInt(zipcode2.substring(3, 5));
                int suffixDiff = Math.abs(suffix1 - suffix2);
                // Approximate: each suffix difference represents roughly 5-10 miles
                return suffixDiff * 7.5; // Average of 5-10
            } else {
                // Different areas
                return prefixDiff * 75.0; // Average of 50-100 miles per prefix
            }
        } catch (NumberFormatException e) {
            logger.warn("Error parsing zipcode for distance calculation: {} or {}", zipcode1, zipcode2);
            return Double.MAX_VALUE;
        }
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
        logger.info("Saving Creator Store '{}' for owner: {}", creatorStore.getName(), creatorStore.getOwner().getEmail());
        
        // Geocode the address to get latitude and longitude
        if (creatorStore.getAddress() != null && creatorStore.getZipcode() != null) {
            try {
                double[] coordinates = geocodingService.geocodeAddress(
                    creatorStore.getAddress(), 
                    creatorStore.getZipcode()
                );
                
                if (coordinates != null && coordinates.length == 2) {
                    creatorStore.setLatitude(coordinates[0]);
                    creatorStore.setLongitude(coordinates[1]);
                    logger.info("Successfully geocoded store location to: [{}, {}]", 
                               coordinates[0], coordinates[1]);
                } else {
                    logger.warn("Unable to geocode address for store: {}", creatorStore.getName());
                }
            } catch (Exception e) {
                logger.error("Error during geocoding for store '{}': {}", 
                           creatorStore.getName(), e.getMessage());
                // Continue saving without coordinates
            }
        }
        
        creatorStoreRepository.save(creatorStore);
    }   

}
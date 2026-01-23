package com.poomaalai.service;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class GeocodingService {

    private static final Logger logger = LoggerFactory.getLogger(GeocodingService.class);

    @Value("${geocoding.enabled}")
    private boolean geocodingEnabled;

    @Value("${geocoding.provider}")
    private String provider;

    @Value("${geocoding.api.key}")
    private String apiKey;

    @Value("${geocoding.nominatim.url}")
    private String nominatimUrl;

    @Value("${geocoding.google.url}")
    private String googleUrl;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GeocodingService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Geocode an address to get latitude and longitude
     * 
     * @param address Full address to geocode
     * @param zipcode Zipcode to help with geocoding
     * @return Array with [latitude, longitude] or null if geocoding fails
     */
    public double[] geocodeAddress(String address, String zipcode) {
        if (!geocodingEnabled) {
            logger.debug("Geocoding is disabled");
            return null;
        }

        try {
            String fullAddress = address + ", " + zipcode;
            
            if ("nominatim".equalsIgnoreCase(provider)) {
                return geocodeWithNominatim(fullAddress);
            } else if ("google".equalsIgnoreCase(provider)) {
                return geocodeWithGoogle(fullAddress);
            } else {
                logger.warn("Unknown geocoding provider: {}", provider);
                return null;
            }
        } catch (Exception e) {
            logger.error("Error geocoding address: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Geocode using OpenStreetMap Nominatim (free, rate-limited)
     */
    private double[] geocodeWithNominatim(String address) {
        try {
            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = nominatimUrl + encodedAddress + "&format=json&limit=1";
            
            logger.debug("Geocoding with Nominatim: {}", address);
            
            // Add delay to respect Nominatim's usage policy (max 1 request per second)
            Thread.sleep(1000);
            
            String response = restTemplate.getForObject(URI.create(url), String.class);
            
            if (response == null || response.equals("[]")) {
                logger.warn("No results found for address: {}", address);
                return null;
            }
            
            JsonNode results = objectMapper.readTree(response);
            if (results.isArray() && results.size() > 0) {
                JsonNode firstResult = results.get(0);
                double lat = firstResult.get("lat").asDouble();
                double lon = firstResult.get("lon").asDouble();
                
                logger.info("Successfully geocoded address to: [{}, {}]", lat, lon);
                return new double[]{lat, lon};
            }
            
            return null;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            logger.error("Geocoding interrupted", e);
            return null;
        } catch (Exception e) {
            logger.error("Error with nominatim geocoding: {}", e.getMessage());
            return null;
        }
    }

    /**
     * Geocode using Google Maps Geocoding API (requires API key)
     */
    private double[] geocodeWithGoogle(String address) {
        try {
            if (apiKey == null || apiKey.isEmpty()) {
                logger.warn("Google Maps API key not configured");
                return null;
            }

            String encodedAddress = URLEncoder.encode(address, StandardCharsets.UTF_8);
            String url = googleUrl + encodedAddress + "&key=" + apiKey;
            
            logger.debug("Geocoding with Google Maps: {}", address);
            
            String response = restTemplate.getForObject(URI.create(url), String.class);
            
            if (response == null) {
                return null;
            }
            
            JsonNode root = objectMapper.readTree(response);
            String status = root.get("status").asText();
            
            if ("OK".equals(status)) {
                JsonNode results = root.get("results");
                if (results.isArray() && results.size() > 0) {
                    JsonNode location = results.get(0).get("geometry").get("location");
                    double lat = location.get("lat").asDouble();
                    double lng = location.get("lng").asDouble();
                    
                    logger.info("Successfully geocoded address to: [{}, {}]", lat, lng);
                    return new double[]{lat, lng};
                }
            } else {
                logger.warn("Google geocoding failed with status: {}", status);
            }
            
            return null;
        } catch (Exception e) {
            logger.error("Error with Google geocoding: {}", e.getMessage());
            return null;
        }
    }
}

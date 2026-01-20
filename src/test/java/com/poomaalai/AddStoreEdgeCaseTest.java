package com.poomaalai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.repository.CreatorStoreRepository;
import com.poomaalai.security.JwtTokenProvider;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AddStoreEdgeCaseTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String validToken;
    private Creator testUser;

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();

        // Create test user
        testUser = new Creator();
        testUser.setEmail("testowner@test.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setFirstName("Test");
        testUser.setLastName("Owner");
        testUser.setPhone("5551001000");
        testUser.setAddress("123 Test St");
        testUser.setZipcode("12345");
        testUser = creatorRepository.save(testUser);

        validToken = jwtTokenProvider.generateToken("testowner@test.com");
    }

    private String createValidStoreBody() {
        return "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";
    }

    @Test
    void addStore_withValidToken_success() throws Exception {
        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createValidStoreBody()))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withoutToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/creator-store/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createValidStoreBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addStore_withInvalidToken_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer invalid.token.here")
                .contentType(MediaType.APPLICATION_JSON)
                .content(createValidStoreBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addStore_withExpiredToken_returnsUnauthorized() throws Exception {
        // This would require creating an expired token - implementation depends on your JWT setup
        String expiredToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ0ZXN0QGV4YW1wbGUuY29tIiwiaWF0IjoxNjAwMDAwMDAwLCJleHAiOjE2MDAwMDAwMDB9.invalid";
        
        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + expiredToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createValidStoreBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addStore_withoutBearerPrefix_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createValidStoreBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addStore_withMalformedAuthHeader_returnsUnauthorized() throws Exception {
        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "InvalidFormat " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createValidStoreBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addStore_withMissingStoreName_returnsBadRequest() throws Exception {
        String requestBody = "{\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withEmptyStoreName_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withStoreNameTooShort_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"A\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withStoreNameTooLong_returnsBadRequest() throws Exception {
        String longName = "A".repeat(201);
        String requestBody = "{\"name\":\"" + longName + "\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withInvalidStoreNameCharacters_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Store@#$%\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withMissingAddress_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withAddressEmpty_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withAddressTooLong_returnsBadRequest() throws Exception {
        String longAddress = "A".repeat(256);
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"" + longAddress + "\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withMissingPhone_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withInvalidPhoneFormat_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"abc\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withMissingZipcode_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withInvalidZipcode_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"!@#$%\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withZipcodeTooShort_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withValidSocialMediaHandles_success() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"," +
                "\"instagramHandle\":\"teststore\",\"facebookHandle\":\"teststore.official\"," +
                "\"youtubeHandle\":\"teststorechannel\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withInvalidInstagramHandle_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"," +
                "\"instagramHandle\":\"test@store#\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withSocialHandleTooLong_returnsBadRequest() throws Exception {
        String longHandle = "a".repeat(101);
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"," +
                "\"instagramHandle\":\"" + longHandle + "\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withXssInName_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"<script>alert('xss')</script>\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withXssInAddress_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"<img src=x onerror=alert(1)>\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withMalformedJson_returnsBadRequest() throws Exception {
        String requestBody = "{name:Test Store,address:123 Store St}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withEmptyBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withNullFields_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":null,\"address\":null," +
                "\"zipcode\":null,\"phone\":null}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_tokenForNonExistentUser_returnsUnauthorized() throws Exception {
        String tokenForNonExistent = jwtTokenProvider.generateToken("nonexistent@test.com");

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + tokenForNonExistent)
                .contentType(MediaType.APPLICATION_JSON)
                .content(createValidStoreBody()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addStore_withValidParenthesesInName_success() throws Exception {
        String requestBody = "{\"name\":\"Test Store (Main Branch)\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withValidInternationalPhone_success() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"+1-555-123-4567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }
}

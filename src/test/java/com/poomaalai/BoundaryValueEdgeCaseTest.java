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
public class BoundaryValueEdgeCaseTest {

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

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();

        // Create test user
        Creator testUser = new Creator();
        testUser.setEmail("test@test.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("5551008000");
        testUser.setAddress("123 Test St");
        testUser.setZipcode("12345");
        creatorRepository.save(testUser);

        validToken = jwtTokenProvider.generateToken("test@test.com");
    }

    // First name boundary tests
    @Test
    void register_withFirstNameExactly3Chars_success() throws Exception {
        String requestBody = "{\"firstName\":\"abc\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test1@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withFirstNameExactly15Chars_success() throws Exception {
        String requestBody = "{\"firstName\":\"abcdefghijklmno\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test2@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withFirstName16Chars_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"abcdefghijklmnop\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test3@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Phone number boundary tests
    @Test
    void register_withPhoneExactly7Chars_success() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test4@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withPhoneExactly15Chars_success() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"123456789012345\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test5@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withPhone21Chars_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"123456789012345678901\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test6@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Password boundary tests
    @Test
    void register_withPasswordExactly8Chars_success() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test7@example.com\"," +
                "\"password\":\"Pass123!\",\"confirmPassword\":\"Pass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withPassword7Chars_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test8@example.com\"," +
                "\"password\":\"Pas12!\",\"confirmPassword\":\"Pas12!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPassword72Chars_success() throws Exception {
        String longPassword = "Aa1!" + "a".repeat(68);
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test9@example.com\"," +
                "\"password\":\"" + longPassword + "\",\"confirmPassword\":\"" + longPassword + "\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // Store name boundary tests
    @Test
    void addStore_withNameExactly2Chars_success() throws Exception {
        String requestBody = "{\"name\":\"AB\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withName1Char_returnsBadRequest() throws Exception {
        String requestBody = "{\"name\":\"A\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withNameExactly200Chars_success() throws Exception {
        String name = "A".repeat(200);
        String requestBody = "{\"name\":\"" + name + "\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withName201Chars_returnsBadRequest() throws Exception {
        String name = "A".repeat(201);
        String requestBody = "{\"name\":\"" + name + "\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Address boundary tests
    @Test
    void addStore_withAddressExactly5Chars_success() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"12345\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
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
    void addStore_withAddressExactly255Chars_success() throws Exception {
        String address = "1".repeat(255);
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"" + address + "\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withAddress256Chars_returnsBadRequest() throws Exception {
        String address = "1".repeat(256);
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"" + address + "\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Social media handle boundary tests
    @Test
    void addStore_withSocialHandle1Char_success() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\",\"instagramHandle\":\"a\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withSocialHandleExactly100Chars_success() throws Exception {
        String handle = "a".repeat(100);
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\",\"instagramHandle\":\"" + handle + "\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withSocialHandle101Chars_returnsBadRequest() throws Exception {
        String handle = "a".repeat(101);
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\",\"instagramHandle\":\"" + handle + "\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Large payload tests
    @Test
    void register_withVeryLargePayload_handledProperly() throws Exception {
        String largeAddress = "A".repeat(5000); // Exceeds max allowed
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"" + largeAddress + "\",\"zipcode\":\"12345\",\"email\":\"large@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withAllOptionalFieldsMaxLength_success() throws Exception {
        String name = "A".repeat(200);
        String address = "1".repeat(255);
        String instagram = "a".repeat(100);
        String facebook = "b".repeat(100);
        String youtube = "c".repeat(100);
        
        String requestBody = "{\"name\":\"" + name + "\",\"address\":\"" + address + "\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"," +
                "\"instagramHandle\":\"" + instagram + "\"," +
                "\"facebookHandle\":\"" + facebook + "\"," +
                "\"youtubeHandle\":\"" + youtube + "\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void register_withUnicodeCharacters_handledProperly() throws Exception {
        String requestBody = "{\"firstName\":\"José\",\"lastName\":\"Müller\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"unicode@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        // Should succeed - Unicode characters are allowed in names
        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withZipcode00000_success() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"00000\",\"email\":\"zero@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withZipcode99999_success() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"99999\",\"email\":\"nine@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }
}

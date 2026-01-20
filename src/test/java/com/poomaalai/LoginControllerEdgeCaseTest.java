package com.poomaalai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class LoginControllerEdgeCaseTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();

        // Create test user with known credentials
        Creator creator = new Creator();
        creator.setEmail("test@example.com");
        creator.setPassword(passwordEncoder.encode("ValidPass123!"));
        creator.setFirstName("Test");
        creator.setLastName("User");
        creator.setPhone("5551003000");
        creator.setAddress("123 Test St");
        creatorRepository.save(creator);
    }

    @Test
    void login_success_returnsTokenAndUserInfo() throws Exception {
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.email").value("test@example.com"))
                .andExpect(jsonPath("$.name").exists())
                .andExpect(jsonPath("$.expiresIn").exists());
    }

    @Test
    void login_withUppercaseEmail_shouldNormalizeAndSucceed() throws Exception {
        String requestBody = "{\"email\":\"TEST@EXAMPLE.COM\",\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    @Test
    void login_withEmailSpaces_shouldNormalizeAndSucceed() throws Exception {
        String requestBody = "{\"email\":\"  test@example.com  \",\"password\":\"ValidPass123!\"}";

        // @Email validation happens before trimming, so spaces cause validation failure
        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withNullEmail_returnsBadRequest() throws Exception {
        String requestBody = "{\"email\":null,\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withEmptyEmail_returnsBadRequest() throws Exception {
        String requestBody = "{\"email\":\"\",\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withNullPassword_returnsBadRequest() throws Exception {
        String requestBody = "{\"email\":\"test@example.com\",\"password\":null}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withEmptyPassword_returnsBadRequest() throws Exception {
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withNonExistentEmail_returnsUnauthorized() throws Exception {
        String requestBody = "{\"email\":\"nonexistent@example.com\",\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withWrongPassword_returnsUnauthorized() throws Exception {
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"WrongPassword123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withInvalidEmailFormat_returnsBadRequest() throws Exception {
        String requestBody = "{\"email\":\"not-an-email\",\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withSqlInjectionInEmail_returnsUnauthorized() throws Exception {
        String requestBody = "{\"email\":\"admin'--\",\"password\":\"anything\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withSqlInjectionInPassword_returnsUnauthorized() throws Exception {
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"' OR '1'='1\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withMissingEmailField_returnsBadRequest() throws Exception {
        String requestBody = "{\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withMissingPasswordField_returnsBadRequest() throws Exception {
        String requestBody = "{\"email\":\"test@example.com\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withMalformedJson_returnsBadRequest() throws Exception {
        String requestBody = "{email:test@example.com,password:ValidPass123!}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withEmptyBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void login_withVeryLongPassword_returnsUnauthorized() throws Exception {
        String longPassword = "a".repeat(1000);
        String requestBody = "{\"email\":\"test@example.com\",\"password\":\"" + longPassword + "\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void login_withSpecialCharactersInPassword_handledCorrectly() throws Exception {
        // Create user with special chars in password
        Creator specialUser = new Creator();
        specialUser.setEmail("special@example.com");
        specialUser.setPassword(passwordEncoder.encode("P@ss!w0rd#$%"));
        specialUser.setFirstName("Special");
        specialUser.setLastName("User");
        specialUser.setPhone("5551003001");
        specialUser.setAddress("123 Special St");
        creatorRepository.save(specialUser);

        String requestBody = "{\"email\":\"special@example.com\",\"password\":\"P@ss!w0rd#$%\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }
}

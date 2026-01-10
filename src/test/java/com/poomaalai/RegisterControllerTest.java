package com.poomaalai;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;

@SpringBootTest
@Transactional
public class RegisterControllerTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @BeforeEach
    void setUp() {
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void registerCreator_success_createsUserWithHashedPassword() throws Exception {
        String email = "newuser@test.com";
        String requestBody = "{\"firstName\":\"New\",\"lastName\":\"User\",\"phone\":\"1234567890\"," +
                "\"address\":\"Some address\",\"zipcode\":\"12345\",\"email\":\"" + email + "\"," +
                "\"password\":\"Strongpass!123\",\"confirmPassword\":\"Strongpass!123\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail(email).orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getPassword()).isNotEqualTo("Strongpass!123");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(encoder.matches("Strongpass!123", created.getPassword())).isTrue();
    }

    @Test
    void registerCreator_passwordMismatch_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"Mis\",\"lastName\":\"Match\",\"phone\":\"1234567890\"," +
                "\"address\":\"Addr\",\"zipcode\":\"12345\",\"email\":\"mismatch@test.com\"," +
                "\"password\":\"pw1\",\"confirmPassword\":\"pw2\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerCreator_duplicateEmail_returnsBadRequest() throws Exception {
        String email = "duplicate@test.com";
        Creator existing = new Creator();
        existing.setEmail(email);
        existing.setPassword("hashedPassword");
        existing.setFirstName("Existing");
        existing.setLastName("User");
        existing.setPhone("1234567890");
        existing.setAddress("123 Existing St");
        creatorRepository.save(existing);

        String requestBody = "{\"firstName\":\"New\",\"lastName\":\"User\",\"phone\":\"1234567890\"," +
                "\"address\":\"Addr\",\"zipcode\":\"12345\",\"email\":\"" + email + "\"," +
                "\"password\":\"password123\",\"confirmPassword\":\"password123\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }
}


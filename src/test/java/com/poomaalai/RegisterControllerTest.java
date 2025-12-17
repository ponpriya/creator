package com.poomaalai;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
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
    void showRegistrationForm_containsFields() throws Exception {
        mockMvc.perform(get("/creator/register"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"email\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"password\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("name=\"confirmPassword\"")));
    }

    @Test
    void registerCreator_success_createsUserWithHashedPassword() throws Exception {
        String email = "newuser@test.com";
        mockMvc.perform(post("/creator/register")
                .param("name", "New User")
                .param("firstName", "New")
                .param("lastName", "User")
                .param("phone", "1234567890")
                .param("address", "Some address")
                .param("zipcode", "12345")
                .param("email", email)
                .param("password", "strongpass")
                .param("confirmPassword", "strongpass"))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail(email).orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getPassword()).isNotEqualTo("strongpass");
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        assertThat(encoder.matches("strongpass", created.getPassword())).isTrue();
    }

    @Test
    void registerCreator_passwordMismatch_redirectsWithError() throws Exception {
        mockMvc.perform(post("/creator/register")
                .param("name", "Mismatch User")
                .param("firstName", "Mis")
                .param("lastName", "Match")
                .param("phone", "1234567890")
                .param("address", "Addr")
                .param("zipcode", "12345")
                .param("email", "mismatch@test.com")
                .param("password", "pw1")
                .param("confirmPassword", "pw2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/creator/register?error=passwordmismatch"));
    }
}

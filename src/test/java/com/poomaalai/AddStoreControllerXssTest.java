package com.poomaalai;

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
import com.poomaalai.security.JwtTokenProvider;

@SpringBootTest
@Transactional
public class AddStoreControllerXssTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private String jwtToken;

    @BeforeEach
    void setUp() {
        creatorRepository.deleteAll();
        Creator c = new Creator();
        c.setEmail("xss@test.com");
        c.setPassword(passwordEncoder.encode("password"));
        c.setFirstName("XSS");
        c.setLastName("Tester");
        c.setPhone("1234567890");
        c.setAddress("123 Test St");
        creatorRepository.save(c);
        
        jwtToken = jwtTokenProvider.generateToken("xss@test.com");
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void addStore_shouldEscapeXssPayloads() throws Exception {
        String requestBody = "{\"name\":\"<script>alert('x')</script>\"," +
                "\"address\":\"<img src=x onerror=alert(1)>\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void addStore_withoutAuth_returnsUnauthorized() throws Exception {
        String requestBody = "{\"name\":\"Test Store\",\"address\":\"123 Main St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isUnauthorized());
    }
}

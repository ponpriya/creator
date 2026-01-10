package com.poomaalai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.repository.CreatorStoreRepository;
import com.poomaalai.security.JwtTokenProvider;

@SpringBootTest
@Transactional
public class AddAndDisplayIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @Autowired
    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void add_store_and_search_integration_test() throws Exception {
        // Create user via registration
        String registerBody = "{\"email\":\"int@test.com\",\"password\":\"StrongPass123!\"," +
                "\"confirmPassword\":\"StrongPass123!\",\"firstName\":\"Integration\"," +
                "\"lastName\":\"Test\",\"phone\":\"1234567890\",\"address\":\"123 Main\"," +
                "\"zipcode\":\"33333\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerBody))
                .andExpect(status().isOk());

        // Generate JWT token
        String jwtToken = jwtTokenProvider.generateToken("int@test.com");

        // Add store with authentication
        String storeBody = "{\"name\":\"Test Store\",\"address\":\"123 Store St\"," +
                "\"zipcode\":\"33333\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(storeBody))
                .andExpect(status().isCreated());

        // Search for the added store
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "33333"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Test Store"))
                .andExpect(jsonPath("$[0].zipcode").value("33333"));
    }
}

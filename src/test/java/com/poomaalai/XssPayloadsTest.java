package com.poomaalai;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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
import com.poomaalai.repository.CreatorStoreRepository;
import com.poomaalai.security.JwtTokenProvider;

@SpringBootTest
@Transactional
public class XssPayloadsTest {

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

    private String jwtToken;

    static Stream<String> xssPayloads() {
        return Stream.of(
                "<script>alert(1)</script>",
                "\"><img src=x onerror=alert(1)>",
                "<svg/onload=alert(1)>",
                "<body onload=alert(1)>");
    }

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();

        // Create a test user
        Creator creator = new Creator();
        creator.setEmail("fuzz@test.com");
        creator.setPassword(passwordEncoder.encode("StrongPass123!"));
        creator.setFirstName("Fuzz");
        creator.setLastName("Tester");
        creator.setPhone("1234567890");
        creator.setAddress("123 Fuzz St");      
        creatorRepository.save(creator);

        jwtToken = jwtTokenProvider.generateToken("fuzz@test.com");
    }

    @ParameterizedTest
    @MethodSource("xssPayloads")
    void addStore_withXssPayload_shouldBeRejectedByValidation(String payload) throws Exception {
        String storeBody = "{\"name\":\"" + payload.replace("\"", "\\\"") + "\"," +
                "\"address\":\"" + payload.replace("\"", "\\\"") + "\"," +
                "\"zipcode\":\"44444\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(storeBody))
                .andExpect(status().isBadRequest());
    }
}

package com.poomaalai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class GetCreatorEndpointsTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Creator testCreator1;
    private Creator testCreator2;

    @BeforeEach
    void setUp() {
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();

        // Create test creators
        testCreator1 = new Creator();
        testCreator1.setEmail("creator1@test.com");
        testCreator1.setPassword(passwordEncoder.encode("Password123!"));
        testCreator1.setFirstName("John");
        testCreator1.setLastName("Doe");
        testCreator1.setPhone("5551004000");
        testCreator1.setAddress("123 First St");
        testCreator1.setZipcode("11111");
        testCreator1 = creatorRepository.save(testCreator1);

        testCreator2 = new Creator();
        testCreator2.setEmail("creator2@test.com");
        testCreator2.setPassword(passwordEncoder.encode("Password123!"));
        testCreator2.setFirstName("Jane");
        testCreator2.setLastName("Smith");
        testCreator2.setPhone("0987654321");
        testCreator2.setAddress("456 Second Ave");
        testCreator2.setZipcode("22222");
        testCreator2 = creatorRepository.save(testCreator2);
    }

    @Test
    void getCreatorById_withValidId_returnsCreator() throws Exception {
        mockMvc.perform(get("/creator/" + testCreator1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testCreator1.getId()))
                .andExpect(jsonPath("$.email").value("creator1@test.com"))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.phone").value("5551004000"))
                .andExpect(jsonPath("$.address").value("123 First St"))
                .andExpect(jsonPath("$.zipcode").value("11111"));
    }

    @Test
    void getCreatorById_withNonExistentId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/creator/99999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCreatorById_withNegativeId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/creator/-1"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCreatorById_withZeroId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/creator/0"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCreatorById_withInvalidIdFormat_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void getCreatorById_withVeryLargeId_returnsNotFound() throws Exception {
        mockMvc.perform(get("/creator/2147483647"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getCreatorById_passwordNotIncludedInResponse() throws Exception {
        mockMvc.perform(get("/creator/" + testCreator1.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void getAllCreators_returnsAllCreators() throws Exception {
        mockMvc.perform(get("/creator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].email").exists())
                .andExpect(jsonPath("$[1].email").exists());
    }

    @Test
    void getAllCreators_withNoCreators_returnsEmptyArray() throws Exception {
        creatorRepository.deleteAll();

        mockMvc.perform(get("/creator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void getAllCreators_passwordsNotIncludedInResponse() throws Exception {
        mockMvc.perform(get("/creator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].password").doesNotExist())
                .andExpect(jsonPath("$[1].password").doesNotExist());
    }

    @Test
    void getAllCreators_withMultipleCreators_returnsCorrectCount() throws Exception {
        // Add more creators
        for (int i = 3; i <= 5; i++) {
            Creator creator = new Creator();
            creator.setEmail("creator" + i + "@test.com");
            creator.setPassword(passwordEncoder.encode("Password123!"));
            creator.setFirstName("First" + i);
            creator.setLastName("Last" + i);
            creator.setPhone("555000000" + i);
            creator.setAddress(i + " Address St");
            creator.setZipcode("0000" + i);
            creatorRepository.save(creator);
        }

        mockMvc.perform(get("/creator"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(5));
    }
}

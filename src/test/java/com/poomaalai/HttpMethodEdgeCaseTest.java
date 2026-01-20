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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
public class HttpMethodEdgeCaseTest {

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
        testUser.setPhone("5551006000");
        testUser.setAddress("123 Test St");
        testUser.setZipcode("12345");
        creatorRepository.save(testUser);

        validToken = jwtTokenProvider.generateToken("test@test.com");
    }

    // Test invalid HTTP methods on registration endpoint
    @Test
    void register_withGetMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/creator/api/register"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void register_withPutMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void register_withDeleteMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/creator/api/register"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void register_withPatchMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(patch("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    // Test invalid HTTP methods on login endpoint
    @Test
    void login_withGetMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/creator/api/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void login_withPutMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/creator/api/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void login_withDeleteMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/creator/api/login"))
                .andExpect(status().isMethodNotAllowed());
    }

    // Test invalid HTTP methods on get creator by ID
    @Test
    void getCreatorById_withPostMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/creator/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getCreatorById_withPutMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/creator/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getCreatorById_withDeleteMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/creator/1"))
                .andExpect(status().isMethodNotAllowed());
    }

    // Test invalid HTTP methods on get all creators
    @Test
    void getAllCreators_withPostMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/creator")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getAllCreators_withPutMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/creator")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void getAllCreators_withDeleteMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/creator"))
                .andExpect(status().isMethodNotAllowed());
    }

    // Test invalid HTTP methods on search store
    @Test
    void searchStore_withPostMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/creator-store/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void searchStore_withPutMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/creator-store/search")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void searchStore_withDeleteMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/creator-store/search"))
                .andExpect(status().isMethodNotAllowed());
    }

    // Test invalid HTTP methods on add store
    @Test
    void addStore_withGetMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(get("/creator-store/add"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void addStore_withPutMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(put("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void addStore_withDeleteMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(delete("/creator-store/add")
                .header("Authorization", "Bearer " + validToken))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    void addStore_withPatchMethod_returnsMethodNotAllowed() throws Exception {
        mockMvc.perform(patch("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isMethodNotAllowed());
    }
}

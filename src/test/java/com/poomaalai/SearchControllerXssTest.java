package com.poomaalai;

import static org.hamcrest.Matchers.containsString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.entity.CreatorStore;
import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.repository.CreatorStoreRepository;

@SpringBootTest
@Transactional
public class SearchControllerXssTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @Autowired
    private CreatorRepository creatorRepository;

    @BeforeEach
    void setUp() {
        creatorRepository.deleteAll();
        creatorStoreRepository.deleteAll();
        
        Creator owner = new Creator();
        owner.setEmail("testuser@example.com");
        owner.setPassword("password");
        owner.setFirstName("Test");
        owner.setLastName("User");
        owner.setPhone("1234567890");
        owner.setAddress("123 Test St");
        owner = creatorRepository.save(owner);
        
        CreatorStore s = new CreatorStore();
        s.setOwner(owner);
        s.setName("<script>alert('x')</script>");
        s.setAddress("<img src=x onerror=alert(1)>");
        s.setZipcode("99999");
        s.setPhone("1234567890");
        creatorStoreRepository.save(s);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void search_shouldReturnResults() throws Exception {
        mockMvc.perform(get("/creator-store/search").param("zipcode", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(content().string(containsString("99999")));
    }

    @Test
    void search_withoutZipcode_returnsAllStores() throws Exception {
        mockMvc.perform(get("/creator-store/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void search_withInvalidZipcode_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search").param("zipcode", "invalid"))
                .andExpect(status().isBadRequest());
    }
}

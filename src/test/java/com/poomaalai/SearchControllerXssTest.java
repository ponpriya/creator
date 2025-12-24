package com.poomaalai;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.CreatorStore;
import com.poomaalai.repository.CreatorStoreRepository;

@SpringBootTest
@Transactional
public class SearchControllerXssTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        CreatorStore s = new CreatorStore();
        s.setName("<script>alert('x')</script>");
        s.setAddress("<img src=x onerror=alert(1)>");
        s.setZipcode("99999");
        s.setPhone("1234567890");
        creatorStoreRepository.save(s);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void search_shouldEscapeMaliciousContent() throws Exception {
        mockMvc.perform(get("/creator-store/search").param("zipcode", "99999"))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>"))));
    }
}

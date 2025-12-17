package com.poomaalai;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;

@SpringBootTest
@Transactional
public class AddStoreControllerXssTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @BeforeEach
    void setUp() {
        creatorRepository.deleteAll();
        Creator c = new Creator();
        c.setEmail("xss@test.com");
        c.setPassword("password");
        c.setName("Xss User");
        creatorRepository.save(c);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void addStore_shouldEscapeXssPayloads() throws Exception {
        mockMvc.perform(post("/creator-store/add")
                .with(user("xss@test.com").roles("USER"))
                .with(csrf())
                .param("name", "<script>alert('x')</script>")
                .param("address", "<img src=x onerror=alert(1)>")
                .param("zipcode", "12345")
                .param("phone", "1234567890"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/creator/dashboard").with(user("xss@test.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>"))));
    }
}

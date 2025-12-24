package com.poomaalai;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
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
public class DashboardXssTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();

        Creator c = new Creator();
        c.setEmail("dash@test.com");
        c.setPassword("pw");
        c.setName("DashUser");
        creatorRepository.save(c);

        CreatorStore s = new CreatorStore();
        s.setName("<script>alert('x')</script>");
        s.setAddress("<img src=x onerror=alert(1)>");
        s.setZipcode("88888");
        s.setPhone("1234567890");
        s.setOwner(c);
        creatorStoreRepository.save(s);
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
    }

    @Test
    void dashboard_should_escape_malicious_content() throws Exception {
        mockMvc.perform(get("/creator/dashboard").with(user("dash@test.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(not(containsString("onerror="))));
    }
}

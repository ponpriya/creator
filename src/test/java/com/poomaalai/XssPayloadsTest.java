package com.poomaalai;

import java.util.stream.Stream;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
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

@SpringBootTest
@Transactional
public class XssPayloadsTest {

        @Autowired
        private WebApplicationContext wac;

        private MockMvc mockMvc;

    static Stream<String> xssPayloads() {
        return Stream.of(
                "<script>alert(1)</script>",
                "\"><img src=x onerror=alert(1)>",
                "<svg/onload=alert(1)>",
                "<body onload=alert(1)>");
    }

    @ParameterizedTest
    @MethodSource("xssPayloads")
    void add_and_search_should_not_render_unescaped(String payload) throws Exception {
                this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac).build();
        mockMvc.perform(post("/creator/register")
                .param("email", "fuzz@test.com")
                .param("password", "pw")
                .param("confirmPassword", "pw"));

        mockMvc.perform(post("/creator-store/add")
                .with(user("fuzz@test.com").roles("USER"))
                .with(csrf())
                .param("name", payload)
                .param("address", payload)
                .param("zipcode", "44444")
                .param("phone", "1234567890"))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/creator/dashboard").with(user("fuzz@test.com").roles("USER")))
                .andExpect(status().isOk())
                .andExpect(content().string(not(containsString("<script>"))))
                .andExpect(content().string(not(containsString("onload="))));
    }
}

package com.poomaalai;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.repository.CreatorStoreRepository;
import com.poomaalai.security.RateLimitFilter;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "rate.limit.enabled=true",
    "rate.limit.capacity=100",
    "rate.limit.refill.tokens=100",
    "rate.limit.refill.duration=60"
})
public class RateLimitFilterTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;
    
    @Autowired
    private RateLimitFilter rateLimitFilter;

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .addFilters(rateLimitFilter)
                .build();

        // Create test user for login tests
        Creator creator = new Creator();
        creator.setEmail("ratelimit@test.com");
        creator.setPassword(passwordEncoder.encode("Password123!"));
        creator.setFirstName("Rate");
        creator.setLastName("Limit");
        creator.setPhone("5551010000");
        creator.setAddress("123 Test St");
        creator.setZipcode("12345");
        creatorRepository.save(creator);
    }

    @Test
    void rateLimiter_allowsRequestsUnderLimit() throws Exception {
        // Make 10 requests - should all succeed
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", "192.168.1.100"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void rateLimiter_blocksRequestsOverLimit() throws Exception {
        String clientIp = "192.168.1.101";
        int requestLimit = 100;
        int successCount = 0;
        int blockedCount = 0;

        // Make 105 requests to exceed the limit
        for (int i = 0; i < requestLimit + 5; i++) {
            MvcResult result = mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", clientIp))
                    .andReturn();

            if (result.getResponse().getStatus() == 200) {
                successCount++;
            } else if (result.getResponse().getStatus() == 429) {
                blockedCount++;
            }
        }

        // First 100 requests should succeed
        assertThat(successCount).isEqualTo(requestLimit);
        // Next 5 requests should be blocked
        assertThat(blockedCount).isEqualTo(5);
    }

    @Test
    void rateLimiter_returnsCorrectErrorResponse() throws Exception {
        String clientIp = "192.168.1.102";

        // Exhaust the rate limit
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", clientIp))
                    .andExpect(status().isOk());
        }

        // Next request should be blocked with proper error message
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", clientIp))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Too many requests. Please try again later."));
    }

    @Test
    void rateLimiter_separateLimitsForDifferentIPs() throws Exception {
        String ip1 = "192.168.1.103";
        String ip2 = "192.168.1.104";

        // Exhaust limit for first IP
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", ip1))
                    .andExpect(status().isOk());
        }

        // First IP should be blocked
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", ip1))
                .andExpect(status().isTooManyRequests());

        // Second IP should still work
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", ip2))
                .andExpect(status().isOk());
    }

    @Test
    void rateLimiter_appliesAcrossAllEndpoints() throws Exception {
        String clientIp = "192.168.1.105";

        // Make requests to different endpoints
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", clientIp))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/creator")
                    .header("X-Forwarded-For", clientIp))
                    .andExpect(status().isOk());
        }

        // 101st request should be blocked regardless of endpoint
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", clientIp))
                .andExpect(status().isTooManyRequests());

        mockMvc.perform(get("/creator")
                .header("X-Forwarded-For", clientIp))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimiter_worksWithRemoteAddrWhenNoXForwardedFor() throws Exception {
        // Make requests without X-Forwarded-For header
        // All will have same RemoteAddr in test context
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/creator-store/search"))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void rateLimiter_handlesMultipleIPsInXForwardedFor() throws Exception {
        String multipleIps = "192.168.1.106, 10.0.0.1, 172.16.0.1";

        // Should use first IP in the list
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", multipleIps))
                    .andExpect(status().isOk());
        }

        // 101st request should be blocked
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", multipleIps))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimiter_treatsFirstIPConsistently() throws Exception {
        String multipleIps1 = "192.168.1.107, 10.0.0.1";
        String multipleIps2 = "192.168.1.107, 172.16.0.1";

        // Both requests have same first IP, should share bucket
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", multipleIps1))
                    .andExpect(status().isOk());
        }

        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", multipleIps2))
                    .andExpect(status().isOk());
        }

        // 101st request should be blocked
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", multipleIps1))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimiter_worksForPostRequests() throws Exception {
        String clientIp = "192.168.1.108";
        String loginBody = "{\"email\":\"ratelimit@test.com\",\"password\":\"Password123!\"}";

        // Make multiple POST requests
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(post("/creator/api/login")
                    .header("X-Forwarded-For", clientIp)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody))
                    .andExpect(status().isOk());
        }

        // 101st request should be blocked
        mockMvc.perform(post("/creator/api/login")
                .header("X-Forwarded-For", clientIp)
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimiter_handlesIPWithSpaces() throws Exception {
        String ipWithSpaces = "  192.168.1.109  ";

        // Should trim spaces and work correctly
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", ipWithSpaces))
                    .andExpect(status().isOk());
        }

        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", ipWithSpaces))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimiter_countsFailedRequestsTowardLimit() throws Exception {
        String clientIp = "192.168.1.110";

        // Make requests that will fail (invalid zipcode)
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", clientIp)
                    .param("zipcode", "INVALID"))
                    .andExpect(status().isBadRequest());
        }

        // Rate limit should still apply
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", clientIp)
                .param("zipcode", "INVALID"))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimiter_allowsExactly100Requests() throws Exception {
        String clientIp = "192.168.1.111";

        // Make exactly 100 requests
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", clientIp))
                    .andExpect(status().isOk());
        }

        // 101st request should be blocked
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", clientIp))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimiter_handlesIPv6Addresses() throws Exception {
        String ipv6 = "2001:0db8:85a3:0000:0000:8a2e:0370:7334";

        // Should work with IPv6 addresses
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", ipv6))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void rateLimiter_handlesLocalhostAddresses() throws Exception {
        String localhost = "127.0.0.1";

        // Should work with localhost
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", localhost))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void rateLimiter_mixedGetAndPostRequests() throws Exception {
        String clientIp = "192.168.1.112";
        String loginBody = "{\"email\":\"ratelimit@test.com\",\"password\":\"Password123!\"}";

        // Mix GET and POST requests
        for (int i = 0; i < 50; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", clientIp))
                    .andExpect(status().isOk());

            mockMvc.perform(post("/creator/api/login")
                    .header("X-Forwarded-For", clientIp)
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(loginBody))
                    .andExpect(status().isOk());
        }

        // 101st request should be blocked
        mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", clientIp))
                .andExpect(status().isTooManyRequests());
    }

    @Test
    void rateLimiter_verifyResponseContentType() throws Exception {
        String clientIp = "192.168.1.113";

        // Exhaust the rate limit
        for (int i = 0; i < 100; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", clientIp));
        }

        // Verify response content type is JSON
        MvcResult result = mockMvc.perform(get("/creator-store/search")
                .header("X-Forwarded-For", clientIp))
                .andExpect(status().isTooManyRequests())
                .andReturn();

        assertThat(result.getResponse().getContentType()).isEqualTo("application/json");
    }

    @Test
    void rateLimiter_emptyXForwardedForFallsBackToRemoteAddr() throws Exception {
        // Send empty X-Forwarded-For header
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", ""))
                    .andExpect(status().isOk());
        }
    }

    @Test
    void rateLimiter_handlesPrivateIPRanges() throws Exception {
        String privateIp1 = "10.0.0.100";
        String privateIp2 = "172.16.0.100";
        String privateIp3 = "192.168.0.100";

        // Each private IP should have its own bucket
        for (int i = 0; i < 10; i++) {
            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", privateIp1))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", privateIp2))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/creator-store/search")
                    .header("X-Forwarded-For", privateIp3))
                    .andExpect(status().isOk());
        }
    }
}

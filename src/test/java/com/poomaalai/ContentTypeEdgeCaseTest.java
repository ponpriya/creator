package com.poomaalai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
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
@ActiveProfiles("test")
public class ContentTypeEdgeCaseTest {

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
        testUser.setPhone("5551009000");
        testUser.setAddress("123 Test St");
        testUser.setZipcode("12345");
        creatorRepository.save(testUser);

        validToken = jwtTokenProvider.generateToken("test@test.com");
    }

    @Test
    void register_withXmlContentType_returnsUnsupportedMediaType() throws Exception {
        String xmlBody = "<creator><email>test@test.com</email></creator>";

        mockMvc.perform(post("/creator/api/register")
                .contentType("application/xml")
                .content(xmlBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void register_withPlainTextContentType_returnsUnsupportedMediaType() throws Exception {
        String textBody = "email=test@test.com&password=pass";

        mockMvc.perform(post("/creator/api/register")
                .contentType("text/plain")
                .content(textBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void register_withFormUrlEncodedContentType_returnsUnsupportedMediaType() throws Exception {
        String formBody = "email=test@test.com&password=pass";

        mockMvc.perform(post("/creator/api/register")
                .contentType("application/x-www-form-urlencoded")
                .content(formBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void register_withNoContentType_returnsBadRequest() throws Exception {
        String jsonBody = "{\"email\":\"test@test.com\",\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .content(jsonBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void login_withXmlContentType_returnsUnsupportedMediaType() throws Exception {
        String xmlBody = "<login><email>test@test.com</email></login>";

        mockMvc.perform(post("/creator/api/login")
                .contentType("application/xml")
                .content(xmlBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void login_withPlainTextContentType_returnsUnsupportedMediaType() throws Exception {
        String textBody = "email=test@test.com&password=pass";

        mockMvc.perform(post("/creator/api/login")
                .contentType("text/plain")
                .content(textBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void login_withNoContentType_returnsUnsupportedMediaType() throws Exception {
        String jsonBody = "{\"email\":\"test@test.com\",\"password\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .content(jsonBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void addStore_withXmlContentType_returnsUnsupportedMediaType() throws Exception {
        String xmlBody = "<store><name>Test Store</name></store>";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType("application/xml")
                .content(xmlBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void addStore_withPlainTextContentType_returnsUnsupportedMediaType() throws Exception {
        String textBody = "name=Test Store&address=123 Main St";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType("text/plain")
                .content(textBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void addStore_withNoContentType_returnsUnsupportedMediaType() throws Exception {
        String jsonBody = "{\"name\":\"Test Store\",\"address\":\"123 Main St\"," +
                "\"zipcode\":\"12345\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .content(jsonBody))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void register_withInvalidContentTypeCharset_handlesProperly() throws Exception {
        String jsonBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        // Should still work with valid JSON and application/json content type regardless of charset
        mockMvc.perform(post("/creator/api/register")
                .contentType("application/json; charset=ISO-8859-1")
                .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void login_withJsonUtf8ContentType_success() throws Exception {
        String jsonBody = "{\"email\":\"test@test.com\",\"password\":\"Password123!\"}";

        mockMvc.perform(post("/creator/api/login")
                .contentType("application/json; charset=UTF-8")
                .content(jsonBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withMultipartFormData_returnsUnsupportedMediaType() throws Exception {
        mockMvc.perform(post("/creator/api/register")
                .contentType("multipart/form-data")
                .content("some-data"))
                .andExpect(status().isUnsupportedMediaType());
    }

    @Test
    void addStore_withOctetStreamContentType_returnsUnsupportedMediaType() throws Exception {
        byte[] binaryData = new byte[]{0x01, 0x02, 0x03};

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType("application/octet-stream")
                .content(binaryData))
                .andExpect(status().isUnsupportedMediaType());
    }
}

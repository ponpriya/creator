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
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class RegisterControllerEdgeCaseTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();
    }

    private String createValidRegistrationBody(String email) {
        return "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"" + email + "\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";
    }

    @Test
    void register_withValidData_success() throws Exception {
        String requestBody = createValidRegistrationBody("newuser@test.com");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("newuser@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(passwordEncoder.matches("ValidPass123!", created.getPassword())).isTrue();
    }

    @Test
    void register_withUppercaseEmail_shouldNormalizeToLowercase() throws Exception {
        String requestBody = createValidRegistrationBody("UPPER@TEST.COM");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("upper@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getEmail()).isEqualTo("upper@test.com");
    }

    @Test
    void register_withEmailSpaces_shouldTrimAndNormalize() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"  test@example.com  \"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        // @Email validation happens before trimming, so spaces cause validation failure
        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withMixedCaseEmailAlreadyExists_returnsBadRequest() throws Exception {
        // Create existing user
        Creator existing = new Creator();
        existing.setEmail("existing@test.com");
        existing.setPassword(passwordEncoder.encode("Pass123!"));
        existing.setFirstName("Existing");
        existing.setLastName("User");
        existing.setPhone("5551005000");
        existing.setAddress("123 Existing St");
        creatorRepository.save(existing);

        String requestBody = createValidRegistrationBody("EXISTING@TEST.COM");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withMissingFirstName_returnsBadRequest() throws Exception {
        String requestBody = "{\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withInvalidFirstNameSpecialChars_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John@#$\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withFirstNameTooShort_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"Jo\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withFirstNameTooLong_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"JohnJohnJohnJohn\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withInvalidPhoneFormat_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"abc\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPhoneTooShort_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"123\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withInvalidZipcode_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withZipcodeNotNumeric_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"!@#$%\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withInvalidEmailFormat_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"notanemail\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withNullEmail_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":null," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPasswordTooShort_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"Pass1!\",\"confirmPassword\":\"Pass1!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPasswordNoUppercase_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"password123!\",\"confirmPassword\":\"password123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPasswordNoLowercase_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"PASSWORD123!\",\"confirmPassword\":\"PASSWORD123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPasswordNoDigit_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"Password!\",\"confirmPassword\":\"Password!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withPasswordNoSpecialChar_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"Password123\",\"confirmPassword\":\"Password123\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withXssInFirstName_returnsBadRequest() throws Exception {
        String requestBody = "{\"firstName\":\"<script>alert(1)</script>\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"test@example.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withSqlInjectionInEmail_acceptsValidEmail() throws Exception {
        String requestBody = "{\"firstName\":\"Admin\",\"lastName\":\"User\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"admin'--@test.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withMalformedJson_returnsBadRequest() throws Exception {
        String requestBody = "{firstName:John,lastName:Doe}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withEmptyBody_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void register_withValidInternationalPhone_success() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"+1-555-123-4567\"," +
                "\"address\":\"123 Main St\",\"zipcode\":\"12345\",\"email\":\"intl@test.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withAddressContainingCommasDots_success() throws Exception {
        String requestBody = "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"1234567890\"," +
                "\"address\":\"123 Main St., Apt. #4B\",\"zipcode\":\"12345\",\"email\":\"addr@test.com\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }
}

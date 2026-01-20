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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.repository.CreatorStoreRepository;
import com.poomaalai.security.JwtTokenProvider;

/**
 * Test class for validating international address, phone number, and zipcode formats
 * for Add Store functionality across different countries:
 * - India
 * - Singapore
 * - Malaysia
 * - UK
 * - Canada
 * - USA
 * - Middle East (UAE, Saudi Arabia)
 * - Europe (France, Germany, Spain)
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class AddStoreInternationalValidationTest {

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
    private Creator testUser;

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();

        // Create test user
        testUser = new Creator();
        testUser.setEmail("intltest@test.com");
        testUser.setPassword(passwordEncoder.encode("Password123!"));
        testUser.setFirstName("Test");
        testUser.setLastName("User");
        testUser.setPhone("5551011000");
        testUser.setAddress("123 Test St");
        testUser.setZipcode("12345");
        testUser = creatorRepository.save(testUser);

        validToken = jwtTokenProvider.generateToken("intltest@test.com");
    }

    // ==================== USA Tests ====================
    
    @Test
    void addStore_withUSAddress_success() throws Exception {
        String requestBody = "{\"name\":\"US Store\",\"address\":\"123 Main Street, Apt 4B\"," +
                "\"zipcode\":\"12345\",\"phone\":\"+1-555-123-4567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withUSZipPlus4_success() throws Exception {
        String requestBody = "{\"name\":\"US Store\",\"address\":\"456 Oak Avenue\"," +
                "\"zipcode\":\"12345-6789\",\"phone\":\"(555) 123-4567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withUS10DigitPhone_success() throws Exception {
        String requestBody = "{\"name\":\"US Store\",\"address\":\"789 Elm Street\"," +
                "\"zipcode\":\"90210\",\"phone\":\"5551234567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ==================== India Tests ====================
    
    @Test
    void addStore_withIndiaAddress_success() throws Exception {
        String requestBody = "{\"name\":\"India Store\",\"address\":\"Plot No 123, Sector 15, Gurgaon, Haryana\"," +
                "\"zipcode\":\"122001\",\"phone\":\"+91-9876543210\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withIndiaComplexAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Chennai Store\",\"address\":\"No. 45/2, Anna Salai, T. Nagar, Chennai, Tamil Nadu\"," +
                "\"zipcode\":\"600017\",\"phone\":\"+91 98765 43210\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withIndiaLocalPhoneFormat_success() throws Exception {
        String requestBody = "{\"name\":\"Mumbai Store\",\"address\":\"Flat 301, Building A, Andheri East, Mumbai\"," +
                "\"zipcode\":\"400069\",\"phone\":\"9876543210\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ==================== Singapore Tests ====================
    
    @Test
    void addStore_withSingaporeAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Singapore Store\",\"address\":\"123 Orchard Road, #05-01, Singapore\"," +
                "\"zipcode\":\"238858\",\"phone\":\"+65-9123-4567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withSingaporeLocalPhone_success() throws Exception {
        String requestBody = "{\"name\":\"Singapore Store 2\",\"address\":\"101 Marina Bay Sands, Singapore\"," +
                "\"zipcode\":\"018956\",\"phone\":\"91234567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ==================== Malaysia Tests ====================
    
    @Test
    void addStore_withMalaysiaAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Malaysia Store\",\"address\":\"No. 45, Jalan Sultan Ismail, Kuala Lumpur\"," +
                "\"zipcode\":\"50250\",\"phone\":\"+60-12-345-6789\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withMalaysiaLocalPhone_success() throws Exception {
        String requestBody = "{\"name\":\"Penang Store\",\"address\":\"123 Lebuh Chulia, Georgetown, Penang\"," +
                "\"zipcode\":\"10200\",\"phone\":\"0123456789\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ==================== UK Tests ====================
    
    @Test
    void addStore_withUKAddress_success() throws Exception {
        String requestBody = "{\"name\":\"UK Store\",\"address\":\"10 Downing Street, Westminster, London\"," +
                "\"zipcode\":\"SW1A 2AA\",\"phone\":\"+44-20-7946-0958\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withUKPostcodeVariant1_success() throws Exception {
        String requestBody = "{\"name\":\"London Store\",\"address\":\"221B Baker Street, Marylebone, London\"," +
                "\"zipcode\":\"NW1 6XE\",\"phone\":\"020 7946 0958\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withUKMobilePhone_success() throws Exception {
        String requestBody = "{\"name\":\"Manchester Store\",\"address\":\"15 Market Street, Manchester\"," +
                "\"zipcode\":\"M1 1WR\",\"phone\":\"+44-7911-123456\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ==================== Canada Tests ====================
    
    @Test
    void addStore_withCanadaAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Canada Store\",\"address\":\"123 Main Street, Toronto, Ontario\"," +
                "\"zipcode\":\"M5H 2N2\",\"phone\":\"+1-416-555-1234\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withCanadaPostalCodeVariant_success() throws Exception {
        String requestBody = "{\"name\":\"Vancouver Store\",\"address\":\"456 Granville Street, Vancouver, BC\"," +
                "\"zipcode\":\"V6C 1S8\",\"phone\":\"(604) 555-1234\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withCanadaFrenchAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Montreal Store\",\"address\":\"789 Rue Saint-Catherine, Montréal, Québec\"," +
                "\"zipcode\":\"H3B 1A1\",\"phone\":\"+1-514-555-9876\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ==================== Middle East Tests (UAE, Saudi Arabia) ====================
    
    @Test
    void addStore_withUAEAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Dubai Store\",\"address\":\"Building 123, Sheikh Zayed Road, Dubai\"," +
                "\"zipcode\":\"12345\",\"phone\":\"+971-50-123-4567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withAbuDhabiAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Abu Dhabi Store\",\"address\":\"Al Maryah Island, Abu Dhabi\"," +
                "\"zipcode\":\"51133\",\"phone\":\"0501234567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withSaudiArabiaAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Riyadh Store\",\"address\":\"King Fahd Road, Riyadh\"," +
                "\"zipcode\":\"11564\",\"phone\":\"+966-50-123-4567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ==================== Europe Tests (France, Germany, Spain) ====================
    
    @Test
    void addStore_withFranceAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Paris Store\",\"address\":\"123 Avenue des Champs-Élysées, Paris\"," +
                "\"zipcode\":\"75008\",\"phone\":\"+33-1-42-34-56-78\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withFranceMobilePhone_success() throws Exception {
        String requestBody = "{\"name\":\"Lyon Store\",\"address\":\"45 Rue de la République, Lyon\"," +
                "\"zipcode\":\"69002\",\"phone\":\"+33-6-12-34-56-78\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withGermanyAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Berlin Store\",\"address\":\"Unter den Linden 77, Berlin\"," +
                "\"zipcode\":\"10117\",\"phone\":\"+49-30-12345678\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withGermanyMobilePhone_success() throws Exception {
        String requestBody = "{\"name\":\"Munich Store\",\"address\":\"Marienplatz 1, München\"," +
                "\"zipcode\":\"80331\",\"phone\":\"+49-151-12345678\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withSpainAddress_success() throws Exception {
        String requestBody = "{\"name\":\"Madrid Store\",\"address\":\"Calle Gran Vía, 28, Madrid\"," +
                "\"zipcode\":\"28013\",\"phone\":\"+34-91-123-4567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withSpainMobilePhone_success() throws Exception {
        String requestBody = "{\"name\":\"Barcelona Store\",\"address\":\"Passeig de Gràcia, 92, Barcelona\"," +
                "\"zipcode\":\"08008\",\"phone\":\"+34-612-345-678\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    // ==================== Edge Cases with International Formats ====================
    
    @Test
    void addStore_withMixedInternationalCharactersInAddress_success() throws Exception {
        String requestBody = "{\"name\":\"International Store\",\"address\":\"123 Rue de l'Église, Montréal, QC\"," +
                "\"zipcode\":\"H2X 1E1\",\"phone\":\"+1-514-555-1234\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withVeryLongInternationalPhone_success() throws Exception {
        String requestBody = "{\"name\":\"Store With Long Phone\",\"address\":\"123 Main Street\"," +
                "\"zipcode\":\"12345\",\"phone\":\"+1 (555) 123-4567\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }

    @Test
    void addStore_withAlphanumericZipcode_success() throws Exception {
        String requestBody = "{\"name\":\"Store Alphanumeric Zip\",\"address\":\"123 Test Road\"," +
                "\"zipcode\":\"SW1A 2AA\",\"phone\":\"1234567890\"}";

        mockMvc.perform(post("/creator-store/add")
                .header("Authorization", "Bearer " + validToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isCreated());
    }
}

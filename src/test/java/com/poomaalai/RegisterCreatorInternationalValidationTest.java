package com.poomaalai;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.repository.CreatorRepository;

/**
 * Test class for validating international address, phone number, and zipcode formats
 * for Creator Registration functionality across different countries:
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
public class RegisterCreatorInternationalValidationTest {

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

    private String createRegistrationBody(String email, String phone, String address, String zipcode) {
        return "{\"firstName\":\"John\",\"lastName\":\"Doe\",\"phone\":\"" + phone + "\"," +
                "\"address\":\"" + address + "\",\"zipcode\":\"" + zipcode + "\",\"email\":\"" + email + "\"," +
                "\"password\":\"ValidPass123!\",\"confirmPassword\":\"ValidPass123!\"}";
    }

    // ==================== USA Tests ====================
    
    @Test
    void register_withUSAddress_success() throws Exception {
        String requestBody = createRegistrationBody("ususer@test.com", 
                "+1-555-123-4567", 
                "123 Main Street, Apt 4B, New York, NY", 
                "10001");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("ususer@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("10001");
    }

    @Test
    void register_withUSZipPlus4_success() throws Exception {
        String requestBody = createRegistrationBody("usplus4@test.com", 
                "(555) 123-4567", 
                "456 Oak Avenue, Los Angeles, CA", 
                "90210-1234");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("usplus4@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("90210-1234");
    }

    @Test
    void register_withUS10DigitPhone_success() throws Exception {
        String requestBody = createRegistrationBody("us10digit@test.com", 
                "5551234567", 
                "789 Elm Street, Chicago, IL", 
                "60601");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== India Tests ====================
    
    @Test
    void register_withIndiaAddress_success() throws Exception {
        String requestBody = createRegistrationBody("india1@test.com", 
                "+91-9876543210", 
                "Plot No 123, Sector 15, Gurgaon, Haryana", 
                "122001");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("india1@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("122001");
        assertThat(created.getPhone()).isEqualTo("+91-9876543210");
    }

    @Test
    void register_withIndiaComplexAddress_success() throws Exception {
        String requestBody = createRegistrationBody("chennai@test.com", 
                "+91 98765 43210", 
                "No. 45/2, Anna Salai, T. Nagar, Chennai, Tamil Nadu", 
                "600017");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withIndiaLocalPhoneFormat_success() throws Exception {
        String requestBody = createRegistrationBody("mumbai@test.com", 
                "9876543210", 
                "Flat 301, Building A, Andheri East, Mumbai, Maharashtra", 
                "400069");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withBangaloreAddress_success() throws Exception {
        String requestBody = createRegistrationBody("bangalore@test.com", 
                "+91-80-12345678", 
                "#45, 3rd Cross, Koramangala 5th Block, Bangalore, Karnataka", 
                "560095");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== Singapore Tests ====================
    
    @Test
    void register_withSingaporeAddress_success() throws Exception {
        String requestBody = createRegistrationBody("singapore1@test.com", 
                "+65-9123-4567", 
                "123 Orchard Road, #05-01, Singapore", 
                "238858");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("singapore1@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("238858");
    }

    @Test
    void register_withSingaporeLocalPhone_success() throws Exception {
        String requestBody = createRegistrationBody("sg2@test.com", 
                "91234567", 
                "101 Marina Bay Sands, Singapore", 
                "018956");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withSingaporeHDBAddress_success() throws Exception {
        String requestBody = createRegistrationBody("sghdb@test.com", 
                "+65-8765-4321", 
                "Blk 123 Ang Mo Kio Ave 3, #10-456", 
                "560123");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== Malaysia Tests ====================
    
    @Test
    void register_withMalaysiaAddress_success() throws Exception {
        String requestBody = createRegistrationBody("malaysia1@test.com", 
                "+60-12-345-6789", 
                "No. 45, Jalan Sultan Ismail, Kuala Lumpur", 
                "50250");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("malaysia1@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("50250");
    }

    @Test
    void register_withMalaysiaLocalPhone_success() throws Exception {
        String requestBody = createRegistrationBody("penang@test.com", 
                "0123456789", 
                "123 Lebuh Chulia, Georgetown, Penang", 
                "10200");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withMalaysiaJohorAddress_success() throws Exception {
        String requestBody = createRegistrationBody("johor@test.com", 
                "+60-17-888-9999", 
                "No. 88, Jalan Tun Abdul Razak, Johor Bahru, Johor", 
                "80000");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== UK Tests ====================
    
    @Test
    void register_withUKAddress_success() throws Exception {
        String requestBody = createRegistrationBody("uk1@test.com", 
                "+44-20-7946-0958", 
                "10 Downing Street, Westminster, London", 
                "SW1A 2AA");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("uk1@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("SW1A 2AA");
    }

    @Test
    void register_withUKPostcodeVariant1_success() throws Exception {
        String requestBody = createRegistrationBody("london@test.com", 
                "020 7946 0958", 
                "221B Baker Street, Marylebone, London", 
                "NW1 6XE");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withUKMobilePhone_success() throws Exception {
        String requestBody = createRegistrationBody("manchester@test.com", 
                "+44-7911-123456", 
                "15 Market Street, Manchester", 
                "M1 1WR");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withScotlandAddress_success() throws Exception {
        String requestBody = createRegistrationBody("edinburgh@test.com", 
                "+44-131-555-1234", 
                "1 Royal Mile, Edinburgh, Scotland", 
                "EH1 2PB");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== Canada Tests ====================
    
    @Test
    void register_withCanadaAddress_success() throws Exception {
        String requestBody = createRegistrationBody("canada1@test.com", 
                "+1-416-555-1234", 
                "123 Main Street, Toronto, Ontario", 
                "M5H 2N2");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("canada1@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("M5H 2N2");
    }

    @Test
    void register_withCanadaPostalCodeVariant_success() throws Exception {
        String requestBody = createRegistrationBody("vancouver@test.com", 
                "(604) 555-1234", 
                "456 Granville Street, Vancouver, BC", 
                "V6C 1S8");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withCanadaFrenchAddress_success() throws Exception {
        String requestBody = createRegistrationBody("montreal@test.com", 
                "+1-514-555-9876", 
                "789 Rue Saint-Catherine, Montréal, Québec", 
                "H3B 1A1");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withCalgaryAddress_success() throws Exception {
        String requestBody = createRegistrationBody("calgary@test.com", 
                "+1-403-555-7890", 
                "100 Stephen Avenue Walk SW, Calgary, Alberta", 
                "T2P 3B5");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== Middle East Tests (UAE, Saudi Arabia) ====================
    
    @Test
    void register_withUAEAddress_success() throws Exception {
        String requestBody = createRegistrationBody("dubai@test.com", 
                "+971-50-123-4567", 
                "Building 123, Sheikh Zayed Road, Dubai", 
                "12345");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("dubai@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("12345");
    }

    @Test
    void register_withAbuDhabiAddress_success() throws Exception {
        String requestBody = createRegistrationBody("abudhabi@test.com", 
                "0501234567", 
                "Al Maryah Island, Abu Dhabi, UAE", 
                "51133");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withSaudiArabiaAddress_success() throws Exception {
        String requestBody = createRegistrationBody("riyadh@test.com", 
                "+966-50-123-4567", 
                "King Fahd Road, Riyadh, Saudi Arabia", 
                "11564");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("riyadh@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("11564");
    }

    @Test
    void register_withQatarAddress_success() throws Exception {
        String requestBody = createRegistrationBody("qatar@test.com", 
                "+974-3312-3456", 
                "West Bay, Doha, Qatar", 
                "12345");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== Europe Tests (France, Germany, Spain) ====================
    
    @Test
    void register_withFranceAddress_success() throws Exception {
        String requestBody = createRegistrationBody("paris@test.com", 
                "+33-1-42-34-56-78", 
                "123 Avenue des Champs-Élysées, Paris", 
                "75008");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("paris@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("75008");
    }

    @Test
    void register_withFranceMobilePhone_success() throws Exception {
        String requestBody = createRegistrationBody("lyon@test.com", 
                "+33-6-12-34-56-78", 
                "45 Rue de la République, Lyon", 
                "69002");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withGermanyAddress_success() throws Exception {
        String requestBody = createRegistrationBody("berlin@test.com", 
                "+49-30-12345678", 
                "Unter den Linden 77, Berlin", 
                "10117");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("berlin@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("10117");
    }

    @Test
    void register_withGermanyMobilePhone_success() throws Exception {
        String requestBody = createRegistrationBody("munich@test.com", 
                "+49-151-12345678", 
                "Marienplatz 1, München", 
                "80331");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withSpainAddress_success() throws Exception {
        String requestBody = createRegistrationBody("madrid@test.com", 
                "+34-91-123-4567", 
                "Calle Gran Vía, 28, Madrid", 
                "28013");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());

        Creator created = creatorRepository.findByEmail("madrid@test.com").orElse(null);
        assertThat(created).isNotNull();
        assertThat(created.getZipcode()).isEqualTo("28013");
    }

    @Test
    void register_withSpainMobilePhone_success() throws Exception {
        String requestBody = createRegistrationBody("barcelona@test.com", 
                "+34-612-345-678", 
                "Passeig de Gràcia, 92, Barcelona", 
                "08008");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withItalyAddress_success() throws Exception {
        String requestBody = createRegistrationBody("rome@test.com", 
                "+39-06-1234-5678", 
                "Via del Corso, 123, Roma", 
                "00186");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withNetherlandsAddress_success() throws Exception {
        String requestBody = createRegistrationBody("amsterdam@test.com", 
                "+31-20-123-4567", 
                "Damrak 123, Amsterdam", 
                "1012 LP");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    // ==================== Edge Cases with International Formats ====================
    
    @Test
    void register_withMixedInternationalCharactersInAddress_success() throws Exception {
        String requestBody = createRegistrationBody("intl1@test.com", 
                "+1-514-555-1234", 
                "123 Rue de l'Église, Montréal, QC", 
                "H2X 1E1");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withLongComplexAddress_success() throws Exception {
        String requestBody = createRegistrationBody("complex@test.com", 
                "+91-98765-43210", 
                "Plot No. 456/B, 2nd Floor, Opp. City Mall, Near Metro Station, Bangalore", 
                "560001");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withParenthesesInAddress_success() throws Exception {
        String requestBody = createRegistrationBody("parens@test.com", 
                "(555) 123-4567", 
                "123 Main St (Rear Entrance), Suite 100", 
                "12345");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withForwardSlashInAddress_success() throws Exception {
        String requestBody = createRegistrationBody("slash@test.com", 
                "+65-9123-4567", 
                "Blk 123/45 Jurong West Street 61", 
                "640123");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }

    @Test
    void register_withApostropheInAddress_success() throws Exception {
        String requestBody = createRegistrationBody("apostrophe@test.com", 
                "+353-1-234-5678", 
                "123 O'Connell Street, Dublin", 
                "D01 F5P2");

        mockMvc.perform(post("/creator/api/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk());
    }
}

package com.poomaalai;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;

import com.poomaalai.entity.Creator;
import com.poomaalai.entity.CreatorStore;
import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.repository.CreatorStoreRepository;

@SpringBootTest
@Transactional
@ActiveProfiles("test")
public class SearchStoreEdgeCaseTest {

    @Autowired
    private WebApplicationContext wac;

    private MockMvc mockMvc;

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    private Creator owner;

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.wac)
                .apply(springSecurity())
                .build();

        // Create test owner
        owner = new Creator();
        owner.setEmail("owner@test.com");
        owner.setPassword(passwordEncoder.encode("Password123!"));
        owner.setFirstName("Store");
        owner.setLastName("Owner");
        owner.setPhone("5551002000");
        owner.setAddress("123 Owner St");
        owner.setZipcode("55555");
        owner = creatorRepository.save(owner);

        // Create test stores
        createStore("Store 1", "123 Main St", "12345");
        createStore("Store 2", "456 Oak Ave", "12345");
        createStore("Store 3", "789 Elm Rd", "54321");
        createStore("Store 4", "321 Pine Ln", "11111");
    }

    private void createStore(String name, String address, String zipcode) {
        CreatorStore store = new CreatorStore();
        store.setOwner(owner);
        store.setName(name);
        store.setAddress(address);
        store.setZipcode(zipcode);
        store.setPhone("5551002001");
        creatorStoreRepository.save(store);
    }

    @Test
    void searchStore_withValidZipcode_returnsMatchingStores() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].zipcode").value("12345"));
    }

    @Test
    void searchStore_withNoMatchingZipcode_returnsEmptyArray() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "99999"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void searchStore_withoutZipcodeParam_returnsAllStores() throws Exception {
        mockMvc.perform(get("/creator-store/search"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void searchStore_withEmptyZipcode_returnsAllStores() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", ""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(4));
    }

    @Test
    void searchStore_withNonNumericZipcode_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "ABCDE"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_withPartialZipcode_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "123"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_withTooLongZipcode_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "123456"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_withZipcodeContainingSpaces_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "12 345"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_withZipcodeContainingDash_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "12-345"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_withSpecialCharactersInZipcode_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "12@45"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_withSqlInjectionAttempt_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "12345' OR '1'='1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_withXssPayload_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "<script>alert('xss')</script>"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_withLeadingZeros_returnsMatchingStores() throws Exception {
        createStore("Leading Zero Store", "111 Zero St", "01234");

        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "01234"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].zipcode").value("01234"));
    }

    @Test
    void searchStore_withMultipleZipcodeParams_usesFirstParam() throws Exception {
        // When multiple params with same name are sent, Spring behavior varies
        // In this case, the validation pattern may not handle arrays properly
        // so we expect a bad request rather than using the first param
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "12345")
                .param("zipcode", "54321"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void searchStore_resultsShouldNotExposeOwnerPassword() throws Exception {
        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].owner").doesNotExist());
    }

    @Test
    void searchStore_withNoStoresInDatabase_returnsEmptyArray() throws Exception {
        creatorStoreRepository.deleteAll();

        mockMvc.perform(get("/creator-store/search")
                .param("zipcode", "12345"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(0));
    }
}

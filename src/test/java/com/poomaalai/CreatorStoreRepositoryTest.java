package com.poomaalai;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.poomaalai.entity.Creator;
import com.poomaalai.entity.CreatorStore;
import com.poomaalai.repository.CreatorRepository;
import com.poomaalai.repository.CreatorStoreRepository;

@SpringBootTest
@Transactional
public class CreatorStoreRepositoryTest {

    @Autowired
    private CreatorRepository creatorRepository;

    @Autowired
    private CreatorStoreRepository creatorStoreRepository;

    @BeforeEach
    void setUp() {
        creatorStoreRepository.deleteAll();
        creatorRepository.deleteAll();
    }

    @Test
    void findByOwnerEmail_shouldReturnStoresForEmail() {
        Creator c = new Creator();
        c.setEmail("repo@test.com");
        c.setFirstName("Repo");
        c.setLastName("Test");
        c.setPhone("1234567890");
        c.setAddress("123 Repo St");
        c.setPassword("password");
        creatorRepository.save(c);

        CreatorStore s1 = new CreatorStore();
        s1.setName("Store 1");
        s1.setZipcode("11111");
        s1.setOwner(c);
        creatorStoreRepository.save(s1);

        CreatorStore s2 = new CreatorStore();
        s2.setName("Store 2");
        s2.setZipcode("22222");
        s2.setOwner(c);
        creatorStoreRepository.save(s2);

        List<CreatorStore> results = creatorStoreRepository.findByOwnerEmail("repo@test.com");
        assertThat(results).isNotNull();
        assertThat(results.size()).isEqualTo(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("Store 1", "Store 2");
    }
}

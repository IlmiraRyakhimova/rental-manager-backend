package com.rental.logging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class SensitiveDataMaskerTest {
    private final SensitiveDataMasker masker = new SensitiveDataMasker(new ObjectMapper(), Set.of("password", "token"));

    @Test
    void masksTopLevelSensitiveField() {
        Map<String, Object> dto = Map.of("email", "a@b.com", "password", "secret");
        String result = masker.mask(dto);

        assertThat(result)
                .contains("\"password\":\"****\"")
                .doesNotContain("secret");
    }

    @Test
    void masksFieldsContainingKeyword() {
        Map<String, Object> dto = Map.of("oldPassword", "oldSecret", "newPassword", "newSecret", "accessToken", "tokenSecret");
        String result = masker.mask(dto);
        assertThat(result)
                .contains("\"oldPassword\":\"****\"",
                          "\"newPassword\":\"****\"",
                          "\"accessToken\":\"****\"")
                .doesNotContain("oldSecret", "newSecret", "tokenSecret");
    }

    @Test
    void masksCaseInsensitively() {
        Map<String, Object> dto = Map.of("PASSWORD", "secret");
        String result = masker.mask(dto);

        assertThat(result)
                .contains("\"PASSWORD\":\"****\"")
                .doesNotContain("secret");
    }

    @Test
    void keepsNonSensitiveFields() {
        Map<String, Object> dto = Map.of("email", "a@b.com", "name", "Ilmira");
        String result = masker.mask(dto);

        assertThat(result)
                .contains("\"email\":\"a@b.com\"", "\"name\":\"Ilmira\"");
    }

    @Test
    void returnsNullStringForNull() {
        String result = masker.mask(null);

        assertThat(result)
                .isEqualTo("null");
    }


    @Test
    void doesNotFailOnNonSerializableObject() {
        Object weird = new Object();

        assertThatCode(() -> masker.mask(weird)).doesNotThrowAnyException();
        assertThat(masker.mask(weird)).contains("Object");
    }

    @Test
    void masksNestedObjectsAndArrays() {
        Map<String, Object> dto = Map.of(
                "owner", Map.of("name", "Anna", "password", "nestedSecret"),
                "guests", List.of(Map.of("name", "Ivan", "token", "arraySecret")));
        String result = masker.mask(dto);

        assertThat(result)
                .contains("\"name\":\"Anna\"",
                          "\"password\":\"****\"",
                          "\"name\":\"Ivan\"",
                          "\"token\":\"****\"")
                .doesNotContain("nestedSecret", "arraySecret");
    }





}

package com.rental.logging;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import lombok.RequiredArgsConstructor;

import java.util.Map;
import java.util.Set;

@RequiredArgsConstructor
public class SensitiveDataMasker {
    private final ObjectMapper objectMapper;
    private final Set<String> maskedKeywords;

    public String mask(Object value) {
        if (value == null) {
            return "null";
        }

        try {
            JsonNode tree = objectMapper.valueToTree(value);
            maskNode(tree);
            return tree.toString();
        } catch (RuntimeException e) {
            return "<" + value.getClass().getSimpleName() + ">";
        }
    }

    private void maskNode(JsonNode node) {
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> field : node.properties()) {
                String name = field.getKey();
                JsonNode value = field.getValue();
                if (isSensitive(name)) {
                    field.setValue(TextNode.valueOf("****"));
                } else {
                    maskNode(value);
                }
            }
        } else if (node.isArray()) {
            for (JsonNode element : node) {
                maskNode(element);
            }
        }
    }

    private boolean isSensitive(String fieldName) {
        return maskedKeywords.stream()
                .anyMatch(keyword -> fieldName.toLowerCase().contains(keyword.toLowerCase()));
    }
    }


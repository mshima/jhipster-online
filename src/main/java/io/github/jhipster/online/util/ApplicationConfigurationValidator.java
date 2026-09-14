/**
 * Copyright 2017-2024 the original author or authors from the JHipster project.
 *
 * This file is part of the JHipster Online project, see https://github.com/jhipster/jhipster-online
 * for more information.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.jhipster.online.util;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Iterator;
import java.util.Map;

/**
 * Validates a user-submitted {@code .yo-rc.json} before it is handed to generator-jhipster.
 *
 * <p>The submitted configuration is generator input, not inert data: almost every property can become
 * part of the generated application, {@code blueprints}/{@code generators} entries are npm packages that
 * the generator installs and executes, and path-like values can make the generator write outside its
 * working directory. JHipster Online only ever needs the fixed set of options offered by its UI, so any
 * configuration that would enable code execution or escape the working directory is rejected here rather
 * than forwarded verbatim.
 */
public final class ApplicationConfigurationValidator {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private ApplicationConfigurationValidator() {
        throw new IllegalStateException("Utility class: ApplicationConfigurationValidator");
    }

    /**
     * Validates the raw application configuration.
     *
     * @param applicationConfiguration the JSON that would be written as {@code .yo-rc.json}
     * @throws IllegalArgumentException if the configuration is not valid JSON, declares blueprints or
     *                                  generators, or contains a path-traversal value
     */
    public static void validate(String applicationConfiguration) {
        JsonNode root;
        try {
            root = MAPPER.readTree(applicationConfiguration);
        } catch (Exception e) {
            throw new IllegalArgumentException("Application configuration is not valid JSON");
        }
        if (root == null || root.isNull()) {
            throw new IllegalArgumentException("Application configuration is empty");
        }
        validateNode(root);
    }

    private static void validateNode(JsonNode node) {
        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> entry = fields.next();
                String key = entry.getKey();
                JsonNode value = entry.getValue();
                if (("blueprints".equals(key) || "generators".equals(key)) && !isEmpty(value)) {
                    // Blueprints and generators are npm packages executed during generation: never allow them
                    // to be selected through user-submitted configuration.
                    throw new IllegalArgumentException("Blueprints are not allowed");
                }
                validateNode(value);
            }
        } else if (node.isArray()) {
            for (JsonNode child : node) {
                validateNode(child);
            }
        } else if (node.isTextual()) {
            validateValue(node.textValue());
        }
    }

    private static boolean isEmpty(JsonNode value) {
        return (
            value == null || value.isNull() || (value.isArray() && value.isEmpty()) || (value.isTextual() && value.textValue().isBlank())
        );
    }

    private static void validateValue(String value) {
        if (value == null) {
            return;
        }
        if (value.indexOf('\0') >= 0) {
            throw new IllegalArgumentException("Application configuration contains an invalid character");
        }
        // Normalize Windows separators so a single check catches both `../` and `..\`.
        String normalized = value.replace('\\', '/');
        if (normalized.equals("..") || normalized.startsWith("../") || normalized.endsWith("/..") || normalized.contains("/../")) {
            throw new IllegalArgumentException("Path traversal is not allowed in application configuration");
        }
    }
}

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

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class ApplicationConfigurationValidatorTest {

    @Test
    void acceptsARegularConfiguration() {
        assertThatCode(
                () ->
                    ApplicationConfigurationValidator.validate(
                        "{\"generator-jhipster\":{\"baseName\":\"jhipster\",\"packageName\":\"com.mycompany.myapp\",\"blueprints\":[]}}"
                    )
            )
            .doesNotThrowAnyException();
    }

    @Test
    void rejectsBlueprints() {
        assertThatIllegalArgumentException()
            .isThrownBy(
                () ->
                    ApplicationConfigurationValidator.validate(
                        "{\"generator-jhipster\":{\"blueprints\":[{\"name\":\"generator-jhipster-evil\"}]}}"
                    )
            );
    }

    @Test
    void rejectsGenerators() {
        assertThatIllegalArgumentException()
            .isThrownBy(() -> ApplicationConfigurationValidator.validate("{\"generators\":{\"foo\":\"generator-jhipster-evil\"}}"));
    }

    @Test
    void rejectsPathTraversalValues() {
        assertThatIllegalArgumentException()
            .isThrownBy(
                () ->
                    ApplicationConfigurationValidator.validate(
                        "{\"generator-jhipster\":{\"clientRootDir\":\"../../../../home/jhipster/.ssh/\"}}"
                    )
            );
    }

    @Test
    void rejectsWindowsPathTraversalValues() {
        assertThatIllegalArgumentException()
            .isThrownBy(
                () -> ApplicationConfigurationValidator.validate("{\"generator-jhipster\":{\"clientRootDir\":\"..\\\\..\\\\evil\"}}")
            );
    }

    @Test
    void doesNotRejectDotsInsideValues() {
        assertThatCode(() -> ApplicationConfigurationValidator.validate("{\"generator-jhipster\":{\"packageName\":\"com.my..app\"}}"))
            .doesNotThrowAnyException();
    }

    @Test
    void rejectsInvalidJson() {
        assertThatIllegalArgumentException().isThrownBy(() -> ApplicationConfigurationValidator.validate("not json"));
    }
}

/*
 * Copyright 2002-2011 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.core.env;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Exception thrown when required properties are not found.
 *
 * 丢失必要的配置异常
 * 
 * @author Chris Beams
 * @since 3.1
 * @see ConfigurablePropertyResolver#setRequiredProperties(String...)
 * @see ConfigurablePropertyResolver#validateRequiredProperties()
 * @see org.springframework.context.support.AbstractApplicationContext#prepareRefresh()
 */
@SuppressWarnings("serial")
public class MissingRequiredPropertiesException extends IllegalStateException {

    /**
     * 丢失的所有的配置项
     */
    private final Set<String> missingRequiredProperties = new LinkedHashSet<String>();

    /**
     * Return the set of properties marked as required but not present
     * upon validation.
     *
     * 获取丢失的配置项
     *
     * @see ConfigurablePropertyResolver#setRequiredProperties(String...)
     * @see ConfigurablePropertyResolver#validateRequiredProperties()
     */
    public Set<String> getMissingRequiredProperties() {
        return missingRequiredProperties;
    }

    /**
     * 添加一个丢失的配置项
     *
     * @param key
     */
    void addMissingRequiredProperty(String key) {
        missingRequiredProperties.add(key);
    }

    /**
     * 获取具体的信息
     *
     * @return
     */
    @Override
    public String getMessage() {
        return String.format(
                "The following properties were declared as required but could " +
                "not be resolved: %s", this.getMissingRequiredProperties());
    }
}

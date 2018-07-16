/*
 * Copyright 2002-2013 the original author or authors.
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

import java.util.Map;
import java.util.Properties;

/**
 * {@link PropertySource} implementation that extracts properties from a
 * {@link java.util.Properties} object.
 *
 * <p>Note that because a {@code Properties} object is technically an
 * {@code <Object, Object>} {@link java.util.Hashtable Hashtable}, one may contain
 * non-{@code String} keys or values. This implementation, however is restricted to
 * accessing only {@code String}-based keys and values, in the same fashion as
 * {@link Properties#getProperty} and {@link Properties#setProperty}.
 *
 * 从Properties文件中提取属性源
 * 其实从它的子类来看，它不仅可以用xx.properties中提取，也可以用xx.xml文件中提取
 *
 * @author Chris Beams
 * @since 3.1
 */
public class PropertiesPropertySource extends MapPropertySource {

    /**
     * 通过名称和Properties文件来构造
     *
     * @param name
     * @param source
     */
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public PropertiesPropertySource(String name, Properties source) {
        super(name, (Map) source);
    }

}
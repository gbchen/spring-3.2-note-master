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

/**
 * Holder containing one or more {@link PropertySource} objects.
 *
 * 保存一个或者多个PropertySource
 * 
 * @author Chris Beams
 * @since 3.1
 */
public interface PropertySources extends Iterable<PropertySource<?>> {

    /**
     * Return whether a property source with the given name is contained.
     *
     * 判断是否包含给定的属性源
     *
     * @param name the {@linkplain PropertySource#getName() name of the property source} to find
     */
    boolean contains(String name);

    /**
     * Return the property source with the given name, {@code null} if not found.
     *
     * 返回特定的属性源
     * 如果未发现，则返回null
     *
     * @param name the {@linkplain PropertySource#getName() name of the property source} to find
     */
    PropertySource<?> get(String name);

}

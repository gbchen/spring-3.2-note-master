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

/**
 * Interface for resolving properties against any underlying source.
 *
 * 解析Property文件
 * 这里的核心功能如下:
 * 1.获取特定的属性值
 * 2.是否是必须的，即不存在直接抛受检异常
 * 3.解析为特定的类型
 * 4.用占位符进行文本替代
 * 
 * @author Chris Beams
 * @since 3.1
 * @see Environment
 * @see PropertySourcesPropertyResolver
 */
public interface PropertyResolver {

    /**
     * Return whether the given property key is available for resolution, i.e.,
     * the value for the given key is not {@code null}.
     *
     * 判断是否包含某个属性
     *
     */
    boolean containsProperty(String key);

    /**
     * Return the property value associated with the given key, or {@code null}
     * if the key cannot be resolved.
     *
     * 获取某个属性的值
     *
     * @param key the property name to resolve
     * @see #getProperty(String, String)
     * @see #getProperty(String, Class)
     * @see #getRequiredProperty(String)
     */
    String getProperty(String key);

    /**
     * Return the property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     *
     * 获取某个属性的值
     * 如果某个属性不能被设置，那么就用默认值代替
     *
     * @param key the property name to resolve
     * @param defaultValue the default value to return if no value is found
     * @see #getRequiredProperty(String)
     * @see #getProperty(String, Class)
     */
    String getProperty(String key, String defaultValue);

    /**
     * Return the property value associated with the given key, or {@code null}
     * if the key cannot be resolved.
     *
     * 获取某个属性的值
     * 并且转换为指定的类型
     *
     * @param key the property name to resolve
     * @param targetType the expected type of the property value
     * @see #getRequiredProperty(String, Class)
     */
    <T> T getProperty(String key, Class<T> targetType);

    /**
     * Return the property value associated with the given key, or
     * {@code defaultValue} if the key cannot be resolved.
     *
     * 获取某个属性的值
     * 并且转换为为指定的类型
     * 并且指定默认值
     *
     * @param key the property name to resolve
     * @param targetType the expected type of the property value
     * @param defaultValue the default value to return if no value is found
     * @see #getRequiredProperty(String, Class)
     */
    <T> T getProperty(String key, Class<T> targetType, T defaultValue);

    /**
     * Convert the property value associated with the given key to a {@code Class}
     * of type {@code T} or {@code null} if the key cannot be resolved.
     *
     *
     * @throws org.springframework.core.convert.ConversionException if class specified
     * by property value cannot be found  or loaded or if targetType is not assignable
     * from class specified by property value
     * @see #getProperty(String, Class)
     */
    <T> Class<T> getPropertyAsClass(String key, Class<T> targetType);

    /**
     * Return the property value associated with the given key, converted to the given
     * targetType (never {@code null}).
     *
     * 获取必须的参数
     * 绝对不会返回空
     *
     * @throws IllegalStateException if the key cannot be resolved
     * @see #getRequiredProperty(String, Class)
     */
    String getRequiredProperty(String key) throws IllegalStateException;

    /**
     * Return the property value associated with the given key, converted to the given
     * targetType (never {@code null}).
     *
     * 获取必须的参数
     * 绝对不会返回值
     * 并且转换为特定的类型
     *
     * @throws IllegalStateException if the given key cannot be resolved
     */
    <T> T getRequiredProperty(String key, Class<T> targetType) throws IllegalStateException;

    /**
     * Resolve ${...} placeholders in the given text, replacing them with corresponding
     * property values as resolved by {@link #getProperty}. Unresolvable placeholders with
     * no default value are ignored and passed through unchanged.
     *
     * 解析占位符
     * 它会把 ${...} 用配置的值代替
     * 未解析的占位符会直接被忽略
     *
     * @param text the String to resolve
     * @return the resolved String (never {@code null})
     * @throws IllegalArgumentException if given text is {@code null}
     * @see #resolveRequiredPlaceholders
     * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders(String)
     */
    String resolvePlaceholders(String text);

    /**
     * Resolve ${...} placeholders in the given text, replacing them with corresponding
     * property values as resolved by {@link #getProperty}. Unresolvable placeholders with
     * no default value will cause an IllegalArgumentException to be thrown.
     *
     * 解析必须的占位符
     * 它会把 ${...} 用配置的值代替
     * 未解析的占位符会抛出IllegalArgumentException异常
     *
     * @return the resolved String (never {@code null})
     * @throws IllegalArgumentException if given text is {@code null}
     * or if any placeholders are unresolvable
     * @see org.springframework.util.SystemPropertyUtils#resolvePlaceholders(String, boolean)
     */
    String resolveRequiredPlaceholders(String text) throws IllegalArgumentException;

}

/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.core.style;

/**
 * A strategy interface for pretty-printing {@code toString()} methods.
 * Encapsulates the print algorithms; some other object such as a builder
 * should provide the workflow.
 *
 * 为了更美观的输出的格式化接口
 * 
 * @author Keith Donald
 * @since 1.2.2
 */
public interface ToStringStyler {

    /**
     * Style a {@code toString()}'ed object before its fields are styled.
     *
     * 格式化开始
     *
     * @param buffer the buffer to print to
     * @param obj the object to style
     */
    void styleStart(StringBuilder buffer, Object obj);

    /**
     * Style a {@code toString()}'ed object after it's fields are styled.
     *
     * 格式化结束
     *
     * @param buffer the buffer to print to
     * @param obj the object to style
     */
    void styleEnd(StringBuilder buffer, Object obj);

    /**
     * Style a field value as a string.
     *
     * 格式化属性
     *
     * @param buffer the buffer to print to
     * @param fieldName the he name of the field
     * @param value the field value
     */
    void styleField(StringBuilder buffer, String fieldName, Object value);

    /**
     * Style the given value.
     *
     * 格式化给定的值
     *
     * @param buffer the buffer to print to
     * @param value the field value
     */
    void styleValue(StringBuilder buffer, Object value);

    /**
     * Style the field separator.
     *
     * 格式化属性分隔符
     *
     * @param buffer buffer to print to
     */
    void styleFieldSeparator(StringBuilder buffer);

}

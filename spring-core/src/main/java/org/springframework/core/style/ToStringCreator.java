/*
 * Copyright 2002-2014 the original author or authors.
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

import org.springframework.util.Assert;

/**
 * Utility class that builds pretty-printing {@code toString()} methods
 * with pluggable styling conventions. By default, ToStringCreator adheres
 * to Spring's {@code toString()} styling conventions.
 *
 * toString方法可以使用它来美化输出
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.2.2
 */
public class ToStringCreator {

    /**
     * Default ToStringStyler instance used by this ToStringCreator.
     * 默认的格式化类
     */
    private static final ToStringStyler DEFAULT_TO_STRING_STYLER =
            new DefaultToStringStyler(StylerUtils.DEFAULT_VALUE_STYLER);


    /**
     * 字符串拼接类
     */
    private final StringBuilder buffer = new StringBuilder(256);

    /**
     * 格式化类
     */
    private final ToStringStyler styler;

    /**
     * 要格式化的对象
     */
    private final Object object;

    /**
     * 当前是否是第一个属性
     */
    private boolean styledFirstField;


    /**
     * Create a ToStringCreator for the given object.
     *
     * 对给定的对象使用ToStringCreator输出
     *
     * @param obj the object to be stringified
     */
    public ToStringCreator(Object obj) {
        this(obj, (ToStringStyler) null);
    }

    /**
     * Create a ToStringCreator for the given object, using the provided style.
     *
     * 使用默认的值格式化工具
     *
     * @param obj the object to be stringified
     * @param styler the ValueStyler encapsulating pretty-print instructions
     */
    public ToStringCreator(Object obj, ValueStyler styler) {
        this(obj, new DefaultToStringStyler(styler != null ? styler : StylerUtils.DEFAULT_VALUE_STYLER));
    }

    /**
     * Create a ToStringCreator for the given object, using the provided style.
     *
     * 根据对象和格式化工具来执行构造方法
     *
     * @param obj the object to be stringified
     * @param styler the ToStringStyler encapsulating pretty-print instructions
     */
    public ToStringCreator(Object obj, ToStringStyler styler) {
        Assert.notNull(obj, "The object to be styled must not be null");
        this.object = obj;
        this.styler = (styler != null ? styler : DEFAULT_TO_STRING_STYLER);
        this.styler.styleStart(this.buffer, this.object);
    }


    /**
     * Append a byte field value.
     *
     * 追加一个byte类型的值
     *
     * @param fieldName the name of the field, usually the member variable name
     * @param value the field value
     * @return this, to support call-chaining
     */
    public ToStringCreator append(String fieldName, byte value) {
        return append(fieldName, new Byte(value));
    }

    /**
     * Append a short field value.
     *
     * 追加一个short类型的值
     *
     * @param fieldName the name of the field, usually the member variable name
     * @param value the field value
     * @return this, to support call-chaining
     */
    public ToStringCreator append(String fieldName, short value) {
        return append(fieldName, new Short(value));
    }

    /**
     * Append a integer field value.
     *
     * 追加一个整型值
     *
     * @param fieldName the name of the field, usually the member variable name
     * @param value the field value
     * @return this, to support call-chaining
     */
    public ToStringCreator append(String fieldName, int value) {
        return append(fieldName, new Integer(value));
    }

    /**
     * Append a long field value.
     *
     * 追加一个长整型值
     *
     * @param fieldName the name of the field, usually the member variable name
     * @param value the field value
     * @return this, to support call-chaining
     */
    public ToStringCreator append(String fieldName, long value) {
        return append(fieldName, new Long(value));
    }

    /**
     * Append a float field value.
     *
     * 追加一个单精度浮点型值
     *
     * @param fieldName the name of the field, usually the member variable name
     * @param value the field value
     * @return this, to support call-chaining
     */
    public ToStringCreator append(String fieldName, float value) {
        return append(fieldName, new Float(value));
    }

    /**
     * Append a double field value.
     *
     * 追加一个浮点型值
     *
     * @param fieldName the name of the field, usually the member variable name
     * @param value the field value
     * @return this, to support call-chaining
     */
    public ToStringCreator append(String fieldName, double value) {
        return append(fieldName, new Double(value));
    }

    /**
     * Append a boolean field value.
     *
     * 追加一个布尔值
     *
     * @param fieldName the name of the field, usually the member variable name
     * @param value the field value
     * @return this, to support call-chaining
     */
    public ToStringCreator append(String fieldName, boolean value) {
        return append(fieldName, Boolean.valueOf(value));
    }

    /**
     * Append a field value.
     *
     * 添加一个属性值
     *
     * @param fieldName the name of the field, usually the member variable name
     * @param value the field value
     * @return this, to support call-chaining
     */
    public ToStringCreator append(String fieldName, Object value) {
        printFieldSeparatorIfNecessary();
        this.styler.styleField(this.buffer, fieldName, value);
        return this;
    }

    /**
     * 是否打印属性分隔符
     */
    private void printFieldSeparatorIfNecessary() {
        if (this.styledFirstField) {
            this.styler.styleFieldSeparator(this.buffer);
        } else {
            this.styledFirstField = true;
        }
    }

    /**
     * Append the provided value.
     *
     * 追加一个值
     *
     * @param value The value to append
     * @return this, to support call-chaining.
     */
    public ToStringCreator append(Object value) {
        this.styler.styleValue(this.buffer, value);
        return this;
    }


    /**
     * Return the String representation that this ToStringCreator built.
     *
     * 返回对应的字符串内容
     */
    @Override
    public String toString() {
        this.styler.styleEnd(this.buffer, this.object);
        return this.buffer.toString();
    }

}

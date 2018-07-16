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

import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Spring's default {@code toString()} styler.
 *
 * <p>This class is used by {@link ToStringCreator} to style {@code toString()}
 * output in a consistent manner according to Spring conventions.
 *
 * Spring的默认的格式化类
 * 它被ToStringCreator来格式化
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.2.2
 */
public class DefaultToStringStyler implements ToStringStyler {

    /**
     * 值格式化对象
     */
	private final ValueStyler valueStyler;


	/**
	 * Create a new DefaultToStringStyler.
     *
     * 构造方法
     *
	 * @param valueStyler the ValueStyler to use
	 */
	public DefaultToStringStyler(ValueStyler valueStyler) {
		Assert.notNull(valueStyler, "ValueStyler must not be null");
		this.valueStyler = valueStyler;
	}

	/**
	 * Return the ValueStyler used by this ToStringStyler.
     *
     * 返回值格式化工具
     *
	 */
	protected final ValueStyler getValueStyler() {
		return this.valueStyler;
	}


    /**
     * 开始格式化
     *
     * @param buffer the buffer to print to
     * @param obj the object to style
     */
	public void styleStart(StringBuilder buffer, Object obj) {
		if (!obj.getClass().isArray()) {
			buffer.append('[').append(ClassUtils.getShortName(obj.getClass()));
			styleIdentityHashCode(buffer, obj);
		} else {
			buffer.append('[');
			styleIdentityHashCode(buffer, obj);
			buffer.append(' ');
			styleValue(buffer, obj);
		}
	}

    /**
     * 获取其哈希值
     *
     * @param buffer
     * @param obj
     */
	private void styleIdentityHashCode(StringBuilder buffer, Object obj) {
		buffer.append('@');
		buffer.append(ObjectUtils.getIdentityHexString(obj));
	}

    /**
     * 格式化结束
     *
     * @param buffer the buffer to print to
     * @param o
     */
	public void styleEnd(StringBuilder buffer, Object o) {
		buffer.append(']');
	}

    /**
     * 格式化字段
     *
     * @param buffer the buffer to print to
     * @param fieldName the he name of the field
     * @param value the field value
     */
	public void styleField(StringBuilder buffer, String fieldName, Object value) {
		styleFieldStart(buffer, fieldName);
		styleValue(buffer, value);
		styleFieldEnd(buffer, fieldName);
	}

    /**
     * 格式化字段开始
     *
     * @param buffer
     * @param fieldName
     */
	protected void styleFieldStart(StringBuilder buffer, String fieldName) {
		buffer.append(' ').append(fieldName).append(" = ");
	}

    /**
     * 格式化字段结束
     *
     * @param buffer
     * @param fieldName
     */
	protected void styleFieldEnd(StringBuilder buffer, String fieldName) {
	}


    /**
     * 格式化值
     *
     * @param buffer the buffer to print to
     * @param value the field value
     */
	public void styleValue(StringBuilder buffer, Object value) {
		buffer.append(this.valueStyler.style(value));
	}

    /**
     * 格式化字段分隔符
     *
     * @param buffer buffer to print to
     */
	public void styleFieldSeparator(StringBuilder buffer) {
		buffer.append(',');
	}

}

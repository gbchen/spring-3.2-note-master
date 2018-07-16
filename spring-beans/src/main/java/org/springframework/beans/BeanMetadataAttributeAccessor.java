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

package org.springframework.beans;

import org.springframework.core.AttributeAccessorSupport;

/**
 * Extension of {@link org.springframework.core.AttributeAccessorSupport},
 * holding attributes as {@link BeanMetadataAttribute} objects in order
 * to keep track of the definition source.
 *
 * Bean元信息属性访问器
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
@SuppressWarnings("serial")
public class BeanMetadataAttributeAccessor extends AttributeAccessorSupport implements BeanMetadataElement {

    /**
     * 源
     */
    private Object source;


    /**
     * Set the configuration source {@code Object} for this metadata element.
     * <p>The exact type of the object will depend on the configuration mechanism used.
     *
     * 设置源
     */
    public void setSource(Object source) {
        this.source = source;
    }

    /**
     * 获取源
     *
     * @return
     */
    public Object getSource() {
        return this.source;
    }


    /**
     * Add the given BeanMetadataAttribute to this accessor's set of attributes.
     *
     * 添加元信息
     *
     * @param attribute the BeanMetadataAttribute object to register
     */
    public void addMetadataAttribute(BeanMetadataAttribute attribute) {
        super.setAttribute(attribute.getName(), attribute);
    }

    /**
     * Look up the given BeanMetadataAttribute in this accessor's set of attributes.
     *
     * 查找给定的元信息
     *
     * @param name the name of the attribute
     * @return the corresponding BeanMetadataAttribute object,
     * or {@code null} if no such attribute defined
     */
    public BeanMetadataAttribute getMetadataAttribute(String name) {
        return (BeanMetadataAttribute) super.getAttribute(name);
    }

    /**
     * 设置属性
     *
     * @param name the unique attribute key
     * @param value the attribute value to be attached
     */
    @Override
    public void setAttribute(String name, Object value) {
        super.setAttribute(name, new BeanMetadataAttribute(name, value));
    }

    /**
     * 获取属性
     *
     * @param name the unique attribute key
     * @return
     */
    @Override
    public Object getAttribute(String name) {
        BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.getAttribute(name);
        return (attribute != null ? attribute.getValue() : null);
    }

    /**
     * 移除属性
     *
     * @param name the unique attribute key
     * @return
     */
    @Override
    public Object removeAttribute(String name) {
        BeanMetadataAttribute attribute = (BeanMetadataAttribute) super.removeAttribute(name);
        return (attribute != null ? attribute.getValue() : null);
    }

}

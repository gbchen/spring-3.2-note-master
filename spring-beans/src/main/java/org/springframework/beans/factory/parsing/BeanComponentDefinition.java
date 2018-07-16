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

package org.springframework.beans.factory.parsing;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.PropertyValue;
import org.springframework.beans.PropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.BeanReference;

/**
 * ComponentDefinition based on a standard BeanDefinition, exposing the given bean
 * definition as well as inner bean definitions and bean references for the given bean.
 *
 * Bean组件定义
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class BeanComponentDefinition extends BeanDefinitionHolder implements ComponentDefinition {

    /**
     * 内部的Bean定义
     */
    private BeanDefinition[] innerBeanDefinitions;

    /**
     * Bean引用
     */
    private BeanReference[] beanReferences;


    /**
     * Create a new BeanComponentDefinition for the given bean.
     *
     * 构造方法
     *
     * @param beanDefinition the BeanDefinition
     * @param beanName the name of the bean
     */
    public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName) {
        super(beanDefinition, beanName);
        findInnerBeanDefinitionsAndBeanReferences(beanDefinition);
    }

    /**
     * Create a new BeanComponentDefinition for the given bean.
     *
     * 构造方法
     *
     * @param beanDefinition the BeanDefinition
     * @param beanName the name of the bean
     * @param aliases alias names for the bean, or {@code null} if none
     */
    public BeanComponentDefinition(BeanDefinition beanDefinition, String beanName, String[] aliases) {
        super(beanDefinition, beanName, aliases);
        findInnerBeanDefinitionsAndBeanReferences(beanDefinition);
    }

    /**
     * Create a new BeanComponentDefinition for the given bean.
     *
     * 构造方法
     *
     * @param holder the BeanDefinitionHolder encapsulating the
     * bean definition as well as the name of the bean
     */
    public BeanComponentDefinition(BeanDefinitionHolder holder) {
        super(holder);
        findInnerBeanDefinitionsAndBeanReferences(holder.getBeanDefinition());
    }


    /**
     * 获取内部Bean和Bean引用
     *
     * @param beanDefinition
     */
    private void findInnerBeanDefinitionsAndBeanReferences(BeanDefinition beanDefinition) {
        List<BeanDefinition> innerBeans = new ArrayList<BeanDefinition>();
        List<BeanReference> references = new ArrayList<BeanReference>();
        PropertyValues propertyValues = beanDefinition.getPropertyValues();
        for (int i = 0; i < propertyValues.getPropertyValues().length; i++) {
            PropertyValue propertyValue = propertyValues.getPropertyValues()[i];
            Object value = propertyValue.getValue();
            if (value instanceof BeanDefinitionHolder) {
                // 添加内置Bean
                innerBeans.add(((BeanDefinitionHolder) value).getBeanDefinition());
            } else if (value instanceof BeanDefinition) {
                // 添加内置Bean
                innerBeans.add((BeanDefinition) value);
            } else if (value instanceof BeanReference) {
                // 添加Bean引用
                references.add((BeanReference) value);
            }
        }
        this.innerBeanDefinitions = innerBeans.toArray(new BeanDefinition[innerBeans.size()]);
        this.beanReferences = references.toArray(new BeanReference[references.size()]);
    }


    /**
     * 获取名称
     *
     * @return
     */
    public String getName() {
        return getBeanName();
    }

    /**
     * 获取描述
     *
     * @return
     */
    public String getDescription() {
        return getShortDescription();
    }

    /**
     * 获取所有的Bean定义
     *
     * @return
     */
    public BeanDefinition[] getBeanDefinitions() {
        return new BeanDefinition[] {getBeanDefinition()};
    }

    /**
     * 获取所有的内部Bean
     *
     * @return
     */
    public BeanDefinition[] getInnerBeanDefinitions() {
        return this.innerBeanDefinitions;
    }

    /**
     * 获取Bean引用
     *
     * @return
     */
    public BeanReference[] getBeanReferences() {
        return this.beanReferences;
    }


    /**
     * This implementation returns this ComponentDefinition's description.
     *
     * 转换为字符串
     *
     * @see #getDescription()
     */
    @Override
    public String toString() {
        return getDescription();
    }

    /**
     * This implementations expects the other object to be of type BeanComponentDefinition
     * as well, in addition to the superclass's equality requirements.
     *
     * 判断是否相等
     */
    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof BeanComponentDefinition && super.equals(other)));
    }

}

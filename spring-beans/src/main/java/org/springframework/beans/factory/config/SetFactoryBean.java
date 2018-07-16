/*
 * Copyright 2002-2008 the original author or authors.
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

package org.springframework.beans.factory.config;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.GenericCollectionTypeResolver;

/**
 * Simple factory for shared Set instances. Allows for central setup
 * of Sets via the "set" element in XML bean definitions.
 *
 * Set形式的工厂Bean
 * 它是在xml定义中的部分
 * 这里我们有源集合类和目标集合类，并且有可能需要元素类型的转换
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see ListFactoryBean
 * @see MapFactoryBean
 */
public class SetFactoryBean extends AbstractFactoryBean<Set> {

    /**
     * 源集合类
     */
    private Set sourceSet;

    /**
     * 目标集合类
     */
    private Class targetSetClass;


    /**
     * Set the source Set, typically populated via XML "set" elements.
     *
     * 设置源集合类，经典的就是xml中的set元素
     */
    public void setSourceSet(Set sourceSet) {
        this.sourceSet = sourceSet;
    }

    /**
     * Set the class to use for the target Set. Can be populated with a fully
     * qualified class name when defined in a Spring application context.
     * <p>Default is a linked HashSet, keeping the registration order.
     *
     * 设置目标集合类
     * 默认是LinkedHashSet
     * 它的顺序和注册的顺序保持一致
     *
     * @see java.util.LinkedHashSet
     */
    public void setTargetSetClass(Class targetSetClass) {
        if (targetSetClass == null) {
            throw new IllegalArgumentException("'targetSetClass' must not be null");
        }
        if (!Set.class.isAssignableFrom(targetSetClass)) {
            throw new IllegalArgumentException("'targetSetClass' must implement [java.util.Set]");
        }
        this.targetSetClass = targetSetClass;
    }


    /**
     * 获取目标类的类型
     *
     * @return
     */
    @Override
    public Class<Set> getObjectType() {
        return Set.class;
    }

    /**
     * 创建实例
     *
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Set createInstance() {
        // 源集合类不能为空
        if (this.sourceSet == null) {
            throw new IllegalArgumentException("'sourceSet' is required");
        }

        // 首先确定目标集合类
        Set result = null;
        if (this.targetSetClass != null) {
            result = (Set) BeanUtils.instantiateClass(this.targetSetClass);
        } else {
            result = new LinkedHashSet(this.sourceSet.size());
        }

        // 是否需要类型的转换
        Class valueType = null;
        if (this.targetSetClass != null) {
            valueType = GenericCollectionTypeResolver.getCollectionType(this.targetSetClass);
        }
        if (valueType != null) {
            TypeConverter converter = getBeanTypeConverter();
            for (Object elem : this.sourceSet) {
                result.add(converter.convertIfNecessary(elem, valueType));
            }
        } else {
            result.addAll(this.sourceSet);
        }
        return result;
    }

}

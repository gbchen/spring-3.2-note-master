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

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.TypeConverter;
import org.springframework.core.GenericCollectionTypeResolver;

/**
 * Simple factory for shared Map instances. Allows for central setup
 * of Maps via the "map" element in XML bean definitions.
 *
 * Map格式的工厂Bean
 * 它允许通过map元素在xml的Bean中进行定义
 *
 * @author Juergen Hoeller
 * @since 09.12.2003
 * @see SetFactoryBean
 * @see ListFactoryBean
 */
public class MapFactoryBean extends AbstractFactoryBean<Map> {

    /**
     * 源对象
     */
    private Map<?, ?> sourceMap;

    /**
     * 目标集合类
     * 必须是java.util.Map的子类
     */
    private Class targetMapClass;


    /**
     * Set the source Map, typically populated via XML "map" elements.
     *
     * 设置目标源
     * 经典的使用就是通过xml的map方式
     */
    public void setSourceMap(Map sourceMap) {
        this.sourceMap = sourceMap;
    }

    /**
     * Set the class to use for the target Map. Can be populated with a fully
     * qualified class name when defined in a Spring application context.
     * <p>Default is a linked HashMap, keeping the registration order.
     *
     * 设置目标类的映射
     *
     * @see java.util.LinkedHashMap
     */
    public void setTargetMapClass(Class targetMapClass) {
        if (targetMapClass == null) {
            throw new IllegalArgumentException("'targetMapClass' must not be null");
        }

        // 必须是java.util.Map的子类
        if (!Map.class.isAssignableFrom(targetMapClass)) {
            throw new IllegalArgumentException("'targetMapClass' must implement [java.util.Map]");
        }
        this.targetMapClass = targetMapClass;
    }


    /**
     * 获取目标类对象
     *
     * @return
     */
    @Override
    public Class<Map> getObjectType() {
        return Map.class;
    }

    /**
     * 创建实例
     *
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Map createInstance() {
        // 源map类型不能为空
        if (this.sourceMap == null) {
            throw new IllegalArgumentException("'sourceMap' is required");
        }

        // 创建目标map
        Map result = null;
        if (this.targetMapClass != null) {
            result = (Map) BeanUtils.instantiateClass(this.targetMapClass);
        } else {
            result = new LinkedHashMap(this.sourceMap.size());
        }

        // 是否需要k、v类型的转换
        Class keyType = null;
        Class valueType = null;
        if (this.targetMapClass != null) {
            keyType = GenericCollectionTypeResolver.getMapKeyType(this.targetMapClass);
            valueType = GenericCollectionTypeResolver.getMapValueType(this.targetMapClass);
        }
        if (keyType != null || valueType != null) {
            TypeConverter converter = getBeanTypeConverter();
            for (Map.Entry entry : this.sourceMap.entrySet()) {
                Object convertedKey = converter.convertIfNecessary(entry.getKey(), keyType);
                Object convertedValue = converter.convertIfNecessary(entry.getValue(), valueType);
                result.put(convertedKey, convertedValue);
            }
        } else {
            result.putAll(this.sourceMap);
        }
        return result;
    }

}

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

package org.springframework.beans.factory.config;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanMetadataElement;
import org.springframework.beans.Mergeable;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;

/**
 * Holder for constructor argument values, typically as part of a bean definition.
 *
 * <p>Supports values for a specific index in the constructor argument list
 * as well as for generic argument matches by type.
 *
 * 构造方法的参数值
 * 它也是一个复数形式
 * 它的单数形式是通过ValueHolder来表示的
 *
 * @author Juergen Hoeller
 * @since 09.11.2003
 * @see BeanDefinition#getConstructorArgumentValues
 */
public class ConstructorArgumentValues {

    /**
     * 索引的参数值
     */
    private final Map<Integer, ValueHolder> indexedArgumentValues = new LinkedHashMap<Integer, ValueHolder>(0);

    /**
     * 通用的参数值
     */
    private final List<ValueHolder> genericArgumentValues = new LinkedList<ValueHolder>();


    /**
     * Create a new empty ConstructorArgumentValues object.
     *
     * 构造方法
     */
    public ConstructorArgumentValues() {
    }

    /**
     * Deep copy constructor.
     *
     * 构造方法
     *
     * @param original the ConstructorArgumentValues to copy
     */
    public ConstructorArgumentValues(ConstructorArgumentValues original) {
        addArgumentValues(original);
    }


    /**
     * Copy all given argument values into this object, using separate holder
     * instances to keep the values independent from the original object.
     * <p>Note: Identical ValueHolder instances will only be registered once,
     * to allow for merging and re-merging of argument value definitions. Distinct
     * ValueHolder instances carrying the same content are of course allowed.
     *
     * 添加参数值
     */
    public void addArgumentValues(ConstructorArgumentValues other) {
        if (other != null) {
            for (Map.Entry<Integer, ValueHolder> entry : other.indexedArgumentValues.entrySet()) {
                addOrMergeIndexedArgumentValue(entry.getKey(), entry.getValue().copy());
            }
            for (ValueHolder valueHolder : other.genericArgumentValues) {
                if (!this.genericArgumentValues.contains(valueHolder)) {
                    addOrMergeGenericArgumentValue(valueHolder.copy());
                }
            }
        }
    }


    /**
     * Add an argument value for the given index in the constructor argument list.
     *
     * 添加索引的参数值
     *
     * @param index the index in the constructor argument list
     * @param value the argument value
     */
    public void addIndexedArgumentValue(int index, Object value) {
        addIndexedArgumentValue(index, new ValueHolder(value));
    }

    /**
     * Add an argument value for the given index in the constructor argument list.
     *
     * 添加索引的参数值
     *
     * @param index the index in the constructor argument list
     * @param value the argument value
     * @param type the type of the constructor argument
     */
    public void addIndexedArgumentValue(int index, Object value, String type) {
        addIndexedArgumentValue(index, new ValueHolder(value, type));
    }

    /**
     * Add an argument value for the given index in the constructor argument list.
     *
     * 添加索引的参数值
     *
     * @param index the index in the constructor argument list
     * @param newValue the argument value in the form of a ValueHolder
     */
    public void addIndexedArgumentValue(int index, ValueHolder newValue) {
        Assert.isTrue(index >= 0, "Index must not be negative");
        Assert.notNull(newValue, "ValueHolder must not be null");
        addOrMergeIndexedArgumentValue(index, newValue);
    }

    /**
     * Add an argument value for the given index in the constructor argument list,
     * merging the new value (typically a collection) with the current value
     * if demanded: see {@link org.springframework.beans.Mergeable}.
     *
     * 添加或者合并索引的参数值
     * 对于集合的话，可能需要合并
     *
     * @param key the index in the constructor argument list
     * @param newValue the argument value in the form of a ValueHolder
     */
    private void addOrMergeIndexedArgumentValue(Integer key, ValueHolder newValue) {
        ValueHolder currentValue = this.indexedArgumentValues.get(key);
        if (currentValue != null && newValue.getValue() instanceof Mergeable) {
            Mergeable mergeable = (Mergeable) newValue.getValue();
            if (mergeable.isMergeEnabled()) {
                newValue.setValue(mergeable.merge(currentValue.getValue()));
            }
        }
        this.indexedArgumentValues.put(key, newValue);
    }

    /**
     * Check whether an argument value has been registered for the given index.
     *
     * 判断指定位置是否有值
     *
     * @param index the index in the constructor argument list
     */
    public boolean hasIndexedArgumentValue(int index) {
        return this.indexedArgumentValues.containsKey(index);
    }

    /**
     * Get argument value for the given index in the constructor argument list.
     *
     * 获取指定位置的值
     *
     * @param index the index in the constructor argument list
     * @param requiredType the type to match (can be {@code null} to match
     * untyped values only)
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType) {
        return getIndexedArgumentValue(index, requiredType, null);
    }

    /**
     * Get argument value for the given index in the constructor argument list.
     *
     * 获取指定位置的值
     *
     * @param index the index in the constructor argument list
     * @param requiredType the type to match (can be {@code null} to match
     * untyped values only)
     * @param requiredName the type to match (can be {@code null} to match
     * unnamed values only)
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getIndexedArgumentValue(int index, Class<?> requiredType, String requiredName) {
        Assert.isTrue(index >= 0, "Index must not be negative");
        ValueHolder valueHolder = this.indexedArgumentValues.get(index);
        if (valueHolder != null &&
                (valueHolder.getType() == null ||
                        (requiredType != null && ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) &&
                (valueHolder.getName() == null ||
                        (requiredName != null && requiredName.equals(valueHolder.getName())))) {
            return valueHolder;
        }
        return null;
    }

    /**
     * Return the map of indexed argument values.
     *
     * 获取指定位置的值
     *
     * @return unmodifiable Map with Integer index as key and ValueHolder as value
     * @see ValueHolder
     */
    public Map<Integer, ValueHolder> getIndexedArgumentValues() {
        return Collections.unmodifiableMap(this.indexedArgumentValues);
    }


    /**
     * Add a generic argument value to be matched by type.
     * <p>Note: A single generic argument value will just be used once,
     * rather than matched multiple times.
     *
     * 添加通用的参数值
     *
     * @param value the argument value
     */
    public void addGenericArgumentValue(Object value) {
        this.genericArgumentValues.add(new ValueHolder(value));
    }

    /**
     * Add a generic argument value to be matched by type.
     * <p>Note: A single generic argument value will just be used once,
     * rather than matched multiple times.
     *
     * 添加通用的参数值
     *
     * @param value the argument value
     * @param type the type of the constructor argument
     */
    public void addGenericArgumentValue(Object value, String type) {
        this.genericArgumentValues.add(new ValueHolder(value, type));
    }

    /**
     * Add a generic argument value to be matched by type or name (if available).
     * <p>Note: A single generic argument value will just be used once,
     * rather than matched multiple times.
     *
     * 添加通用的参数值
     *
     * @param newValue the argument value in the form of a ValueHolder
     * <p>Note: Identical ValueHolder instances will only be registered once,
     * to allow for merging and re-merging of argument value definitions. Distinct
     * ValueHolder instances carrying the same content are of course allowed.
     */
    public void addGenericArgumentValue(ValueHolder newValue) {
        Assert.notNull(newValue, "ValueHolder must not be null");
        if (!this.genericArgumentValues.contains(newValue)) {
            addOrMergeGenericArgumentValue(newValue);
        }
    }

    /**
     * Add a generic argument value, merging the new value (typically a collection)
     * with the current value if demanded: see {@link org.springframework.beans.Mergeable}.
     *
     * 添加或合并通用的参数值
     *
     * @param newValue the argument value in the form of a ValueHolder
     */
    private void addOrMergeGenericArgumentValue(ValueHolder newValue) {
        if (newValue.getName() != null) {
            for (Iterator<ValueHolder> it = this.genericArgumentValues.iterator(); it.hasNext();) {
                ValueHolder currentValue = it.next();
                if (newValue.getName().equals(currentValue.getName())) {
                    if (newValue.getValue() instanceof Mergeable) {
                        Mergeable mergeable = (Mergeable) newValue.getValue();
                        if (mergeable.isMergeEnabled()) {
                            newValue.setValue(mergeable.merge(currentValue.getValue()));
                        }
                    }
                    it.remove();
                }
            }
        }
        this.genericArgumentValues.add(newValue);
    }

    /**
     * Look for a generic argument value that matches the given type.
     *
     * 获取通用的参数值
     *
     * @param requiredType the type to match
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getGenericArgumentValue(Class<?> requiredType) {
        return getGenericArgumentValue(requiredType, null, null);
    }

    /**
     * Look for a generic argument value that matches the given type.
     *
     * 获取通用的参数值
     *
     * @param requiredType the type to match
     * @param requiredName the name to match
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName) {
        return getGenericArgumentValue(requiredType, requiredName, null);
    }

    /**
     * Look for the next generic argument value that matches the given type,
     * ignoring argument values that have already been used in the current
     * resolution process.
     *
     * 获取通用的参数值
     *
     * @param requiredType the type to match (can be {@code null} to find
     * an arbitrary next generic argument value)
     * @param requiredName the name to match (can be {@code null} to not
     * match argument values by name)
     * @param usedValueHolders a Set of ValueHolder objects that have already been used
     * in the current resolution process and should therefore not be returned again
     * @return the ValueHolder for the argument, or {@code null} if none found
     */
    public ValueHolder getGenericArgumentValue(Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
        for (ValueHolder valueHolder : this.genericArgumentValues) {
            if (usedValueHolders != null && usedValueHolders.contains(valueHolder)) {
                continue;
            }
            if (valueHolder.getName() != null &&
                    (requiredName == null || !valueHolder.getName().equals(requiredName))) {
                continue;
            }
            if (valueHolder.getType() != null &&
                    (requiredType == null || !ClassUtils.matchesTypeName(requiredType, valueHolder.getType()))) {
                continue;
            }
            if (requiredType != null && valueHolder.getType() == null && valueHolder.getName() == null &&
                    !ClassUtils.isAssignableValue(requiredType, valueHolder.getValue())) {
                continue;
            }
            return valueHolder;
        }
        return null;
    }

    /**
     * Return the list of generic argument values.
     *
     * 获取通用的参数值
     *
     * @return unmodifiable List of ValueHolders
     * @see ValueHolder
     */
    public List<ValueHolder> getGenericArgumentValues() {
        return Collections.unmodifiableList(this.genericArgumentValues);
    }


    /**
     * Look for an argument value that either corresponds to the given index
     * in the constructor argument list or generically matches by type.
     *
     * 获取参数值
     *
     * @param index the index in the constructor argument list
     * @param requiredType the parameter type to match
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getArgumentValue(int index, Class<?> requiredType) {
        return getArgumentValue(index, requiredType, null, null);
    }

    /**
     * Look for an argument value that either corresponds to the given index
     * in the constructor argument list or generically matches by type.
     *
     * 获取参数值
     *
     * @param index the index in the constructor argument list
     * @param requiredType the parameter type to match
     * @param requiredName the parameter name to match
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName) {
        return getArgumentValue(index, requiredType, requiredName, null);
    }

    /**
     * Look for an argument value that either corresponds to the given index
     * in the constructor argument list or generically matches by type.
     *
     * 获取参数值
     *
     * @param index the index in the constructor argument list
     * @param requiredType the parameter type to match (can be {@code null}
     * to find an untyped argument value)
     * @param requiredName the parameter name to match (can be {@code null}
     * to find an unnamed argument value)
     * @param usedValueHolders a Set of ValueHolder objects that have already
     * been used in the current resolution process and should therefore not
     * be returned again (allowing to return the next generic argument match
     * in case of multiple generic argument values of the same type)
     * @return the ValueHolder for the argument, or {@code null} if none set
     */
    public ValueHolder getArgumentValue(int index, Class<?> requiredType, String requiredName, Set<ValueHolder> usedValueHolders) {
        Assert.isTrue(index >= 0, "Index must not be negative");
        ValueHolder valueHolder = getIndexedArgumentValue(index, requiredType, requiredName);
        if (valueHolder == null) {
            valueHolder = getGenericArgumentValue(requiredType, requiredName, usedValueHolders);
        }
        return valueHolder;
    }

    /**
     * Return the number of argument values held in this instance,
     * counting both indexed and generic argument values.
     *
     * 获取参数个数
     */
    public int getArgumentCount() {
        return (this.indexedArgumentValues.size() + this.genericArgumentValues.size());
    }

    /**
     * Return if this holder does not contain any argument values,
     * neither indexed ones nor generic ones.
     *
     * 判断是否为空
     */
    public boolean isEmpty() {
        return (this.indexedArgumentValues.isEmpty() && this.genericArgumentValues.isEmpty());
    }

    /**
     * Clear this holder, removing all argument values.
     *
     * 清空
     */
    public void clear() {
        this.indexedArgumentValues.clear();
        this.genericArgumentValues.clear();
    }


    /**
     * 判断是否相等
     *
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof ConstructorArgumentValues)) {
            return false;
        }
        ConstructorArgumentValues that = (ConstructorArgumentValues) other;
        if (this.genericArgumentValues.size() != that.genericArgumentValues.size() ||
                this.indexedArgumentValues.size() != that.indexedArgumentValues.size()) {
            return false;
        }
        Iterator<ValueHolder> it1 = this.genericArgumentValues.iterator();
        Iterator<ValueHolder> it2 = that.genericArgumentValues.iterator();
        while (it1.hasNext() && it2.hasNext()) {
            ValueHolder vh1 = it1.next();
            ValueHolder vh2 = it2.next();
            if (!vh1.contentEquals(vh2)) {
                return false;
            }
        }
        for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
            ValueHolder vh1 = entry.getValue();
            ValueHolder vh2 = that.indexedArgumentValues.get(entry.getKey());
            if (!vh1.contentEquals(vh2)) {
                return false;
            }
        }
        return true;
    }

    /**
     * 哈希值
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hashCode = 7;
        for (ValueHolder valueHolder : this.genericArgumentValues) {
            hashCode = 31 * hashCode + valueHolder.contentHashCode();
        }
        hashCode = 29 * hashCode;
        for (Map.Entry<Integer, ValueHolder> entry : this.indexedArgumentValues.entrySet()) {
            hashCode = 31 * hashCode + (entry.getValue().contentHashCode() ^ entry.getKey().hashCode());
        }
        return hashCode;
    }


    /**
     * Holder for a constructor argument value, with an optional type
     * attribute indicating the target type of the actual constructor argument.
     *
     * 值存储器
     */
    public static class ValueHolder implements BeanMetadataElement {

        /**
         * 值
         */
        private Object value;

        /**
         * 类型
         */
        private String type;

        /**
         * 名称
         */
        private String name;

        /**
         * 源
         */
        private Object source;

        /**
         * 是否经过了转换
         */
        private boolean converted = false;

        /**
         * 转换后的值
         */
        private Object convertedValue;

        /**
         * Create a new ValueHolder for the given value.
         *
         * 构造方法
         *
         * @param value the argument value
         */
        public ValueHolder(Object value) {
            this.value = value;
        }

        /**
         * Create a new ValueHolder for the given value and type.
         *
         * 构造方法
         *
         * @param value the argument value
         * @param type the type of the constructor argument
         */
        public ValueHolder(Object value, String type) {
            this.value = value;
            this.type = type;
        }

        /**
         * Create a new ValueHolder for the given value, type and name.
         *
         * 构造方法
         *
         * @param value the argument value
         * @param type the type of the constructor argument
         * @param name the name of the constructor argument
         */
        public ValueHolder(Object value, String type, String name) {
            this.value = value;
            this.type = type;
            this.name = name;
        }

        /**
         * Set the value for the constructor argument.
         *
         * 设置值
         *
         * @see PropertyPlaceholderConfigurer
         */
        public void setValue(Object value) {
            this.value = value;
        }

        /**
         * Return the value for the constructor argument.
         *
         * 获取值
         */
        public Object getValue() {
            return this.value;
        }

        /**
         * Set the type of the constructor argument.
         *
         * 设置类型
         */
        public void setType(String type) {
            this.type = type;
        }

        /**
         * Return the type of the constructor argument.
         *
         * 获取类型
         */
        public String getType() {
            return this.type;
        }

        /**
         * Set the name of the constructor argument.
         *
         * 设置名称
         */
        public void setName(String name) {
            this.name = name;
        }

        /**
         * Return the name of the constructor argument.
         *
         * 获取名称
         */
        public String getName() {
            return this.name;
        }

        /**
         * Set the configuration source {@code Object} for this metadata element.
         * <p>The exact type of the object will depend on the configuration mechanism used.
         *
         * 设置源对象
         */
        public void setSource(Object source) {
            this.source = source;
        }

        /**
         * 获取源对象
         *
         * @return
         */
        public Object getSource() {
            return this.source;
        }

        /**
         * Return whether this holder contains a converted value already ({@code true}),
         * or whether the value still needs to be converted ({@code false}).
         *
         * 是否经过了转换
         */
        public synchronized boolean isConverted() {
            return this.converted;
        }

        /**
         * Set the converted value of the constructor argument,
         * after processed type conversion.
         *
         * 设置转换后的值
         */
        public synchronized void setConvertedValue(Object value) {
            this.converted = true;
            this.convertedValue = value;
        }

        /**
         * Return the converted value of the constructor argument,
         * after processed type conversion.
         *
         * 获取转换后的值
         */
        public synchronized Object getConvertedValue() {
            return this.convertedValue;
        }

        /**
         * Determine whether the content of this ValueHolder is equal
         * to the content of the given other ValueHolder.
         * <p>Note that ValueHolder does not implement {@code equals}
         * directly, to allow for multiple ValueHolder instances with the
         * same content to reside in the same Set.
         *
         * 内容是否相等
         */
        private boolean contentEquals(ValueHolder other) {
            return (this == other ||
                    (ObjectUtils.nullSafeEquals(this.value, other.value) && ObjectUtils.nullSafeEquals(this.type, other.type)));
        }

        /**
         * Determine whether the hash code of the content of this ValueHolder.
         * <p>Note that ValueHolder does not implement {@code hashCode}
         * directly, to allow for multiple ValueHolder instances with the
         * same content to reside in the same Set.
         *
         * 获取内容的哈希值
         */
        private int contentHashCode() {
            return ObjectUtils.nullSafeHashCode(this.value) * 29 + ObjectUtils.nullSafeHashCode(this.type);
        }

        /**
         * Create a copy of this ValueHolder: that is, an independent
         * ValueHolder instance with the same contents.
         *
         * 拷贝
         */
        public ValueHolder copy() {
            ValueHolder copy = new ValueHolder(this.value, this.type, this.name);
            copy.setSource(this.source);
            return copy;
        }
    }

}

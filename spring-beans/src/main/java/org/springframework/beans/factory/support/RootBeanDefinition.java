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

package org.springframework.beans.factory.support;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.util.Assert;

/**
 * A root bean definition represents the merged bean definition that backs
 * a specific bean in a Spring BeanFactory at runtime. It might have been created
 * from multiple original bean definitions that inherit from each other,
 * typically registered as {@link GenericBeanDefinition GenericBeanDefinitions}.
 * A root bean definition is essentially the 'unified' bean definition view at runtime.
 *
 * <p>Root bean definitions may also be used for registering individual bean definitions
 * in the configuration phase. However, since Spring 2.5, the preferred way to register
 * bean definitions programmatically is the {@link GenericBeanDefinition} class.
 * GenericBeanDefinition has the advantage that it allows to dynamically define
 * parent dependencies, not 'hard-coding' the role as a root bean definition.
 *
 * 根Bean定义
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @see GenericBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public class RootBeanDefinition extends AbstractBeanDefinition {

    /**
     * 是否允许缓存
     */
    boolean allowCaching = true;

    /**
     * Bean定义保存器
     */
    private BeanDefinitionHolder decoratedDefinition;

    /**
     * 目标类型
     */
    private volatile Class<?> targetType;

    /**
     * 工厂方法是否唯一
     */
    boolean isFactoryMethodUnique = false;

    /**
     * 构造方法参数锁
     */
    final Object constructorArgumentLock = new Object();

    /**
     * Package-visible field for caching the resolved constructor or factory method
     *
     * 解析过的构造方法或者工厂方法
     */
    Object resolvedConstructorOrFactoryMethod;

    /**
     * Package-visible field that marks the constructor arguments as resolved
     *
     * 构造方法是否被解析
     */
    boolean constructorArgumentsResolved = false;

    /**
     * Package-visible field for caching fully resolved constructor arguments
     *
     * 构造方法的参数是否被解析过
     */
    Object[] resolvedConstructorArguments;

    /**
     * Package-visible field for caching partly prepared constructor arguments
     *
     * 预解析的构造方法参数
     */
    Object[] preparedConstructorArguments;

    /**
     * 后置调用锁
     */
    final Object postProcessingLock = new Object();

    /**
     * Package-visible field that indicates MergedBeanDefinitionPostProcessor having been applied
     *
     * 是否完成了后置调用
     */
    boolean postProcessed = false;

    /**
     * Package-visible field that indicates a before-instantiation post-processor having kicked in
     *
     * 是否初始化方法未被调用
     * 具体使用方法参见AbastractAutowireCapableBeanFactory的resolveBeforeInstantiantion方法
     */
    volatile Boolean beforeInstantiationResolved;

    /**
     * 管理的成员
     */
    private Set<Member> externallyManagedConfigMembers;

    /**
     * 初始化方法
     */
    private Set<String> externallyManagedInitMethods;

    /**
     * 销毁方法
     */
    private Set<String> externallyManagedDestroyMethods;


    /**
     * Create a new RootBeanDefinition, to be configured through its bean
     * properties and configuration methods.
     *
     * 构造方法
     *
     * @see #setBeanClass
     * @see #setBeanClassName
     * @see #setScope
     * @see #setAutowireMode
     * @see #setDependencyCheck
     * @see #setConstructorArgumentValues
     * @see #setPropertyValues
     */
    public RootBeanDefinition() {
        super();
    }

    /**
     * Create a new RootBeanDefinition for a singleton.
     *
     * 构造方法
     *
     * @param beanClass the class of the bean to instantiate
     */
    public RootBeanDefinition(Class<?> beanClass) {
        super();
        setBeanClass(beanClass);
    }

    /**
     * Create a new RootBeanDefinition with the given singleton status.
     *
     * 构造方法
     *
     * @param beanClass the class of the bean to instantiate
     * @param singleton the singleton status of the bean
     * @deprecated since Spring 2.5, in favor of {@link #setScope}
     */
    @Deprecated
    public RootBeanDefinition(Class beanClass, boolean singleton) {
        super();
        setBeanClass(beanClass);
        setSingleton(singleton);
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * using the given autowire mode.
     *
     * 构造方法
     *
     * @param beanClass the class of the bean to instantiate
     * @param autowireMode by name or type, using the constants in this interface
     * @deprecated as of Spring 3.0, in favor of {@link #setAutowireMode} usage
     */
    @Deprecated
    public RootBeanDefinition(Class beanClass, int autowireMode) {
        super();
        setBeanClass(beanClass);
        setAutowireMode(autowireMode);
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * using the given autowire mode.
     *
     * 构造方法
     *
     * @param beanClass the class of the bean to instantiate
     * @param autowireMode by name or type, using the constants in this interface
     * @param dependencyCheck whether to perform a dependency check for objects
     * (not applicable to autowiring a constructor, thus ignored there)
     */
    public RootBeanDefinition(Class<?> beanClass, int autowireMode, boolean dependencyCheck) {
        super();
        setBeanClass(beanClass);
        setAutowireMode(autowireMode);
        if (dependencyCheck && getResolvedAutowireMode() != AUTOWIRE_CONSTRUCTOR) {
            setDependencyCheck(RootBeanDefinition.DEPENDENCY_CHECK_OBJECTS);
        }
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * providing property values.
     *
     * 构造方法
     *
     * @param beanClass the class of the bean to instantiate
     * @param pvs the property values to apply
     * @deprecated as of Spring 3.0, in favor of {@link #getPropertyValues} usage
     */
    @Deprecated
    public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs) {
        super(null, pvs);
        setBeanClass(beanClass);
    }

    /**
     * Create a new RootBeanDefinition with the given singleton status,
     * providing property values.
     *
     * 构造方法
     *
     * @param beanClass the class of the bean to instantiate
     * @param pvs the property values to apply
     * @param singleton the singleton status of the bean
     * @deprecated since Spring 2.5, in favor of {@link #setScope}
     */
    @Deprecated
    public RootBeanDefinition(Class beanClass, MutablePropertyValues pvs, boolean singleton) {
        super(null, pvs);
        setBeanClass(beanClass);
        setSingleton(singleton);
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * providing constructor arguments and property values.
     *
     * 构造方法
     *
     * @param beanClass the class of the bean to instantiate
     * @param cargs the constructor argument values to apply
     * @param pvs the property values to apply
     */
    public RootBeanDefinition(Class<?> beanClass, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        super(cargs, pvs);
        setBeanClass(beanClass);
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * providing constructor arguments and property values.
     * <p>Takes a bean class name to avoid eager loading of the bean class.
     *
     * 构造方法
     *
     * @param beanClassName the name of the class to instantiate
     */
    public RootBeanDefinition(String beanClassName) {
        setBeanClassName(beanClassName);
    }

    /**
     * Create a new RootBeanDefinition for a singleton,
     * providing constructor arguments and property values.
     * <p>Takes a bean class name to avoid eager loading of the bean class.
     *
     * 构造方法
     *
     * @param beanClassName the name of the class to instantiate
     * @param cargs the constructor argument values to apply
     * @param pvs the property values to apply
     */
    public RootBeanDefinition(String beanClassName, ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        super(cargs, pvs);
        setBeanClassName(beanClassName);
    }

    /**
     * Create a new RootBeanDefinition as deep copy of the given
     * bean definition.
     *
     * 构造方法
     *
     * @param original the original bean definition to copy from
     */
    public RootBeanDefinition(RootBeanDefinition original) {
        super((BeanDefinition) original);
        this.allowCaching = original.allowCaching;
        this.decoratedDefinition = original.decoratedDefinition;
        this.targetType = original.targetType;
        this.isFactoryMethodUnique = original.isFactoryMethodUnique;
    }

    /**
     * Create a new RootBeanDefinition as deep copy of the given
     * bean definition.
     *
     * 构造方法
     *
     * @param original the original bean definition to copy from
     */
    RootBeanDefinition(BeanDefinition original) {
        super(original);
    }


    /**
     * 根Bean定义没有父Bean名称
     *
     * @return
     */
    public String getParentName() {
        return null;
    }

    /**
     * 根Bean不允许设置父Bean名称
     *
     * @param parentName
     */
    public void setParentName(String parentName) {
        if (parentName != null) {
            throw new IllegalArgumentException("Root bean cannot be changed into a child bean with parent reference");
        }
    }

    /**
     * Register a target definition that is being decorated by this bean definition.
     *
     * 设置持有者
     */
    public void setDecoratedDefinition(BeanDefinitionHolder decoratedDefinition) {
        this.decoratedDefinition = decoratedDefinition;
    }

    /**
     * Return the target definition that is being decorated by this bean definition, if any.
     *
     * 获取持有者
     */
    public BeanDefinitionHolder getDecoratedDefinition() {
        return this.decoratedDefinition;
    }

    /**
     * Specify the target type of this bean definition, if known in advance.
     *
     * 设置目标类型
     */
    public void setTargetType(Class<?> targetType) {
        this.targetType = targetType;
    }

    /**
     * Return the target type of this bean definition, if known
     * (either specified in advance or resolved on first instantiation).
     *
     * 获取目标类型
     */
    public Class<?> getTargetType() {
        return this.targetType;
    }

    /**
     * 设置唯一的工厂方法名
     *
     * Specify a factory method name that refers to a non-overloaded method.
     */
    public void setUniqueFactoryMethodName(String name) {
        Assert.hasText(name, "Factory method name must not be empty");
        setFactoryMethodName(name);
        this.isFactoryMethodUnique = true;
    }

    /**
     * 判断某个方法是否是工厂方法
     *
     * Check whether the given candidate qualifies as a factory method.
     */
    public boolean isFactoryMethod(Method candidate) {
        return (candidate != null && candidate.getName().equals(getFactoryMethodName()));
    }

    /**
     * Return the resolved factory method as a Java Method object, if available.
     * @return the factory method, or {@code null} if not found or not resolved yet
     */
    public Method getResolvedFactoryMethod() {
        synchronized (this.constructorArgumentLock) {
            Object candidate = this.resolvedConstructorOrFactoryMethod;
            return (candidate instanceof Method ? (Method) candidate : null);
        }
    }

    /**
     *
     * @param configMember
     */
    public void registerExternallyManagedConfigMember(Member configMember) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedConfigMembers == null) {
                this.externallyManagedConfigMembers = new HashSet<Member>(1);
            }
            this.externallyManagedConfigMembers.add(configMember);
        }
    }

    /**
     *
     * @param configMember
     * @return
     */
    public boolean isExternallyManagedConfigMember(Member configMember) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedConfigMembers != null &&
                    this.externallyManagedConfigMembers.contains(configMember));
        }
    }

    /**
     *
     * @param initMethod
     */
    public void registerExternallyManagedInitMethod(String initMethod) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedInitMethods == null) {
                this.externallyManagedInitMethods = new HashSet<String>(1);
            }
            this.externallyManagedInitMethods.add(initMethod);
        }
    }

    /**
     *
     * @param initMethod
     * @return
     */
    public boolean isExternallyManagedInitMethod(String initMethod) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedInitMethods != null &&
                    this.externallyManagedInitMethods.contains(initMethod));
        }
    }

    /**
     *
     *
     * @param destroyMethod
     */
    public void registerExternallyManagedDestroyMethod(String destroyMethod) {
        synchronized (this.postProcessingLock) {
            if (this.externallyManagedDestroyMethods == null) {
                this.externallyManagedDestroyMethods = new HashSet<String>(1);
            }
            this.externallyManagedDestroyMethods.add(destroyMethod);
        }
    }

    /**
     *
     *
     * @param destroyMethod
     * @return
     */
    public boolean isExternallyManagedDestroyMethod(String destroyMethod) {
        synchronized (this.postProcessingLock) {
            return (this.externallyManagedDestroyMethods != null &&
                    this.externallyManagedDestroyMethods.contains(destroyMethod));
        }
    }


    /**
     * 克隆方法
     *
     * @return
     */
    @Override
    public RootBeanDefinition cloneBeanDefinition() {
        return new RootBeanDefinition(this);
    }

    /**
     * 判断是否相等
     *
     * @param other
     * @return
     */
    @Override
    public boolean equals(Object other) {
        return (this == other || (other instanceof RootBeanDefinition && super.equals(other)));
    }

    /**
     * 转换为字符串
     *
     * @return
     */
    @Override
    public String toString() {
        return "Root bean: " + super.toString();
    }

}

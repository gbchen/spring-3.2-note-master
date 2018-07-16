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

package org.springframework.beans.factory.support;

import java.lang.reflect.Constructor;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.BeanMetadataAttributeAccessor;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConstructorArgumentValues;
import org.springframework.core.io.DescriptiveResource;
import org.springframework.core.io.Resource;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Base class for concrete, full-fledged
 * {@link org.springframework.beans.factory.config.BeanDefinition} classes,
 * factoring out common properties of {@link GenericBeanDefinition},
 * {@link RootBeanDefinition} and {@link ChildBeanDefinition}.
 *
 * <p>The autowire constants match the ones defined in the
 * {@link org.springframework.beans.factory.config.AutowireCapableBeanFactory}
 * interface.
 *
 * 抽象的Bean定义
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Mark Fisher
 * @see RootBeanDefinition
 * @see ChildBeanDefinition
 */
@SuppressWarnings("serial")
public abstract class AbstractBeanDefinition extends BeanMetadataAttributeAccessor
        implements BeanDefinition, Cloneable {

    /**
     * Constant for the default scope name: "", equivalent to singleton status
     * but to be overridden from a parent bean definition (if applicable).
     *
     * 默认的作用域
     */
    public static final String SCOPE_DEFAULT = "";

    /**
     * Constant that indicates no autowiring at all.
     *
     * 不使用自动装配
     *
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_NO = AutowireCapableBeanFactory.AUTOWIRE_NO;

    /**
     * Constant that indicates autowiring bean properties by name.
     *
     * 根据名称自动装配
     *
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_BY_NAME = AutowireCapableBeanFactory.AUTOWIRE_BY_NAME;

    /**
     * Constant that indicates autowiring bean properties by type.
     *
     * 根据类型自动装配
     *
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_BY_TYPE = AutowireCapableBeanFactory.AUTOWIRE_BY_TYPE;

    /**
     * Constant that indicates autowiring a constructor.
     *
     * 根据构造方法自动装配
     *
     * @see #setAutowireMode
     */
    public static final int AUTOWIRE_CONSTRUCTOR = AutowireCapableBeanFactory.AUTOWIRE_CONSTRUCTOR;

    /**
     * Constant that indicates determining an appropriate autowire strategy
     * through introspection of the bean class.
     *
     * 通过内省来自动装配
     * 如果使用混合的自动装配方式的话
     * 可以使用基于注解的自动装配方式
     *
     * @see #setAutowireMode
     * @deprecated as of Spring 3.0: If you are using mixed autowiring strategies,
     * use annotation-based autowiring for clearer demarcation of autowiring needs.
     */
    @Deprecated
    public static final int AUTOWIRE_AUTODETECT = AutowireCapableBeanFactory.AUTOWIRE_AUTODETECT;

    /**
     * Constant that indicates no dependency check at all.
     *
     * 不检测依赖
     *
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_NONE = 0;

    /**
     * Constant that indicates dependency checking for object references.
     *
     * 对于对象类型检测依赖
     *
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_OBJECTS = 1;

    /**
     * Constant that indicates dependency checking for "simple" properties.
     *
     * 对于简单类型检测依赖
     *
     * @see #setDependencyCheck
     * @see org.springframework.beans.BeanUtils#isSimpleProperty
     */
    public static final int DEPENDENCY_CHECK_SIMPLE = 2;

    /**
     * Constant that indicates dependency checking for all properties
     * (object references as well as "simple" properties).
     *
     * 对于所有属性都检测依赖
     *
     * @see #setDependencyCheck
     */
    public static final int DEPENDENCY_CHECK_ALL = 3;

    /**
     * Constant that indicates the container should attempt to infer the
     * {@link #setDestroyMethodName destroy method name} for a bean as opposed to
     * explicit specification of a method name. The value {@value} is specifically
     * designed to include characters otherwise illegal in a method name, ensuring
     * no possibility of collisions with legitimately named methods having the same
     * name.
     * <p>Currently, the method names detected during destroy method inference
     * are "close" and "shutdown", if present on the specific bean class.
     *
     * 指定一个非法的关闭方法名
     */
    public static final String INFER_METHOD = "(inferred)";


    /**
     * Bean类
     */
    private volatile Object beanClass;

    /**
     * 作用域
     */
    private String scope = SCOPE_DEFAULT;

    /**
     * 是否是单例模式，默认为真
     */
    private boolean singleton = true;

    /**
     * 是否是原型模式，默认为假
     */
    private boolean prototype = false;

    /**
     * 是否是抽象类
     */
    private boolean abstractFlag = false;

    /**
     * 延迟初始化
     */
    private boolean lazyInit = false;

    /**
     * 装配模式
     */
    private int autowireMode = AUTOWIRE_NO;

    /**
     * 依赖检测模式
     */
    private int dependencyCheck = DEPENDENCY_CHECK_NONE;

    /**
     * 依赖的内容
     */
    private String[] dependsOn;

    /**
     * 是否自动装配
     */
    private boolean autowireCandidate = true;

    /**
     * 是否是主类
     */
    private boolean primary = false;

    /**
     * 修饰符
     */
    private final Map<String, AutowireCandidateQualifier> qualifiers = new LinkedHashMap<String, AutowireCandidateQualifier>(0);

    /**
     * 是否允许非public方法访问
     */
    private boolean nonPublicAccessAllowed = true;

    /**
     * 是否是宽松模式
     */
    private boolean lenientConstructorResolution = true;

    /**
     * 构造方法参数值
     */
    private ConstructorArgumentValues constructorArgumentValues;

    /**
     * 属性值
     */
    private MutablePropertyValues propertyValues;

    /**
     * 方法重写
     */
    private MethodOverrides methodOverrides = new MethodOverrides();

    /**
     * 工厂Bean的名称
     */
    private String factoryBeanName;

    /**
     * 工厂方法的名称
     */
    private String factoryMethodName;

    /**
     * 初始化方法的名称
     */
    private String initMethodName;

    /**
     * 销毁方法的名称
     */
    private String destroyMethodName;

    /**
     * 强制初始化方法
     */
    private boolean enforceInitMethod = true;

    /**
     * 强制销毁方法
     */
    private boolean enforceDestroyMethod = true;

    /**
     * 是否以合成
     */
    private boolean synthetic = false;

    /**
     * 角色
     */
    private int role = BeanDefinition.ROLE_APPLICATION;

    /**
     * 描述
     */
    private String description;

    /**
     * 对应的资源
     */
    private Resource resource;


    /**
     * Create a new AbstractBeanDefinition with default settings.
     *
     * 构造方法
     */
    protected AbstractBeanDefinition() {
        this(null, null);
    }

    /**
     * Create a new AbstractBeanDefinition with the given
     * constructor argument values and property values.
     *
     * 构造方法
     */
    protected AbstractBeanDefinition(ConstructorArgumentValues cargs, MutablePropertyValues pvs) {
        setConstructorArgumentValues(cargs);
        setPropertyValues(pvs);
    }

    /**
     * Create a new AbstractBeanDefinition as a deep copy of the given
     * bean definition.
     *
     * 构造方法
     *
     * @param original the original bean definition to copy from
     * @deprecated since Spring 2.5, in favor of {@link #AbstractBeanDefinition(BeanDefinition)}
     */
    @Deprecated
    protected AbstractBeanDefinition(AbstractBeanDefinition original) {
        this((BeanDefinition) original);
    }

    /**
     * Create a new AbstractBeanDefinition as a deep copy of the given
     * bean definition.
     *
     * 深拷贝
     *
     * @param original the original bean definition to copy from
     */
    protected AbstractBeanDefinition(BeanDefinition original) {
        setParentName(original.getParentName());
        setBeanClassName(original.getBeanClassName());
        setFactoryBeanName(original.getFactoryBeanName());
        setFactoryMethodName(original.getFactoryMethodName());
        setScope(original.getScope());
        setAbstract(original.isAbstract());
        setLazyInit(original.isLazyInit());
        setRole(original.getRole());
        setConstructorArgumentValues(new ConstructorArgumentValues(original.getConstructorArgumentValues()));
        setPropertyValues(new MutablePropertyValues(original.getPropertyValues()));
        setSource(original.getSource());
        copyAttributesFrom(original);

        if (original instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition originalAbd = (AbstractBeanDefinition) original;
            if (originalAbd.hasBeanClass()) {
                setBeanClass(originalAbd.getBeanClass());
            }
            setAutowireMode(originalAbd.getAutowireMode());
            setDependencyCheck(originalAbd.getDependencyCheck());
            setDependsOn(originalAbd.getDependsOn());
            setAutowireCandidate(originalAbd.isAutowireCandidate());
            copyQualifiersFrom(originalAbd);
            setPrimary(originalAbd.isPrimary());
            setNonPublicAccessAllowed(originalAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(originalAbd.isLenientConstructorResolution());
            setInitMethodName(originalAbd.getInitMethodName());
            setEnforceInitMethod(originalAbd.isEnforceInitMethod());
            setDestroyMethodName(originalAbd.getDestroyMethodName());
            setEnforceDestroyMethod(originalAbd.isEnforceDestroyMethod());
            setMethodOverrides(new MethodOverrides(originalAbd.getMethodOverrides()));
            setSynthetic(originalAbd.isSynthetic());
            setResource(originalAbd.getResource());
        } else {
            setResourceDescription(original.getResourceDescription());
        }
    }


    /**
     * Override settings in this bean definition (presumably a copied parent
     * from a parent-child inheritance relationship) from the given bean
     * definition (presumably the child).
     *
     * 继承关系
     * 通常是从父Bean向子Bean拷贝
     *
     * @deprecated since Spring 2.5, in favor of {@link #overrideFrom(BeanDefinition)}
     */
    @Deprecated
    public void overrideFrom(AbstractBeanDefinition other) {
        overrideFrom((BeanDefinition) other);
    }

    /**
     * Override settings in this bean definition (presumably a copied parent
     * from a parent-child inheritance relationship) from the given bean
     * definition (presumably the child).
     * <ul>
     * <li>Will override beanClass if specified in the given bean definition.
     * <li>Will always take {@code abstract}, {@code scope},
     * {@code lazyInit}, {@code autowireMode}, {@code dependencyCheck},
     * and {@code dependsOn} from the given bean definition.
     * <li>Will add {@code constructorArgumentValues}, {@code propertyValues},
     * {@code methodOverrides} from the given bean definition to existing ones.
     * <li>Will override {@code factoryBeanName}, {@code factoryMethodName},
     * {@code initMethodName}, and {@code destroyMethodName} if specified
     * in the given bean definition.
     * </ul>
     *
     * 继承自其他Bean
     */
    public void overrideFrom(BeanDefinition other) {
        if (StringUtils.hasLength(other.getBeanClassName())) {
            setBeanClassName(other.getBeanClassName());
        }
        if (StringUtils.hasLength(other.getFactoryBeanName())) {
            setFactoryBeanName(other.getFactoryBeanName());
        }
        if (StringUtils.hasLength(other.getFactoryMethodName())) {
            setFactoryMethodName(other.getFactoryMethodName());
        }
        if (StringUtils.hasLength(other.getScope())) {
            setScope(other.getScope());
        }
        setAbstract(other.isAbstract());
        setLazyInit(other.isLazyInit());
        setRole(other.getRole());
        getConstructorArgumentValues().addArgumentValues(other.getConstructorArgumentValues());
        getPropertyValues().addPropertyValues(other.getPropertyValues());
        setSource(other.getSource());
        copyAttributesFrom(other);

        if (other instanceof AbstractBeanDefinition) {
            AbstractBeanDefinition otherAbd = (AbstractBeanDefinition) other;
            if (otherAbd.hasBeanClass()) {
                setBeanClass(otherAbd.getBeanClass());
            }
            setAutowireCandidate(otherAbd.isAutowireCandidate());
            setAutowireMode(otherAbd.getAutowireMode());
            copyQualifiersFrom(otherAbd);
            setPrimary(otherAbd.isPrimary());
            setDependencyCheck(otherAbd.getDependencyCheck());
            setDependsOn(otherAbd.getDependsOn());
            setNonPublicAccessAllowed(otherAbd.isNonPublicAccessAllowed());
            setLenientConstructorResolution(otherAbd.isLenientConstructorResolution());
            if (StringUtils.hasLength(otherAbd.getInitMethodName())) {
                setInitMethodName(otherAbd.getInitMethodName());
                setEnforceInitMethod(otherAbd.isEnforceInitMethod());
            }
            if (StringUtils.hasLength(otherAbd.getDestroyMethodName())) {
                setDestroyMethodName(otherAbd.getDestroyMethodName());
                setEnforceDestroyMethod(otherAbd.isEnforceDestroyMethod());
            }
            getMethodOverrides().addOverrides(otherAbd.getMethodOverrides());
            setSynthetic(otherAbd.isSynthetic());
            setResource(otherAbd.getResource());
        } else {
            setResourceDescription(other.getResourceDescription());
        }
    }

    /**
     * Apply the provided default values to this bean.
     *
     * 提供默认值
     *
     * @param defaults the defaults to apply
     */
    public void applyDefaults(BeanDefinitionDefaults defaults) {
        setLazyInit(defaults.isLazyInit());
        setAutowireMode(defaults.getAutowireMode());
        setDependencyCheck(defaults.getDependencyCheck());
        setInitMethodName(defaults.getInitMethodName());
        setEnforceInitMethod(false);
        setDestroyMethodName(defaults.getDestroyMethodName());
        setEnforceDestroyMethod(false);
    }


    /**
     * Return whether this definition specifies a bean class.
     *
     * 是否指定了Bean类
     */
    public boolean hasBeanClass() {
        return (this.beanClass instanceof Class);
    }

    /**
     * Specify the class for this bean.
     *
     * 指定Bean类
     */
    public void setBeanClass(Class<?> beanClass) {
        this.beanClass = beanClass;
    }

    /**
     * Return the class of the wrapped bean, if already resolved.
     *
     * 获取装配的Bean类
     *
     * @return the bean class, or {@code null} if none defined
     * @throws IllegalStateException if the bean definition does not define a bean class,
     * or a specified bean class name has not been resolved into an actual Class
     */
    public Class<?> getBeanClass() throws IllegalStateException {
        Object beanClassObject = this.beanClass;
        if (beanClassObject == null) {
            throw new IllegalStateException("No bean class specified on bean definition");
        }
        if (!(beanClassObject instanceof Class)) {
            throw new IllegalStateException(
                    "Bean class name [" + beanClassObject + "] has not been resolved into an actual Class");
        }
        return (Class<?>) beanClassObject;
    }

    /**
     * 设置Bean类名
     *
     * @param beanClassName
     */
    public void setBeanClassName(String beanClassName) {
        this.beanClass = beanClassName;
    }

    public String getBeanClassName() {
        Object beanClassObject = this.beanClass;
        if (beanClassObject instanceof Class) {
            return ((Class<?>) beanClassObject).getName();
        } else {
            return (String) beanClassObject;
        }
    }

    /**
     * Determine the class of the wrapped bean, resolving it from a
     * specified class name if necessary. Will also reload a specified
     * Class from its name when called with the bean class already resolved.
     *
     * 解析Bean类
     *
     * @param classLoader the ClassLoader to use for resolving a (potential) class name
     * @return the resolved bean class
     * @throws ClassNotFoundException if the class name could be resolved
     */
    public Class<?> resolveBeanClass(ClassLoader classLoader) throws ClassNotFoundException {
        String className = getBeanClassName();
        if (className == null) {
            return null;
        }
        Class<?> resolvedClass = ClassUtils.forName(className, classLoader);
        this.beanClass = resolvedClass;
        return resolvedClass;
    }


    /**
     * Set the name of the target scope for the bean.
     * <p>Default is singleton status, although this is only applied once
     * a bean definition becomes active in the containing factory. A bean
     * definition may eventually inherit its scope from a parent bean definitionFor this
     * reason, the default scope name is empty (empty String), with
     * singleton status being assumed until a resolved scope will be set.
     *
     * 设置作用域
     *
     * @see #SCOPE_SINGLETON
     * @see #SCOPE_PROTOTYPE
     */
    public void setScope(String scope) {
        this.scope = scope;
        this.singleton = SCOPE_SINGLETON.equals(scope) || SCOPE_DEFAULT.equals(scope);
        this.prototype = SCOPE_PROTOTYPE.equals(scope);
    }

    /**
     * Return the name of the target scope for the bean.
     *
     * 获取作用域
     */
    public String getScope() {
        return this.scope;
    }

    /**
     * Set if this a <b>Singleton</b>, with a single, shared instance returned
     * on all calls. In case of "false", the BeanFactory will apply the <b>Prototype</b>
     * design pattern, with each caller requesting an instance getting an independent
     * instance. How this is exactly defined will depend on the BeanFactory.
     * <p>"Singletons" are the commoner type, so the default is "true".
     * Note that as of Spring 2.0, this flag is just an alternative way to
     * specify scope="singleton" or scope="prototype".
     *
     * 设置是否是单例模式
     *
     * @deprecated since Spring 2.5, in favor of {@link #setScope}
     * @see #setScope
     * @see #SCOPE_SINGLETON
     * @see #SCOPE_PROTOTYPE
     */
    @Deprecated
    public void setSingleton(boolean singleton) {
        this.scope = (singleton ? SCOPE_SINGLETON : SCOPE_PROTOTYPE);
        this.singleton = singleton;
        this.prototype = !singleton;
    }

    /**
     * Return whether this a <b>Singleton</b>, with a single shared instance
     * returned from all calls.
     *
     * 判断是否是单例模式
     *
     * @see #SCOPE_SINGLETON
     */
    public boolean isSingleton() {
        return this.singleton;
    }

    /**
     * Return whether this a <b>Prototype</b>, with an independent instance
     * returned for each call.
     *
     * 判断是否是原型模式
     *
     * @see #SCOPE_PROTOTYPE
     */
    public boolean isPrototype() {
        return this.prototype;
    }

    /**
     * Set if this bean is "abstract", i.e. not meant to be instantiated itself but
     * rather just serving as parent for concrete child bean definitions.
     * <p>Default is "false". Specify true to tell the bean factory to not try to
     * instantiate that particular bean in any case.
     *
     * 获取是否是抽象的
     */
    public void setAbstract(boolean abstractFlag) {
        this.abstractFlag = abstractFlag;
    }

    /**
     * Return whether this bean is "abstract", i.e. not meant to be instantiated
     * itself but rather just serving as parent for concrete child bean definitions.
     *
     * 判断是否是抽象的
     */
    public boolean isAbstract() {
        return this.abstractFlag;
    }

    /**
     * Set whether this bean should be lazily initialized.
     * <p>If {@code false}, the bean will get instantiated on startup by bean
     * factories that perform eager initialization of singletons.
     *
     * 设置是否延迟初始化
     */
    public void setLazyInit(boolean lazyInit) {
        this.lazyInit = lazyInit;
    }

    /**
     * Return whether this bean should be lazily initialized, i.e. not
     * eagerly instantiated on startup. Only applicable to a singleton bean.
     *
     * 是否延迟初始化
     */
    public boolean isLazyInit() {
        return this.lazyInit;
    }


    /**
     * Set the autowire mode. This determines whether any automagical detection
     * and setting of bean references will happen. Default is AUTOWIRE_NO,
     * which means there's no autowire.
     *
     * 设置装配模式
     *
     * @param autowireMode the autowire mode to set.
     * Must be one of the constants defined in this class.
     * @see #AUTOWIRE_NO
     * @see #AUTOWIRE_BY_NAME
     * @see #AUTOWIRE_BY_TYPE
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_AUTODETECT
     */
    public void setAutowireMode(int autowireMode) {
        this.autowireMode = autowireMode;
    }

    /**
     * Return the autowire mode as specified in the bean definition.
     *
     * 获取自动装配模式
     */
    public int getAutowireMode() {
        return this.autowireMode;
    }

    /**
     * Return the resolved autowire code,
     * (resolving AUTOWIRE_AUTODETECT to AUTOWIRE_CONSTRUCTOR or AUTOWIRE_BY_TYPE).
     *
     * 解析装配模式
     *
     * @see #AUTOWIRE_AUTODETECT
     * @see #AUTOWIRE_CONSTRUCTOR
     * @see #AUTOWIRE_BY_TYPE
     */
    public int getResolvedAutowireMode() {
        if (this.autowireMode == AUTOWIRE_AUTODETECT) {
            // Work out whether to apply setter autowiring or constructor autowiring.
            // If it has a no-arg constructor it's deemed to be setter autowiring,
            // otherwise we'll try constructor autowiring.
            Constructor<?>[] constructors = getBeanClass().getConstructors();
            for (Constructor<?> constructor : constructors) {
                // 如果有一个无惨的构造方法，就按照类型装配
                if (constructor.getParameterTypes().length == 0) {
                    return AUTOWIRE_BY_TYPE;
                }
            }
            return AUTOWIRE_CONSTRUCTOR;
        } else {
            return this.autowireMode;
        }
    }

    /**
     * Set the dependency check code.
     *
     * 设置依赖检测
     *
     * @param dependencyCheck the code to set.
     * Must be one of the four constants defined in this class.
     * @see #DEPENDENCY_CHECK_NONE
     * @see #DEPENDENCY_CHECK_OBJECTS
     * @see #DEPENDENCY_CHECK_SIMPLE
     * @see #DEPENDENCY_CHECK_ALL
     */
    public void setDependencyCheck(int dependencyCheck) {
        this.dependencyCheck = dependencyCheck;
    }

    /**
     * Return the dependency check code.
     *
     * 返回是否依赖检测
     */
    public int getDependencyCheck() {
        return this.dependencyCheck;
    }

    /**
     * Set the names of the beans that this bean depends on being initialized.
     * The bean factory will guarantee that these beans get initialized first.
     * <p>Note that dependencies are normally expressed through bean properties or
     * constructor arguments. This property should just be necessary for other kinds
     * of dependencies like statics (*ugh*) or database preparation on startup.
     *
     * 设置依赖
     * 它通常对某些静态数据或者数据库连接类是必须的
     * 对于非必须的，可以不设置它
     */
    public void setDependsOn(String[] dependsOn) {
        this.dependsOn = dependsOn;
    }

    /**
     * Return the bean names that this bean depends on.
     *
     * 获取依赖
     */
    public String[] getDependsOn() {
        return this.dependsOn;
    }

    /**
     * Set whether this bean is a candidate for getting autowired into some other bean.
     *
     * 设置是否自动装配
     */
    public void setAutowireCandidate(boolean autowireCandidate) {
        this.autowireCandidate = autowireCandidate;
    }

    /**
     * Return whether this bean is a candidate for getting autowired into some other bean.
     *
     * 是否自动装配
     */
    public boolean isAutowireCandidate() {
        return this.autowireCandidate;
    }

    /**
     * Set whether this bean is a primary autowire candidate.
     * If this value is true for exactly one bean among multiple
     * matching candidates, it will serve as a tie-breaker.
     *
     * 设置是否是primary
     */
    public void setPrimary(boolean primary) {
        this.primary = primary;
    }

    /**
     * Return whether this bean is a primary autowire candidate.
     * If this value is true for exactly one bean among multiple
     * matching candidates, it will serve as a tie-breaker.
     *
     * 是否是primary
     */
    public boolean isPrimary() {
        return this.primary;
    }

    /**
     * Register a qualifier to be used for autowire candidate resolution,
     * keyed by the qualifier's type name.
     *
     * 添加自动装配的限定符
     *
     * @see AutowireCandidateQualifier#getTypeName()
     */
    public void addQualifier(AutowireCandidateQualifier qualifier) {
        this.qualifiers.put(qualifier.getTypeName(), qualifier);
    }

    /**
     * Return whether this bean has the specified qualifier.
     *
     * 返回特定的限定符
     */
    public boolean hasQualifier(String typeName) {
        return this.qualifiers.keySet().contains(typeName);
    }

    /**
     * Return the qualifier mapped to the provided type name.
     *
     * 获取限定符
     */
    public AutowireCandidateQualifier getQualifier(String typeName) {
        return this.qualifiers.get(typeName);
    }

    /**
     * Return all registered qualifiers.
     *
     * 返回所有注册的限定符
     *
     * @return the Set of {@link AutowireCandidateQualifier} objects.
     */
    public Set<AutowireCandidateQualifier> getQualifiers() {
        return new LinkedHashSet<AutowireCandidateQualifier>(this.qualifiers.values());
    }

    /**
     * Copy the qualifiers from the supplied AbstractBeanDefinition to this bean definition.
     *
     * 拷贝限定符
     *
     * @param source the AbstractBeanDefinition to copy from
     */
    public void copyQualifiersFrom(AbstractBeanDefinition source) {
        Assert.notNull(source, "Source must not be null");
        this.qualifiers.putAll(source.qualifiers);
    }


    /**
     * Specify whether to allow access to non-public constructors and methods,
     * for the case of externalized metadata pointing to those. The default is
     * {@code true}; switch this to {@code false} for public access only.
     * <p>This applies to constructor resolution, factory method resolution,
     * and also init/destroy methods. Bean property accessors have to be public
     * in any case and are not affected by this setting.
     * <p>Note that annotation-driven configuration will still access non-public
     * members as far as they have been annotated. This setting applies to
     * externalized metadata in this bean definition only.
     *
     * 设置是否使用非public的构造方法和普通方法
     * 默认为true
     * 它的作用范围有:
     * 1).构造方法
     * 2).工厂方法
     * 3).init和destroy方法
     */
    public void setNonPublicAccessAllowed(boolean nonPublicAccessAllowed) {
        this.nonPublicAccessAllowed = nonPublicAccessAllowed;
    }

    /**
     * Return whether to allow access to non-public constructors and methods.
     *
     * 是否允许非public的构造方法和普通方法
     */
    public boolean isNonPublicAccessAllowed() {
        return this.nonPublicAccessAllowed;
    }

    /**
     * Specify whether to resolve constructors in lenient mode ({@code true},
     * which is the default) or to switch to strict resolution (throwing an exception
     * in case of ambiguous constructors that all match when converting the arguments,
     * whereas lenient mode would use the one with the 'closest' type matches).
     *
     * 设置是否使用宽松模式来解析构造方法
     * 默认是宽松模式
     */
    public void setLenientConstructorResolution(boolean lenientConstructorResolution) {
        this.lenientConstructorResolution = lenientConstructorResolution;
    }

    /**
     * Return whether to resolve constructors in lenient mode or in strict mode.
     *
     * 是否宽松的方式解析构造方法
     * 可以有宽松模式和严格模式
     */
    public boolean isLenientConstructorResolution() {
        return this.lenientConstructorResolution;
    }

    /**
     * Specify constructor argument values for this bean.
     *
     * 设置构造方法参数值
     */
    public void setConstructorArgumentValues(ConstructorArgumentValues constructorArgumentValues) {
        this.constructorArgumentValues =
                (constructorArgumentValues != null ? constructorArgumentValues : new ConstructorArgumentValues());
    }

    /**
     * Return constructor argument values for this bean (never {@code null}).
     *
     * 获取构造方法参数值
     */
    public ConstructorArgumentValues getConstructorArgumentValues() {
        return this.constructorArgumentValues;
    }

    /**
     * Return if there are constructor argument values defined for this bean.
     *
     * 判断是否有构造方法的参数值
     */
    public boolean hasConstructorArgumentValues() {
        return !this.constructorArgumentValues.isEmpty();
    }

    /**
     * Specify property values for this bean, if any.
     *
     * 设置所有的属性值
     */
    public void setPropertyValues(MutablePropertyValues propertyValues) {
        this.propertyValues = (propertyValues != null ? propertyValues : new MutablePropertyValues());
    }

    /**
     * Return property values for this bean (never {@code null}).
     *
     * 获取所有的属性值
     */
    public MutablePropertyValues getPropertyValues() {
        return this.propertyValues;
    }

    /**
     * Specify method overrides for the bean, if any.
     *
     * 设置方法重写
     */
    public void setMethodOverrides(MethodOverrides methodOverrides) {
        this.methodOverrides = (methodOverrides != null ? methodOverrides : new MethodOverrides());
    }

    /**
     * Return information about methods to be overridden by the IoC
     * container. This will be empty if there are no method overrides.
     * Never returns null.
     *
     * 返回重写的方法
     */
    public MethodOverrides getMethodOverrides() {
        return this.methodOverrides;
    }


    /**
     * 设置工厂Bean名称
     *
     * @param factoryBeanName
     */
    public void setFactoryBeanName(String factoryBeanName) {
        this.factoryBeanName = factoryBeanName;
    }

    /**
     * 获取工厂Bean名称
     *
     * @return
     */
    public String getFactoryBeanName() {
        return this.factoryBeanName;
    }

    /**
     * 设置工厂方法名
     *
     * @param factoryMethodName static factory method name,
     * or {@code null} if normal constructor creation should be used
     */
    public void setFactoryMethodName(String factoryMethodName) {
        this.factoryMethodName = factoryMethodName;
    }

    /**
     * 获取工厂方法名
     *
     * @return
     */
    public String getFactoryMethodName() {
        return this.factoryMethodName;
    }

    /**
     * Set the name of the initializer method. The default is {@code null}
     * in which case there is no initializer method.
     *
     * 设置初始化方法名
     */
    public void setInitMethodName(String initMethodName) {
        this.initMethodName = initMethodName;
    }

    /**
     * Return the name of the initializer method.
     *
     * 获取初始化方法名
     */
    public String getInitMethodName() {
        return this.initMethodName;
    }

    /**
     * Specify whether or not the configured init method is the default.
     * Default value is {@code false}.
     *
     * 设置是否是用默认的初始化方法
     *
     * @see #setInitMethodName
     */
    public void setEnforceInitMethod(boolean enforceInitMethod) {
        this.enforceInitMethod = enforceInitMethod;
    }

    /**
     * Indicate whether the configured init method is the default.
     *
     * 获取是否使用默认的初始化方法
     *
     * @see #getInitMethodName()
     */
    public boolean isEnforceInitMethod() {
        return this.enforceInitMethod;
    }

    /**
     * Set the name of the destroy method. The default is {@code null}
     * in which case there is no destroy method.
     *
     * 设置销毁方法的名称
     */
    public void setDestroyMethodName(String destroyMethodName) {
        this.destroyMethodName = destroyMethodName;
    }

    /**
     * Return the name of the destroy method.
     *
     * 获取销毁方法的名称
     */
    public String getDestroyMethodName() {
        return this.destroyMethodName;
    }

    /**
     * Specify whether or not the configured destroy method is the default.
     * Default value is {@code false}.
     *
     * 设置是否是默认的销毁方法
     *
     * @see #setDestroyMethodName
     */
    public void setEnforceDestroyMethod(boolean enforceDestroyMethod) {
        this.enforceDestroyMethod = enforceDestroyMethod;
    }

    /**
     * Indicate whether the configured destroy method is the default.
     *
     * 获取是否是默认的销毁方法
     *
     * @see #getDestroyMethodName
     */
    public boolean isEnforceDestroyMethod() {
        return this.enforceDestroyMethod;
    }


    /**
     * Set whether this bean definition is 'synthetic', that is, not defined
     * by the application itself (for example, an infrastructure bean such
     * as a helper for auto-proxying, created through {@code &ltaop:config&gt;}).
     *
     * 设置是否是合成的
     */
    public void setSynthetic(boolean synthetic) {
        this.synthetic = synthetic;
    }

    /**
     * Return whether this bean definition is 'synthetic', that is,
     * not defined by the application itself.
     *
     * 是否是合成的
     * 也就是不是应用本身定义的
     */
    public boolean isSynthetic() {
        return this.synthetic;
    }

    /**
     * Set the role hint for this {@code BeanDefinition}.
     *
     * 设置角色
     */
    public void setRole(int role) {
        this.role = role;
    }

    /**
     * Return the role hint for this {@code BeanDefinition}.
     *
     * 获取角色
     */
    public int getRole() {
        return this.role;
    }


    /**
     * Set a human-readable description of this bean definition.
     *
     * 设置描述
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * 获取描述
     *
     * @return
     */
    public String getDescription() {
        return this.description;
    }

    /**
     * Set the resource that this bean definition came from
     * (for the purpose of showing context in case of errors).
     *
     * 设置资源
     */
    public void setResource(Resource resource) {
        this.resource = resource;
    }

    /**
     * Return the resource that this bean definition came from.
     *
     * 获取资源
     */
    public Resource getResource() {
        return this.resource;
    }

    /**
     * Set a description of the resource that this bean definition
     * came from (for the purpose of showing context in case of errors).
     *
     * 设置资源描述
     */
    public void setResourceDescription(String resourceDescription) {
        this.resource = new DescriptiveResource(resourceDescription);
    }

    /**
     * 获取资源描述
     *
     * @return
     */
    public String getResourceDescription() {
        return (this.resource != null ? this.resource.getDescription() : null);
    }

    /**
     * Set the originating (e.g. decorated) BeanDefinition, if any.
     *
     * 设置原始的Bean定义
     */
    public void setOriginatingBeanDefinition(BeanDefinition originatingBd) {
        this.resource = new BeanDefinitionResource(originatingBd);
    }

    /**
     * 获取原始的Bean定义
     *
     * @return
     */
    public BeanDefinition getOriginatingBeanDefinition() {
        return (this.resource instanceof BeanDefinitionResource ?
                ((BeanDefinitionResource) this.resource).getBeanDefinition() : null);
    }

    /**
     * Validate this bean definition.
     *
     * 验证
     *
     * @throws BeanDefinitionValidationException in case of validation failure
     */
    public void validate() throws BeanDefinitionValidationException {
        if (!getMethodOverrides().isEmpty() && getFactoryMethodName() != null) {
            throw new BeanDefinitionValidationException(
                    "Cannot combine static factory method with method overrides: " +
                    "the static factory method must create the instance");
        }

        if (hasBeanClass()) {
            prepareMethodOverrides();
        }
    }

    /**
     * Validate and prepare the method overrides defined for this bean.
     * Checks for existence of a method with the specified name.
     *
     * 预处理所有的方法重写
     *
     * @throws BeanDefinitionValidationException in case of validation failure
     */
    public void prepareMethodOverrides() throws BeanDefinitionValidationException {
        // Check that lookup methods exists.
        MethodOverrides methodOverrides = getMethodOverrides();
        if (!methodOverrides.isEmpty()) {
            for (MethodOverride mo : methodOverrides.getOverrides()) {
                prepareMethodOverride(mo);
            }
        }
    }

    /**
     * Validate and prepare the given method override.
     * Checks for existence of a method with the specified name,
     * marking it as not overloaded if none found.
     *
     * 预特定的处理方法重写
     *
     * @param mo the MethodOverride object to validate
     * @throws BeanDefinitionValidationException in case of validation failure
     */
    protected void prepareMethodOverride(MethodOverride mo) throws BeanDefinitionValidationException {
        int count = ClassUtils.getMethodCountForName(getBeanClass(), mo.getMethodName());
        if (count == 0) {
            throw new BeanDefinitionValidationException(
                    "Invalid method override: no method with name '" + mo.getMethodName() +
                    "' on class [" + getBeanClassName() + "]");
        } else if (count == 1) {
            // Mark override as not overloaded, to avoid the overhead of arg type checking.
            mo.setOverloaded(false);
        }
    }


    /**
     * Public declaration of Object's {@code clone()} method.
     * Delegates to {@link #cloneBeanDefinition()}.
     *
     * 克隆方法
     *
     * @see Object#clone()
     */
    @Override
    public Object clone() {
        return cloneBeanDefinition();
    }

    /**
     * Clone this bean definition.
     * To be implemented by concrete subclasses.
     *
     * 克隆的具体实现
     * 交给子类去完成
     *
     * @return the cloned bean definition object
     */
    public abstract AbstractBeanDefinition cloneBeanDefinition();


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
        if (!(other instanceof AbstractBeanDefinition)) {
            return false;
        }

        AbstractBeanDefinition that = (AbstractBeanDefinition) other;

        if (!ObjectUtils.nullSafeEquals(getBeanClassName(), that.getBeanClassName())) return false;
        if (!ObjectUtils.nullSafeEquals(this.scope, that.scope)) return false;
        if (this.abstractFlag != that.abstractFlag) return false;
        if (this.lazyInit != that.lazyInit) return false;

        if (this.autowireMode != that.autowireMode) return false;
        if (this.dependencyCheck != that.dependencyCheck) return false;
        if (!Arrays.equals(this.dependsOn, that.dependsOn)) return false;
        if (this.autowireCandidate != that.autowireCandidate) return false;
        if (!ObjectUtils.nullSafeEquals(this.qualifiers, that.qualifiers)) return false;
        if (this.primary != that.primary) return false;

        if (this.nonPublicAccessAllowed != that.nonPublicAccessAllowed) return false;
        if (this.lenientConstructorResolution != that.lenientConstructorResolution) return false;
        if (!ObjectUtils.nullSafeEquals(this.constructorArgumentValues, that.constructorArgumentValues)) return false;
        if (!ObjectUtils.nullSafeEquals(this.propertyValues, that.propertyValues)) return false;
        if (!ObjectUtils.nullSafeEquals(this.methodOverrides, that.methodOverrides)) return false;

        if (!ObjectUtils.nullSafeEquals(this.factoryBeanName, that.factoryBeanName)) return false;
        if (!ObjectUtils.nullSafeEquals(this.factoryMethodName, that.factoryMethodName)) return false;
        if (!ObjectUtils.nullSafeEquals(this.initMethodName, that.initMethodName)) return false;
        if (this.enforceInitMethod != that.enforceInitMethod) return false;
        if (!ObjectUtils.nullSafeEquals(this.destroyMethodName, that.destroyMethodName)) return false;
        if (this.enforceDestroyMethod != that.enforceDestroyMethod) return false;

        if (this.synthetic != that.synthetic) return false;
        if (this.role != that.role) return false;

        return super.equals(other);
    }

    /**
     * 哈希值
     *
     * @return
     */
    @Override
    public int hashCode() {
        int hashCode = ObjectUtils.nullSafeHashCode(getBeanClassName());
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.scope);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.constructorArgumentValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.propertyValues);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryBeanName);
        hashCode = 29 * hashCode + ObjectUtils.nullSafeHashCode(this.factoryMethodName);
        hashCode = 29 * hashCode + super.hashCode();
        return hashCode;
    }

    /**
     * 转换为字符串
     *
     * @return
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("class [");
        sb.append(getBeanClassName()).append("]");
        sb.append("; scope=").append(this.scope);
        sb.append("; abstract=").append(this.abstractFlag);
        sb.append("; lazyInit=").append(this.lazyInit);
        sb.append("; autowireMode=").append(this.autowireMode);
        sb.append("; dependencyCheck=").append(this.dependencyCheck);
        sb.append("; autowireCandidate=").append(this.autowireCandidate);
        sb.append("; primary=").append(this.primary);
        sb.append("; factoryBeanName=").append(this.factoryBeanName);
        sb.append("; factoryMethodName=").append(this.factoryMethodName);
        sb.append("; initMethodName=").append(this.initMethodName);
        sb.append("; destroyMethodName=").append(this.destroyMethodName);
        if (this.resource != null) {
            sb.append("; defined in ").append(this.resource.getDescription());
        }
        return sb.toString();
    }

}

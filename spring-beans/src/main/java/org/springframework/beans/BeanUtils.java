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

package org.springframework.beans;

import java.beans.PropertyDescriptor;
import java.beans.PropertyEditor;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.WeakHashMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.core.MethodParameter;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

/**
 * Static convenience methods for JavaBeans: for instantiating beans,
 * checking bean property types, copying bean properties, etc.
 *
 * <p>Mainly for use within the framework, but to some degree also
 * useful for application classes.
 *
 * 对于JavaBean操作的一些遍历的方法:
 * 1.实例化Bean
 * 2.检测Bean的属性类型
 * 3.拷贝Bean的属性
 *
 * 主要是框架本身使用，在一定程度上对应用也很有用
 *
 * @author Rod Johnson
 * @author Juergen Hoeller
 * @author Rob Harrop
 * @author Sam Brannen
 */
public abstract class BeanUtils {

    /**
     * 日志
     */
    private static final Log logger = LogFactory.getLog(BeanUtils.class);

    /**
     * Effectively using a WeakHashMap as a Set
     */
    private static final Map<Class<?>, Boolean> unknownEditorTypes =
            Collections.synchronizedMap(new WeakHashMap<Class<?>, Boolean>());


    /**
     * Convenience method to instantiate a class using its no-arg constructor.
     * As this method doesn't try to load classes by name, it should avoid
     * class-loading issues.
     *
     * 实例化一个Bean
     * 这里利用了它的无参的构造方法
     * 它不会根据名称去加载类
     * 因此应该避免类加载的问题
     *
     * @param clazz class to instantiate
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
    public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new BeanInstantiationException(clazz, "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
        }
    }

    /**
     * Instantiate a class using its no-arg constructor.
     * As this method doesn't try to load classes by name, it should avoid
     * class-loading issues.
     * <p>Note that this method tries to set the constructor accessible
     * if given a non-accessible (that is, non-public) constructor.
     *
     * 利用一个类的无参构造方法来实例化
     * 它会调用私有的构造方法
     *
     * @param clazz class to instantiate
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
    public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {
        Assert.notNull(clazz, "Class must not be null");
        if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return instantiateClass(clazz.getDeclaredConstructor());
        } catch (NoSuchMethodException ex) {
            throw new BeanInstantiationException(clazz, "No default constructor found", ex);
        }
    }

    /**
     * Instantiate a class using its no-arg constructor and return the new instance
     * as the the specified assignable type.
     * <p>Useful in cases where
     * the type of the class to instantiate (clazz) is not available, but the type
     * desired (assignableTo) is known.
     * <p>As this method doesn't try to load classes by name, it should avoid
     * class-loading issues.
     * <p>Note that this method tries to set the constructor accessible
     * if given a non-accessible (that is, non-public) constructor.
     *
     * 利用一个类的无参构造方法来初始化一个类
     * 并且转型为特定的类型
     * 它也会调用私有的构造方法
     *
     * @param clazz class to instantiate
     * @param assignableTo type that clazz must be assignableTo
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
    @SuppressWarnings("unchecked")
    public static <T> T instantiateClass(Class<?> clazz, Class<T> assignableTo) throws BeanInstantiationException {
        Assert.isAssignable(assignableTo, clazz);
        return (T) instantiateClass(clazz);
    }

    /**
     * Convenience method to instantiate a class using the given constructor.
     * As this method doesn't try to load classes by name, it should avoid
     * class-loading issues.
     * <p>Note that this method tries to set the constructor accessible
     * if given a non-accessible (that is, non-public) constructor.
     *
     * 根据构造方法和参数列表来实例化一个类
     * 可以调用类的私有方法
     *
     * @param ctor the constructor to instantiate
     * @param args the constructor arguments to apply
     * @return the new instance
     * @throws BeanInstantiationException if the bean cannot be instantiated
     */
    public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws BeanInstantiationException {
        Assert.notNull(ctor, "Constructor must not be null");
        try {
            ReflectionUtils.makeAccessible(ctor);
            return ctor.newInstance(args);
        } catch (InstantiationException ex) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Is the constructor accessible?", ex);
        } catch (IllegalArgumentException ex) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Illegal arguments for constructor", ex);
        } catch (InvocationTargetException ex) {
            throw new BeanInstantiationException(ctor.getDeclaringClass(),
                    "Constructor threw exception", ex.getTargetException());
        }
    }

    /**
     * Find a method with the given method name and the given parameter types,
     * declared on the given class or one of its superclasses. Prefers public methods,
     * but will return a protected, package access, or private method too.
     * <p>Checks {@code Class.getMethod} first, falling back to
     * {@code findDeclaredMethod}. This allows to find public methods
     * without issues even in environments with restricted Java security settings.
     *
     * 根据给定的方法名和参数类型来获取一个方法
     * 优先选择public类型的方法
     * 但是也会返回protected、package access、private方法
     *
     * 优先使用getMethod方法
     * 把findDeclaredMethod作为一个降级方法使用
     * 这里是有可能返回父类的方法的
     *
     * @param clazz the class to check
     * @param methodName the name of the method to find
     * @param paramTypes the parameter types of the method to find
     * @return the Method object, or {@code null} if not found
     * @see Class#getMethod
     * @see #findDeclaredMethod
     */
    public static Method findMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            return findDeclaredMethod(clazz, methodName, paramTypes);
        }
    }

    /**
     * Find a method with the given method name and the given parameter types,
     * declared on the given class or one of its superclasses. Will return a public,
     * protected, package access, or private method.
     * <p>Checks {@code Class.getDeclaredMethod}, cascading upwards to all superclasses.
     *
     * 根据方法名和参数类型返回一个方法
     * 优先选择本类的方法，但是也有可能返回父类的方法
     *
     * @param clazz the class to check
     * @param methodName the name of the method to find
     * @param paramTypes the parameter types of the method to find
     * @return the Method object, or {@code null} if not found
     * @see Class#getDeclaredMethod
     */
    public static Method findDeclaredMethod(Class<?> clazz, String methodName, Class<?>... paramTypes) {
        try {
            return clazz.getDeclaredMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            if (clazz.getSuperclass() != null) {
                return findDeclaredMethod(clazz.getSuperclass(), methodName, paramTypes);
            }
            return null;
        }
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none),
     * declared on the given class or one of its superclasses. Prefers public methods,
     * but will return a protected, package access, or private method too.
     * <p>Checks {@code Class.getMethods} first, falling back to
     * {@code findDeclaredMethodWithMinimalParameters}. This allows for finding public
     * methods without issues even in environments with restricted Java security settings.
     *
     * 根据方法名获取参数最少的那个方法
     *
     * @param clazz the class to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     * could not be resolved to a unique method with minimal parameters
     * @see Class#getMethods
     * @see #findDeclaredMethodWithMinimalParameters
     */
    public static Method findMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = findMethodWithMinimalParameters(clazz.getMethods(), methodName);
        if (targetMethod == null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz, methodName);
        }
        return targetMethod;
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none),
     * declared on the given class or one of its superclasses. Will return a public,
     * protected, package access, or private method.
     * <p>Checks {@code Class.getDeclaredMethods}, cascading upwards to all superclasses.
     *
     * 根据方法名来获取参数最少的那个方法
     *
     * @param clazz the class to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     * could not be resolved to a unique method with minimal parameters
     * @see Class#getDeclaredMethods
     */
    public static Method findDeclaredMethodWithMinimalParameters(Class<?> clazz, String methodName)
            throws IllegalArgumentException {

        Method targetMethod = findMethodWithMinimalParameters(clazz.getDeclaredMethods(), methodName);
        if (targetMethod == null && clazz.getSuperclass() != null) {
            targetMethod = findDeclaredMethodWithMinimalParameters(clazz.getSuperclass(), methodName);
        }
        return targetMethod;
    }

    /**
     * Find a method with the given method name and minimal parameters (best case: none)
     * in the given list of methods.
     *
     * 根据名称获取参数最少的方法
     *
     * @param methods the methods to check
     * @param methodName the name of the method to find
     * @return the Method object, or {@code null} if not found
     * @throws IllegalArgumentException if methods of the given name were found but
     * could not be resolved to a unique method with minimal parameters
     */
    public static Method findMethodWithMinimalParameters(Method[] methods, String methodName)
            throws IllegalArgumentException {
        // 目标方法
        Method targetMethod = null;
        int numMethodsFoundWithCurrentMinimumArgs = 0;
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                int numParams = method.getParameterTypes().length;
                if (targetMethod == null || numParams < targetMethod.getParameterTypes().length) {
                    targetMethod = method;
                    numMethodsFoundWithCurrentMinimumArgs = 1;
                } else {
                    if (targetMethod.getParameterTypes().length == numParams) {
                        // Additional candidate with same length
                        numMethodsFoundWithCurrentMinimumArgs++;
                    }
                }
            }
        }
        if (numMethodsFoundWithCurrentMinimumArgs > 1) {
            throw new IllegalArgumentException("Cannot resolve method '" + methodName +
                    "' to a unique method. Attempted to resolve to overloaded method with " +
                    "the least number of parameters, but there were " +
                    numMethodsFoundWithCurrentMinimumArgs + " candidates.");
        }
        return targetMethod;
    }

    /**
     * Parse a method signature in the form {@code methodName[([arg_list])]},
     * where {@code arg_list} is an optional, comma-separated list of fully-qualified
     * type names, and attempts to resolve that signature against the supplied {@code Class}.
     * <p>When not supplying an argument list ({@code methodName}) the method whose name
     * matches and has the least number of parameters will be returned. When supplying an
     * argument type list, only the method whose name and argument types match will be returned.
     * <p>Note then that {@code methodName} and {@code methodName()} are <strong>not</strong>
     * resolved in the same way. The signature {@code methodName} means the method called
     * {@code methodName} with the least number of arguments, whereas {@code methodName()}
     * means the method called {@code methodName} with exactly 0 arguments.
     * <p>If no method can be found, then {@code null} is returned.
     *
     * 通过解析签名来得到方法的实例
     *
     * @param signature the method signature as String representation
     * @param clazz the class to resolve the method signature against
     * @return the resolved Method
     * @see #findMethod
     * @see #findMethodWithMinimalParameters
     */
    public static Method resolveSignature(String signature, Class<?> clazz) {
        Assert.hasText(signature, "'signature' must not be empty");
        Assert.notNull(clazz, "Class must not be null");
        int firstParen = signature.indexOf("(");
        int lastParen = signature.indexOf(")");
        if (firstParen > -1 && lastParen == -1) {
            // 只有左小括号没有右小括号
            throw new IllegalArgumentException("Invalid method signature '" + signature +
                    "': expected closing ')' for args list");
        } else if (lastParen > -1 && firstParen == -1) {
            // 只有右小括号没有左小括号
            throw new IllegalArgumentException("Invalid method signature '" + signature +
                    "': expected opening '(' for args list");
        } else if (firstParen == -1 && lastParen == -1) {
            // 左右小括号都没有
            return findMethodWithMinimalParameters(clazz, signature);
        } else {
            // 方法名
            String methodName = signature.substring(0, firstParen);
            // 参数类型名,字符串形式
            String[] parameterTypeNames =
                    StringUtils.commaDelimitedListToStringArray(signature.substring(firstParen + 1, lastParen));
            Class<?>[] parameterTypes = new Class<?>[parameterTypeNames.length];
            for (int i = 0; i < parameterTypeNames.length; i++) {
                String parameterTypeName = parameterTypeNames[i].trim();
                try {
                    // 加载具体的类
                    parameterTypes[i] = ClassUtils.forName(parameterTypeName, clazz.getClassLoader());
                } catch (Throwable ex) {
                    throw new IllegalArgumentException("Invalid method signature: unable to resolve type [" +
                            parameterTypeName + "] for argument " + i + ". Root cause: " + ex);
                }
            }
            return findMethod(clazz, methodName, parameterTypes);
        }
    }


    /**
     * Retrieve the JavaBeans {@code PropertyDescriptor}s of a given class.
     *
     * 获取一个类的所有的属性描述符
     *
     * @param clazz the Class to retrieve the PropertyDescriptors for
     * @return an array of {@code PropertyDescriptors} for the given class
     * @throws BeansException if PropertyDescriptor look fails
     */
    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
        CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
        return cr.getPropertyDescriptors();
    }

    /**
     * Retrieve the JavaBeans {@code PropertyDescriptors} for the given property.
     *
     * 获取一个类的某个名称的所有的属性描述符
     *
     * @param clazz the Class to retrieve the PropertyDescriptor for
     * @param propertyName the name of the property
     * @return the corresponding PropertyDescriptor, or {@code null} if none
     * @throws BeansException if PropertyDescriptor lookup fails
     */
    public static PropertyDescriptor getPropertyDescriptor(Class<?> clazz, String propertyName)
            throws BeansException {

        CachedIntrospectionResults cr = CachedIntrospectionResults.forClass(clazz);
        return cr.getPropertyDescriptor(propertyName);
    }

    /**
     * Find a JavaBeans {@code PropertyDescriptor} for the given method,
     * with the method either being the read method or the write method for
     * that bean property.
     *
     * 根据方法获取属性描述符
     * 这个方法可以是读方法也可以是写方法
     *
     * @param method the method to find a corresponding PropertyDescriptor for,
     * introspecting its declaring class
     * @return the corresponding PropertyDescriptor, or {@code null} if none
     * @throws BeansException if PropertyDescriptor lookup fails
     */
    public static PropertyDescriptor findPropertyForMethod(Method method) throws BeansException {
        return findPropertyForMethod(method, method.getDeclaringClass());
    }

    /**
     * Find a JavaBeans {@code PropertyDescriptor} for the given method,
     * with the method either being the read method or the write method for
     * that bean property.
     *
     * 根据方法和类来获取属性描述符
     *
     * @param method the method to find a corresponding PropertyDescriptor for
     * @param clazz the (most specific) class to introspect for descriptors
     * @return the corresponding PropertyDescriptor, or {@code null} if none
     * @throws BeansException if PropertyDescriptor lookup fails
     * @since 3.2.13
     */
    public static PropertyDescriptor findPropertyForMethod(Method method, Class<?> clazz) throws BeansException {
        Assert.notNull(method, "Method must not be null");
        PropertyDescriptor[] pds = getPropertyDescriptors(clazz);
        for (PropertyDescriptor pd : pds) {
            if (method.equals(pd.getReadMethod()) || method.equals(pd.getWriteMethod())) {
                // 读方法或者写方法均可
                return pd;
            }
        }
        return null;
    }

    /**
     * Find a JavaBeans PropertyEditor following the 'Editor' suffix convention
     * (e.g. "mypackage.MyDomainClass" -> "mypackage.MyDomainClassEditor").
     * <p>Compatible to the standard JavaBeans convention as implemented by
     * {@link java.beans.PropertyEditorManager} but isolated from the latter's
     * registered default editors for primitive types.
     *
     * 找到对应的属性编辑器
     *
     * @param targetType the type to find an editor for
     * @return the corresponding editor, or {@code null} if none found
     */
    public static PropertyEditor findEditorByConvention(Class<?> targetType) {
        if (targetType == null || targetType.isArray() || unknownEditorTypes.containsKey(targetType)) {
            return null;
        }
        ClassLoader cl = targetType.getClassLoader();
        if (cl == null) {
            try {
                cl = ClassLoader.getSystemClassLoader();
                if (cl == null) {
                    return null;
                }
            } catch (Throwable ex) {
                // e.g. AccessControlException on Google App Engine
                if (logger.isDebugEnabled()) {
                    logger.debug("Could not access system ClassLoader: " + ex);
                }
                return null;
            }
        }
        String editorName = targetType.getName() + "Editor";
        try {
            Class<?> editorClass = cl.loadClass(editorName);
            if (!PropertyEditor.class.isAssignableFrom(editorClass)) {
                if (logger.isWarnEnabled()) {
                    logger.warn("Editor class [" + editorName +
                            "] does not implement [java.beans.PropertyEditor] interface");
                }
                unknownEditorTypes.put(targetType, Boolean.TRUE);
                return null;
            }
            return (PropertyEditor) instantiateClass(editorClass);
        } catch (ClassNotFoundException ex) {
            if (logger.isDebugEnabled()) {
                logger.debug("No property editor [" + editorName + "] found for type " +
                        targetType.getName() + " according to 'Editor' suffix convention");
            }
            unknownEditorTypes.put(targetType, Boolean.TRUE);
            return null;
        }
    }

    /**
     * Determine the bean property type for the given property from the
     * given classes/interfaces, if possible.
     *
     * 从一系列类中找到一个包含该属性名的属性的类型
     *
     * @param propertyName the name of the bean property
     * @param beanClasses the classes to check against
     * @return the property type, or {@code Object.class} as fallback
     */
    public static Class<?> findPropertyType(String propertyName, Class<?>... beanClasses) {
        if (beanClasses != null) {
            for (Class<?> beanClass : beanClasses) {
                PropertyDescriptor pd = getPropertyDescriptor(beanClass, propertyName);
                if (pd != null) {
                    return pd.getPropertyType();
                }
            }
        }
        return Object.class;
    }

    /**
     * Obtain a new MethodParameter object for the write method of the
     * specified property.
     *
     * 获取一个属性描述符的写方法
     *
     * @param pd the PropertyDescriptor for the property
     * @return a corresponding MethodParameter object
     */
    public static MethodParameter getWriteMethodParameter(PropertyDescriptor pd) {
        if (pd instanceof GenericTypeAwarePropertyDescriptor) {
            return new MethodParameter(((GenericTypeAwarePropertyDescriptor) pd).getWriteMethodParameter());
        } else {
            return new MethodParameter(pd.getWriteMethod(), 0);
        }
    }

    /**
     * Check if the given type represents a "simple" property:
     * a primitive, a String or other CharSequence, a Number, a Date,
     * a URI, a URL, a Locale, a Class, or a corresponding array.
     * <p>Used to determine properties to check for a "simple" dependency-check.
     *
     * 是否是简单的属性
     *
     * @param clazz the type to check
     * @return whether the given type represents a "simple" property
     * @see org.springframework.beans.factory.support.RootBeanDefinition#DEPENDENCY_CHECK_SIMPLE
     * @see org.springframework.beans.factory.support.AbstractAutowireCapableBeanFactory#checkDependencies
     */
    public static boolean isSimpleProperty(Class<?> clazz) {
        Assert.notNull(clazz, "Class must not be null");
        return isSimpleValueType(clazz) || (clazz.isArray() && isSimpleValueType(clazz.getComponentType()));
    }

    /**
     * Check if the given type represents a "simple" value type:
     * a primitive, a String or other CharSequence, a Number, a Date,
     * a URI, a URL, a Locale or a Class.
     *
     * 是否是简单类型:
     * 基础类型
     * 字符串
     * 字符序列
     * 数字
     * 日期
     * URI
     * URL
     * Locale
     * Class
     *
     * @param clazz the type to check
     * @return whether the given type represents a "simple" value type
     */
    public static boolean isSimpleValueType(Class<?> clazz) {
        return ClassUtils.isPrimitiveOrWrapper(clazz) || clazz.isEnum() ||
                CharSequence.class.isAssignableFrom(clazz) ||
                Number.class.isAssignableFrom(clazz) ||
                Date.class.isAssignableFrom(clazz) ||
                clazz.equals(URI.class) || clazz.equals(URL.class) ||
                clazz.equals(Locale.class) || clazz.equals(Class.class);
    }


    /**
     * Copy the property values of the given source bean into the target bean.
     * <p>Note: The source and target classes do not have to match or even be derived
     * from each other, as long as the properties match. Any bean properties that the
     * source bean exposes but the target bean does not will silently be ignored.
     * <p>This is just a convenience method. For more complex transfer needs,
     * consider using a full BeanWrapper.
     *
     * 拷贝属性
     *
     * @param source the source bean
     * @param target the target bean
     * @throws BeansException if the copying failed
     * @see BeanWrapper
     */
    public static void copyProperties(Object source, Object target) throws BeansException {
        copyProperties(source, target, null, (String[]) null);
    }

    /**
     * Copy the property values of the given source bean into the given target bean,
     * only setting properties defined in the given "editable" class (or interface).
     * <p>Note: The source and target classes do not have to match or even be derived
     * from each other, as long as the properties match. Any bean properties that the
     * source bean exposes but the target bean does not will silently be ignored.
     * <p>This is just a convenience method. For more complex transfer needs,
     * consider using a full BeanWrapper.
     *
     * 拷贝属性
     *
     * @param source the source bean
     * @param target the target bean
     * @param editable the class (or interface) to restrict property setting to
     * @throws BeansException if the copying failed
     * @see BeanWrapper
     */
    public static void copyProperties(Object source, Object target, Class<?> editable) throws BeansException {
        copyProperties(source, target, editable, (String[]) null);
    }

    /**
     * Copy the property values of the given source bean into the given target bean,
     * ignoring the given "ignoreProperties".
     * <p>Note: The source and target classes do not have to match or even be derived
     * from each other, as long as the properties match. Any bean properties that the
     * source bean exposes but the target bean does not will silently be ignored.
     * <p>This is just a convenience method. For more complex transfer needs,
     * consider using a full BeanWrapper.
     *
     * 拷贝属性
     *
     * @param source the source bean
     * @param target the target bean
     * @param ignoreProperties array of property names to ignore
     * @throws BeansException if the copying failed
     * @see BeanWrapper
     */
    public static void copyProperties(Object source, Object target, String... ignoreProperties) throws BeansException {
        copyProperties(source, target, null, ignoreProperties);
    }

    /**
     * Copy the property values of the given source bean into the given target bean.
     * <p>Note: The source and target classes do not have to match or even be derived
     * from each other, as long as the properties match. Any bean properties that the
     * source bean exposes but the target bean does not will silently be ignored.
     *
     * 拷贝属性
     * 
     * @param source the source bean
     * @param target the target bean
     * @param editable the class (or interface) to restrict property setting to
     * @param ignoreProperties array of property names to ignore
     * @throws BeansException if the copying failed
     * @see BeanWrapper
     */
    private static void copyProperties(Object source, Object target, Class<?> editable, String... ignoreProperties)
            throws BeansException {

        Assert.notNull(source, "Source must not be null");
        Assert.notNull(target, "Target must not be null");

        Class<?> actualEditable = target.getClass();
        if (editable != null) {
            // 这里主要是为了确保类型相同
            if (!editable.isInstance(target)) {
                throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
                        "] not assignable to Editable class [" + editable.getName() + "]");
            }
            actualEditable = editable;
        }

        // 获取所有的属性描述符
        PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
        List<String> ignoreList = (ignoreProperties != null ? Arrays.asList(ignoreProperties) : null);

        for (PropertyDescriptor targetPd : targetPds) {
            // 获取写方法
            Method writeMethod = targetPd.getWriteMethod();
            if (writeMethod != null && (ignoreList == null || !ignoreList.contains(targetPd.getName()))) {
                // 需要确保目标属性是可写的
                // 然后获取原始类型的属性描述符
                PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
                if (sourcePd != null) {
                    Method readMethod = sourcePd.getReadMethod();
                    if (readMethod != null &&
                            ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType())) {
                        try {
                            // 设置读方法为可访问的
                            if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
                                readMethod.setAccessible(true);
                            }
                            Object value = readMethod.invoke(source);
                            // 设置写方法是可访问的
                            if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
                                writeMethod.setAccessible(true);
                            }
                            writeMethod.invoke(target, value);
                        } catch (Throwable ex) {
                            throw new FatalBeanException(
                                    "Could not copy property '" + targetPd.getName() + "' from source to target", ex);
                        }
                    }
                }
            }
        }
    }

}

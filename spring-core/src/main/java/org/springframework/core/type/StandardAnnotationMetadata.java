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

package org.springframework.core.type;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.annotation.AnnotationUtils;

/**
 * {@link AnnotationMetadata} implementation that uses standard reflection
 * to introspect a given {@link Class}.
 *
 * 标准的注解元信息类
 *
 * @author Juergen Hoeller
 * @author Mark Fisher
 * @author Chris Beams
 * @since 2.5
 */
public class StandardAnnotationMetadata extends StandardClassMetadata implements AnnotationMetadata {

    /**
     * 是否嵌套
     */
    private final boolean nestedAnnotationsAsMap;


    /**
     * Create a new {@code StandardAnnotationMetadata} wrapper for the given Class.
     *
     * 构造方法
     *
     * @param introspectedClass the Class to introspect
     * @see #StandardAnnotationMetadata(Class, boolean)
     */
    public StandardAnnotationMetadata(Class<?> introspectedClass) {
        this(introspectedClass, false);
    }

    /**
     * Create a new {@link StandardAnnotationMetadata} wrapper for the given Class,
     * providing the option to return any nested annotations or annotation arrays in the
     * form of {@link AnnotationAttributes} instead of actual {@link Annotation} instances.
     *
     * 构造方法
     *
     * @param introspectedClass the Class to instrospect
     * @param nestedAnnotationsAsMap return nested annotations and annotation arrays as
     * {@link AnnotationAttributes} for compatibility with ASM-based
     * {@link AnnotationMetadata} implementations
     * @since 3.1.1
     */
    public StandardAnnotationMetadata(Class<?> introspectedClass, boolean nestedAnnotationsAsMap) {
        super(introspectedClass);
        this.nestedAnnotationsAsMap = nestedAnnotationsAsMap;
    }


    /**
     * 获取注解类型
     *
     * @return
     */
    public Set<String> getAnnotationTypes() {
        Set<String> types = new LinkedHashSet<String>();
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (Annotation ann : anns) {
            types.add(ann.annotationType().getName());
        }
        return types;
    }

    /**
     * 获取某个注解的注解信息
     * 比如类A被注解@Config注解，而@Config又有@Global注解，那么这里可以通过获取@Config上面的所有的注解
     *
     * @param annotationType the meta-annotation type to look for
     * @return
     */
    public Set<String> getMetaAnnotationTypes(String annotationType) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (Annotation ann : anns) {
            // 第一层的注解等于参数值
            if (ann.annotationType().getName().equals(annotationType)) {
                // 收集第二层的注解信息
                Set<String> types = new LinkedHashSet<String>();
                Annotation[] metaAnns = ann.annotationType().getAnnotations();
                for (Annotation metaAnn : metaAnns) {
                    types.add(metaAnn.annotationType().getName());
                    for (Annotation metaMetaAnn : metaAnn.annotationType().getAnnotations()) {
                        types.add(metaMetaAnn.annotationType().getName());
                    }
                }
                return types;
            }
        }
        return null;
    }

    /**
     * 判断是否有某个注解
     * 这里要求注解的全限定名
     *
     * @param annotationType the annotation type to look for
     * @return
     */
    public boolean hasAnnotation(String annotationType) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (Annotation ann : anns) {
            if (ann.annotationType().getName().equals(annotationType)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 判断某个注解上是否有元注解
     *
     * @param annotationType
     * @return
     */
    public boolean hasMetaAnnotation(String annotationType) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (Annotation ann : anns) {
            Annotation[] metaAnns = ann.annotationType().getAnnotations();
            for (Annotation metaAnn : metaAnns) {
                if (metaAnn.annotationType().getName().equals(annotationType)) {
                    return true;
                }
                for (Annotation metaMetaAnn : metaAnn.annotationType().getAnnotations()) {
                    if (metaMetaAnn.annotationType().getName().equals(annotationType)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 是否被某个注解修饰
     *
     * @param annotationType the annotation type to look for
     * @return
     */
    public boolean isAnnotated(String annotationType) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (Annotation ann : anns) {
            if (ann.annotationType().getName().equals(annotationType)) {
                return true;
            }
            for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
                if (metaAnn.annotationType().getName().equals(annotationType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 获取注解的属性
     *
     * @param annotationType the annotation type to look for
     * @return
     */
    public Map<String, Object> getAnnotationAttributes(String annotationType) {
        return this.getAnnotationAttributes(annotationType, false);
    }

    /**
     * 获取注解的属性
     *
     * @param annotationType the annotation type to look for
     * @param classValuesAsString whether to convert class references to String
     * class names for exposure as values in the returned Map, instead of Class
     * references which might potentially have to be loaded first
     * @return
     */
    public Map<String, Object> getAnnotationAttributes(String annotationType, boolean classValuesAsString) {
        Annotation[] anns = getIntrospectedClass().getAnnotations();
        for (Annotation ann : anns) {
            if (ann.annotationType().getName().equals(annotationType)) {
                return AnnotationUtils.getAnnotationAttributes(
                        ann, classValuesAsString, this.nestedAnnotationsAsMap);
            }
        }
        for (Annotation ann : anns) {
            for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
                if (metaAnn.annotationType().getName().equals(annotationType)) {
                    return AnnotationUtils.getAnnotationAttributes(
                            metaAnn, classValuesAsString, this.nestedAnnotationsAsMap);
                }
            }
        }
        return null;
    }

    /**
     * 是否有带有某个注解的方法
     *
     * @param annotationType
     * @return
     */
    public boolean hasAnnotatedMethods(String annotationType) {
        Method[] methods = getIntrospectedClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!method.isBridge()) {
                for (Annotation ann : method.getAnnotations()) {
                    if (ann.annotationType().getName().equals(annotationType)) {
                        return true;
                    } else {
                        for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
                            if (metaAnn.annotationType().getName().equals(annotationType)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * 获取被某个注解修饰的所有方法列表
     *
     * @param annotationType the annotation type to look for
     * @return
     */
    public Set<MethodMetadata> getAnnotatedMethods(String annotationType) {
        Set<MethodMetadata> annotatedMethods = new LinkedHashSet<MethodMetadata>();
        Method[] methods = getIntrospectedClass().getDeclaredMethods();
        for (Method method : methods) {
            if (!method.isBridge()) {
                for (Annotation ann : method.getAnnotations()) {
                    if (ann.annotationType().getName().equals(annotationType)) {
                        annotatedMethods.add(new StandardMethodMetadata(method, this.nestedAnnotationsAsMap));
                        break;
                    } else {
                        for (Annotation metaAnn : ann.annotationType().getAnnotations()) {
                            if (metaAnn.annotationType().getName().equals(annotationType)) {
                                annotatedMethods.add(new StandardMethodMetadata(method, this.nestedAnnotationsAsMap));
                                break;
                            }
                        }
                    }
                }
            }
        }
        return annotatedMethods;
    }

}

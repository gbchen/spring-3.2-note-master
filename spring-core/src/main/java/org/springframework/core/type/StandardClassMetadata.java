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

package org.springframework.core.type;

import java.lang.reflect.Modifier;
import java.util.LinkedHashSet;

import org.springframework.util.Assert;

/**
 * {@link ClassMetadata} implementation that uses standard reflection
 * to introspect a given {@code Class}.
 *
 * 标准的类的元信息
 *
 * @author Juergen Hoeller
 * @since 2.5
 */
public class StandardClassMetadata implements ClassMetadata {

    /**
     * 内省类
     */
    private final Class introspectedClass;


    /**
     * Create a new StandardClassMetadata wrapper for the given Class.
     *
     * 获取一个给定类的标准的元信息类
     *
     * @param introspectedClass the Class to introspect
     */
    public StandardClassMetadata(Class introspectedClass) {
        Assert.notNull(introspectedClass, "Class must not be null");
        this.introspectedClass = introspectedClass;
    }

    /**
     * Return the underlying Class.
     *
     * 获取内省类
     */
    public final Class getIntrospectedClass() {
        return this.introspectedClass;
    }


    /**
     * 获取类名称
     *
     * @return
     */
    public String getClassName() {
        return this.introspectedClass.getName();
    }

    /**
     * 是否是接口
     *
     * @return
     */
    public boolean isInterface() {
        return this.introspectedClass.isInterface();
    }

    /**
     * 是否是抽象类
     *
     * @return
     */
    public boolean isAbstract() {
        return Modifier.isAbstract(this.introspectedClass.getModifiers());
    }

    /**
     * 是否是具体的
     * 即它不是接口，也不是抽象类
     *
     * @return
     */
    public boolean isConcrete() {
        return !(isInterface() || isAbstract());
    }

    /**
     * 是否被final修饰符修饰
     *
     * @return
     */
    public boolean isFinal() {
        return Modifier.isFinal(this.introspectedClass.getModifiers());
    }

    /**
     * 是否是独立的类
     * 如果一个类没有外部类，那么它是独立的
     * 如果一个类有外部类，但是它是静态的内部类，它也是独立的
     *
     * @return
     */
    public boolean isIndependent() {
        return (!hasEnclosingClass() ||
                (this.introspectedClass.getDeclaringClass() != null &&
                        Modifier.isStatic(this.introspectedClass.getModifiers())));
    }

    /**
     * 是否有外部类
     *
     * @return
     */
    public boolean hasEnclosingClass() {
        return (this.introspectedClass.getEnclosingClass() != null);
    }

    /**
     * 获取外部类的名称
     *
     * @return
     */
    public String getEnclosingClassName() {
        Class enclosingClass = this.introspectedClass.getEnclosingClass();
        return (enclosingClass != null ? enclosingClass.getName() : null);
    }

    /**
     * 是否有父类
     *
     * @return
     */
    public boolean hasSuperClass() {
        return (this.introspectedClass.getSuperclass() != null);
    }

    /**
     * 获取父类的名称
     *
     * @return
     */
    public String getSuperClassName() {
        Class superClass = this.introspectedClass.getSuperclass();
        return (superClass != null ? superClass.getName() : null);
    }

    /**
     * 获取所实现的接口名称
     *
     * @return
     */
    public String[] getInterfaceNames() {
        Class[] ifcs = this.introspectedClass.getInterfaces();
        String[] ifcNames = new String[ifcs.length];
        for (int i = 0; i < ifcs.length; i++) {
            ifcNames[i] = ifcs[i].getName();
        }
        return ifcNames;
    }

    /**
     * 获取属性的类名称
     *
     * @return
     */
    public String[] getMemberClassNames() {
        LinkedHashSet<String> memberClassNames = new LinkedHashSet<String>();
        for (Class<?> nestedClass : this.introspectedClass.getDeclaredClasses()) {
            memberClassNames.add(nestedClass.getName());
        }
        return memberClassNames.toArray(new String[memberClassNames.size()]);
    }

}

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

package org.springframework.beans.propertyeditors;

import java.beans.PropertyEditorSupport;

import org.springframework.util.ClassUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Property editor for an array of {@link Class Classes}, to enable
 * the direct population of a {@code Class[]} property without having to
 * use a {@code String} class name property as bridge.
 *
 * <p>Also supports "java.lang.String[]"-style array class names, in contrast
 * to the standard {@link Class#forName(String)} method.
 *
 * 类数组编辑器
 *
 * @author Rob Harrop
 * @author Juergen Hoeller
 * @since 2.0
 */
public class ClassArrayEditor extends PropertyEditorSupport {

    /**
     * 类加载器
     */
    private final ClassLoader classLoader;


    /**
     * Create a default {@code ClassEditor}, using the thread
     * context {@code ClassLoader}.
     *
     * 构造方法
     */
    public ClassArrayEditor() {
        this(null);
    }

    /**
     * Create a default {@code ClassArrayEditor}, using the given
     * {@code ClassLoader}.
     *
     * 构造方法
     *
     * @param classLoader the {@code ClassLoader} to use
     * (or pass {@code null} for the thread context {@code ClassLoader})
     */
    public ClassArrayEditor(ClassLoader classLoader) {
        this.classLoader = (classLoader != null ? classLoader : ClassUtils.getDefaultClassLoader());
    }


    /**
     * 设置为文本
     *
     * @param text
     * @throws IllegalArgumentException
     */
    @Override
    public void setAsText(String text) throws IllegalArgumentException {
        if (StringUtils.hasText(text)) {
            // 用逗号分隔
            String[] classNames = StringUtils.commaDelimitedListToStringArray(text);
            Class[] classes = new Class[classNames.length];
            for (int i = 0; i < classNames.length; i++) {
                String className = classNames[i].trim();
                classes[i] = ClassUtils.resolveClassName(className, this.classLoader);
            }
            setValue(classes);
        } else {
            setValue(null);
        }
    }

    /**
     * 获取为文本
     *
     * @return
     */
    @Override
    public String getAsText() {
        Class[] classes = (Class[]) getValue();
        if (ObjectUtils.isEmpty(classes)) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classes.length; ++i) {
            if (i > 0) {
                sb.append(",");
            }
            sb.append(ClassUtils.getQualifiedName(classes[i]));
        }
        return sb.toString();
    }

}

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

package org.springframework.context;

/**
 * Interface for objects that may participate in a phased
 * process such as lifecycle management.
 *
 * 阶段
 * 在启动时，有最低phase的对象首先启动
 * 在停止时，按照相反的顺序结束
 *
 * 如果我们设置了Integer.MIN_VALUE的话，那么它会首先启动并且最后停止
 * 如果我们设置了Integer.MAX_VALUE的话，那么他会最后启动并且最先停止
 *
 * @author Mark Fisher
 * @since 3.0
 * @see SmartLifecycle
 */
public interface Phased {

    /**
     * Return the phase value of this object.
     *
     * 获取当前阶段代表的值
     */
    int getPhase();

}

/*
 * Copyright 2002-2015 the original author or authors.
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
 * A common interface defining methods for start/stop lifecycle control.
 * The typical use case for this is to control asynchronous processing.
 * <b>NOTE: This interface does not imply specific auto-startup semantics.
 * Consider implementing {@link SmartLifecycle} for that purpose.</b>
 *
 * <p>Can be implemented by both components (typically a Spring bean defined in a
 * Spring context) and containers  (typically a Spring {@link ApplicationContext}
 * itself). Containers will propagate start/stop signals to all components that
 * apply within each container, e.g. for a stop/restart scenario at runtime.
 *
 * <p>Can be used for direct invocations or for management operations via JMX.
 * In the latter case, the {@link org.springframework.jmx.export.MBeanExporter}
 * will typically be defined with an
 * {@link org.springframework.jmx.export.assembler.InterfaceBasedMBeanInfoAssembler},
 * restricting the visibility of activity-controlled components to the Lifecycle
 * interface.
 *
 * <p>Note that the Lifecycle interface is only supported on <b>top-level singleton
 * beans</b>. On any other component, the Lifecycle interface will remain undetected
 * and hence ignored. Also, note that the extended {@link SmartLifecycle} interface
 * provides integration with the application context's startup and shutdown phases.
 *
 * 生命周期
 * 一个通用的接口来定义方法来开始/结束生命周期的控制
 * 它的经典使用方式就是控制异步调用
 *
 * 它可以被一个组件继承(比如Spring的Bean)
 * 它也可以被容器继承(比如ApplicationContext)
 * 容器会传播 开始/结束 的信号到它的所有子组件中
 *
 * 需要说明的是，Lifecycle接口只被顶层的单例Bean支持
 * 
 *
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see SmartLifecycle
 * @see ConfigurableApplicationContext
 * @see org.springframework.jms.listener.AbstractMessageListenerContainer
 * @see org.springframework.scheduling.quartz.SchedulerFactoryBean
 */
public interface Lifecycle {

    /**
     * Start this component.
     * Should not throw an exception if the component is already running.
     * <p>In the case of a container, this will propagate the start signal
     * to all components that apply.
     * @see SmartLifecycle#isAutoStartup()
     *
     * 启动当前组件
     */
    void start();

    /**
     * Stop this component, typically in a synchronous fashion, such that
     * the component is fully stopped upon return of this method. Consider
     * implementing {@link SmartLifecycle} and its {@code stop(Runnable)}
     * variant in cases where asynchronous stop behavior is necessary.
     * <p>Should not throw an exception if the component isn't started yet.
     * <p>In the case of a container, this will propagate the stop signal
     * to all components that apply.
     * @see SmartLifecycle#stop(Runnable)
     *
     * 停止当前组件
     */
    void stop();

    /**
     * Check whether this component is currently running.
     * <p>In the case of a container, this will return {@code true}
     * only if <i>all</i> components that apply are currently running.
     *
     * 判断当前组件是否仍然在运行
     *
     * @return whether the component is currently running
     */
    boolean isRunning();

}
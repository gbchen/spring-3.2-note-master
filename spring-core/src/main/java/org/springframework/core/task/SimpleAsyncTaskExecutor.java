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

package org.springframework.core.task;

import java.io.Serializable;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.ThreadFactory;

import org.springframework.util.Assert;
import org.springframework.util.ConcurrencyThrottleSupport;
import org.springframework.util.CustomizableThreadCreator;

/**
 * {@link TaskExecutor} implementation that fires up a new Thread for each task,
 * executing it asynchronously.
 *
 * <p>Supports limiting concurrent threads through the "concurrencyLimit"
 * bean property. By default, the number of concurrent threads is unlimited.
 *
 * <p><b>NOTE: This implementation does not reuse threads!</b> Consider a
 * thread-pooling TaskExecutor implementation instead, in particular for
 * executing a large number of short-lived tasks.
 *
 * 简单的异步任务执行类
 *
 *
 * @author Juergen Hoeller
 * @since 2.0
 * @see #setConcurrencyLimit
 * @see SyncTaskExecutor
 * @see org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
 * @see org.springframework.scheduling.commonj.WorkManagerTaskExecutor
 */
@SuppressWarnings("serial")
public class SimpleAsyncTaskExecutor extends CustomizableThreadCreator implements AsyncTaskExecutor, Serializable {

    /**
     * Permit any number of concurrent invocations: that is, don't throttle concurrency.
     *
     * 允许任何数量的并发
     */
    public static final int UNBOUNDED_CONCURRENCY = ConcurrencyThrottleSupport.UNBOUNDED_CONCURRENCY;

    /**
     * Switch concurrency 'off': that is, don't allow any concurrent invocations.
     *
     * 不允许任何的并发调用
     */
    public static final int NO_CONCURRENCY = ConcurrencyThrottleSupport.NO_CONCURRENCY;


    /**
     * Internal concurrency throttle used by this executor
     *
     * 内部的并发控制器
     */
    private final ConcurrencyThrottleAdapter concurrencyThrottle = new ConcurrencyThrottleAdapter();

    /**
     * 线程工厂
     */
    private ThreadFactory threadFactory;


    /**
     * Create a new SimpleAsyncTaskExecutor with default thread name prefix.
     *
     * 默认的构造方法
     */
    public SimpleAsyncTaskExecutor() {
        super();
    }

    /**
     * Create a new SimpleAsyncTaskExecutor with the given thread name prefix.
     *
     * 带有线程名称前缀的构造方法
     *
     * @param threadNamePrefix the prefix to use for the names of newly created threads
     */
    public SimpleAsyncTaskExecutor(String threadNamePrefix) {
        super(threadNamePrefix);
    }

    /**
     * Create a new SimpleAsyncTaskExecutor with the given external thread factory.
     *
     * 执行了线程工厂的构造方法
     *
     * @param threadFactory the factory to use for creating new Threads
     */
    public SimpleAsyncTaskExecutor(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }


    /**
     * Specify an external factory to use for creating new Threads,
     * instead of relying on the local properties of this executor.
     * <p>You may specify an inner ThreadFactory bean or also a ThreadFactory reference
     * obtained from JNDI (on a Java EE 6 server) or some other lookup mechanism.
     * @see #setThreadNamePrefix
     * @see #setThreadPriority
     *
     * 设置线程工厂来创建新的线程
     */
    public void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    /**
     * Return the external factory to use for creating new Threads, if any.
     *
     * 返回创建线程的工厂
     */
    public final ThreadFactory getThreadFactory() {
        return this.threadFactory;
    }

    /**
     * Set the maximum number of parallel accesses allowed.
     * -1 indicates no concurrency limit at all.
     * <p>In principle, this limit can be changed at runtime,
     * although it is generally designed as a config time setting.
     * NOTE: Do not switch between -1 and any concrete limit at runtime,
     * as this will lead to inconsistent concurrency counts: A limit
     * of -1 effectively turns off concurrency counting completely.
     * @see #UNBOUNDED_CONCURRENCY
     *
     * 设置并发数
     * -1代表不对并发数做限制
     * 原则上它可以在运行时被修改
     *
     */
    public void setConcurrencyLimit(int concurrencyLimit) {
        this.concurrencyThrottle.setConcurrencyLimit(concurrencyLimit);
    }

    /**
     * Return the maximum number of parallel accesses allowed.
     *
     * 返回允许的最大的并发数
     */
    public final int getConcurrencyLimit() {
        return this.concurrencyThrottle.getConcurrencyLimit();
    }

    /**
     * Return whether this throttle is currently active.
     * @return {@code true} if the concurrency limit for this instance is active
     * @see #getConcurrencyLimit()
     * @see #setConcurrencyLimit
     *
     * 是否启用了限流
     */
    public final boolean isThrottleActive() {
        return this.concurrencyThrottle.isThrottleActive();
    }


    /**
     * Executes the given task, within a concurrency throttle
     * if configured (through the superclass's settings).
     * @see #doExecute(Runnable)
     *
     * 执行一个任务
     */
    public void execute(Runnable task) {
        execute(task, TIMEOUT_INDEFINITE);
    }

    /**
     * Executes the given task, within a concurrency throttle
     * if configured (through the superclass's settings).
     * <p>Executes urgent tasks (with 'immediate' timeout) directly,
     * bypassing the concurrency throttle (if active). All other
     * tasks are subject to throttling.
     * @see #TIMEOUT_IMMEDIATE
     * @see #doExecute(Runnable)
     *
     * 执行一个任务，
     */
    public void execute(Runnable task, long startTimeout) {
        Assert.notNull(task, "Runnable must not be null");
        if (isThrottleActive() && startTimeout > TIMEOUT_IMMEDIATE) {
            this.concurrencyThrottle.beforeAccess();
            doExecute(new ConcurrencyThrottlingRunnable(task));
        } else {
            doExecute(task);
        }
    }

    /**
     * 提交一个任务
     *
     * @param task the {@code Runnable} to execute (never {@code null})
     * @return
     */
    public Future<?> submit(Runnable task) {
        FutureTask<Object> future = new FutureTask<Object>(task, null);
        execute(future, TIMEOUT_INDEFINITE);
        return future;
    }

    /**
     * 提交一个任务
     *
     * @param task the {@code Callable} to execute (never {@code null})
     * @param <T>
     * @return
     */
    public <T> Future<T> submit(Callable<T> task) {
        FutureTask<T> future = new FutureTask<T>(task);
        execute(future, TIMEOUT_INDEFINITE);
        return future;
    }

    /**
     * Template method for the actual execution of a task.
     * <p>The default implementation creates a new Thread and starts it.
     * @param task the Runnable to execute
     * @see #setThreadFactory
     * @see #createThread
     * @see java.lang.Thread#start()
     *
     * 执行一个任务的模板方法
     * 默认的实现是创建一个新的线程并且执行它
     */
    protected void doExecute(Runnable task) {
        Thread thread = (this.threadFactory != null ? this.threadFactory.newThread(task) : createThread(task));
        thread.start();
    }


    /**
     * Subclass of the general ConcurrencyThrottleSupport class,
     * making {@code beforeAccess()} and {@code afterAccess()}
     * visible to the surrounding class.
     *
     * 它是ConcurrencyThrottleSupport的子类
     * 它使得beforeAccess()和afterAccess()方法对当前类可见
     */
    private static class ConcurrencyThrottleAdapter extends ConcurrencyThrottleSupport {

        @Override
        protected void beforeAccess() {
            super.beforeAccess();
        }

        @Override
        protected void afterAccess() {
            super.afterAccess();
        }
    }


    /**
     * 具有限流功能的线程
     *
     * This Runnable calls {@code afterAccess()} after the
     * target Runnable has finished its execution.
     */
    private class ConcurrencyThrottlingRunnable implements Runnable {

        /**
         * 待执行的任务
         */
        private final Runnable target;

        /**
         * 构造方法
         *
         * @param target
         */
        public ConcurrencyThrottlingRunnable(Runnable target) {
            this.target = target;
        }

        /**
         * 重写run方法
         * 主要是为了在执行结束后调用afterAccess方法
         */
        public void run() {
            try {
                this.target.run();
            } finally {
                concurrencyThrottle.afterAccess();
            }
        }
    }

}

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

package org.springframework.core.task.support;

import java.util.concurrent.Callable;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.FutureTask;
import java.util.concurrent.RejectedExecutionException;

import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.util.Assert;

/**
 * Adapter that takes a JDK {@code java.util.concurrent.Executor} and
 * exposes a Spring {@link org.springframework.core.task.TaskExecutor} for it.
 * Also detects an extended {@code java.util.concurrent.ExecutorService}, adapting
 * the {@link org.springframework.core.task.AsyncTaskExecutor} interface accordingly.
 *
 * 线程池适配器
 * 它即支持Java原生的Executor，也支持Spring自己的TaskExecutor
 *
 * @author Juergen Hoeller
 * @since 3.0
 * @see java.util.concurrent.Executor
 * @see java.util.concurrent.ExecutorService
 * @see java.util.concurrent.Executors
 */
public class TaskExecutorAdapter implements AsyncTaskExecutor {

    private final Executor concurrentExecutor;


    /**
     * Create a new TaskExecutorAdapter,
     * using the given JDK concurrent executor.
     *
     * 构造方法
     *
     * @param concurrentExecutor the JDK concurrent executor to delegate to
     */
    public TaskExecutorAdapter(Executor concurrentExecutor) {
        Assert.notNull(concurrentExecutor, "Executor must not be null");
        this.concurrentExecutor = concurrentExecutor;
    }


    /**
     * Delegates to the specified JDK concurrent executor.
     * @see java.util.concurrent.Executor#execute(Runnable)
     *
     * 执行任务
     */
    public void execute(Runnable task) {
        try {
            this.concurrentExecutor.execute(task);
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException(
                    "Executor [" + this.concurrentExecutor + "] did not accept task: " + task, ex);
        }
    }

    /**
     * 执行任务
     *
     * @param task the {@code Runnable} to execute (never {@code null})
     * @param startTimeout the time duration (milliseconds) within which the task is
     * supposed to start. This is intended as a hint to the executor, allowing for
     * preferred handling of immediate tasks. Typical values are {@link #TIMEOUT_IMMEDIATE}
     * or {@link #TIMEOUT_INDEFINITE} (the default as used by {@link #execute(Runnable)}).
     */
    public void execute(Runnable task, long startTimeout) {
        execute(task);
    }

    /**
     * 提交一个任务
     *
     * @param task the {@code Runnable} to execute (never {@code null})
     * @return
     */
    public Future<?> submit(Runnable task) {
        try {
            if (this.concurrentExecutor instanceof ExecutorService) {
                return ((ExecutorService) this.concurrentExecutor).submit(task);
            } else {
                FutureTask<Object> future = new FutureTask<Object>(task, null);
                this.concurrentExecutor.execute(future);
                return future;
            }
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException(
                    "Executor [" + this.concurrentExecutor + "] did not accept task: " + task, ex);
        }
    }

    /**
     * 提交一个任务
     *
     * @param task the {@code Callable} to execute (never {@code null})
     * @param <T>
     * @return
     */
    public <T> Future<T> submit(Callable<T> task) {
        try {
            if (this.concurrentExecutor instanceof ExecutorService) {
                return ((ExecutorService) this.concurrentExecutor).submit(task);
            } else {
                FutureTask<T> future = new FutureTask<T>(task);
                this.concurrentExecutor.execute(future);
                return future;
            }
        } catch (RejectedExecutionException ex) {
            throw new TaskRejectedException(
                    "Executor [" + this.concurrentExecutor + "] did not accept task: " + task, ex);
        }
    }

}

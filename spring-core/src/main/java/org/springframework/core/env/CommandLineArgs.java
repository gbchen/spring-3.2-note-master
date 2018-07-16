/*
 * Copyright 2002-2011 the original author or authors.
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

package org.springframework.core.env;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A simple representation of command line arguments, broken into "option arguments" and
 * "non-option arguments".
 *
 * 命令行参数的简单表示
 * 通常我们的命令行习惯中，参数分为两大类:option和non-option
 * 一般non-option是放在option之后的
 *
 * @author Chris Beams
 * @since 3.1
 * @see SimpleCommandLineArgsParser
 */
class CommandLineArgs {

    /**
     * 配置项
     * 注意这里的配置的值是一个list
     */
    private final Map<String, List<String>> optionArgs = new HashMap<String, List<String>>();

    /**
     * 非配置项
     */
    private final List<String> nonOptionArgs = new ArrayList<String>();

    /**
     * Add an option argument for the given option name and add the given value to the
     * list of values associated with this option (of which there may be zero or more).
     * The given value may be {@code null}, indicating that the option was specified
     * without an associated value (e.g. "--foo" vs. "--foo=bar").
     *
     * 添加一个配置项
     */
    public void addOptionArg(String optionName, String optionValue) {
        if (!this.optionArgs.containsKey(optionName)) {
            // 如果不包含特定的配置项
            this.optionArgs.put(optionName, new ArrayList<String>());
        }
        if (optionValue != null) {
            // 如果已经包含了特定的配置项
            this.optionArgs.get(optionName).add(optionValue);
        }
    }

    /**
     * Return the set of all option arguments present on the command line.
     *
     * 获取所有的配置项的名称
     */
    public Set<String> getOptionNames() {
        return Collections.unmodifiableSet(this.optionArgs.keySet());
    }

    /**
     * Return whether the option with the given name was present on the command line.
     *
     * 是否包含某个特定的配置项
     */
    public boolean containsOption(String optionName) {
        return this.optionArgs.containsKey(optionName);
    }

    /**
     * Return the list of values associated with the given option. {@code null} signifies
     * that the option was not present; empty list signifies that no values were associated
     * with this option.
     *
     * 获取配置项的值列表
     */
    public List<String> getOptionValues(String optionName) {
        return this.optionArgs.get(optionName);
    }

    /**
     * Add the given value to the list of non-option arguments.
     *
     * 添加一个非配置项
     */
    public void addNonOptionArg(String value) {
        this.nonOptionArgs.add(value);
    }

    /**
     * Return the list of non-option arguments specified on the command line.
     *
     * 获取所有的非配置项
     */
    public List<String> getNonOptionArgs() {
        return Collections.unmodifiableList(this.nonOptionArgs);
    }

}

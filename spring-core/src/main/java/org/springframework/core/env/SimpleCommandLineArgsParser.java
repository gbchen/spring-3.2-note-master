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

package org.springframework.core.env;

/**
 * Parses a {@code String[]} of command line arguments in order to populate a
 * {@link CommandLineArgs} object.
 *
 * <h3>Working with option arguments</h3>
 * Option arguments must adhere to the exact syntax:
 * <pre class="code">--optName[=optValue]</pre>
 * That is, options must be prefixed with "{@code --}", and may or may not specify a value.
 * If a value is specified, the name and value must be separated <em>without spaces</em>
 * by an equals sign ("=").
 *
 * <h4>Valid examples of option arguments</h4>
 * <pre class="code">
 * --foo
 * --foo=bar
 * --foo="bar then baz"
 * --foo=bar,baz,biz</pre>
 *
 * <h4>Invalid examples of option arguments</h4>
 * <pre class="code">
 * -foo
 * --foo bar
 * --foo = bar
 * --foo=bar --foo=baz --foo=biz</pre>
 *
 * <h3>Working with non-option arguments</h3>
 * Any and all arguments specified at the command line without the "{@code --}" option
 * prefix will be considered as "non-option arguments" and made available through the
 * {@link CommandLineArgs#getNonOptionArgs()} method.
 *
 * 通常的使用语法:
 * --optName[=optValue]
 * 即:  --配置项[=配置值]
 * 配置项必须带有前缀--
 * 配置项可以没有值
 *
 * 合法的配置项:
 * --foo
 * --foo=bar
 * --foo="bar then baz"
 * --foo=bar,baz,biz
 *
 * @author Chris Beams
 * @since 3.1
 */
class SimpleCommandLineArgsParser {

    /**
     * Parse the given {@code String} array based on the rules described
     * {@linkplain SimpleCommandLineArgsParser above}, returning a
     * fully-populated {@link CommandLineArgs} object.
     *
     * 解析给定的命令行参数
     *
     * @param args command line arguments, typically from a {@code main()} method
     */
    public CommandLineArgs parse(String... args) {
        CommandLineArgs commandLineArgs = new CommandLineArgs();
        for (String arg : args) {
            if (arg.startsWith("--")) {
                String optionText = arg.substring(2, arg.length());

                // 配置项
                String optionName;

                // 配置的值
                String optionValue = null;

                if (optionText.contains("=")) {
                    // 如果包含等号则提取:配置项和值
                    optionName = optionText.substring(0, optionText.indexOf('='));
                    optionValue = optionText.substring(optionText.indexOf('=') + 1, optionText.length());
                } else {
                    // 否则只提取配置项
                    optionName = optionText;
                }

                if (optionName.length() == 0 || (optionValue != null && optionValue.length() == 0)) {
                    // 配置项和配置值都不允许空字符串
                    throw new IllegalArgumentException("Invalid argument syntax: " + arg);
                }

                commandLineArgs.addOptionArg(optionName, optionValue);
            } else {
                // 非可选参数
                commandLineArgs.addNonOptionArg(arg);
            }
        }
        return commandLineArgs;
    }

}

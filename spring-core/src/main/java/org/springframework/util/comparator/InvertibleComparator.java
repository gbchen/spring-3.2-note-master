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

package org.springframework.util.comparator;

import java.io.Serializable;
import java.util.Comparator;

import org.springframework.util.Assert;

/**
 * A decorator for a comparator, with an "ascending" flag denoting
 * whether comparison results should be treated in forward (standard
 * ascending) order or flipped for reverse (descending) order.
 *
 * 可颠倒的比较器
 *
 * @author Keith Donald
 * @author Juergen Hoeller
 * @since 1.2.2
 */
@SuppressWarnings("serial")
public class InvertibleComparator<T> implements Comparator<T>, Serializable {

    /**
     * 比较器
     */
    private final Comparator<T> comparator;

    /**
     * 是否为升序
     */
    private boolean ascending = true;


    /**
     * Create an InvertibleComparator that sorts ascending by default.
     * For the actual comparison, the specified Comparator will be used.
     *
     * 构造方法
     *
     * @param comparator the comparator to decorate
     */
    public InvertibleComparator(Comparator<T> comparator) {
        Assert.notNull(comparator, "Comparator must not be null");
        this.comparator = comparator;
    }

    /**
     * Create an InvertibleComparator that sorts based on the provided order.
     * For the actual comparison, the specified Comparator will be used.
     *
     * 构造方法
     * 可以设置为是否为升序
     *
     * @param comparator the comparator to decorate
     * @param ascending the sort order: ascending (true) or descending (false)
     */
    public InvertibleComparator(Comparator<T> comparator, boolean ascending) {
        Assert.notNull(comparator, "Comparator must not be null");
        this.comparator = comparator;
        setAscending(ascending);
    }


    /**
     * Specify the sort order: ascending (true) or descending (false).
     *
     * 设置是否升序
     */
    public void setAscending(boolean ascending) {
        this.ascending = ascending;
    }

    /**
     * Return the sort order: ascending (true) or descending (false).
     *
     * 获取是否升序
     */
    public boolean isAscending() {
        return this.ascending;
    }

    /**
     * Invert the sort order: ascending -> descending or
     * descending -> ascending.
     *
     * 反转顺序
     */
    public void invertOrder() {
        this.ascending = !this.ascending;
    }


    /**
     * 比较
     * 这里要注意升序还是降序
     *
     * @param o1
     * @param o2
     * @return
     */
    public int compare(T o1, T o2) {
        int result = this.comparator.compare(o1, o2);
        if (result != 0) {
            // Invert the order if it is a reverse sort.
            if (!this.ascending) {
                if (Integer.MIN_VALUE == result) {
                    result = Integer.MAX_VALUE;
                } else {
                    result *= -1;
                }
            }
            return result;
        }
        return 0;
    }

    /**
     * 判断是否相等
     *
     * @param obj
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof InvertibleComparator)) {
            return false;
        }
        InvertibleComparator<T> other = (InvertibleComparator<T>) obj;
        return (this.comparator.equals(other.comparator) && this.ascending == other.ascending);
    }

    /**
     * 哈希值
     *
     * @return
     */
    @Override
    public int hashCode() {
        return this.comparator.hashCode();
    }

    /**
     * 获取字符串形式
     *
     * @return
     */
    @Override
    public String toString() {
        return "InvertibleComparator: [" + this.comparator + "]; ascending=" + this.ascending;
    }

}

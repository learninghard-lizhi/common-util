package com.lizhi.common.lock;

/**
 * @author zhi.li
 * @Description
 * @created 2018/12/29 19:07
 */
public interface DistributedLock {
    /**
     * 获取锁
     * @author zhi.li
     * @return 锁标识
     */
    String acquire();

    /**
     * 释放锁
     * @author zhi.li
     * @param indentifier
     * @return
     */
    boolean release(String indentifier);
}

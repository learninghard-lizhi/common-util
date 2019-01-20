package com.lizhi.common.lock.test;

import com.lizhi.common.lock.DistributedLock;
import com.lizhi.common.lock.impl.ZooKeeperDistributedLock;

/**
 * @author zhi.li
 * @Description
 * @created 2019/1/6 19:47
 */
public class ZookeeperDistributedLockTest {
    static int n = 500;

    public static void secskill() {
        System.out.println(--n);
    }
    public static void main(String[] args) {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                ZooKeeperDistributedLock lock = null;
                try {
                    lock = new ZooKeeperDistributedLock("127.0.0.1:2181", "test1");
                    lock.acquire();
                    secskill();
                    System.out.println(Thread.currentThread().getName() + "正在运行");
                } finally {
                    if (lock != null) {
                        lock.release(null);
                    }
                }
            }
        };

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}

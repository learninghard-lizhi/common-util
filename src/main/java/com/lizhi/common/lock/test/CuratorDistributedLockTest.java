package com.lizhi.common.lock.test;

import com.lizhi.common.lock.impl.RedisDistributedLock;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import redis.clients.jedis.Jedis;

import java.util.concurrent.TimeUnit;

/**
 * Curator 分布式锁使用实例
 * @author zhi.li
 * @Description
 * @created 2019/1/8 21:51
 */
public class CuratorDistributedLockTest {
    static int n = 500;
    public static void secskill() {
        System.out.println(--n);
    }



    public static void main(String[] args) {
        String zkAddr = "127.0.0.1:2181";
        String lockPath = "/distribute-lock";
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zkAddr)
                .sessionTimeoutMs(2000)
                .retryPolicy(retryPolicy)
                .build();
        client.start();

        Runnable runnable = () -> {
            InterProcessMutex lock = new InterProcessMutex(client, lockPath);
            try {
                lock.acquire(1, TimeUnit.SECONDS);
                System.out.println(Thread.currentThread().getName() + "正在运行");
                secskill();
            }catch (Exception e) {
                System.out.println("acquire exception: " + e);
            } finally {
                if (lock != null) {
                    try {
                        lock.release();
                    } catch (Exception e) {
                        System.out.println("release exception: " + e);
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

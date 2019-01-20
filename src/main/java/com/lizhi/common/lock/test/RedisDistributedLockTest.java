package com.lizhi.common.lock.test;

import com.lizhi.common.lock.impl.RedisDistributedLock;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.swing.JEditorPane;


/**
 * @author zhi.li
 * @Description
 * @created 2019/1/1 21:42
 */
public class RedisDistributedLockTest {
    static int n = 500;
    public static void secskill() {
        System.out.println(--n);
    }

    public static void main(String[] args) {
        Runnable runnable = () -> {
            RedisDistributedLock lock = null;
            String unLockIdentify = null;
            try {
                Jedis conn = new Jedis("127.0.0.1",6379);
                lock = new RedisDistributedLock(conn, "test1");
                unLockIdentify = lock.acquire();
                System.out.println(Thread.currentThread().getName() + "正在运行");
                secskill();
            } finally {
                if (lock != null) {
                    lock.release(unLockIdentify);
                }
            }
        };

        for (int i = 0; i < 10; i++) {
            Thread t = new Thread(runnable);
            t.start();
        }
    }
}

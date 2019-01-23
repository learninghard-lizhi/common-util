package com.lizhi.common.lock.impl;

import com.lizhi.common.lock.DistributedLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;

import java.util.concurrent.TimeUnit;

/**
 * @author zhi.li
 * @Description
 * @created 2019/1/20 20:41
 */
public class RedisDistributedRedLock implements DistributedLock {

    /**
     * redis 客户端
     */
    private RedissonClient redissonClient;

    /**
     * 分布式锁的键值
     */
    private String lockKey;

    private RLock redLock;

    /**
     * 锁的有效时间 10s
     */
    int expireTime = 10 * 1000;

    /**
     * 获取锁的超时时间
     */
    int acquireTimeout  = 500;

    public RedisDistributedRedLock(RedissonClient redissonClient, String lockKey) {
        this.redissonClient = redissonClient;
        this.lockKey = lockKey;
    }

    @Override
    public String acquire() {
        redLock = redissonClient.getLock(lockKey);
        boolean isLock;
        try{
            isLock = redLock.tryLock(acquireTimeout, expireTime, TimeUnit.MILLISECONDS);
            if(isLock){
                System.out.println(Thread.currentThread().getName() + " " + lockKey + "获得了锁");
                return null;
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean release(String indentifier) {
        if(null != redLock){
            redLock.unlock();
            return true;
        }

        return false;
    }
}

package com.hza.lock.redis;

import com.hza.lock.entity.LockResource;
import org.springframework.data.redis.core.RedisTemplate;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisCluster;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class RedisLockImpl implements Lock {

    private static final String SET_IF_NOT_EXIST = "NX";
    private static final String SET_WITH_EXPIRE_TIME = "PX";
    private static final String LOCK_PREFIX = "redis:lock:";
    private static final long TIME = 1;
    private static final String LOCK_MSG = "OK";
    private static final String UNLOCK_MSG = "1";

    private Jedis jedis;

    private LockResource lockResource;

    public RedisLockImpl(Jedis jedis, LockResource lockResource) {
        this.jedis = jedis;
        this.lockResource = lockResource;
    }

    public RedisLockImpl(String name, Jedis jedis) {
        LockResource lockResource = new LockResource();
        lockResource.setCount(1L);
        String addr = null;
        try {
            addr = InetAddress.getLocalHost().getHostAddress();//获得本机IP
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        lockResource.setNodeInfo(addr);
        lockResource.setResourceName(name);
        this.lockResource = lockResource;
        this.jedis = jedis;
    }

    @Override
    public void lock() {

    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        String result = jedis.set(LOCK_PREFIX + lockResource.getResourceName(), lockResource.getNodeInfo(), SET_IF_NOT_EXIST, SET_WITH_EXPIRE_TIME, 10000 * TIME);
        return LOCK_MSG.equals(result);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return false;
    }

    @Override
    public void unlock() {
        //lua script
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 1 end";
        Object result ;
        result = jedis.eval(script, Collections.singletonList(LOCK_PREFIX + lockResource.getResourceName()), Collections.singletonList(lockResource.getNodeInfo()));
        if (!UNLOCK_MSG.equals(result)){
            System.out.println("Unlock failure");
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}

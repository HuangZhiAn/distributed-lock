package com.hza.lock;

import com.hza.lock.entity.LockResource;
import com.hza.lock.mapper.LockResourceMapper;
import com.hza.lock.mysql.MysqlLockImpl;
import com.hza.lock.redis.RedisLockImpl;
import com.hza.lock.service.LockResourceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

import javax.swing.tree.TreeCellEditor;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LockApplicationTests {

    @Autowired
    private LockResourceService lockResourceService;

    @Autowired
    private LockResourceMapper lockResourceMapper;
    @Autowired
    private RedissonClient redissonClient;

    private int c = 0;
    private String addr = null;

    @Test
    public void contextLoads() {
        try {
            addr = InetAddress.getLocalHost().getHostAddress();//获得本机IP
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                MysqlLockImpl mysqlLock = new MysqlLockImpl("a123", lockResourceService);
                boolean ret = false;
                ret = mysqlLock.tryLock(1L, TimeUnit.SECONDS);
                if (ret) {
                    c += 3;
                    System.out.println(Thread.currentThread().getId() + "-aaaaa-" + c);
                    c -= 3;
                }
                mysqlLock.unlock();
            }).start();
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void redissonLock() {
        /*for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                RLock lock = redissonClient.getLock("a123");
                lock.lock();
                lock.lock();
                c += 3;
                System.out.println(lock.getHoldCount() + "-aaaaa-" + c);
                c -= 3;
                lock.unlock();
                lock.unlock();
            }).start();
        }*/
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                RLock lock = redissonClient.getLock("a123");
                boolean ret = false;
                try {
                    ret = lock.tryLock(1000L, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (ret) {
                    c += 3;
                    System.out.println(lock.getHoldCount() + "-aaaaa-" + c);
                    c -= 3;
                    lock.unlock();
                }

            }).start();
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void deadLockTest() {
        LockResource lockResource = new LockResource();
        lockResource.setResourceName("a123");
        try {
            addr = InetAddress.getLocalHost().getHostAddress();//获得本机IP
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        lockResource.setNodeInfo(addr + "-" + Thread.currentThread().getId() + "");
        lockResource.setCount(1L);
        for (int i = 0; i < 2; i++) {
            new Thread(() -> {
                lockResource.setNodeInfo(addr + "-" + Thread.currentThread().getId() + "");
                lockResourceService.lock(lockResource);
            }).start();
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void redisLockTest() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        RedisLockImpl redisLock = new RedisLockImpl("a123", jedis);
        boolean b = redisLock.tryLock();
        redisLock.unlock();
        boolean c = redisLock.tryLock();
        //redisLock.unlock();
        boolean d = redisLock.tryLock();
        System.out.println(b + ":" + c + ":" + d);
    }

    @Test
    public void redisLockTest2() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                RedisLockImpl redisLock = new RedisLockImpl("a123", jedis);
                redisLock.lock();
                redisLock.unlock();
                redisLock.lock();
                //redisLock.unlock();
                boolean b = redisLock.tryLock();
                c += 3;
                System.out.println(Thread.currentThread().getId() + "-aaaaa-" + c);
                c -= 3;
                System.out.println(b);
                redisLock.unlock();
            }).start();
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void redisLockTest3() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        for (int i = 0; i < 10; i++) {
            new Thread(() -> {
                RedisLockImpl redisLock = new RedisLockImpl("a123", jedis);
                boolean b = false;
                b = redisLock.tryLock();
                if(b){
                    c += 3;
                    try {
                        Thread.sleep(100L);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getId() + "-aaaaa-" + c);
                    c -= 3;
                    //System.out.println(b);
                    redisLock.unlock();
                }
            }).start();
            try {
                Thread.sleep(200L);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        try {
            System.in.read();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void redisUnlockTest() {
        Jedis jedis = new Jedis("127.0.0.1", 6379);
        RedisLockImpl redisLock = new RedisLockImpl("a123", jedis);
        /*String script = "return redis.call('del', KEYS[1])";
        Object result;
        result = jedis.eval(script, Collections.singletonList("a123"), Collections.singletonList(""));*/
        redisLock.lock();
        redisLock.unlock();
    }
}

package com.hza.lock;

import com.hza.lock.entity.LockResource;
import com.hza.lock.mapper.LockResourceMapper;
import com.hza.lock.mysql.MysqlLockImpl;
import com.hza.lock.service.LockResourceService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.redisson.RedissonLock;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
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
                boolean ret  = false;
                ret = mysqlLock.tryLock(1L, TimeUnit.SECONDS);
                if(ret){
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
                if(ret){
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

}

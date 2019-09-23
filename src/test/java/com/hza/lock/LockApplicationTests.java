package com.hza.lock;

import com.hza.lock.entity.LockResource;
import com.hza.lock.mapper.LockResourceMapper;
import com.hza.lock.mysql.MysqlLockImpl;
import com.hza.lock.service.LockResourceService;
import org.junit.Test;
import org.junit.runner.RunWith;
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
                LockResource lockResource = new LockResource();
                lockResource.setResourceName("a123");
                lockResource.setNodeInfo(addr + "-" + Thread.currentThread().getId() + "");
                lockResource.setCount(1L);
                MysqlLockImpl mysqlLock = new MysqlLockImpl(lockResource, lockResourceService);
                boolean ret  = false;
                ret = mysqlLock.tryLock(1L, TimeUnit.SECONDS);
                if(ret){
                    c += 3;
                    System.out.println(Thread.currentThread().getId() + "aaaaa-" + c);
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

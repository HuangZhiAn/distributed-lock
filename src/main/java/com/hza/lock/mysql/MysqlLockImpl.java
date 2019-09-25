package com.hza.lock.mysql;

import com.hza.lock.entity.LockResource;
import com.hza.lock.service.LockResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class MysqlLockImpl implements Lock {

    private LockResource lockResource;

    private LockResourceService lockResourceService;

    public MysqlLockImpl(LockResource lockResource, LockResourceService lockResourceService) {
        this.lockResource = lockResource;
        this.lockResourceService = lockResourceService;
    }

    public MysqlLockImpl(String name, LockResourceService lockResourceService) {
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
        this.lockResourceService = lockResourceService;
    }

    @Override
    public void lock() {
        while (true) {
            try{
                if(lockResourceService.lock(lockResource)){
                    break;
                }
            }catch (Exception e){
                //System.out.println(e.getLocalizedMessage());
            }
            try {
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        return lockResourceService.lock(lockResource);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) {
        long endTime = System.currentTimeMillis() + unit.toMillis(time);
        while (System.currentTimeMillis() < endTime) {
            try{
                if(lockResourceService.lock(lockResource)){
                    return true;
                }
            }catch (Exception e){
                //System.out.println(e.getLocalizedMessage());
            }
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    @Override
    public void unlock() {
        lockResourceService.unlock(lockResource);
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}

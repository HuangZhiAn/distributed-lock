package com.hza.lock.zookeeper;

import com.hza.lock.entity.LockResource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ZookeeperCuratorLockImpl implements Lock {
    private static final long TIME = 1;
    private static final String ROOT_LOCK_PATH = "/curator/lock";
    private CuratorFramework curatorFramework;
    private LockResource lockResource;
    private InterProcessMutex mutex;

    public ZookeeperCuratorLockImpl(CuratorFramework curatorFramework, LockResource lockResource) {
        this.curatorFramework = curatorFramework;
        this.lockResource = lockResource;
    }

    public ZookeeperCuratorLockImpl(String name, CuratorFramework curatorFramework) {
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
        this.curatorFramework = curatorFramework;
        mutex = new InterProcessMutex(curatorFramework, ROOT_LOCK_PATH + "/" + lockResource.getResourceName());
    }

    @Override
    public void lock() {
        try {
            mutex.acquire();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        try {
            return mutex.acquire(TIME, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        try {
            return mutex.acquire(time, unit);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void unlock() {
        try {
            mutex.release();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }
}

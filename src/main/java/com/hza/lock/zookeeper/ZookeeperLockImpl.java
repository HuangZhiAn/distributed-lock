package com.hza.lock.zookeeper;

import com.hza.lock.entity.LockResource;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

public class ZookeeperLockImpl implements Lock {
    private static final Logger logger = LoggerFactory.getLogger(ZookeeperLockImpl.class);
    private static final String ROOT_LOCK_PATH = "/curator/lock";
    private CuratorFramework curatorFramework;
    private LockResource lockResource;
    private CountDownLatch countDownLatch = new CountDownLatch(1);

    public ZookeeperLockImpl(CuratorFramework curatorFramework, LockResource lockResource) {
        this.curatorFramework = curatorFramework;
        this.lockResource = lockResource;
    }

    public ZookeeperLockImpl(String name, CuratorFramework curatorFramework) {
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
        afterPropertiesSet();
    }

    @Override
    public void lock() {
        String keyPath = ROOT_LOCK_PATH + "/" + lockResource.getResourceName();
        while (true) {
            try {
                curatorFramework
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(keyPath);
                break;
            } catch (Exception e) {
                logger.info("failed to acquire lock for path:{},while try again .......", keyPath);
                try {
                    if (countDownLatch.getCount() <= 0) {
                        countDownLatch = new CountDownLatch(1);
                    }
                    countDownLatch.await();
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
        }
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {

    }

    @Override
    public boolean tryLock() {
        String keyPath = ROOT_LOCK_PATH + "/" + lockResource.getResourceName();
        try {
            curatorFramework
                    .create()
                    .creatingParentsIfNeeded()
                    .withMode(CreateMode.EPHEMERAL)
                    .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                    .forPath(keyPath);
            return true;
        } catch (Exception e) {
            logger.info("failed to acquire lock for path:{}", keyPath);
            return false;
        }
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        String keyPath = ROOT_LOCK_PATH + "/" + lockResource.getResourceName();
        long endTime = System.currentTimeMillis()+unit.toMillis(time);
        while (System.currentTimeMillis()<endTime) {
            try {
                curatorFramework
                        .create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.EPHEMERAL)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(keyPath);
                return true;
            } catch (Exception e) {
                logger.info("failed to acquire lock for path:{},while try again ......", keyPath);
            }
        }
        return false;
    }

    @Override
    public void unlock() {
        try {
            String keyPath = ROOT_LOCK_PATH + "/" + lockResource.getResourceName();
            if (curatorFramework.checkExists().forPath(keyPath) != null) {
                curatorFramework.delete().forPath(keyPath);
            }
        } catch (Exception e) {
            logger.error("failed to release lock");
        }
    }

    @Override
    public Condition newCondition() {
        return null;
    }

    /**
     * 创建 watcher 事件
     */
    private void addWatcher(String path) throws Exception {
        String keyPath;
        if (path.equals(ROOT_LOCK_PATH)) {
            keyPath = path;
        } else {
            keyPath = ROOT_LOCK_PATH + "/" + path;
        }
        final PathChildrenCache cache = new PathChildrenCache(curatorFramework, keyPath, false);
        cache.start(PathChildrenCache.StartMode.POST_INITIALIZED_EVENT);
        cache.getListenable().addListener((client, event) -> {
            if (event.getType().equals(PathChildrenCacheEvent.Type.CHILD_REMOVED)) {
                String oldPath = event.getData().getPath();
                if (oldPath.contains(path)) {
                    //释放计数器，让当前的请求获取锁
                    countDownLatch.countDown();
                }
            }
        });
    }

    //创建父节点，并创建永久节点
    public void afterPropertiesSet() {
        curatorFramework = curatorFramework.usingNamespace("lock-namespace");
        String path = ROOT_LOCK_PATH;
        try {
            if (curatorFramework.checkExists().forPath(path) == null) {
                curatorFramework.create()
                        .creatingParentsIfNeeded()
                        .withMode(CreateMode.PERSISTENT)
                        .withACL(ZooDefs.Ids.OPEN_ACL_UNSAFE)
                        .forPath(path);
            }
            addWatcher(ROOT_LOCK_PATH);
            logger.info("root path 的 watcher 事件创建成功");
        } catch (Exception e) {
            logger.error("connect zookeeper fail，please check the log >> {}", e.getMessage(), e);
        }
    }
}

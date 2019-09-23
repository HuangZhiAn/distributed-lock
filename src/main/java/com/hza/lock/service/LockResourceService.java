package com.hza.lock.service;

import com.hza.lock.entity.LockResource;

/**
 * 服务接口
 *
 * @author 123@hand-china.com
 * @date 2019-09-20 13:50:51
 */
public interface LockResourceService {

    /**
     * 非阻塞获取锁
     *
     * @param lockResource
     * @return
     */
    boolean lock(LockResource lockResource);

    /**
     * 释放锁
     *
     * @param lockResource
     * @return
     */
    boolean unlock(LockResource lockResource);
}


package com.hza.lock.service.impl;

import com.hza.lock.entity.LockResource;
import com.hza.lock.mapper.LockResourceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.hza.lock.service.LockResourceService;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现类
 *
 * @author 123@hand-china.com
 * @date 2019-09-20 13:50:51
 */
@Service
public class LockResourceServiceImpl implements LockResourceService {

    @Autowired
    private LockResourceMapper lockResourceMapper;

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean lock(LockResource lockResource) {
        LockResource lr = lockResourceMapper.selectForUpdate(lockResource.getResourceName());
        if (lr != null && lr.getId() != null) {
            if (lr.getNodeInfo().equals(lockResource.getNodeInfo())) {
                System.out.println(lr.getNodeInfo()+lockResource.getNodeInfo());
                lockResourceMapper.updateCount(lr.getId());
                return true;
            }
        } else {
            lockResourceMapper.insertLockResource(lockResource);
            return true;
        }
        return false;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean unlock(LockResource lockResource) {
        LockResource lr = lockResourceMapper.select(lockResource.getId());
        if (lr != null && lr.getId() != null) {
            if(lr.getNodeInfo().equals(lockResource.getNodeInfo())){
                if(lr.getCount()>1){
                    lockResourceMapper.updateCountDecrease(lockResource.getId());
                }else{
                    lockResourceMapper.deleteLockResource(lockResource.getId());
                }
                return true;
            }
        }
        return false;
    }
}

package com.hza.lock.controller;

import com.hza.lock.entity.LockResource;
import com.hza.lock.service.LockResourceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 *  管理 API
 *
 * @author 123@hand-china.com
 * @date 2019-09-20 13:50:51
 */
@RestController("lockResourceController.v1")
@RequestMapping("/v1/lockresources")
public class LockResourceController {

    @Autowired
    private LockResourceService lockResourceService;

}

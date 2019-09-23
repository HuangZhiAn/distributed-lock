package com.hza.lock.entity;

import java.util.Date;

/**
 * 
 *
 * @author 123@hand-china.com 2019-09-20 13:50:51
 */
public class LockResource {

    public static final String FIELD_ID = "id";
    public static final String FIELD_RESOURCE_NAME = "resourceName";
    public static final String FIELD_COUNT = "count";
    public static final String FIELD_NODE_INFO = "nodeInfo";
    public static final String FIELD_DESC = "desc";
    public static final String FIELD_CREATE_DATE = "createDate";
    public static final String FIELD_UPDATE_DATE = "updateDate";

    private Long id;
    private String resourceName;
    private Long count;
    private String nodeInfo;
    private String desc;
    private Date createDate;
    private Date updateDate;

    /**
     * @return 
     */
    public Long getId() {
            return id;
    }

    public void setId(Long id) {
            this.id = id;
    }
    /**
     * @return 资源名称
     */
    public String getResourceName() {
            return resourceName;
    }

    public void setResourceName(String resourceName) {
            this.resourceName = resourceName;
    }
    /**
     * @return 锁次数，用于可重入
     */
    public Long getCount() {
            return count;
    }

    public void setCount(Long count) {
            this.count = count;
    }
    /**
     * @return 机器或线程ID，用于可重入
     */
    public String getNodeInfo() {
            return nodeInfo;
    }

    public void setNodeInfo(String nodeInfo) {
            this.nodeInfo = nodeInfo;
    }
    /**
     * @return 额外描述
     */
    public String getDesc() {
            return desc;
    }

    public void setDesc(String desc) {
            this.desc = desc;
    }
    /**
     * @return 创建时间
     */
    public Date getCreateDate() {
            return createDate;
    }

    public void setCreateDate(Date createDate) {
            this.createDate = createDate;
    }
    /**
     * @return 更新时间
     */
    public Date getUpdateDate() {
            return updateDate;
    }

    public void setUpdateDate(Date updateDate) {
            this.updateDate = updateDate;
    }
}

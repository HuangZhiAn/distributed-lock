# distributed-lock

Mysql、redis、zookeeper实现分布式锁

### 分布式锁特点

 1. 互斥，同一时间只能有一个线程持有
 2. 可重入，获得锁的线程可再次获取相同的锁
 3. 支持阻塞和非阻塞，锁已被别的线程持有时，产生阻塞或直接返回失败
 4. 锁超时，线程获得锁后发生异常退出，锁将自动释放
 5. 高性能、高可用，获取锁和释放锁过程需要高效，锁服务高可用
 6. 支持公平和非公平(可选)，公平：按请求时间将线程排序，先到的线程先获取锁，非公平：线程同时竞争，可能会导致某个线程一直获取不到锁

### 分布式锁特点

1. 互斥，同一时间内只有一个线程持有
2. 可重入，获取锁的线程可重复获取该锁
3. 支持阻塞和非阻塞，线程获取锁失败后产生阻塞或直接返回失败
4. 高可用、高性能，获取锁和释放锁的过程需要高效实现，锁服务高可用
5. 锁超时，线程获取锁后异常退出，超时后将自动释放
6. 支持公平和非公平(可选)，线程按顺序获取锁或竞争获取

### 基于数据库的分布式锁实现

创建锁表

```sql
CREATE TABLE `hap_dev`.`Untitled`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `resource_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '资源名称',
  `count` int(11) NOT NULL COMMENT '锁次数，用于可重入',
  `node_info` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NOT NULL COMMENT '机器或线程ID，用于可重入',
  `desc` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci NULL DEFAULT NULL COMMENT '额外描述',
  `create_date` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) COMMENT '创建时间',
  `update_date` timestamp(0) NOT NULL DEFAULT CURRENT_TIMESTAMP(0) ON UPDATE CURRENT_TIMESTAMP(0) COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `lock_resource_name_uniqe`(`resource_name`) USING BTREE COMMENT '资源名称唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 37 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_general_ci ROW_FORMAT = Dynamic;
```
加锁：

    查询数据库锁表看锁是否存在
    存在则判断是不是相同节点和线程，相同则重入加1，不同则返回false
    不存在则插入新的锁

解锁：

    查询数据库锁表看锁是否存在
    存在则判断是不是相同节点和线程，相同则重入减1或删除，不同则返回false
    不存在则返回false

使用数据库唯一索引保证互斥  
使用数据库读写，性能不好  
需要自己处理锁超时  

### 基于Redis分布式锁实现

使用set 命令的nx px参数保证互斥  
使用内存，所以性能较好，需要处理高可用问题  
可设置过期时间，不用处理超时

### 基于Zookeeper分布式锁实现

使用zookeeper有序节点的特性保证互斥  
使用临时节点处理未主动释放锁的情况  
使用事件监听特性，释放锁时zookeeper会唤醒下一需要锁的线程(对比redis需要循环监听)，因此Zookeeper分布式锁是公平锁

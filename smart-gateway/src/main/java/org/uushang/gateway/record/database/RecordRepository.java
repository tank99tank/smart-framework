package org.uushang.gateway.record.database;

import org.uushang.gateway.record.AccessRecord;

/**
 *
 * 请求记录-数据库操作
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-03 16:10:43
 * @copyright www.liderong.cn
 */
public interface RecordRepository {

    /**
     * 插入请求记录
     * @param record
     * @return
     */
    int insert(AccessRecord record);
}
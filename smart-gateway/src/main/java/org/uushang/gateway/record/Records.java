package org.uushang.gateway.record;

import lombok.extern.slf4j.Slf4j;

/**
 *
 * 请求访问记录处理
 *
 *
 * @author liyue
 * @version v1
 * @create 2019-04-04 10:14:08
 * @copyright www.liderong.cn
 */
@Slf4j
public class Records {

    public static void handler(AccessRecord record) {

        //System.out.println(record);

        log.info(record.toString());
    }
}
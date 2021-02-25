package org.uushang.gateway.config.routes;

/**
 *
 * 使用编码的方式配置route
 *
 * 因为requestbody只能读取一次 而大多数的解决办法都是重新包装一个request传到下游
 * 这个办法太过于麻烦 同时在源码中发现有一个ReadBodyPredicateFactory的Predicate
 * 这个Predicate中缓存了一份requestbody 所以只要启用此Predicate即可
 * 但是这个Predicate是不能通过yml进行配置的 只支持编码方式
 * 参见
 * https://github.com/spring-cloud/spring-cloud-gateway/issues/690
 * https://github.com/spring-cloud/spring-cloud-gateway/issues/1259（目前此issue处于open状态 后续可能会支持）
 * 官方明确表示
 * There is currently not a way to use it via yaml. It requires a function to read the body and evaluate to true.
 * That can't happen in yaml.
 *
 * readBody只能用于POST方式 GET方式用不了 因为根据HTTP协议 GET请求不应该把参数放到请求体中
 *
 * @author liyue
 * @version v1
 * @create 2019-04-03 10:03:04
 * @copyright www.liderong.cn
 */
//@Configuration
//public class RouteBuilderConfig {
//
//    //虽然此方式没有在yml中配置方便
//    //但是可以采用一个折中的办法
//    //使用一个properties类来预先配置好比如-Path -URI之类的参数 然后在此处使用即可
//    //也可以达到不修改代码的效果
//
//    @Bean
//    public RouteLocator routes(RouteLocatorBuilder builder) {
//        //以下代码均使用webflux方式进行编写
//        return builder.routes()
//                .route("service-provider", fn -> fn
//                    .path("/provider/**")
//                    .and()
//                    .readBody(String.class, readBody -> !StringUtils.isEmpty(readBody))
//                    .uri("lb://service-provider"))
//                .build();
//
//    }
//}
package com.sky.test;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author maqiangfei
 * @since 2024/10/5 下午8:50
 */
@SpringBootTest
public class SpringDataRedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    void testRedisTemplate() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        HashOperations hashOperations = redisTemplate.opsForHash();
        ListOperations listOperations = redisTemplate.opsForList();
        SetOperations setOperations = redisTemplate.opsForSet();
        ZSetOperations zSetOperations = redisTemplate.opsForZSet();
    }

    /**
     * 操作字符串类型的数据
     * set get setex setnx
     */
    @Test
    void testString() {
        redisTemplate.opsForValue().set("city", "杭州");
        redisTemplate.opsForValue().get("city");// 杭州

        redisTemplate.opsForValue().set("code", "123456", 1, TimeUnit.MINUTES);
        redisTemplate.getExpire("code", TimeUnit.SECONDS); // 59

        redisTemplate.opsForValue().setIfAbsent("lock", "1"); // true
        redisTemplate.opsForValue().setIfAbsent("lock", "2"); // false
    }

    /**
     * 操作Hash类型数据
     * hset hget hmget hdel hkeys hvals hlen hsetnx
     */
    @Test
    void testHash() {
        String name = "maffy";
        Map<String, Object> maffy = new HashMap<>();
        maffy.put("age", 22);
        maffy.put("gender", "male");
        redisTemplate.opsForHash().put("maffy", "name", name);
        redisTemplate.opsForHash().putAll("maffy", maffy);

        redisTemplate.opsForHash().get("maffy", "name"); // maffy
        redisTemplate.opsForHash().multiGet("maffy", maffy.keySet()); // [male, 22]

        redisTemplate.opsForHash().size("maffy"); // 3

        redisTemplate.opsForHash().delete("maffy", "gender");

        redisTemplate.opsForHash().keys("maffy"); // [age, name]
        redisTemplate.opsForHash().values("maffy"); // [22, maffy]

        redisTemplate.opsForHash().entries("maffy"); // {age=22, name=maffy}
    }

    /**
     * 操作list类型数据
     * lpush rpush lpop rpop llen lrange
     */
    @Test
    void testList() {
        redisTemplate.opsForList().leftPush("message", "m1");
        redisTemplate.opsForList().leftPushAll("message", Arrays.asList("m2", "m3"));

        redisTemplate.opsForList().range("message", 0, -1); // [m3, m2, m1]

        redisTemplate.opsForList().rightPop("message"); // m1

        redisTemplate.opsForList().size("message"); // 2
    }

    /**
     * 操作set类型数据
     * sadd srem scard sismember smembers sinter sdiff sunion
     */
    @Test
    void testSet() {
        redisTemplate.opsForSet().add("userids", "1", "2", "2", "3");
        redisTemplate.opsForSet().add("userids2", "1", "2", "3", "4");

        redisTemplate.opsForSet().size("userids"); // 3

        redisTemplate.opsForSet().members("userids"); // [1, 2, 3]

        redisTemplate.opsForSet().remove("userids",  "3");
        redisTemplate.opsForSet().isMember("userids", "3"); // false

        redisTemplate.opsForSet().intersect("userids", "userids2"); // [1, 2]

        redisTemplate.opsForSet().difference("userids2", "userids");// [3, 4]
    }

    /**
     * 操作zset类型数据
     * zadd zrem zscore zrange zrangebyscore zcard zrank zrevrank zdiff zinter zunion
     */
    @Test
    void testZSet() {
        redisTemplate.opsForZSet().add("top", "hot1", 9d);
        Set<ZSetOperations.TypedTuple<String>> set = new HashSet<>();
        set.add(ZSetOperations.TypedTuple.of("hot2", 8d));
        set.add(ZSetOperations.TypedTuple.of("hot3", 7d));
        redisTemplate.opsForZSet().add("top", set);

        redisTemplate.opsForZSet().score("top", "hot1");// 9.0

        redisTemplate.opsForZSet().reverseRank("top", "hot1");// 0

        redisTemplate.opsForZSet().reverseRange("top", 0, -1);// [hot1, hot2, hot3]

        redisTemplate.opsForZSet().rangeByScore("top", 8d, 10d); // [hot2, hot1]

        redisTemplate.opsForZSet().remove("top", "hot1");
        redisTemplate.opsForZSet().size("top"); // 2
    }
}

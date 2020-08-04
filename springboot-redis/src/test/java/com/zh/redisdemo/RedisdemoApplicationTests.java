package com.zh.redisdemo;

import com.zh.redisdemo.entiy.User;
import com.zh.redisdemo.lock.RedisLock;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.lang.model.element.VariableElement;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SpringBootTest(classes = RedisdemoApplicationTests.class)
@ComponentScan("com.zh")
class RedisdemoApplicationTests {
    @Resource
    private RedisTemplate redisTemplate;
    @Resource
    private RedisLock redisLock;

    @Test
    void testRedisLock() {
        String key = "goods_1";
        String uuid = UUID.randomUUID().toString();
        new Thread(() -> {
            if (redisLock.lock(key, uuid, 30L)) {
                System.out.println(Thread.currentThread().getName() + " 加锁成功 " + uuid);
                /*Boolean unlock = redisLock.unlock(key, uuid);
                if (unlock) {
                    System.err.println(Thread.currentThread() + " 解锁成功 " + unlock);

                }*/
            } else {
                System.out.println(Thread.currentThread().getName() + " 加锁失败 " + uuid);
            }

        }).start();

        String uuid1 = UUID.randomUUID().toString();

        new Thread(() -> {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (redisLock.lock(key, uuid1, 60L)) {
                System.out.println(Thread.currentThread().getName() + " 加锁成功1 " + uuid1);
            } else {
                System.out.println(Thread.currentThread().getName() + " 加锁失败1" + uuid1);
            }


        }).start();

        // 解锁
        new Thread(() -> {
            try {
                Thread.sleep(2500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Boolean unlock = redisLock.unlock(key, uuid);
            if (unlock) {
                System.err.println(Thread.currentThread() + " 解锁成功 " + unlock);

            }
        }).start();

        try {
            Thread.sleep(Integer.MAX_VALUE);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    void contextLoads() throws Exception {
        String name = "王五";
        redisTemplate.opsForValue().set("name", name);
        System.out.println(redisTemplate.opsForValue().get("name"));

        User user = new User();
        user.setId(1);
        user.setName("王五");
        user.setAge(18);
        redisTemplate.opsForValue().set("user", user, 30, TimeUnit.SECONDS);
        user.setName("张三");
        Boolean setIfAbsent = redisTemplate.opsForValue().setIfAbsent("user", user, 30, TimeUnit.SECONDS);
        System.out.println("如果不存在 " + setIfAbsent);
        user.setName("李四");
        Boolean ifPresent = redisTemplate.opsForValue().setIfPresent("user", user, 60, TimeUnit.SECONDS);
        System.out.println("如果存在 " + ifPresent);
        System.out.println(redisTemplate.opsForValue().get("user"));
    }

}

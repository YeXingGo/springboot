package com.zh.redisdemo.lock;

import org.springframework.data.redis.connection.ReturnType;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Connection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

/**
 * @author Light
 * @Classname RedisLock
 * @Description TODO
 * @Created 2020/7/18 6:54
 */
@Component
public class RedisLock {
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    private static final Long SUCCESS = 1L;
    private static final Long SUCCESS_ = 1L;

    /**
     * 加锁
     *
     * @param key
     * @param value
     * @param seconds
     */
    public Boolean lock(String key, String value, Long seconds) {
        System.out.println("lock");
        boolean ret = false;
        int i = 0;
        while (true) {
            try {

                ret = redisTemplate.opsForValue().setIfAbsent(key, value, seconds, TimeUnit.SECONDS);
                if (ret) {
                    return ret;
                }

            } catch (Exception e) {
                e.printStackTrace();

            } finally {
               /* if (i >= 10) {
                    return ret;
                }else if (i >= 2) {
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }*/

                // System.out.println(Thread.currentThread().getName() +"  第"+ (++i)+" 次");
            }
        }


    }

    /**
     * 解锁
     *
     * @param key
     * @return
     */
    public Boolean unlock(String key, Object value) {
        Boolean unlockFlag = false;
        System.err.println(Thread.currentThread() + " unlock");

        // 手动操作
        Object o = redisTemplate.opsForValue().get(key);
        if (o != null || ((String)o).equals(value)) {
            Boolean delete = redisTemplate.delete(key);
            System.out.println(delete);
            return delete;
        }
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        DefaultRedisScript<Object> redisScript = new DefaultRedisScript<>(script, Object.class);
         o = redisTemplate.execute(redisScript, Collections.singletonList(key), Collections.singletonList(value));
        System.out.println(Thread.currentThread()+" unlock="+ o);


        return unlockFlag;
    }
}

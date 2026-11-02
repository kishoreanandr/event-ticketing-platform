package com.eventticketing.booking.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;

@Configuration
public class RedisConnectionTest {

    @Bean
    CommandLineRunner testRedis(StringRedisTemplate redisTemplate){
        return args -> {
            redisTemplate
                    .opsForValue()
                    .set("booking:redis-test","connected");
            String result= redisTemplate.opsForValue()
                    .get("booking:redis-test");
            System.out.println("Redis test result: "+result);
        };
    }
}

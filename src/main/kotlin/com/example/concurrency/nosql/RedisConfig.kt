package com.example.concurrency.nosql

import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory
import org.springframework.data.redis.core.StringRedisTemplate

@Configuration
class RedisConfig {

    @Bean
    fun redisConnectionFactory(): LettuceConnectionFactory {
        return LettuceConnectionFactory()
    }

    @Bean
    fun redisTemplate() =
        StringRedisTemplate().apply {
            connectionFactory = redisConnectionFactory()
        }

    @Bean
    fun redissonClient(): RedissonClient {
        return Redisson.create()
    }
}

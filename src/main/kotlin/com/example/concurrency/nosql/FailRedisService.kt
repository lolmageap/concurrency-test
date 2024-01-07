package com.example.concurrency.nosql

import org.redisson.api.RedissonClient
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.util.concurrent.TimeUnit.SECONDS


@Component
@Transactional
class FailRedisService(
    private val redisTemplate: StringRedisTemplate,
    private val redissonClient: RedissonClient,
) {

    fun spinLock(key: Long, func: () -> Unit) {
        val redisKey = PATH + key

        while (
            isLock(redisKey) == false
        ) {
            println("not yet")
            Thread.sleep(100)
        }

        try {
            func()
        } finally {
            redisTemplate.delete(redisKey)
        }

    }

    private fun isLock(redisKey: String) = redisTemplate.opsForValue()
        .setIfAbsent(redisKey, WRITE_LOCK, Duration.ofSeconds(5))

    fun tryLock(key: Long, func: () -> Unit) {
        val redisKey = PATH + key
        val lock = redissonClient.getLock(redisKey)

        try {
            if (lock.tryLock(5, 1, SECONDS)) {
                func()
            } else {
                println("Failed to acquire lock.")
            }
        } finally {
            lock.unlock()
        }

        /**
         * 이 부분에서 unlock은 되었지만 transacion 이 끝나지 않았기 때문에 다른 thread가 lock을 획득할 수 있습니다.
         * 때문에 동시성 이슈가 발생하게 됩니다.
         * 그래서 traction이 끝나기 전에 savsAndFlush를 해주어야 합니다.
         * 하지만 그래도 동시성 이슈가 발생합니다..
         */
    }

    companion object {
        private const val PATH = "count:"
        private const val WRITE_LOCK = "write-lock"
    }
}
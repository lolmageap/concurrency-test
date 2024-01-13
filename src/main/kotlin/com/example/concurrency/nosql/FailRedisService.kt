package com.example.concurrency.nosql

import org.redisson.api.RedissonClient
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit.SECONDS


@Component
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
         * 이 부분 에서 unlock 은 되었지만 transaction 이 끝나지 않았기 때문에 다른 thread 가 lock 을 획득할 수 있습니다.
         * 때문에 동시성 이슈가 발생 하게 됩니다.
         * 그래서 transaction 이 끝나기 전에 saveAndFlush 를 해줘야 합니다.
         * 하지만 saveAndFlush 를 해도 바로 commit 이 되는 것이 아니라
         * @Transaction 이 커넥션 을 끊는 동작 에서 commit 이 발생 하기 때문에 동시성 이슈가 발생 합니다..
         * 동작 과정 Flow : lock -> find -> saveAndFlush(commit x) -> unlock -> transaction end(commit o)
         * unlock 은 되었 지만 commit 이 되지 않은 시점 에서 다른 thread 가 조회를 시도 해서 동시성 이슈가 발생 합니다.
         * redis 의 lock, unlock 을 transaction 으로 묶게 되면 이와 같은 이슈가 발생 하여 실패 하게 됩니다.
         */
    }

    companion object {
        private const val PATH = "count:"
        private const val WRITE_LOCK = "write-lock"
    }
}

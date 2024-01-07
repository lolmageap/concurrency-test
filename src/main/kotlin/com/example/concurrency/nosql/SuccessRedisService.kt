package com.example.concurrency.nosql

import org.redisson.api.RedissonClient
import org.springframework.data.redis.core.StringRedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.concurrent.TimeUnit.SECONDS


@Component
class SuccessRedisService(
    private val redisTemplate: StringRedisTemplate,
    private val redissonClient: RedissonClient,
    private val conCurrencyWithRedisService: ConCurrencyWithRedisService,
) {

    /**
     * Redis 의 Spin Lock 을 이용한 락 처리 입니다.
     * Redis 의 Spin Lock 은 Redis 의 SETNX 를 이용하여 락을 처리 합니다.
     * SETNX 는 키가 존재 하지 않을 때만 키를 저장 하고, 키가 존재 하면 저장 하지 않습니다.
     * SETNX 는 키가 존재 하지 않을 때만 1 을 반환 하고, 키가 존재 하면 0 을 반환 합니다.
     * Spin Lock 은 락을 획득할 때까지 주기적 으로 락을 획득 하려 시도 합니다.
     * 많은 요청이 발생 하면 Redis 의 성능에 영향을 줄 수 있습니다.
     */
    fun increaseCountWithSpinLock(key: Long) {
        val redisKey = PATH + key

        while (
            isLock(redisKey) == false
        ) {
            Thread.sleep(100)
        }

        try {
            conCurrencyWithRedisService.increaseCount(key)
        } finally {
            redisTemplate.delete(redisKey)
        }
    }

    private fun isLock(redisKey: String) = redisTemplate.opsForValue()
        .setIfAbsent(redisKey, WRITE_LOCK, Duration.ofSeconds(5))

    fun increaseCountWithTryLock(key: Long) {
        val redisKey = PATH + key
        val lock = redissonClient.getLock(redisKey)

        /**
         * Redisson 을 이용한 락 처리 입니다.
         * Redisson 은 Redis 를 이용한 분산 락 처리를 지원 합니다.
         * Redis 에 Pessimistic Lock 을 걸고 락이 풀리는 시점에 락 점유를 기다리는 노드들에게 subscript 하여 락 처리가 완료 되었다고 알립니다.
         * 이벤트 브로커 를 이용 하여 락을 획득한 Thread 가 종료 되면 이벤트 를 발생 시켜 다른 Thread 가 락을 획득 할 수 있도록 합니다.
         * Spin Lock 과 달리 thread 들은 락을 주기적 으로 획득 하려 시도 하지 않고, 락을 획득할 때까지 대기 합니다.
         * 아래 로직에 대한 설명입니다 {
         *  5초 동안 락을 획득 하지 못하면 예외를 발생 시킵니다.
         *  락을 획득 하면 1초 동안 락을 유지 합니다.
         *  락을 획득 하면 1초 동안 락을 유지 하기 때문에 락을 획득한 Thread 가 종료 되기 전에는 다른 Thread 가 락을 획득 할 수 없습니다.
         * }
         * 락을 획득한 Thread 가 종료 되기 전에는 다른 Thread 가 락을 획득 할 수 없기 때문에 데드락 이 발생할 수 있습니다.
         * Redisson 에서 Redis Cluster 에 대한 설정을 하면 다른 레디스 노드도 락에 대한 영향을 받습니다.
         * 예를 들어 Redis Cluster 에서 3개의 레디스 노드가 있고, 1개의 레디스 노드에 락을 걸었다면 다른 2개의 레디스 노드도 락에 대한 영향을 받습니다.
         */
        try {
            if (lock.tryLock(5, 1, SECONDS)) {
                conCurrencyWithRedisService.increaseCount(key)
            } else {
                throw RedisLockException("Failed to acquire lock.")
            }
        } finally {
            lock.unlock()
        }
    }

    companion object {
        private const val PATH = "count:"
        private const val WRITE_LOCK = "write-lock"
    }
}

class RedisLockException(message: String) : RuntimeException(message)
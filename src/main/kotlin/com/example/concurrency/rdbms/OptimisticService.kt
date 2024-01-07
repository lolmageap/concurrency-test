package com.example.concurrency.rdbms

import io.github.oshai.kotlinlogging.KotlinLogging
import org.hibernate.StaleStateException
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class OptimisticService(
    private val optimisticRepository: OptimisticRepository,
) {
    private val logger = KotlinLogging.logger {}

    @Retryable(
        value = [StaleStateException::class],
        maxAttempts = 3,
        backoff = Backoff(delay = 100),
        recover = "increaseCountWithOptimisticLockRecover",
    )
    fun increaseCountWithOptimisticLock(id: Long) {
        val entity = optimisticRepository.findWithOptimisticLockById(id)
        entity?.let {
            it.count += 1
        }
    }

    @Recover
    fun increaseCountWithOptimisticLockRecover(id: Long) {
        logger.error { "StaleStateException : $id" }
    }
}

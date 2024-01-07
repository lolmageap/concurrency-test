package com.example.concurrency.nosql

import com.example.concurrency.rdbms.ConCurrencyRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ConCurrencyWithFailRedisService(
    private val conCurrencyRepository: ConCurrencyRepository,
    private val failRedisService: FailRedisService,
) {
    fun increaseCountWithSpinLock(id: Long) {
        failRedisService.spinLock(
            key = id,
            func = {
                increaseCount(id)
            }
        )
    }

    fun increaseCountWithTryLock(id: Long) {
        failRedisService.tryLock(
            key = id,
            func = {
                increaseCount(id)
            }
        )
    }

    private fun increaseCount(id: Long) {
        conCurrencyRepository.findByIdOrNull(id)
            ?.let { entity ->
                entity.count += 1
                conCurrencyRepository.saveAndFlush(entity)
            }
    }
}
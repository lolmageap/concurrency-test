package com.example.concurrency.nosql

import com.example.concurrency.rdbms.ConCurrencyRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ConCurrencyWithRedisService(
    private val conCurrencyRepository: ConCurrencyRepository,
) {
    fun increaseCount(id: Long) {
        conCurrencyRepository.findByIdOrNull(id)
            ?.let {
                it.count += 1
            }
    }
}
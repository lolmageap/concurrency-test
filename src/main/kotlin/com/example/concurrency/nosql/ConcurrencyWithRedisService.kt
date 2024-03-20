package com.example.concurrency.nosql

import com.example.concurrency.rdbms.ConcurrencyRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ConcurrencyWithRedisService(
    private val concurrencyRepository: ConcurrencyRepository,
) {
    fun increaseCount(id: Long) {
        concurrencyRepository.findByIdOrNull(id)
            ?.let {
                it.count += 1
            }
    }
}
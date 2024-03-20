package com.example.concurrency.rdbms

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ConcurrencyService(
    private val concurrencyRepository: ConcurrencyRepository,
) {

    fun increaseCountNoLock(id: Long) {
        concurrencyRepository.findByIdOrNull(id)
            ?.let {
                it.count += 1
            }
    }

    fun increaseCountWithPessimisticLock(id: Long) {
        concurrencyRepository.findWithPessimisticLockById(id)
            ?.let {
                it.count += 1
            }
    }

}
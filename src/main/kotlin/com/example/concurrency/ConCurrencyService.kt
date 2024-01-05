package com.example.concurrency

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class ConCurrencyService(
    private val conCurrencyRepository: ConCurrencyRepository,
) {

    fun increaseCountNoLock(id: Long) {
        conCurrencyRepository.findByIdOrNull(id)
            ?.let {
                it.likeCount += 1
            }
    }

    fun increaseCountWithPessimisticLock(id: Long) {
        conCurrencyRepository.findWithPessimisticLockById(id)
            ?.let {
                it.likeCount += 1
            }
    }

}
package com.example.concurrency.rdbms

import com.example.concurrency.ConCurrencyEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface ConCurrencyRepository : JpaRepository<ConCurrencyEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithPessimisticLockById(id: Long): ConCurrencyEntity?
}

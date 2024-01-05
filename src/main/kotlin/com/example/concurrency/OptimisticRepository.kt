package com.example.concurrency

import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock

interface OptimisticRepository : JpaRepository<OptimisticEntity, Long> {
    @Lock(LockModeType.OPTIMISTIC_FORCE_INCREMENT)
    fun findWithOptimisticLockById(id: Long): OptimisticEntity?
}
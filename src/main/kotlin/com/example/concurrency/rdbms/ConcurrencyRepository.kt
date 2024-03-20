package com.example.concurrency.rdbms

import com.example.concurrency.ConcurrencyEntity
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface ConcurrencyRepository : JpaRepository<ConcurrencyEntity, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    fun findWithPessimisticLockById(id: Long): ConcurrencyEntity?

    @Modifying
    @Query("update ConcurrencyEntity set name = :name where id = :id")
    fun updateForce(@Param("id") id: Long, @Param("name") name: String)
}

package com.example.concurrency.mvc

import com.example.concurrency.rdbms.ConcurrencyRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReadService(private val concurrencyRepository: ConcurrencyRepository) {

    fun getAll() = concurrencyRepository.findAll()

    fun get(id: Long) =
        concurrencyRepository.findByIdOrNull(id)!!

}
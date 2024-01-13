package com.example.concurrency.mvc

import com.example.concurrency.rdbms.ConCurrencyRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ReadService(private val conCurrencyRepository: ConCurrencyRepository) {

    fun getAll() = conCurrencyRepository.findAll()

    fun get(id: Long) =
        conCurrencyRepository.findByIdOrNull(id)!!

}
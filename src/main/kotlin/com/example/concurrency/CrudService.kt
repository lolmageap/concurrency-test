package com.example.concurrency

import com.example.concurrency.rdbms.ConCurrencyRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Propagation
import org.springframework.transaction.annotation.Transactional

@Service
class CrudService(private val conCurrencyRepository: ConCurrencyRepository) {

    @Transactional(readOnly = true)
    fun getAll() = conCurrencyRepository.findAll()

    @Transactional(readOnly = true)
    fun get(id: Long) =
        conCurrencyRepository.findByIdOrNull(id)!!

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun create() =
        conCurrencyRepository.save(
            ConCurrencyEntity(
                id = 0,
                name = "Test",
            )
        )

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun update(entity: ConCurrencyEntity, name: String) =
        entity.also {
            it.name = name
            conCurrencyRepository.save(it)
        }

    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun update(id: Long, name: String) =
        conCurrencyRepository.updateForce(id, name)


    @Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false)
    fun delete(id: Long) = conCurrencyRepository.deleteById(id)

}

@Service
class UseCase(private val crudService: CrudService) {

    @Transactional(readOnly = true)
    fun readAndWrite(id: Long, name: String) {
        crudService.get(id)
        crudService.update(id, name)
    }

}
package com.example.concurrency

import UseCase
import com.example.concurrency.rdbms.ConCurrencyRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class TransactionRouteTest(
    @Autowired private val useCase: UseCase,
    @Autowired private val conCurrencyRepository: ConCurrencyRepository,
): StringSpec({

    beforeEach {
        conCurrencyRepository.saveAndFlush(
            ConCurrencyEntity(
                id = 1,
                name = "Test",
            )
        )
    }

    "read slave and write master" {
        useCase.readAndWrite(1, "Updated")

        val result = conCurrencyRepository.findByIdOrNull(1)!!
        result.name shouldBe "Updated"
    }


})
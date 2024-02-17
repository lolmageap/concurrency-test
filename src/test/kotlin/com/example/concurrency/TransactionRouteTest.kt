package com.example.concurrency

import com.example.concurrency.mvc.UseCase
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

    "read slave and write master" {
        val entity = conCurrencyRepository.save(
            ConCurrencyEntity(
                name = "Test",
            )
        )

        useCase.readAndWrite(entity.id, "Updated")

        val result = conCurrencyRepository.findByIdOrNull(entity.id)!!
        result.name shouldBe "Updated"
    }

})
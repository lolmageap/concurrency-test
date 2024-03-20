package com.example.concurrency

import com.example.concurrency.mvc.UseCase
import com.example.concurrency.rdbms.ConcurrencyRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class TransactionRouteTest(
    @Autowired private val useCase: UseCase,
    @Autowired private val concurrencyRepository: ConcurrencyRepository,
): StringSpec({

    "read slave and write master" {
        val entity = concurrencyRepository.save(
            ConcurrencyEntity(
                name = "Test",
            )
        )

        useCase.readAndWrite(entity.id, "Updated")

        val result = concurrencyRepository.findByIdOrNull(entity.id)!!
        result.name shouldBe "Updated"
    }

})
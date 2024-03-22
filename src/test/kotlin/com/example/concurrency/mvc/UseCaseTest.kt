package com.example.concurrency.mvc

import com.example.concurrency.ConcurrencyEntity
import com.example.concurrency.rdbms.ConcurrencyRepository
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

@SpringBootTest
class UseCaseTest(
    private val useCase: UseCase,
    private val concurrencyRepository: ConcurrencyRepository,
): StringSpec({
    "read slave and write master" {
        val entity = concurrencyRepository.save(
            ConcurrencyEntity(
                name = "Test",
            )
        )

        useCase.transactionTest2(entity.id)
        concurrencyRepository.findByIdOrNull(entity.id)!!.name shouldBe "second"
    }
})
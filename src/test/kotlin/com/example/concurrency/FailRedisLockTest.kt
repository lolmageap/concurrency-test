package com.example.concurrency

import com.example.concurrency.nosql.ConCurrencyWithFailRedisService
import com.example.concurrency.rdbms.ConCurrencyRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.longs.shouldBeLessThan
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.containers.GenericContainer

@SpringBootTest
internal class FailRedisLockTest(
    @Autowired private val conCurrencyRepository: ConCurrencyRepository,
    @Autowired private val conCurrencyWithFailRedisService: ConCurrencyWithFailRedisService,
) : BehaviorSpec({

    val redisContainer = GenericContainer<Nothing>("redis:5.0.3-alpine")

    beforeSpec {
        redisContainer.portBindings.add("16379:6379")
        redisContainer.start()
        listener(redisContainer.perSpec())
    }

    afterSpec {
        redisContainer.stop()
    }

    Given("동시성 처리를 위해 Redis 에 Spin Lock 을 적용한 뒤") {
        val entity = conCurrencyRepository.save(
            ConCurrencyEntity(
                name = "Spin Lock",
            )
        )

        Then("5개의 Thread 로 1,000번의 Business Logic 을 비동기 로 실행 하고 검증 한다.").config(
            threads = 5,
            invocations = 1000,
            enabled = true,
        ) {
            conCurrencyWithFailRedisService.increaseCountWithSpinLock(entity.id)
        }.let {
            conCurrencyRepository.findByIdOrNull(entity.id)
                ?.let {
                    it.count shouldBeLessThan 1000
                }
        }
    }

    Given("동시성 처리를 위해 Redis 에 Try Lock 을 적용한 뒤") {
        val entity = conCurrencyRepository.save(
            ConCurrencyEntity(
                name = "Try Lock",
            )
        )

        Then("5개의 Thread 로 1,000번의 Business Logic 을 비동기 로 실행 하고 검증 한다.").config(
            threads = 5,
            invocations = 1000,
            enabled = true,
        ) {
            conCurrencyWithFailRedisService.increaseCountWithTryLock(entity.id)
        }.let {
            conCurrencyRepository.findByIdOrNull(entity.id)
                ?.let {
                    it.count shouldBeLessThan 1000
                }
        }
    }

})
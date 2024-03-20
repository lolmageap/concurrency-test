package com.example.concurrency

import com.example.concurrency.nosql.RedisService
import com.example.concurrency.rdbms.ConcurrencyRepository
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.extensions.testcontainers.perSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull
import org.testcontainers.containers.GenericContainer

@SpringBootTest
internal class RedisLockTest(
    @Autowired private val concurrencyRepository: ConcurrencyRepository,
    @Autowired private val redisService: RedisService,
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
        val entity = concurrencyRepository.save(
            ConcurrencyEntity(
                name = "Spin Lock",
            )
        )

        Then("5개의 Thread 로 1,000번의 Business Logic 을 비동기 로 실행 하고 검증 한다.").config(
            threads = 5,
            invocations = 1000,
            enabled = true,
        ) {
            redisService.increaseCountWithSpinLock(entity.id)
        }.let {
            concurrencyRepository.findByIdOrNull(entity.id)
                ?.let {
                    it.count shouldBe 1000
                }
        }
    }

    Given("동시성 처리를 위해 Redis 에 Try Lock 을 적용한 뒤") {
        val entity = concurrencyRepository.save(
            ConcurrencyEntity(
                name = "Try Lock",
            )
        )

        Then("5개의 Thread 로 1,000번의 Business Logic 을 비동기 로 실행 하고 검증 한다.").config(
            threads = 5,
            invocations = 1000,
            enabled = true,
        ) {
            redisService.increaseCountWithTryLock(entity.id)
        }.let {
            concurrencyRepository.findByIdOrNull(entity.id)
                ?.let {
                    it.count shouldBe 1000
                }
        }
    }

})
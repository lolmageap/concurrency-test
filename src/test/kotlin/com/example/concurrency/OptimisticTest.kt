package com.example.concurrency

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

/**
 * Optimistic Lock 은 트랜잭션을 시작할 때 버전을 체크하고, 트랜잭션을 종료할 때 버전을 체크하여
 * 트랜잭션을 시작할 때와 종료할 때 버전이 다르면 예외를 발생시킨다.
 * 그렇기에 트랜잭션을 종료할 때까지 lock 이 걸려있지 않기 때문에 데드락에 대한 이슈가 없다.
 * 실시간 요청이 많아 동시성 충돌이 많다면 성능은 Pessimistic Lock 보다 더 떨어 질 수 있다.
 */

@SpringBootTest
internal class OptimisticTest(
    @Autowired private val optimisticService: OptimisticService,
    @Autowired private val optimisticRepository: OptimisticRepository,
) : StringSpec({

    beforeTest {
        optimisticRepository.deleteAllInBatch()

        optimisticRepository.save(
            OptimisticEntity(
                id = 1,
                name = "name",
            )
        )
    }

    """5개의 Thread 로 1,000번의 Business Logic 을 비동기 로 실행 하여 Optimistic Lock 으로 동시성 을 최대한 제어 한다.""".config(
        threads = 5,
        invocations = 1000
    ) {
        optimisticService.increaseCountWithOptimisticLock(1)
    }.let {
        optimisticRepository.findByIdOrNull(1)
            ?.let {
                it.likeCount shouldBe 1000
            }
    }

})
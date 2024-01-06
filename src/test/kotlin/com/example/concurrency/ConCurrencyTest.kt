package com.example.concurrency

import com.example.concurrency.Prefix.*
import io.kotest.core.spec.style.BehaviorSpec
import io.kotest.matchers.longs.shouldBeLessThan
import io.kotest.matchers.shouldBe
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.repository.findByIdOrNull

/**
 * Pessimistic Lock 은 transaction 을 시작할 때 해당 row 에 lock 을 걸고, 트랜잭션이 종료될 때 lock 을 해제 한다.
 * 다른 트랜잭션이 해당 row 에 접근 하려고 하면 lock 이 걸려 있기 때문에 대기 하게 된다.
 * 단편적인 성능은 Optimistic Lock 보다 더 좋지만, 대기 하는 시간이 길어 지면 성능이 떨어 진다.
 * 트랜잭션을 종료할 때 lock 을 해제 하기 때문에 트랜잭션을 종료할 때까지 lock 이 걸려 있어 데드락 에 대한 이슈가 존재 한다.
 * recovery 를 위해 timeout 을 설정할 수 있지만, 이는 데드락 을 해결 하는 것이 아니라 데드락 을 방지 하는 것이다.
 * 그렇기에 데드락 이 발생할 수 있는 상황 에서는 사용 하지 않는 것이 좋다.
 *
 * MySQL 과 PostgreSQL 은 서로 다른 락킹 메커니즘 을 사용 합니다. MySQL 에서는 InnoDB 스토리지 엔진이 사용 되며,
 * 이는 행 기반 락킹(ROW level locking)과 인덱스 레코드 락킹을 모두 지원 합니다. 따라서 MySQL 에서는 인덱스 레코드 에 락을 걸 수 있습니다.
 * 이렇게 되면, 잘못 설계된 인덱스 나 쿼리로 인해 데드락 이 발생할 수 있습니다.
 *
 * 반면에 PostgreSQL 에서는 MVCC(Multi-Version Concurrency Control)를 사용 하여 동시성 을 관리 합니다.
 * PostgreSQL에서는 행 레벨 락킹을 주로 사용 하며, 이는 각 트랜잭션이 데이터 의 버전에 대해 작업을 수행함 으로써 여러 트랜잭션 사이의 충돌을 방지 합니다.
 * 한 트랜잭션에서 변경 되는 데이터 는 다른 트랜잭션에 영향을 주지 않습니다.
 *
 * PostgreSQL 에서 락은 다음과 같이 여러 가지 유형이 있습니다.
 * ROW SHARE
 * ROW EXCLUSIVE
 * SHARE
 * SHARE ROW EXCLUSIVE
 * EXCLUSIVE
 * ACCESS EXCLUSIVE
 */


@SpringBootTest
internal class ConCurrencyTest(
    @Autowired private val concurrencyService: ConCurrencyService,
    @Autowired private val conCurrencyRepository: ConCurrencyRepository,
) : BehaviorSpec({

    // given 절 전에 clean up
    beforeContainer {
        if (it.prefix === GIVEN) {
            conCurrencyRepository.deleteAllInBatch()
        }
    }

    // when 절 전에 clean up
    beforeEach {
        if (it.prefix === WHEN) {
            conCurrencyRepository.deleteAllInBatch()
        }
    }

    Given("동시성 처리를 위해 Lock 을 걸지 않은 뒤") {
        val entity = conCurrencyRepository.save(
            ConCurrencyEntity(
                name = "No Lock",
            )
        )

        Then("5개의 Thread 로 1,000번의 Business Logic 을 비동기 로 실행 하고 검증 한다.").config(
            threads = 5, invocations = 1000
        ) {
            concurrencyService.increaseCountNoLock(entity.id)
        }.let {
            conCurrencyRepository.findByIdOrNull(entity.id)?.let {
                it.likeCount shouldBeLessThan 800
            }
        }
    }

    Given("동시성 처리를 위해 Pessimistic Lock 적용한 뒤") {
        val entity = conCurrencyRepository.save(
            ConCurrencyEntity(
                name = "Pessimistic Lock",
            )
        )

        Then("5개의 Thread 로 1,000번의 Business Logic 을 비동기 로 실행 하고 검증 한다.").config(
            threads = 5, invocations = 1000
        ) {
            concurrencyService.increaseCountWithPessimisticLock(entity.id)
        }.let {
            conCurrencyRepository.findByIdOrNull(entity.id)?.let {
                it.likeCount shouldBe 1000
            }
        }
    }

})
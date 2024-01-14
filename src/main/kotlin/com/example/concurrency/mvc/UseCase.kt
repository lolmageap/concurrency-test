package com.example.concurrency.mvc

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class UseCase(
    private val writeService: WriteService,
    private val readService: ReadService,
) {
    /**
     * readDB 에서 읽고 writeDB에 쓰는 작업을 수행 하려면 propagation = REQUIRES_NEW 와 @Modifying 을 사용 하면 가능 하다.
     * 그러면 조회가 readDB 에서만 발생 하고 writeDB 에서는 발생 하지 않는다.
     * 하지만 이 방법은 dirty checking 이 적용 되지 않아서 사이드 이펙트 가 발생할 수 있다.
     * 또한 수정 작업시 동기화 이슈가 발생할 수 있기에 @Modifying 은 권장 되지 않는다.
     *
     * 결론적 으로 수정 작업을 할 때는 writeDB 에서 읽고 쓰기 작업 을 해야 한다.
     * ChainedTransactionManager 를 사용 하여 readDB 와 writeDB 를 분리 하여 사용 할 수 있지만 스프링 에서는 이 방법을 권장 하지 않는다. (disable 되었음)
     * readDB와 writeDB 간에 동기화 이슈가 발생할 수 있기 때문 이다.
     * 그래서 일반적 으로 구성을 할 때 readService 와 useCase 는 transactional(readOnly = true)로 설정 하고
     * writeService 는 transactional(propagation = REQUIRES_NEW, readOnly = false)로 설정 하는 것이 좋아 보인다.
     * 이렇게 하면 쓰기 작업 때만 새로운 transaction 이 열려 writeDB의 커넥션 을 최대한 짧게 유지할 수 있다.(부하 감소)
     * 필요(성능)에 따라 @Modifying 을 사용 하여 dirty checking 을 적용 하지 않는 것이 좋다.
     * 일반적 으로 update 작업을 할 땐 무거운 조인이 발생 하지 않고 pk를 이용한 조회 후
     * 수정 작업을 하므로 이런 방식도 부하를 최소한 으로 할 수 있을것 이라고 예상 한다.
     * 만약 pk가 아닌 다른 컬럼을 이용한 수정 작업을 한다면 readDB 에서 pk를 조회한 후 작업을 하는 것이 masterDB 의 부하를 최대한 줄여줄 것으로 예상 한다.
     */

    fun readAndWrite(id: Long, name: String) {
        readService.get(id)
        writeService.update(id, name)
    }

    @Transactional(readOnly = true)
    fun transactionTest(id: Long, name: String) {
        readService.get(id)
        writeService.update(id, "first")
        writeService.updateForce(id, "second")
        writeService.throwException(id, "throw")
    }

}
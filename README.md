## Kotest로 동시성 이슈를 제어하는 예시 코드 및 Database Replication 설정 예시 입니다.

### Database 동시성 이슈

현재 프로젝트는 Lock에 대해서 다룹니다.

코루틴 스코프 내에서 여러개의 thread로 동시성 및 성능을 테스트합니다.

1. No Lock test
2. Pessimistic Lock test
3. Optimistic Lock test
4. Redis Spin Lock test
5. Redis RedLock test
---

### Database 이중화
번외로 여러개의 Database를 Connection 하고 Transaction의 ReadOnly 여부로 라우팅 제어하는 방법을 추가했습니다.

이처럼 설정을 하면 MasterDB와 SlaveDB를 @Transactional의 ReadOnly 유무로 라우팅 할 수 있습니다.

## 실행 방식
아래 명령어들이 끝날때마다 천천히 순차적으로 실행해주세요.

이중화된 Database를 Master, Slave 구조로 설정하고 쓰기 작업시 MasterDB의 데이터를 SlaveDB로 마이그레이션 해주도록 설정했습니다.

```shell
docker compose up
```

```shell
docker exec -it master_db bin/bash
mysql --password
```

```
master-password
```

```mysql
CREATE DATABASE test_database;
```

```
exit
```
```
exit
```

``` shell
docker exec -it slave_db bin/bash
mysql --password
```

```
slave-password
```

```mysql
CHANGE MASTER TO
    MASTER_HOST='master_db',
    MASTER_USER='root',
    MASTER_PASSWORD='master-password',
    MASTER_PORT=3306,
    MASTER_LOG_FILE='mysql-bin.000001',
    MASTER_LOG_POS=0,
    MASTER_CONNECT_RETRY=10;

START SLAVE;
```

```
exit
```
```
exit
```

## TODO
- 멀티 모듈로 나눠서 R2DBC도 transaction으로 제어하기

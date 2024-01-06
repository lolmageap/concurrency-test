## Kotest로 동시성 이슈를 제어하는 예시 코드입니다.
번외로 여러개의 Database를 Connection 하고 Transaction의 ReadOnly 여부로 라우팅을 제어하는 방법까지 추가했습니다.

이처럼 설정을 하면 MasterDB와 SlaveDB를 나누고 @Transactional의 ReadOnly 유무로 손쉽게 핸들링 할 수 있습니다.

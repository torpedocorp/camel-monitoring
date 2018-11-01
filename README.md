# camel-monitoring

camel-monitoring은 camel 기반의 어플리케이션을 모니터링 하기 위한 에이전트 컴포넌트 입니다.
<!--more-->
jpa, rest api 를 사용하여 모니터링 데이터를 수집
<!--more-->
hawtio를 사용하여 JMX 모니터링을 할 수 있음

## feature

 - bizframe-mas 어플리케이션 컨테이너 상태 모니터링
 - bizframe-mas Application 시작/중지
 - bizframe-mas config 조회
 - camel 어플리케이션 context, route 트리 조회
 - camel 어플리케이션 라우트 상태 조회 및 제어
 - camel exchange 정보 수집
 - camel exchange 별 Trace 정보 모니터링

## Building from the source

 1. jdk 8 이상 설치 
 2. apache ant 설치
 3. camel-monitoring 프로젝트 저장소에서 소스를 clone 혹은 다운르도 (https://github.com/torpedocorp/camel-monitoring)
 4. 커맨드 라인상에서 ant build 수행

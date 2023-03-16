# Server-Sent Events(SSE) 기술

- 클라이언트와 서버가 연결 후 서버에서 클라이언트로 text message를 보내는 브라우저 기반 웹 애플리케이션 기술
- HTTP의 persistent connections를 기반으로하는 HTML5 표준 기술

1. Client: SSE Subscribe 요청
	```
	GET /sse/subscribe HTTP/1.1
	Accept: text/event-stream
	Cache-Control: no-cache
	Connection: keep-alive
	```
	> 클라이언트에서 서버의 이벤트를 구독하기 위한 요청
	> 요청 미디어 타입은 text/event-stream이 표준으로 캐싱하지 않으며 지속적 연결을 사용해야 함
	> * HTTP 1.1에서는 기본적으로 지속 연결을 사용함
2. Server: Subscription에 대한 응답
	```
	HTTP/1.1 200
	Content-Type: text/event-stream;charset=UTF-8
	Transfer-Encoding: chunked
	```
	> 응답 미디어 타입은 text/event-stream
	> Transfer-Encoding 헤더의 값을 chunked로 설정 필요
	> : 서버는 동적으로 생성된 컨텐츠를 스트리밍하기 때문에 본문의 크기를 미리 알 수 없으므로
3. Server: 이벤트 전달
   > 클라이언트에서 Subscribe를 한 후 서버는 해당 클라이언트에게 비동기적으로 데이터를 전송할 수 있음
   > 데이터는 UTF-8로 인코딩된 텍스트 데이터만 가능 (바이너리 데이터는 전송 불가)
   > * 서로 다른 이벤트는 줄바꿈 문자 두개(\n\n)
   > 각각의 이벤트는 한 개 이상의 name: value 필드로 구성되며, 줄바꿈 문자 하나로 구분
   > ```
   > event: type1
   > data: An event of type1
   > 
   > event: type2
   > data: An event of type2

## 트러블슈팅
1. WEB -> Server subscribe 시 http status 503 Service Unavailable 발생
   > Server에서 SseEmitter 연결 성공에 대한 첫번째 응답을 내려주지 않아 WEB에서 서버 응답을 받지 못해 발생
   > WEB에서 text/event-stream request 후 status가 pending 상태로 서버 메시지 수신을 대기하다 발생하게 됨
   > 따라서, 서버 subscribe 처리 후 연결 성공에 대한 메시지 응답하면 200 status 확인되며 연결 됨

# 참고 사이트
* [demo-spring-sse](https://github.com/aliakh/demo-spring-sse)

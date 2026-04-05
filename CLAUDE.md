# nadle-backend

## 기술 스택
- Java 21
- Spring Boot 3.2.4
- Maven
- H2 In-memory DB
- Spring Data JPA

## 코드 컨벤션

### 응답 형식
- 모든 Controller 메서드는 반드시 `ResponseEntity`를 반환한다.

```java
public ResponseEntity<User> getUser(@PathVariable Long id) {
    return ResponseEntity.ok(userService.findById(id));
}
```

### 예외 처리
- 예외 처리는 `@ControllerAdvice` 클래스에서 통일하여 처리한다.
- 개별 Controller에 `try-catch`를 남기지 않는다.

```java
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(EntityNotFoundException e) { ... }
}
```

### 주석
- 모든 주석은 한국어로 작성한다.

## 패키지 구조
```
com.nadle.backend
├── controller   # HTTP 요청 처리
├── service      # 비즈니스 로직
├── repository   # DB 접근
└── entity       # JPA 엔티티
```

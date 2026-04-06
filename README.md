# Nadle Backend

공공데이터 API를 활용한 자전거 대여소 조회 백엔드 서버입니다.

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 21 |
| Spring Boot | 3.2.4 |
| Build Tool | Maven |
| Database | H2 In-memory |
| ORM | Spring Data JPA |

## 시작하기

### 사전 요구사항

- JDK 21 이상
- Maven 3.x
- [공공데이터포털](https://www.data.go.kr) 자전거 대여소 API 키

### 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성하고 API 키를 설정합니다.

```bash
cp .env.example .env
```

```
BIKE_API_KEY=발급받은_서비스키
```

### 실행

```bash
mvn spring-boot:run
```

서버가 실행되면 `http://localhost:8080` 에서 접근 가능합니다.

H2 콘솔: `http://localhost:8080/h2-console`

## API 명세

### 근처 자전거 대여소 목록 조회

현재 위치 기준 반경 내 자전거 대여소 목록을 거리 순으로 반환합니다.

```
GET /api/v1/stations/nearby
```

**Query Parameters**

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| lat | Double | Y | 현재 위치 위도 |
| lng | Double | Y | 현재 위치 경도 |
| radius | Integer | N | 검색 반경(m), 기본값 1000, 최대 5000 |

**응답 예시**

```json
{
  "status": "success",
  "message": "대여소 조회 성공",
  "data": [
    {
      "stationId": "ST-001",
      "stationName": "서울역 앞",
      "lat": 37.5546,
      "lng": 126.9707,
      "rackTotCnt": null,
      "parkingBikeTotCnt": 5,
      "distance": 123.4
    }
  ]
}
```

---

### 자전거 대여소 상세 조회

특정 대여소의 상세 정보를 반환합니다.

```
GET /api/v1/stations/{stationId}
```

**Path Variables**

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| stationId | String | 대여소 고유 ID (공공데이터 기준) |

**응답 예시**

```json
{
  "status": "success",
  "message": "대여소 상세 조회 성공",
  "data": {
    "stationId": "ST-001",
    "stationName": "서울역 앞",
    "address": "서울특별시 중구 세종대로 지하2",
    "lat": 37.5546,
    "lng": 126.9707,
    "totalSlots": null,
    "availableBikes": 5,
    "operatingHours": "00:00 ~ 24:00",
    "status": "ACTIVE"
  }
}
```

**대여소 상태값**

| 값 | 설명 |
|----|------|
| ACTIVE | 대여 가능한 자전거가 1대 이상 존재 |
| INACTIVE | 대여 가능한 자전거 없음 |

## 패키지 구조

```
com.nadle.backend
├── controller   # HTTP 요청 처리
├── service      # 비즈니스 로직
├── repository   # DB 접근
├── entity       # JPA 엔티티
├── dto          # 요청/응답 DTO
├── exception    # 예외 처리
└── config       # 설정 클래스
```

## 외부 API

공공데이터포털 **공영자전거 대여소 현황** API (`pbdo_v2`)를 사용합니다.

| 엔드포인트 | 설명 |
|-----------|------|
| `inf_101_00010001_v2` | 대여소 기본정보 (주소, 운영시간) |
| `inf_101_00010002_v2` | 대여소 현황 (위치, 대여 가능 자전거 수) |

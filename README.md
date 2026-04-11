# Nadle Backend

공공데이터 API와 AI를 활용한 자전거 여행 코스 추천 백엔드 서버입니다.

자전거 대여소 조회, 주변 관광지/상권 탐색, AI 기반 여행 코스 추천, 관광지 OX 퀴즈 기능을 제공합니다.

## 기술 스택

| 항목 | 버전 |
|------|------|
| Java | 21 |
| Spring Boot | 3.2.4 |
| Build Tool | Maven |
| Database | H2 In-memory |
| ORM | Spring Data JPA |
| AI | Groq API (llama-3.1-8b-instant) |
| 배포 | Render (Docker) |

## 시작하기

### 사전 요구사항

- JDK 21 이상
- Maven 3.x
- [공공데이터포털](https://www.data.go.kr) API 키 (자전거, 관광, 상권)
- [Groq](https://console.groq.com) API 키

### 환경 변수 설정

`src/main/resources/application-secret.yml` 파일을 생성합니다.

```yaml
bike-api:
  service-key: 발급받은_자전거_API_키

tour-api:
  service-key: 발급받은_관광_API_키

store-api:
  service-key: 발급받은_상권_API_키

groq-api:
  api-key: 발급받은_Groq_API_키
```

### 실행

```bash
mvn spring-boot:run
```

서버가 실행되면 `http://localhost:8080` 에서 접근 가능합니다.

H2 콘솔: `http://localhost:8080/h2-console`

## API 명세

### 자전거 대여소 목록 조회

현재 위치 기준 반경 내 자전거 대여소 목록을 반환합니다.

```
GET /api/v1/stations/nearby
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| lat | Double | Y | 현재 위치 위도 |
| lng | Double | Y | 현재 위치 경도 |
| radius | Integer | N | 검색 반경(m), 기본값 1000, 최대 5000 |

---

### 자전거 대여소 상세 조회

```
GET /api/v1/stations/{stationId}
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| stationId | String | 대여소 고유 ID |

---

### 주변 관광지 목록 조회

현재 위치 기준 반경 내 관광지 목록을 반환합니다.

```
GET /api/v1/spots/nearby
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| lat | Double | Y | 위도 |
| lng | Double | Y | 경도 |
| radius | Integer | N | 검색 반경(m), 기본값 3000 |
| category | String | N | `TOUR` / `FOOD` / `CULTURE` / `NATURE` |
| page | Integer | N | 페이지 번호, 기본값 1 |
| size | Integer | N | 페이지당 개수, 기본값 20 |

---

### 관광지 상세 조회

```
GET /api/v1/spots/{spotId}
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| spotId | String | 관광지 고유 ID (contentId) |

---

### 관광지 OX 퀴즈 생성

관광지 소개글을 기반으로 AI가 OX 퀴즈 1개를 생성합니다.

```
GET /api/v1/spots/{spotId}/quiz
```

| 파라미터 | 타입 | 설명 |
|---------|------|------|
| spotId | String | 관광지 고유 ID (contentId) |

**응답 예시**

```json
{
  "isSuccess": true,
  "code": 200,
  "message": "퀴즈 생성 성공",
  "result": {
    "question": "경복궁은 조선 시대 최초로 건립된 궁궐이다.",
    "answer": true,
    "explanation": "경복궁은 1395년 태조 이성계가 건립한 조선 최초의 궁궐입니다."
  }
}
```

---

### 주변 상권 조회

현재 위치 기준 반경 내 상권(음식점 등) 목록을 반환합니다.

```
GET /api/v1/stores/nearby
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| lat | Double | Y | 위도 |
| lng | Double | Y | 경도 |
| radius | Integer | N | 검색 반경(m), 기본값 500, 최대 1000 |
| category | String | N | 업종 코드, 기본값 `I2` (음식점) |

---

### AI 여행 코스 추천

대여소 위치 기반으로 AI가 자전거 여행 코스를 추천합니다.

```
GET /api/v1/routes/recommend
```

| 파라미터 | 타입 | 필수 | 설명 |
|---------|------|------|------|
| stationLat | Double | Y | 기준 대여소 위도 |
| stationLng | Double | Y | 기준 대여소 경도 |
| duration | Integer | N | 여행 예상 시간(분), 기본값 120 |
| requirements | String | N | 요구사항 (예: 아이와 함께하는 가족 코스) |

**응답 예시**

```json
{
  "isSuccess": true,
  "code": 200,
  "message": "AI 코스 추천 성공",
  "result": {
    "duration": 120,
    "aiSummary": "서울 도심에서 즐기는 역사와 자연 자전거 여행 코스",
    "spots": [
      {
        "contentId": "126508",
        "title": "경복궁",
        "sequence": 1,
        "reason": "서울의 대표 역사 관광지로 자전거 접근이 용이합니다.",
        "mapx": 126.977041,
        "mapy": 37.579617
      }
    ]
  }
}
```

## 패키지 구조

```
com.nadle.backend
├── controller   # HTTP 요청 처리
├── service      # 비즈니스 로직 및 외부 API 연동
├── dto          # 요청/응답 DTO
├── exception    # 예외 처리
└── config       # RestTemplate, WebMvc 설정
```

## 외부 API

| API | 설명 |
|-----|------|
| 공공데이터포털 공영자전거 대여소 현황 | 대여소 위치, 대여 가능 자전거 수 |
| 한국관광공사 관광 정보 서비스 | 주변 관광지 목록 및 상세 정보 |
| 소상공인 상권정보 서비스 | 주변 음식점 등 상권 정보 |
| Groq API (llama-3.1-8b-instant) | AI 여행 코스 추천, OX 퀴즈 생성 |

## 배포

Render에 Docker 기반으로 배포됩니다.

```
Base URL: https://nadle-backend.onrender.com
```

Render 환경 변수 설정 필요:

| Key | 설명 |
|-----|------|
| `BIKE_API_KEY` | 공공자전거 API 키 |
| `TOUR_API_KEY` | 관광 API 키 |
| `STORE_API_KEY` | 상권 API 키 |
| `GROQ_API_KEY` | Groq API 키 |

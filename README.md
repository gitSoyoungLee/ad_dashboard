# Ad Dashboard

## ⚠️ 안내 및 출처 (Notice & Disclaimer)

본 프로젝트는 작성자의 **인턴십(스타트업 '인톡') 당시 데이터 분석 업무를 수행하며 느꼈던 실제 현업의 불편함**을 기술적으로 해결하기 위해 개인적으로 기획하고 개발한 토이
프로젝트입니다.

- **데이터 보안:** 실제 기업의 내부 DB 스키마나 소스 코드를 참고하거나 복제하지 않았습니다.
- **독자적 설계:** 본 프로젝트의 데이터 모델 및 API 구조는 Meta Graph API 공식 문서와 일반적인 백엔드 설계 패턴을 기반으로 새롭게 설계되었습니다.
- **가공 데이터:** 대시보드에 노출되는 모든 수치와 캠페인 데이터는 테스트 목적으로 생성된 가상 데이터 또는 메타 개발자 샌드박스 데이터입니다.

---

Meta 광고 지출 데이터와 내부 전환 데이터를 통합하여 실질적인 광고 효율(CPA) 및 리드 가치를 분석하는 대시보드 시스템입니다.

<img width="1918" height="837" alt="image" src="https://github.com/user-attachments/assets/f5affa82-d91e-47fb-bad5-d396ba2ab44f" />


## 프로젝트 배경

Meta Ads Manager의 지출/결과 데이터와 서비스 내부의 가입/리드 데이터가 파편화되어 있어, 매일 수동으로 서로 다른 사이트에서 데이터를 확인하고 취합하는 비효율이
발생했습니다. 이 과정에서 리소스 낭비와 데이터 정합성 오류 가능성도 존재했습니다.

**"측정할 수 없으면 관리할 수 없다"** 는 원칙하에, 데이터 수집을 자동화하고 도메인 특화 지표를 한눈에 볼 수 있는 대시보드를 구축합니다.

## 비즈니스 도메인

| 광고 유형           | 설명                  | 추적 방식            |
|-----------------|---------------------|------------------|
| **트래픽/전환**      | 사이트 접속 후 공식 회원가입 유도 | UTM 파라미터 기반      |
| **잠재고객(DB 광고)** | 연락처 직접 획득           | 유효성 검증 후 회원으로 등록 |

## 핵심 지표

단순 조회가 아닌 **의사결정을 위한 지표**로 구성되어 있습니다.

- **효율(Efficiency):** CTR (클릭수/노출수), CPC (지출액/클릭수)
- **비용(Cost):** Budget (예산), Amount Spent (지출액)
- **전환(Conversion):** CPA (지출액/가입수), CVR (가입수/클릭수) - 내부 데이터 기반

## 대시보드 구성

### 1. 통합 성과 요약 (Summary)

최상단에서 전체 광고 계정의 건강 상태를 한눈에 파악합니다.

- **지표:** 회원가입 수, 유효 리드 수, 총 지출 금액, 통합 CAC, 사이트 유입 수
- **기간 필터:** 1일 / 7일(기본) / 14일 / 30일 / 90일
- **핵심 가치:** 엑셀 취합 없이 실시간으로 목표 CAC 대비 현재 수치를 비교하여 광고 중단/증액 결정

### 2. 캠페인 유형별 드릴다운 (Campaign Drill-down)

광고 목적에 따라 서로 다른 KPI를 관리합니다.

| 캠페인 유형    | 주요 지표                  | 데이터 출처       | 표시 목적                     |
|-----------|------------------------|--------------|---------------------------|
| **트래픽**   | 지출액, 클릭수, CTR, CPC     | Meta API     | 소재 매력도 및 클릭 단가 판별         |
| **전환**    | 지출액, 회원가입 수, CPA, CTR  | Meta + 내부 DB | **실제 DB 기준** 가입자 획득 단가 파악 |
| **DB 광고** | 지출액, 유효 리드 수, CPA, CTR | Meta + 내부 DB | **유효 데이터 품질** 기준 성과 측정    |

### 3. 광고 소재 레벨 분석 (Ad-Level View)

캠페인 내 개별 소재별 성과를 비교합니다.

- A/B 테스트 결과 판별: 어떤 이미지/문구가 전환율이 높은지 비교
- 좀비 광고 탐지: 예산만 소진하고 전환이 없는 저효율 소재 식별

### 4. 시계열 추이 그래프 (Time-series)

최근 30일간 지출액, 노출수, 클릭수의 추이를 시각화합니다.

- 이상 징후 포착: 지출 대비 노출 급감, 클릭률 하락 추세 선제 대응
- 요일별 패턴 분석: 특정 요일 효율 집중 여부 확인

## 기술 스택

| 구분           | 기술                          |
|--------------|-----------------------------|
| Language     | Java 17                     |
| Framework    | Spring Boot 4.0.5           |
| ORM          | Spring Data JPA (Hibernate) |
| Database     | MySQL 8.x                   |
| HTTP Client  | Spring RestClient           |
| Build        | Gradle                      |
| External API | Meta Graph API v25.0        |

## 프로젝트 구조

```
src/main/java/io/soyoung/addashboard/
├── client/                  # 외부 API 클라이언트
│   └── MetaApiClient        # Meta Graph API 연동
├── controller/              # REST API 엔드포인트
│   ├── StatsController      # 대시보드 통계 API
│   ├── LeadController       # 리드 관리 API
│   ├── UserController       # 유저 조회 API
│   └── SyncController       # 데이터 동기화 API
├── service/                 # 비즈니스 로직
│   ├── DashboardService     # 요약 지표 및 트렌드 계산
│   ├── CampaignService      # 캠페인별 성과 분석
│   ├── AdService            # 소재별 성과 분석
│   └── SyncService          # Meta API 데이터 동기화
├── repository/              # 데이터 접근 계층
├── entity/                  # JPA 엔티티 (6개) + Enum
└── dto/                     # 요청/응답 DTO
```

## API 명세

### 통계 API (`/api/v1/stats`)

| Method | Endpoint                                               | 설명                           |
|--------|--------------------------------------------------------|------------------------------|
| GET    | `/summary?startDate={}&endDate={}`                     | 통합 성과 요약 (지출, 가입수, 리드수, CAC) |
| GET    | `/trends?endDate={}`                                   | 최근 30일 시계열 추이                |
| GET    | `/campaigns?startDate={}&endDate={}&type={}&sortBy={}` | 캠페인별 성과 목록                   |
| GET    | `/campaigns/{campaignId}/ads?startDate={}&endDate={}`  | 캠페인 내 소재별 성과                 |

### 리드/유저 API

| Method | Endpoint                                    | 설명       |
|--------|---------------------------------------------|----------|
| GET    | `/api/v1/leads?status={}&metaCampaignId={}` | 리드 목록 조회 |
| GET    | `/api/v1/users?utmCampaign={}`              | 유저 목록 조회 |

### 동기화 API

| Method | Endpoint            | 설명                 |
|--------|---------------------|--------------------|
| POST   | `/api/v1/sync/meta` | Meta 광고 데이터 수동 동기화 |

## 데이터 모델

```
AdEntity (캠페인/광고)
  ├── AdInsightRaw (일별 성과 원시 데이터)
  └── DailyPerformanceSummary (일별 성과 요약)

User (회원)                    Lead (잠재고객)
  └── Conversion (전환 이벤트) ←──┘
```

- **AdEntity:** 캠페인/광고 메타데이터 (유형: TRAFFIC, CONVERSION, DB_AD)
- **AdInsightRaw:** Meta API에서 수집한 일별 지출/노출/클릭 원시 데이터
- **User:** UTM 파라미터 기반 가입 유저 (signupPath: DIRECT, VIA_LEAD)
- **Lead:** DB 광고를 통해 획득한 리드 (status: NEW, VERIFIED, REJECTED)
- **Conversion:** 내부 전환 이벤트 (type: DIRECT_SIGNUP, LEAD_SIGNUP)

## 시작하기

### 사전 요구사항

- Java 17+
- MySQL 8.x
- Meta 광고 계정 및 API 액세스 토큰 : 메타 개발자 도구에서 테스트 앱으로 개발

### 환경 변수 설정

프로젝트 루트에 `.env` 파일을 생성합니다:

```properties
DB_USERNAME=root
DB_PASSWORD=your_password
META_AD_ACCOUNT_ID=act_XXXXXXXXX
META_ACCESS_TOKEN=your_access_token
```

### 실행

```bash
./gradlew bootRun
```

서버가 시작되면 `http://localhost:8080`에서 API를 사용할 수 있습니다.

## 아키텍처 설계 원칙

- **트랜잭션 안전성:** 외부 API 호출은 트랜잭션 밖에서, DB 쓰기는 트랜잭션 안에서 수행
- **Upsert 패턴:** `(metaId, logDate)` 유니크 제약으로 중복 데이터 방지
- **N+1 최적화:** 배치 JPQL 쿼리로 집계 연산 수행 (루프 내 개별 쿼리 제거)
- **실질 전환 기반:** Meta API의 '결과' 대신 내부 DB의 실제 전환 데이터로 CPA 산출

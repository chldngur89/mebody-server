# MEBODY Server

MEBODY의 웹 홈페이지, 웹 관리자 화면, 회원/관리자 API를 담당하는 Spring Boot 서버입니다. 기존 Vite/React 진단 앱은 `3000` 모바일 앱 전용으로 유지하고, 이 서버는 `8000`에서 웹 화면과 API를 함께 제공합니다.

## 현재 범위

- 일반회원 API: 내 정보, 내 몸BTI, 미션 요약
- 관리자 API: 대시보드 요약, 회원 목록/상세/수정/soft delete
- 판매자 API: 판매 기능 준비 중 shell 응답
- 상품 API: 공개 상품 목록/상세 조회
- Supabase Postgres 직접 연결
- Supabase Auth JWT 검증
- role/status/grade 기반 권한 분리
- Swagger UI 제공
- 웹 홈페이지 제공: `GET /`
- 웹 관리자 화면 제공: `GET /admin`

아직 실제 결제, 주문, 정산, 관리자 수동 Auth 회원 생성은 구현하지 않았습니다.

## Stack

- Java 17
- Spring Boot 3.3
- Spring Security OAuth2 Resource Server
- Spring Data JPA
- PostgreSQL / Supabase Postgres
- springdoc-openapi Swagger UI
- Maven

## Folder Structure

```text
Server/
  db/
    supabase_v1_foundation.sql
  src/main/java/com/mebody/
    common/      # config, security, exception, response
    user/        # member profile and dashboard data
    admin/       # admin dashboard, user CRUD, audit log
    product/     # product/store shell
    seller/      # seller shell
    mission/     # mission progress summary
    bodybti/     # bodyBTI result summary
```

## Database Migration

Supabase SQL Editor에서 먼저 실행하세요.

```text
Server/db/supabase_v1_foundation.sql
```

이 migration은 기존 `user_profiles`를 새로 갈아엎지 않고 `ALTER TABLE`로 확장합니다. 추가 테이블은 아래입니다.

- `body_bti_results`
- `missions`
- `user_mission_progress`
- `products`
- `admin_audit_logs`

회원 기본값은 다음으로 고정합니다.

- `role = MEMBER`
- `status = ACTIVE`
- `grade = BASIC`

## Environment Variables

로컬에서는 `.env.example`을 보고 shell 환경변수로 넣은 뒤 실행하세요. `.env` 파일은 git에 올리지 않습니다.

```bash
cd Server
cp .env.example .env
```

필수 값:

- `SUPABASE_DB_URL`: Supabase Postgres JDBC URL
- `SUPABASE_DB_USERNAME`: DB username
- `SUPABASE_DB_PASSWORD`: DB password
- `SUPABASE_JWT_SECRET` 또는 `SUPABASE_JWKS_URL`: Supabase access token 검증용
- `FRONTEND_ORIGIN`: Vite 앱 origin. 예: `http://localhost:3000`
- `SERVER_PORT`: 기본 `8000`

서버 전용 값:

- `SUPABASE_SERVICE_ROLE_KEY`: 추후 관리자 수동 회원 생성에만 사용. 프론트/Vite 환경변수에 절대 넣지 않습니다.
- `SUPABASE_ANON_KEY`: 서버 정적 웹 화면에서 Supabase Auth 로그인/회원가입을 호출할 때 사용합니다. service role key와 다르게 공개 가능한 anon key만 넣습니다.

## Run

```bash
cd Server
mvn spring-boot:run
```

Swagger:

- `http://localhost:8000/swagger-ui.html`
- `http://localhost:8000/api-docs`

웹 화면:

- `http://localhost:8000`
- `http://localhost:8000/admin`

## Build / Verify

```bash
cd Server
mvn test
```

현재는 DB 연결 없이 컴파일 가능한 구조까지 검증합니다. 실제 API 200/401/403 검증은 Supabase migration과 환경변수 설정 후 진행해야 합니다.

## Auth Flow

1. 프론트는 기존처럼 Supabase Auth로 로그인/회원가입합니다.
2. 프론트가 Supabase session의 `access_token`을 읽습니다.
3. 백엔드 API 호출 시 `Authorization: Bearer <access_token>`을 붙입니다.
4. 서버는 JWT를 검증하고 `user_profiles`에서 `role/status/grade`를 읽습니다.
5. `SUSPENDED` 또는 `DELETED` 사용자는 403으로 차단합니다.

## Roles

- `MEMBER`: 일반회원
- `SELLER`: 판매자
- `ADMIN`: 관리자

회원가입 화면에서는 role 선택을 노출하지 않습니다. 판매자/관리자 권한은 관리자만 변경하는 구조입니다.

## Main APIs

### Member

- `GET /api/me`
- `PATCH /api/me`
- `GET /api/me/body-bti`
- `GET /api/me/missions`
- `GET /api/me/summary`

### Admin

모두 `ADMIN`만 접근 가능합니다.

- `GET /api/admin/dashboard/summary`
- `GET /api/admin/users`
- `GET /api/admin/users/{id}`
- `POST /api/admin/users`
- `PATCH /api/admin/users/{id}`
- `DELETE /api/admin/users/{id}`

주의: `POST /api/admin/users`는 현재 501을 반환합니다. 기존 `user_profiles.id`가 `auth.users.id`를 참조하므로, profile만 수동 생성하면 FK가 깨질 수 있습니다. Supabase service role 기반 Auth 사용자 생성까지 연결한 뒤 활성화하는 게 맞습니다.

### Products

공개 조회입니다.

- `GET /api/products`
- `GET /api/products/{id}`

### Seller

`SELLER` 또는 `ADMIN`만 접근 가능합니다.

- `GET /api/seller/dashboard`

## Admin Delete Policy

회원 삭제는 hard delete가 아니라 soft delete입니다.

- `status = DELETED`
- `deleted_at = now()`
- `admin_audit_logs` 기록

## Frontend / Server Split

- `http://127.0.0.1:3000`: 모바일 진단 앱 전용
- `http://localhost:8000`: 웹 홈페이지
- `http://localhost:8000/admin`: 웹 관리자 화면
- 모바일 앱은 `VITE_API_BASE_URL=http://localhost:8000`으로 이 서버의 `/api/me`를 호출해 회원 프로필을 동기화합니다.

## Operational TODO

- Supabase service role로 관리자 수동 회원 생성 구현
- 관리자 API 통합 테스트 추가
- 상품 등록/이미지 업로드/주문/결제/정산 구현
- Vercel frontend와 별도 서버 배포 환경 정리
- production CORS origin 확정

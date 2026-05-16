# fraud-scoring-api

Secure transaction risk-scoring API built with Java 17, Spring Boot 3, PostgreSQL, JWT, Flyway, Swagger, and Docker.

## Tech Stack

- Java 17
- Spring Boot 3
- Spring Security + JWT
- PostgreSQL
- Flyway
- Spring Data JPA
- Swagger UI via springdoc-openapi
- Docker / Docker Compose

## Quick Start

### Local

1. Start PostgreSQL on `localhost:5432` with database `fraud_scoring`.
2. Run the app:

```bash
./mvnw spring-boot:run
```

3. Open Swagger:

```text
http://localhost:8080/swagger-ui/index.html
```

### Docker Compose

```bash
docker compose up --build
```

The app starts with PostgreSQL and seeds an admin user for demo access.

## Demo Flow in Swagger

1. `POST /api/auth/register`
2. `POST /api/auth/login`
3. `POST /api/transactions`
4. `POST /api/risk/score`
5. `GET /api/me`
6. `GET /api/admin/audit-logs` with an admin token

Use the `accessToken` from login in the `Authorization: Bearer <token>` header.

## Endpoints

- `POST /api/auth/register`
- `POST /api/auth/login`
- `POST /api/auth/refresh`
- `POST /api/auth/logout`
- `GET /api/me`
- `POST /api/transactions`
- `POST /api/risk/score`
- `GET /api/admin/audit-logs?page=&size=&action=`

## Rule List

- `AMOUNT_GT_10M` +30 if amount is above `10,000,000`
- `NEW_ACCOUNT_LT_3_DAYS` +20 if the account is younger than 3 days
- `VELOCITY_TX_10M_GT_5` +25 if there are more than 5 transactions in 10 minutes
- `TOTAL_24H_GT_100M` +20 if the last 24h total is above `100,000,000`
- `NIGHT_TIME` +10 if the transaction falls between 22:00-05:59
- `COUNTRY_CHANGED` +15 if the country differs from the previous transaction

Score is capped at 100 and mapped to:

- `LOW` for 0-29
- `MEDIUM` for 30-69
- `HIGH` for 70-100

## Sample Risk Response

```json
{
	"riskAssessmentId": 12,
	"transactionId": 8,
	"score": 55,
	"level": "MEDIUM",
	"reasons": [
		{
			"code": "AMOUNT_GT_10M",
			"points": 30,
			"description": "Transaction amount is above 10,000,000"
		}
	],
	"recommendation": "Review the transaction before approval.",
	"createdAt": "2026-05-17T08:00:00Z"
}
```

## Useful cURL

```bash
curl -X POST http://localhost:8080/api/auth/register \
	-H 'Content-Type: application/json' \
	-d '{"email":"user@example.com","password":"Password123!"}'
```

```bash
curl -X POST http://localhost:8080/api/transactions \
	-H 'Authorization: Bearer <accessToken>' \
	-H 'Content-Type: application/json' \
	-d '{"amount":12500000,"currency":"VND","country":"VN","ip":"1.2.3.4"}'
```

```bash
curl -X POST http://localhost:8080/api/risk/score \
	-H 'Authorization: Bearer <accessToken>' \
	-H 'Content-Type: application/json' \
	-d '{"transactionId":1}'
```

## Demo Admin

- Email: `admin@demo.local`
- Password: `Admin123!`

## Done Checklist

- [ ] Register works
- [ ] Login returns access + refresh tokens
- [ ] Create transaction as USER
- [ ] Score own transaction
- [ ] Reasons and recommendation returned
- [ ] Refresh token rotation works
- [ ] Logout revokes refresh token
- [ ] Admin audit logs page works
- [ ] Docker Compose starts app and database

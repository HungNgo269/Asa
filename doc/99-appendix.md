# 99 — PHỤ LỤC

### 99.1 – Quy ước Git và Pull Request
- Nhánh: `main` luôn xanh; làm việc trên nhánh `feat/<mã-bài>-<mô-tả>`; sửa lỗi `fix/...`; tài liệu `docs/...`.
- Commit theo **Conventional Commits**: `feat(2.9): add dynamic job search`, `fix(2.11): reject reused refresh token`, `test(1.7): cover JobSearch predicates`, `docs(5.6): server hardening notes`.
- Mỗi bài là một PR (từ Giai đoạn 2); mô tả PR theo mẫu:
  ```markdown
  ## Mục tiêu (mã bài)
  ## Thay đổi chính
  ## Cách kiểm tra (lệnh + kết quả mong đợi)
  ## Bằng chứng nghiệm thu (dán/ liên kết)
  ## Rủi ro & việc còn lại
  ```
- Checklist PR: build xanh, test mới/ cập nhật, không secret, tài liệu cập nhật, migration có phiên bản mới (không sửa cũ), OpenAPI cập nhật.

### 99.2 – Mẫu ADR (Architecture Decision Record)
```markdown
# ADR-<số>: <tiêu đề quyết định>
- Trạng thái: Đề xuất | Chấp nhận | Thay thế bởi ADR-xxx
- Ngày:
## Bối cảnh
## Các lựa chọn đã xét (kèm ưu/ nhược)
## Quyết định
## Hệ quả (tích cực, tiêu cực, rủi ro)
## Điều kiện để xem xét lại
```
ADR bắt buộc tối thiểu: ADR-001 kiến trúc 4 tầng + mapper (xem `01-architecture.md` §10); JWT + refresh rotation; tìm kiếm động bằng Specification; chiến lược cache; Next.js BFF + cookie; outbox; Spring AI vs thủ công; cách kết hợp luật + AI.

### 99.3 – Bảng ánh xạ NestJS/TypeScript ↔ Spring/Java (tham khảo nhanh)
| NestJS / TS | Spring / Java |
|---|---|
| Module | Package + `@Configuration` / component scan |
| Provider (`@Injectable`) | Bean (`@Component`, `@Service`, `@Repository`) |
| Controller + decorators | `@RestController` + mapping annotations |
| DTO + class-validator | Record + Bean Validation (`@Valid`) |
| Pipe | `Converter`, `Formatter`, `HandlerMethodArgumentResolver`, validation |
| Guard | Spring Security filter + `@PreAuthorize` |
| Interceptor | `HandlerInterceptor` / AOP (`@Aspect`) / Filter |
| Middleware | `Filter` / `OncePerRequestFilter` |
| Exception filter | `@RestControllerAdvice` + `@ExceptionHandler` |
| ConfigModule / `.env` | `application.yml` + `@ConfigurationProperties` + profiles |
| TypeORM / Prisma | Spring Data JPA / Hibernate (+ jOOQ nếu cần) |
| Migration (TypeORM CLI) | Flyway / Liquibase |
| Jest | JUnit 5 + AssertJ + Mockito |
| Supertest | MockMvc / `TestRestClient` |
| Bull / BullMQ | Spring AMQP (RabbitMQ) / Spring Kafka |
| `@nestjs/schedule` | `@Scheduled` |
| `@nestjs/cache-manager` | Spring Cache + Redis |
| Swagger module | springdoc-openapi |
| Passport JWT | Spring Security resource server / filter |
| Service (`@Injectable` chứa nghiệp vụ) | `application.XxxService` (use case) |
| Repository / TypeORM Repository | Port ở `domain` + adapter ở `infrastructure` |
| Entity (TypeORM) | Domain model **và** `XxxEntity` (JPA) tách riêng, nối bằng `Mapper` |

### 99.4 – Danh mục tài liệu chính thức nên tra cứu (không dùng trí nhớ)
Spring Boot Reference, Spring Framework Reference, Spring Data JPA, Spring Security, Spring AMQP, Spring AI; Hibernate ORM User Guide; Flyway Documentation; PostgreSQL Documentation (và pgvector README); Testcontainers; Resilience4j; Docker/ Compose Documentation; GitHub Actions Documentation; Caddy/Traefik/nginx Documentation; RabbitMQ Documentation (Reliability Guide, Quorum Queues); Next.js Documentation; React Hook Form, Zod, TanStack Query, Playwright, Vitest; OWASP (Top 10, API Security Top 10, Cheat Sheets); hướng dẫn dữ liệu có cấu trúc `JobPosting` của Google Search Central; Ollama Documentation và thư viện mô hình.

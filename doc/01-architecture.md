# 01 — KIẾN TRÚC CHUẨN CỦA WELDJOBS (áp dụng từ bài đầu tiên)

> **Chọn: Layered/Onion Architecture kiểu DDD-lite, 4 tầng** — `presentation` / `application` / `domain` / `infrastructure`.
> Đây là cách tổ chức phổ biến trong các dự án Java/Spring Boot nghiệp vụ ở Nhật (MVC mở rộng + tư tưởng DDD: tầng domain thuần, phụ thuộc trỏ vào trong).
> Không làm: CQRS, Event Sourcing, microservices, module hóa Maven nhiều module. Chỉ làm đúng 4 tầng + luật phụ thuộc, kiểm tra bằng ArchUnit.

## 1. Bốn nguyên tắc
1. **Dependency Rule:** phụ thuộc chỉ trỏ **vào trong**: `presentation → application → domain`; `infrastructure → application/domain` (cài đặt các interface do tầng trong định nghĩa). `domain` **không biết ai khác**.
2. **Domain thuần Java:** không Spring, không JPA, không Jackson, không Lombok-magic. Nghiệp vụ nằm trong domain (entity tự bảo vệ bất biến), không nằm ở controller/repository.
3. **Interface thuộc tầng trong, cài đặt ở tầng ngoài** (Port & Adapter): `JobRepository` là *port* ở `domain`; `JpaJobRepository` là *adapter* ở `infrastructure`.
4. **Một nơi duy nhất lắp ráp (composition root):** `config` + class `Main`/`WeldJobsApplication`. Giai đoạn 1 lắp bằng `new` tay; Giai đoạn 2 Spring làm hộ.

## 2. Bốn tầng làm gì

| Tầng | Chứa | Được phụ thuộc | Không được |
|---|---|---|---|
| `domain` | Entity, Value Object, Enum, **Repository interface (port)**, Domain Service (scorer, luật nghiệp vụ), Domain Exception | Chỉ JDK + `shared` | Spring, JPA, Jackson, `application`, `infrastructure`, `presentation`, `config` |
| `application` | **Use case** (Application Service), Command/Result, **Port ngoài domain** (`Notifier`, `PasswordHasher`, `JobSourceClient`, `EmailSender`…), điều phối transaction | `domain`, `shared`; Spring chỉ `@Service`, `@Transactional` | `infrastructure`, `presentation`, JPA, Jackson, HTTP |
| `infrastructure` | **Adapter**: JPA entity + mapper + repository adapter, JSON/in-memory repo, security/JWT, Redis, RabbitMQ, HTTP client, Ollama, scheduler | `domain`, `application`, thư viện bất kỳ | `presentation` |
| `presentation` | Controller/`@RestControllerAdvice`, Request/Response (web) hoặc Menu (console); validate đầu vào; map Request→Command, Result→Response | `application`, `shared`; enum của domain nếu cần hiển thị | `infrastructure`, **repository** trực tiếp, trả domain object/entity ra ngoài |
| `config` + root | `@Configuration`, lắp bean, `Main`/`WeldJobsApplication` | tất cả tầng (ngoại lệ duy nhất) | chứa nghiệp vụ |
| `shared` | Tiện ích thuần (`Text`…) | JDK | bất kỳ tầng nào |

## 3. Cây thư mục

### 3.1. Repo
```text
weldjobs/
 ├─ weldjobs-console/     Giai đoạn 1 (Java thuần)
 ├─ weldjobs-api/         Giai đoạn 2+ (Spring Boot) — mang domain/application từ console sang
 ├─ weldjobs-web/         Giai đoạn 4 (Next.js)
 └─ docs/                 ghi chú, ADR, runbook
```

### 3.2. Bên trong `weldjobs-console` và `weldjobs-api`
```text
src/main/java/com/weldjobs
 ├─ Main.java  (console)  |  WeldJobsApplication.java  (api)
 ├─ config/                         lắp bean (console: AppConfig bằng new; api: @Configuration)
 ├─ shared/                         Text, ...
 ├─ domain/
 │   ├─ model/
 │   │   ├─ common/                 Regions, Repository<T,ID>, PageQuery, PageResult
 │   │   ├─ skill/                  Skill, SkillCategory, SkillCatalog, SkillRepository
 │   │   ├─ user/                   User, Role, UserRepository
 │   │   ├─ worker/                 Worker, WorkerSkill, SkillLevel, Availability, Certificate, WorkerRepository
 │   │   ├─ employer/               Employer, EmployerRepository
 │   │   ├─ job/                    JobPost, JobRequiredSkill, JobStatus, JobType, SalaryRange,
 │   │   │                          JobSearchCriteria, SkillMatchMode, JobSearch, JobRepository
 │   │   └─ jobapplication/         JobApplication, ApplicationStatus, JobApplicationRepository
 │   ├─ service/                    JobScorer, BaseJobScorer, *Scorer, WeightedScorer, ScoreResult
 │   └─ exception/                  WeldJobsException, NotFoundException, ConflictException, ...
 ├─ application/
 │   ├─ skill/ worker/ employer/ job/ jobapplication/ matching/ auth/ analytics/   (XxxService + Command/Result)
 │   └─ port/                       Notifier, PasswordHasher, TokenService, JobSourceClient, EmailSender, SkillExtractor...
 ├─ infrastructure/
 │   ├─ persistence/
 │   │   ├─ inmemory/               InMemoryRepository, InMemoryXxxRepository         (GĐ1)
 │   │   ├─ json/                   JsonFileStore, JsonXxxRepository, XxxDocument, XxxMapper  (GĐ1)
 │   │   └─ jpa/<feature>/          XxxEntity, SpringDataXxxRepository, XxxMapper, XxxRepositoryAdapter  (GĐ2)
 │   ├─ security/  notification/  external/  scheduler/  cache/  messaging/  ai/  report/  seed/
 └─ presentation/
     ├─ console/                    MainMenu, ... (GĐ1)
     └─ web/
         ├─ <feature>/              XxxController, XxxRequest, XxxResponse           (GĐ2)
         └─ common/                 GlobalExceptionHandler (ProblemDetail), PageResponse
src/test/java/...                   cùng cây package; test builder (WorkerBuilder, JobBuilder); ArchitectureTest
```

> Cách chia: **tầng trước, tính năng sau** (layer → feature). Trong mỗi tầng, gom theo tính năng như trên.

## 4. Một use case đi xuyên các tầng (ví dụ: đổi trạng thái hồ sơ ứng tuyển)
```text
PATCH /api/v1/applications/{id}/status
 presentation.web.jobapplication.JobApplicationController
    ChangeStatusRequest ──map──> ChangeStatusCommand(applicationId, next, actorId)
 application.jobapplication.JobApplicationService.changeStatus(cmd)        @Transactional
    1. repo.findById(...)                       (port: domain.model.jobapplication.JobApplicationRepository)
    2. kiểm tra quyền sở hữu tin
    3. jobApplication.changeStatus(next)        (domain: luật chuyển trạng thái nằm TRONG entity)
    4. repo.save(...)  → trả JobApplicationResult
 infrastructure.persistence.jpa.jobapplication.JobApplicationRepositoryAdapter   (cài đặt port, dùng entity + mapper)
 presentation: JobApplicationResult ──map──> JobApplicationResponse  → 200
```
Điểm cần nhớ: **luật chuyển trạng thái nằm ở domain**, controller chỉ dịch HTTP, repository chỉ lưu/tải.

## 5. BẢNG VỊ TRÍ CLASS (tra cứu khi bài tập nhắc tên class)

| Class / loại | Vị trí |
|---|---|
| `Skill`, `SkillCategory`, `SkillCatalog` (dữ liệu mẫu) | `domain.model.skill` |
| `User`, `Role`, `UserRepository` | `domain.model.user` |
| `Worker`, `WorkerSkill`, `SkillLevel`, `Availability`, `Certificate`, `CertificateType` | `domain.model.worker` |
| `Employer` | `domain.model.employer` |
| `JobPost`, `JobRequiredSkill`, `JobStatus`, `JobType`, `SalaryRange`, `JobSearchCriteria`, `SkillMatchMode`, `JobSearch` | `domain.model.job` |
| `JobApplication`, `ApplicationStatus`, `ApplicationEvent` | `domain.model.jobapplication` |
| `XxxRepository` (interface), `Repository<T,ID>`, `PageQuery`, `PageResult`, `Regions` | cạnh aggregate / `domain.model.common` |
| `JobScorer`, `BaseJobScorer`, `*Scorer`, `WeightedScorer`, `ScoreResult` | `domain.service` |
| `WeldJobsException` và các exception nghiệp vụ | `domain.exception` |
| `JobService`, `JobApplicationService`, `MatchingService`, `AuthService`, `WorkerService`… | `application.<feature>` |
| `JobAnalytics`, `WorkerIndex`, `JobAggregator` | `application.analytics` / `application.matching` / `application.job` (import) |
| `Notifier`, `PasswordHasher`, `TokenService`, `JobSourceClient`, `EmailSender`, `SkillExtractor` (interface) | `application.port` |
| `InMemoryRepository`, `InMemoryXxxRepository` | `infrastructure.persistence.inmemory` |
| `JsonFileStore`, `JsonXxxRepository`, `XxxDocument`, `JsonMapperFactory` | `infrastructure.persistence.json` |
| `XxxEntity`, `SpringDataXxxRepository`, `XxxMapper`, `XxxRepositoryAdapter` | `infrastructure.persistence.jpa.<feature>` |
| `ConsoleNotifier`, `SmtpEmailSender`, `BCryptPasswordHasher`, `JwtTokenService` | `infrastructure.notification` / `.security` |
| RSS/HTML/HTTP client, scheduler job, Redis cache, RabbitMQ, Ollama/Spring AI, `ReportExporter`, `SeedData` | `infrastructure.external` / `.scheduler` / `.cache` / `.messaging` / `.ai` / `.report` / `.seed` |
| `MainMenu` (console) | `presentation.console` |
| `XxxController`, `XxxRequest`, `XxxResponse`, `GlobalExceptionHandler`, `PageResponse` | `presentation.web.<feature>` / `.common` |
| `Clock`, scorer beans, security config, bean wiring | `config` |
| `Text` (tiện ích thuần) | `shared` |
| `WorkerBuilder`, `JobBuilder` (test data builder) | `src/test/java/...` |

Tên thực thể ứng tuyển là **`JobApplication`** (không dùng `Application`, để khỏi lẫn với tầng `application` và `@SpringBootApplication`).
`User` ≠ `Worker`: `User` là tài khoản (email, vai trò); `Worker`/`Employer` là hồ sơ, giữ `userId`. **Không** `Worker extends User`.

## 6. Quy ước đặt tên (hậu tố rõ trách nhiệm)

| Hậu tố | Nghĩa | Tầng |
|---|---|---|
| `XxxController` | endpoint HTTP | presentation |
| `XxxRequest` / `XxxResponse` | dữ liệu vào/ra của web (`record`) | presentation |
| `XxxService` | use case (Application Service) | application |
| `XxxCommand` / `XxxResult` | dữ liệu vào/ra của use case (`record`) | application |
| `XxxRepository` | port lưu trữ (interface) | domain |
| `XxxRepositoryAdapter`, `InMemoryXxxRepository`, `JsonXxxRepository` | adapter cài đặt port | infrastructure |
| `XxxEntity` | **chỉ** JPA entity | infrastructure |
| `XxxDocument` | bản ghi lưu JSON | infrastructure |
| `XxxMapper` | chuyển đổi giữa hai kiểu | nơi chứa cả hai kiểu |
| `XxxScorer`, `XxxPolicy` | domain service | domain |
| `XxxException` | lỗi nghiệp vụ | domain |
| `XxxTest` / `XxxIT` | unit / integration test | test |

## 7. Quy tắc chuyển đổi dữ liệu giữa các tầng
```text
HTTP JSON ⇄ Request/Response (presentation) ⇄ Command/Result (application) ⇄ Domain model ⇄ Entity/Document (infrastructure) ⇄ DB/JSON
```
- Chuyển bằng mapper thủ công (static factory/`XxxMapper`), **không** MapStruct ở lộ trình này.
- **Không bao giờ** trả domain object hoặc `XxxEntity` ra ngoài API.
- Annotation theo tầng: validation (`@NotBlank`…) ở Request; JPA ở Entity; Jackson ở Request/Response/Document; **domain không có annotation framework**.
- Spring annotation ở `application` chỉ gồm `@Service` và `@Transactional`.
- Kiểu phân trang của domain: `PageQuery`, `PageResult<T>` (không dùng `Pageable`/`Page` của Spring ngoài `infrastructure`).

## 8. Luật kiến trúc phải có trong `ArchitectureTest` (viết ở bài 0.2, chạy trong `./mvnw verify`)

| Mã | Luật |
|---|---|
| R1 | `domain..` không phụ thuộc `application..`, `infrastructure..`, `presentation..`, `config..` |
| R2 | `domain..` không phụ thuộc `org.springframework..`, `jakarta.persistence..`, `com.fasterxml.jackson..` (từ GĐ1 chỉ cần Jackson; thêm Spring/JPA khi tới GĐ2) |
| R3 | `application..` không phụ thuộc `infrastructure..`, `presentation..` |
| R4 | `presentation..` không phụ thuộc `infrastructure..` và không phụ thuộc class tên `*Repository` |
| R5 | `infrastructure..` không phụ thuộc `presentation..` |
| R6 | Class có `@Entity` chỉ nằm trong `infrastructure.persistence.jpa..` (từ GĐ2) |
| R7 | `@RestController`/`@RestControllerAdvice` chỉ nằm trong `presentation.web..` (từ GĐ2) |
| R8 | Không có `@Autowired` trên field (từ GĐ2) |

Ngoại lệ **duy nhất**: package `config..` và class `Main`/`WeldJobsApplication` ở gốc được phép phụ thuộc mọi tầng.
Gợi ý công cụ: ArchUnit (`layeredArchitecture()`, `noClasses().that().resideInAPackage(...).should().dependOnClassesThat()...`).

## 9. Từ console sang Spring (Giai đoạn 1 → 2) — vì sao kiến trúc này đáng giá
- **Giữ nguyên:** `domain`, `application`, `shared`, test của chúng.
- **Thay:** `infrastructure.persistence.inmemory/json` → `infrastructure.persistence.jpa`; `presentation.console` → `presentation.web`; `Main` + `AppConfig` (new tay) → Spring `@Configuration`.
- Câu hỏi tự kiểm tra: *"Đổi JSON sang PostgreSQL thì phải sửa những file nào?"* — đáp án đúng: chỉ `infrastructure` và `config`.

## 10. ADR-001 (tóm tắt quyết định kiến trúc — người học tự viết bản đầy đủ ở bài 2.1)
- **Quyết định:** layered/onion DDD-lite 4 tầng; JPA entity **tách** khỏi domain, có mapper.
- **Đánh đổi:** thêm mapper và class, đổi lại domain thuần, test nhanh, đổi hạ tầng dễ.
- **Ghi chú:** lớp persistence nằm gọn trong `infrastructure`, nên có thể thay JPA bằng MyBatis (phổ biến ở Nhật) mà không sửa `domain`/`application`. Lộ trình này dùng JPA.
- **Điều kiện xem xét lại:** nếu mapper trở thành gánh nặng lớn hoặc nhóm quyết định cho phép annotation JPA trên domain.

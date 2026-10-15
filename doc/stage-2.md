## GIAI ĐOẠN 2 – SPRING BOOT NỀN TẢNG (REST API WeldJobs)

> **Ghi chú v2 cho file này:** (1) Dạy **từng bước** theo `00-rules.md` — mỗi mục đánh số trong *Bài tập* là một bước, Agent chỉ đưa một bước mỗi lần. (2) Vị trí class theo bảng ở `01-architecture.md` §5; `ArchitectureTest` phải xanh sau mỗi bài. (3) "Bài thi" là **tùy chọn**; báo cáo giai đoạn theo `00-rules.md` mục 9. (4) Mục *Nghiệm thu* gắn *(mục phụ)* hoặc là ghi chú `docs/*.md` có thể ghi nợ `[~]` theo `00-rules.md` mục 4.2.

> **Quy ước giai đoạn 2**
> - Project mới: `weldjobs-api` (Maven, Java 21, **Spring Boot bản ổn định mới nhất** – Agent kiểm tra tại start.spring.io và báo các thay đổi lớn so với tài liệu cũ).
> - Mang `domain`, `application`, `shared` (và test của chúng) từ `weldjobs-console` sang bằng cách chép; **không sửa nghiệp vụ** trừ khi bài yêu cầu. Chỉ **thêm** `infrastructure.persistence.jpa`, `presentation.web`, `config` — đây chính là lợi ích của kiến trúc 4 tầng.
> - Kiến trúc **4 tầng** (`presentation` / `application` / `domain` / `infrastructure`) + `config` + `shared` đúng `01-architecture.md`; trong mỗi tầng chia tiếp theo tính năng (`job`, `worker`, …). `ArchitectureTest` (từ bài 0.2, thêm luật R2-Spring/JPA, R6, R7, R8) chạy trong `./mvnw verify`.
> - Phụ thuộc ban đầu: Web, Validation, Data JPA, PostgreSQL Driver, Flyway, Security, Actuator, Testcontainers. (Lombok: **không dùng** ở 2.1–2.7; từ 2.8 chỉ được dùng `@Getter`, `@NoArgsConstructor(access = PROTECTED)`; **cấm** `@Data`/`@EqualsAndHashCode` trên entity.)
> - Các lớp đối tượng tách biệt: **Request/Response** (`presentation.web`, `record`), **Command/Result** (`application`), **domain model**, và **`XxxEntity` của JPA** (chỉ ở `infrastructure.persistence.jpa`). **Không bao giờ trả entity/domain object ra API.** Mapping thủ công (static factory/`Mapper`), chưa dùng MapStruct.
> - Mọi code từ giai đoạn này: constructor injection, logger SLF4J, có test.
> - Base path API: `/api/v1`. JSON dùng `camelCase`, thời gian ISO-8601 (UTC).

### Danh sách endpoint mục tiêu (hoàn thành dần qua các bài)
| Nhóm | Endpoint | Quyền |
|---|---|---|
| Auth | `POST /auth/register`, `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout` | Công khai |
| Skills | `GET /skills?category=`; `POST/PUT/DELETE /admin/skills` | Công khai / ADMIN |
| Worker | `GET/PUT /workers/me`, `PUT /workers/me/skills`, `POST/DELETE /workers/me/certificates/{id}` | WORKER |
| Employer | `GET/PUT /employers/me` | EMPLOYER |
| Jobs | `GET /jobs` (tìm kiếm), `GET /jobs/{id}`, `POST /jobs`, `PUT /jobs/{id}`, `PATCH /jobs/{id}/status` | Công khai (đọc) / EMPLOYER chủ tin |
| Applications | `POST /jobs/{id}/applications`, `GET /workers/me/applications`, `GET /jobs/{id}/applications`, `PATCH /applications/{id}/status` | WORKER / EMPLOYER chủ tin |
| Matching | `GET /workers/me/recommendations`, `GET /jobs/{id}/candidates` | WORKER / EMPLOYER chủ tin |

---

### Bài 2.1 – Khởi tạo project, cấu trúc, Actuator
**Thời lượng:** 0,5 ngày

**Mục tiêu:** Có project Spring Boot chạy được, hiểu auto-configuration ở mức khái niệm, ánh xạ sang NestJS.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Spring Initializr; `@SpringBootApplication` = `@Configuration` + `@EnableAutoConfiguration` + `@ComponentScan`.
- Starter là gì; `spring-boot-starter-parent`/BOM quản lý phiên bản.
- Vòng đời khởi động; `SpringApplication.run`; `--debug` để xem auto-config report.
- Actuator: `/actuator/health`, `/actuator/info`.
- Ánh xạ: NestJS `AppModule`/`main.ts` ↔ `@SpringBootApplication`/`main`; `@Module` ↔ package + `@Configuration`; provider ↔ bean.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Tạo project `weldjobs-api` (group `com.weldjobs`, artifact `weldjobs-api`). Chọn dependency như quy ước. Dựng cấu trúc 4 tầng + `config` + `shared` đúng `01-architecture.md` (mỗi package có `package-info.java` mô tả trách nhiệm); chép `domain`, `application`, `shared` từ `weldjobs-console` sang và để `ArchitectureTest` chạy xanh trên project mới.
2. Cấu hình để app **chưa cần DB** vẫn khởi động được (tạm loại `DataSourceAutoConfiguration` hoặc dùng profile) — ghi rõ vì sao và sẽ gỡ ở bài 2.6.
3. Bật Actuator: chỉ expose `health` và `info`. Thêm `info.app.name`, `info.app.version` từ cấu hình.
4. Chạy với `--debug` hoặc `debug=true`, tìm trong báo cáo 3 auto-configuration **được bật** và 3 **không được bật**, giải thích vì sao.
5. Viết `docs/spring-vs-nest.md`: bảng ánh xạ ≥ 12 khái niệm NestJS ↔ Spring (module, provider, controller, DTO + class-validator, guard, interceptor, pipe, middleware, config module, TypeORM, e2e test...).
6. Tạo `GET /api/v1/ping` trả `{"status":"ok","time":"..."}` (dùng `Clock` bean khai báo ở `config`; controller đặt ở `presentation.web.system`).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `./mvnw spring-boot:run` khởi động thành công (dán log dòng `Started ... in X seconds`).
- [ ] `curl localhost:8080/actuator/health` trả `{"status":"UP"}`; `/actuator/env` **không** truy cập được (404/401).
- [ ] `curl localhost:8080/api/v1/ping` trả JSON đúng định dạng; có test `@WebMvcTest` hoặc `@SpringBootTest` cho endpoint này.
- [ ] Cấu trúc 4 tầng tạo sẵn, có `package-info.java`; `ArchitectureTest` xanh trên `weldjobs-api`.
- [ ] `docs/spring-vs-nest.md` đủ ≥ 12 dòng, đúng.
- [ ] Danh sách 3 auto-config bật/tắt kèm giải thích.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. `@SpringBootApplication` gồm những gì? → `@Configuration`, `@EnableAutoConfiguration`, `@ComponentScan`.
2. Auto-configuration quyết định bật dựa vào đâu? → `@Conditional...` (có class trên classpath, có/không bean, property).
3. Vì sao không expose toàn bộ endpoint Actuator ra công khai? → Lộ cấu hình/biến môi trường/heap dump.
4. Component scan bắt đầu từ package nào và hệ quả khi đặt `WeldJobsApplication` sai package? → Package chứa class chính và con của nó; bean ngoài phạm vi sẽ không được quét.

---

### Bài 2.2 – IoC / Dependency Injection
**Thời lượng:** 1 ngày

**Mục tiêu:** Hiểu container quản lý bean thế nào; viết cấu hình bean đúng; tránh các bẫy DI.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Inversion of Control, `ApplicationContext`, bean, scope (`singleton` mặc định, `prototype`, `request`).
- `@Component/@Service/@Repository/@Controller`; `@Configuration` + `@Bean`; **constructor injection** (vì sao ưu việt hơn field injection: bất biến, dễ test, lộ phụ thuộc vòng).
- `@Primary`, `@Qualifier`, inject `List<T>`/`Map<String,T>`, `@Order`.
- Vòng đời: `@PostConstruct`, `@PreDestroy`, `ApplicationRunner`; `@Profile`; `@ConditionalOnProperty`.
- Circular dependency và cách gỡ (thiết kế lại, không dùng `@Lazy` để "chữa cháy" làm giải pháp chính).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Chuyển `JobScorer` + các scorer + `WeightedScorer` từ Giai đoạn 1 sang `domain.service` — **giữ thuần Java, không annotation Spring** (domain không được import Spring). Khai báo mỗi scorer thành bean bằng `@Bean` trong `config/ScoringConfig`, thứ tự bằng `@Order`; `WeightedScorer` nhận `List<JobScorer>` qua constructor và **loại chính nó khỏi danh sách** (gợi ý: đừng khai báo nó là bean kiểu `JobScorer` trùng, hoặc dùng interface riêng `CompositeScorer`).
2. `@Configuration` định nghĩa `Clock` bean (UTC) và `PasswordEncoder` bean (tạm), có `@Profile("test")` ghi đè `Clock` cố định.
3. Cố ý tạo **vòng phụ thuộc**: `AService` ↔ `BService` (constructor injection). Dán thông báo lỗi khởi động. Gỡ bằng thiết kế lại (tách `CService`). Không dùng `@Lazy`, `setter injection` hay cho phép `spring.main.allow-circular-references`.
4. Demo `prototype` scope: một bean `RequestIdGenerator` trả id khác nhau mỗi lần được inject qua `ObjectProvider`; giải thích vì sao inject prototype vào singleton trực tiếp chỉ lấy **một lần**.
5. Viết `ApplicationRunner` (chỉ chạy ở profile `dev`) in ra danh sách tên bean thuộc package `com.weldjobs` (dùng `ApplicationContext.getBeanDefinitionNames()` lọc theo package).
6. Test: `@SpringBootTest` kiểm tra context load, `WeightedScorer` có đúng số scorer và thứ tự; test đơn vị `WeightedScorer` không cần Spring.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Không có `@Autowired` trên field (grep xác nhận); chỉ constructor injection.
- [ ] Không class nào trong `domain` import `org.springframework` (`ArchitectureTest` R2 xanh); scorer được đăng ký bằng `@Bean` trong `config`.
- [ ] Test chứng minh thứ tự scorer theo `@Order`.
- [ ] Thông báo lỗi vòng phụ thuộc được dán; cách gỡ **không** dùng `@Lazy`/cho phép circular.
- [ ] Demo prototype: test cho thấy `ObjectProvider` trả id khác nhau mỗi lần, bean singleton giữ một instance.
- [ ] `@Profile("test")` làm test chạy với `Clock` cố định (assert thời gian).
- [ ] `WeightedScorer` unit test **không khởi động Spring** (chạy < 1 giây).

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao constructor injection được ưu tiên? → Dependency rõ ràng, field `final`, test dễ không cần reflection, phát hiện vòng phụ thuộc sớm.
2. Bean singleton của Spring khác Singleton pattern thế nào? → Singleton theo từng container, không phải theo JVM/ClassLoader; không ép bằng static.
3. Khi có 2 bean cùng kiểu thì Spring chọn thế nào? → `@Primary`, `@Qualifier`, hoặc theo tên tham số; không có thì lỗi `NoUniqueBeanDefinitionException`.
4. `@Component` khác `@Bean`? → `@Component` quét lớp của bạn; `@Bean` khai báo thủ công, hợp cho lớp bên thứ ba.

---

### Bài 2.3 – REST controller và DTO
**Thời lượng:** 1 ngày

**Mục tiêu:** Thiết kế REST API đúng ngữ nghĩa HTTP; tách DTO khỏi domain.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `@RestController`, `@RequestMapping`, `@GetMapping/@PostMapping/@PutMapping/@PatchMapping/@DeleteMapping`.
- `@PathVariable`, `@RequestParam` (required/default), `@RequestBody`, `@RequestHeader`, `ResponseEntity`, `@ResponseStatus`.
- Mã trạng thái: 200, 201 + `Location`, 204, 400, 401, 403, 404, 409, 422; idempotency của `PUT/DELETE`; khác biệt `PUT` và `PATCH`.
- DTO vs entity; mapping thủ công (static factory `from(...)`).
- Cấu hình Jackson trong Spring Boot (`spring.jackson.*`).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `GET /api/v1/skills?category=PROCESS` trả danh sách kỹ năng **từ danh mục in-memory** (tạm: `SkillService` ở `application.skill` đọc qua port `SkillRepository`; bản in-memory từ Giai đoạn 1 nằm ở `infrastructure.persistence.inmemory`, ≥ 40 kỹ năng); `GET /api/v1/skills/{code}` trả 404 khi không có. `SkillResponse(String code, String name, SkillCategory category)` (record). `category` sai giá trị → 400 với thông báo hữu ích.
2. **CRUD tạm in-memory** cho tin tuyển dụng để học mapping (sẽ thay bằng DB ở 2.9): `POST /api/v1/jobs` (201 + `Location: /api/v1/jobs/{id}`), `GET /api/v1/jobs`, `GET /api/v1/jobs/{id}`, `PUT /api/v1/jobs/{id}` (thay thế toàn bộ), `PATCH /api/v1/jobs/{id}/status` (chỉ đổi trạng thái), `DELETE /api/v1/jobs/{id}` (204; xóa lần 2 → 404, nêu rõ quy ước idempotent).
3. Web (`presentation.web.job`, `record`): `CreateJobRequest`, `UpdateJobRequest`, `JobResponse`, `JobSummaryResponse` (rút gọn cho danh sách), `ChangeStatusRequest`; application: `CreateJobCommand`, `JobResult`, … Controller đổi Request → Command, gọi `JobService`, nhận Result → Response. Giải thích trong `docs/dto-notes.md` vì sao không dùng một DTO cho tất cả (over-posting, lộ field nội bộ, tiến hóa API) và vì sao Request ≠ Command.
4. Cấu hình Jackson: bỏ timestamp số cho ngày giờ, trả `Instant` dạng ISO-8601, bỏ qua property thừa trong request **hay** từ chối (chọn và giải thích).
5. Test bằng `MockMvc` cho tất cả endpoint (thành công + 404 + 400).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `curl` các endpoint cho đúng status code (dán lệnh + response cho: tạo 201, lấy 200, không có 404, xóa 204, xóa lại 404).
- [ ] Response `POST` có header `Location` đúng.
- [ ] Không controller nào trả entity/domain object trực tiếp (đọc code).
- [ ] Controller chỉ gọi `application` (không gọi repository trực tiếp); `ArchitectureTest` R4 xanh.
- [ ] `PUT` thiếu field bắt buộc → 400 (chưa cần format lỗi chuẩn; sẽ làm ở 2.10).
- [ ] `docs/dto-notes.md` nêu ≥ 3 lý do tách DTO.
- [ ] ≥ 15 test MockMvc, tất cả xanh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. `PUT` và `PATCH` khác gì? → PUT thay thế toàn bộ tài nguyên (idempotent); PATCH sửa một phần.
2. Khi nào trả 201 và 204? → 201 khi tạo mới (kèm Location); 204 khi thành công nhưng không có body.
3. Idempotent nghĩa là gì và ví dụ? → Gọi nhiều lần cho cùng kết quả trạng thái (PUT, DELETE, GET); POST thường không.
4. Over-posting là gì? → Client gửi field không nên được sửa (ví dụ `role`, `id`) và bị bind vào entity.

---

### Bài 2.4 – Validation
**Thời lượng:** 1 ngày

**Mục tiêu:** Kiểm tra dữ liệu đầu vào đúng chỗ, thông báo rõ ràng, có validator tùy chỉnh.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Bean Validation (Jakarta): `@NotNull`, `@NotBlank`, `@Size`, `@Min/@Max`, `@Email`, `@Pattern`, `@Future`, `@Valid` (lồng nhau, trên phần tử collection), `@Validated` (trên class/method), validation groups (nhận biết).
- Custom constraint: annotation + `ConstraintValidator`; validator mức **class** (nhiều field).
- Khác biệt giữa **validation cú pháp** (DTO) và **quy tắc nghiệp vụ** (service).
- Nơi đặt validation: tầng DTO cho định dạng; tầng service cho quy tắc cần dữ liệu.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `CreateJobRequest` (record) với ràng buộc:
   - `title`: 10–150 ký tự, không toàn khoảng trắng.
   - `description`: ≤ 5000 ký tự.
   - `province`: thuộc danh sách tỉnh/thành hợp lệ (đọc từ file tài nguyên; custom `@ValidProvince`, so khớp có chuẩn hóa).
   - `jobType`: bắt buộc.
   - `salaryMin ≤ salaryMax` (custom constraint **mức class** `@ValidSalaryRange`), tiền tệ thuộc danh sách cho phép.
   - `minYearsExperience`: 0–50; `headcount`: 1–1000.
   - `expiresAt`: trong tương lai và không quá 180 ngày kể từ hiện tại (dùng `Clock` đã tiêm — gợi ý: kiểm tra ở service, và giải thích vì sao).
   - `requiredSkills`: danh sách 1–15 phần tử, mỗi phần tử `{skillCode, mandatory, minLevel}` hợp lệ (validate lồng nhau); **không trùng `skillCode`**.
2. `RegisterRequest`: email hợp lệ, mật khẩu ≥ 8 ký tự có ít nhất một chữ và một số (custom `@StrongPassword`), `role` chỉ cho phép `WORKER`/`EMPLOYER` (không cho tự đăng ký `ADMIN`).
3. Viết ≥ 25 test tham số hóa các payload sai và kiểm tra HTTP 400 + chứa đúng **tên field** lỗi (định dạng lỗi chuẩn sẽ làm ở 2.10; ở đây chỉ cần đọc được field nào lỗi).
4. Viết `docs/validation-notes.md`: phân loại 10 quy tắc của WeldJobs thành "validate ở DTO" hay "validate ở service" và lý do.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] ≥ 25 test tham số hóa, mỗi trường hợp xấu cho 400 và nêu đúng field.
- [ ] Payload hợp lệ tối thiểu và payload hợp lệ đầy đủ đều qua (test).
- [ ] Validator `@ValidSalaryRange` và `@StrongPassword` có unit test **riêng** (không cần Spring).
- [ ] Request `role=ADMIN` bị từ chối.
- [ ] Danh sách `requiredSkills` trùng code bị từ chối.
- [ ] `docs/validation-notes.md` phân loại đúng và có lý do hợp lý.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao validate cả ở DTO và ở service? → DTO chặn dữ liệu sai cú pháp sớm; service giữ bất biến nghiệp vụ dù được gọi từ nơi khác (job, message queue).
2. `@Valid` và `@Validated` khác gì? → `@Valid` chuẩn Jakarta, kích hoạt validate lồng; `@Validated` của Spring, hỗ trợ groups và validate tham số method.
3. Vì sao không tin dữ liệu từ client dù frontend đã validate? → Client có thể bị bỏ qua/giả mạo.
4. Vì sao không cho client gửi `role=ADMIN` khi đăng ký? → Leo thang đặc quyền.

---

### Bài 2.5 – Cấu hình: `application.yml`, profiles, `@ConfigurationProperties`
**Thời lượng:** 0,5–1 ngày

**Mục tiêu:** Quản lý cấu hình theo môi trường, không lộ secret, fail-fast khi thiếu.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Thứ tự ưu tiên nguồn cấu hình (env var, command line, `application-{profile}.yml`, `application.yml`).
- Relaxed binding (`APP_JWT_SECRET` ↔ `app.jwt.secret`).
- `@Value` vs `@ConfigurationProperties` (ưu việt hơn: type-safe, validate); `@EnableConfigurationProperties`/`@ConfigurationPropertiesScan`.
- Profiles: `dev`, `test`, `prod`; `spring.profiles.active`.
- Secret không commit; `.env.example`.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Tạo `WeldJobsProperties` (record + `@ConfigurationProperties(prefix="weldjobs")`) gồm: `jwt{secret, accessTtl (Duration), refreshTtl (Duration), issuer}`, `jobs{defaultExpiryDays, maxPageSize}`, `cors{allowedOrigins (List<String>)}`; thêm `@Validated` và ràng buộc (secret ≥ 32 ký tự, TTL > 0, `maxPageSize` 1–100).
2. Tạo `application.yml` (chung), `application-dev.yml`, `application-test.yml`, `application-prod.yml` (không chứa secret; secret lấy từ biến môi trường `JWT_SECRET`).
3. Ở profile `prod`, thiếu `JWT_SECRET` ⇒ **ứng dụng không khởi động** với thông báo rõ ràng.
4. Tạo `.env.example` (không chứa giá trị thật) và thêm `.env` vào `.gitignore`.
5. Test với `ApplicationContextRunner` hoặc `@SpringBootTest(properties=...)`: (a) cấu hình hợp lệ nạp đúng, (b) secret quá ngắn ⇒ context fail, (c) `Duration` được parse từ chuỗi như `15m`, `7d`.
6. Chứng minh thứ tự ưu tiên: đặt cùng một property ở `application.yml`, profile yml và biến môi trường; cho thấy giá trị thắng cuộc ở từng cấp (dán log).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Chạy `--spring.profiles.active=prod` không có `JWT_SECRET` ⇒ fail với thông báo hữu ích (dán log).
- [ ] Không có secret trong git (chạy `git grep -i "secret"` và giải thích mọi kết quả).
- [ ] 3 test cấu hình ở mục 5 xanh.
- [ ] Có bằng chứng về thứ tự ưu tiên cấu hình.
- [ ] `.env.example` có đủ biến, mô tả ngắn từng biến.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao `@ConfigurationProperties` tốt hơn nhiều `@Value`? → Gom nhóm, type-safe, validate, dễ test, tài liệu hóa.
2. Biến môi trường `WELDJOBS_JWT_SECRET` ánh xạ tới property nào? → `weldjobs.jwt.secret` (relaxed binding).
3. Vì sao fail-fast quan trọng với secret? → Tránh chạy production với cấu hình yếu/mặc định.

---

### Bài 2.6 – PostgreSQL, Docker Compose, Spring Data JPA cơ bản, Flyway căn bản
**Thời lượng:** 1–1,5 ngày

**Mục tiêu:** Kết nối DB thật, ánh xạ entity đơn giản, quản lý schema bằng migration ngay từ đầu.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Docker Compose: service, volume, healthcheck, network, biến môi trường.
- DataSource + HikariCP; `spring.datasource.*`; `spring.jpa.hibernate.ddl-auto` (`validate` cho mọi môi trường; **cấm `update`/`create` ngoài thí nghiệm**); `spring.jpa.open-in-view=false` (giải thích).
- Entity: `@Entity`, `@Table`, `@Id`, `@GeneratedValue`, `@Column`, `@Enumerated(EnumType.STRING)` (**không dùng ORDINAL**), `@Version`, JPA auditing (`@CreatedDate`, `@LastModifiedDate`).
- `JpaRepository`, derived query method, `@Query` (JPQL), `Optional` trả về.
- `@Transactional` cơ bản (`readOnly`); persistence context & dirty checking (khái niệm).
- Flyway: file `V1__create_skill.sql`, bảng `flyway_schema_history`.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `docker-compose.yml` ở root với dịch vụ `postgres:16` (volume bền vững, healthcheck `pg_isready`, biến môi trường lấy từ `.env`).
2. Gỡ cấu hình "bỏ qua DataSource" ở bài 2.1; cấu hình datasource từ biến môi trường.
3. Migration `V1__create_skill.sql`: bảng `skill(id bigserial PK, code varchar(40) unique not null, name varchar(100) not null, category varchar(20) not null check (...), active boolean not null default true, created_at timestamptz not null default now())`.
4. Trong `infrastructure.persistence.jpa.skill`: `SkillEntity`, `SpringDataSkillRepository` (`JpaRepository`: `findByCode`, `existsByCode`, `findByCategoryOrderByNameAsc`, `findByActiveTrue`), `SkillMapper` (entity ↔ domain `Skill`) và `SkillRepositoryAdapter implements SkillRepository` (port ở domain). `SkillService` (application, đọc) không biết JPA. Thay controller bài 2.3 để đọc từ DB **mà không sửa `SkillService`** (chỉ đổi bean adapter ở `config`).
5. `ddl-auto=validate`; cố ý sửa tên cột trong entity để thấy lỗi validate; sau đó sửa lại.
6. Bật `spring.jpa.show-sql` + `format_sql` **ở dev**; quan sát SQL sinh ra khi gọi `findByCategoryOrderByNameAsc`.
7. Viết test `@DataJpaTest` + **Testcontainers PostgreSQL** (không dùng H2) cho repository; viết ghi chú `docs/why-not-h2.md` (3 lý do).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `docker compose up -d` chạy Postgres `healthy`; app kết nối thành công (dán log).
- [ ] `flyway_schema_history` có bản ghi V1 `success` (dán kết quả truy vấn).
- [ ] `ddl-auto=validate` đang bật; có bằng chứng lỗi validate khi lệch schema.
- [ ] Mọi enum dùng `EnumType.STRING` (grep xác nhận).
- [ ] `open-in-view=false` và giải thích lý do trong `docs/jpa-notes.md`.
- [ ] Test repository dùng Testcontainers Postgres và xanh; ≥ 6 test.
- [ ] `GET /api/v1/skills` đọc từ DB.
- [ ] `SkillService` và `domain` không đổi dòng nào khi chuyển in-memory → JPA (`git diff --stat`); có test mapper entity ↔ domain.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao không dùng `ddl-auto=update` ở production? → Không kiểm soát, có thể mất/hỏng dữ liệu; cần migration có phiên bản, review được.
2. `EnumType.ORDINAL` nguy hiểm vì sao? → Đổi thứ tự/ thêm giá trị làm sai dữ liệu cũ.
3. Open-in-view là gì và vì sao tắt? → Giữ session JPA suốt request, che giấu lazy loading ở view, giữ kết nối DB lâu.
4. Dirty checking là gì? → Hibernate tự phát hiện thay đổi của entity trong transaction và flush UPDATE.
5. Vì sao Testcontainers tốt hơn H2? → Cùng engine/ dialect/ tính năng như production (kiểu dữ liệu, JSONB, index).

---

### Bài 2.7 – Quan hệ JPA, N+1, bảng nối có thuộc tính
**Thời lượng:** 2 ngày

**Mục tiêu:** Mô hình hóa đầy đủ miền WeldJobs; hiểu và kiểm soát hiệu năng truy vấn quan hệ.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `@OneToOne`, `@OneToMany`, `@ManyToOne`, `@ManyToMany` (và vì sao **tránh** `@ManyToMany` khi bảng nối có thuộc tính → dùng entity trung gian).
- Owning side vs `mappedBy`; helper method đồng bộ hai phía; `cascade`, `orphanRemoval`.
- Fetch: LAZY vs EAGER (đặt `@ManyToOne(fetch = LAZY)` tường minh); `LazyInitializationException`.
- Vấn đề **N+1**; `join fetch`, `@EntityGraph`, `@BatchSize`; DTO projection.
- `equals/hashCode` cho entity (không dùng toàn bộ field; cẩn trọng với id null trước khi persist).
- Ràng buộc DB: FK, unique, check, index.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Viết migration `V2__core_tables.sql` tạo các bảng: `app_user`, `worker`, `worker_skill`, `certificate`, `employer`, `job_post`, `job_required_skill`, `job_application`. Yêu cầu schema:
   - `app_user(email unique, password_hash, role, enabled, created_at)`.
   - `worker_skill`: **unique (worker_id, skill_id)**, `years_of_experience` check 0–50, `level`.
   - `job_post`: check `salary_min <= salary_max`, `status`, `job_type`, `posted_at`, `expires_at`; index `(status, province)` và `(expires_at)`.
   - `job_required_skill`: unique `(job_id, skill_id)`.
   - `job_application`: **unique (worker_id, job_id)**.
2. JPA entity (đặt ở `infrastructure.persistence.jpa.*`, **tách khỏi domain model**) với quan hệ đúng: `WorkerEntity` 1–1 `UserEntity`; `WorkerSkillEntity` (ManyToOne worker, ManyToOne skill, years, level); `JobPostEntity` 1–N `JobRequiredSkillEntity`; `JobApplicationEntity` ManyToOne worker/job. Mỗi aggregate có `Mapper` (entity ↔ domain) và adapter cài đặt port repository; viết test khứ hồi cho mapper.
3. Seed test: 30 tin × 3–5 kỹ năng, 20 thợ × 4–8 kỹ năng.
4. **Tái hiện N+1:** viết method `findAll()` + mapping sang `JobResponse` (có danh sách kỹ năng). Đếm số câu SQL (dùng `hibernate.generate_statistics` hoặc `datasource-proxy`). Ghi lại số câu.
5. **Sửa N+1** bằng ít nhất 2 cách (`@EntityGraph` và `join fetch`/`@BatchSize`/projection), đo lại. Giải thích trade-off (join fetch + phân trang ⇒ cảnh báo in-memory pagination; cách xử lý bằng 2 truy vấn).
6. Gây `LazyInitializationException` có chủ đích, sau đó sửa **không** bằng cách bật EAGER hay `open-in-view`.
7. Viết `docs/jpa-notes.md`: bảng quyết định cho từng quan hệ trong WeldJobs (kiểu quan hệ, owning side, cascade, fetch) kèm lý do.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Migration chạy sạch trên DB trống (dán `flyway_schema_history`).
- [ ] Không còn `@ManyToMany` cho kỹ năng thợ/tin (dùng entity trung gian).
- [ ] Test chứng minh **ràng buộc unique** hoạt động: thêm trùng `(worker, skill)` và `(worker, job)` đều bị DB từ chối.
- [ ] Test đếm query: danh sách 30 tin kèm kỹ năng **≤ 3 câu SQL** sau khi sửa (dán số trước/sau).
- [ ] Mọi `@ManyToOne`/`@OneToOne` đều khai báo `fetch = LAZY` tường minh.
- [ ] Domain model không có annotation JPA (`ArchitectureTest` R2, R6 xanh); adapter + mapper có test.
- [ ] Không có `@Data` trên entity; `equals/hashCode` được cài đúng/ giải thích.
- [ ] `docs/jpa-notes.md` đầy đủ.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. N+1 là gì? Nhận biết thế nào? → 1 truy vấn lấy N bản ghi rồi N truy vấn lấy quan hệ; thấy hàng loạt SELECT lặp trong log.
2. Vì sao `join fetch` kèm phân trang nguy hiểm? → Hibernate phân trang trong bộ nhớ (cảnh báo HHH000104), tốn RAM.
3. Khi nào dùng cascade/orphanRemoval? → Khi con không có vòng đời độc lập (certificate của worker); không dùng cho quan hệ tới entity dùng chung (skill).
4. Vì sao đặt `LAZY` tường minh? → `@ManyToOne` mặc định EAGER, dễ gây truy vấn thừa.
5. Vì sao bảng nối có thuộc tính nên là entity? → `@ManyToMany` không chứa được cột thêm (years, level).

---

### Bài 2.8 – Quản lý migration thực chiến và seed dữ liệu
**Thời lượng:** 0,5–1 ngày

**Mục tiêu:** Thay đổi schema an toàn khi đã có dữ liệu; seed danh mục kỹ năng đúng cách.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Quy tắc Flyway: **không sửa migration đã áp dụng** (checksum); đặt tên `V<n>__mô_tả.sql`; migration lặp lại `R__`; `baseline`; `validateOnMigrate`.
- Thay đổi tương thích: thêm cột nullable → backfill → đặt NOT NULL (expand/contract).
- Dữ liệu tham chiếu (reference data) vs dữ liệu mẫu (sample data); seed theo môi trường (`dev`).
- Chiến lược rollback (Flyway Community không có undo: dùng migration "sửa tiếp").

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `V3__seed_skills.sql`: seed **≥ 45 kỹ năng** thuộc đủ 5 nhóm (bao gồm tất cả mục trong `02-project.md` (C.3), thêm: hàn cắt/đọc bản vẽ/NDT cơ bản). Dùng `ON CONFLICT (code) DO NOTHING` để chạy lại an toàn.
2. `V4__add_skill_display_order.sql`: thêm cột `display_order int` cho `skill` an toàn (thêm nullable → cập nhật theo nhóm → `NOT NULL DEFAULT 0`).
3. Sửa file `V3` sau khi đã chạy để thấy lỗi **checksum mismatch** (dán lỗi), rồi hoàn tác và dùng `V5__...` để sửa đúng cách.
4. Dữ liệu mẫu cho dev: `ApplicationRunner` ở profile `dev` (hoặc `afterMigrate.sql` ở thư mục riêng) tạo 1 admin, 3 employer, 20 worker, 30 tin — **idempotent** (chạy lại không nhân đôi).
5. Thêm `R__job_search_view.sql` (view hoặc index lặp lại) và giải thích vì sao phù hợp loại migration này.
6. Viết `docs/migration-playbook.md`: 8 quy tắc làm migration trong nhóm, cách đối phó khi migration thất bại giữa chừng ở production.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] DB trống chạy toàn bộ migration thành công (dán lịch sử).
- [ ] `SELECT count(*) FROM skill` ≥ 45 và phân bố đủ 5 nhóm (dán kết quả `GROUP BY category`).
- [ ] Có bằng chứng lỗi checksum và cách xử lý đúng.
- [ ] Seed dev chạy 2 lần liên tiếp không tạo dữ liệu trùng (test hoặc đếm bản ghi).
- [ ] `docs/migration-playbook.md` ≥ 8 quy tắc.
- [ ] Không còn `ddl-auto` khác `validate`.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao không được sửa migration đã chạy? → Checksum đổi ⇒ lỗi; các môi trường khác đã chạy bản cũ nên lệch.
2. Thêm cột NOT NULL vào bảng có dữ liệu nên làm thế nào? → Nhiều bước: thêm nullable, backfill, rồi ràng buộc.
3. Reference data nên nằm ở đâu? → Trong migration có phiên bản (để mọi môi trường đồng nhất).

---

### Bài 2.9 – CRUD tin tuyển dụng + tìm kiếm động + phân trang
**Thời lượng:** 2 ngày

**Mục tiêu:** API tìm việc thực tế: lọc nhiều tiêu chí, kể cả kỹ năng ANY/ALL, phân trang và sắp xếp an toàn.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `Pageable`, `Page`, `Slice`; `PageRequest`, `Sort`; count query và chi phí của nó.
- `JpaSpecificationExecutor` + `Specification` (kết hợp điều kiện động); subquery `EXISTS`; `GROUP BY ... HAVING count`.
- Giới hạn `size` tối đa; **whitelist** trường sắp xếp (chống sort theo field nhạy cảm/không có index).
- Chuyển entity → domain trong adapter, domain → Result ở `application`, Result → Response ở `presentation`.
- Index phù hợp truy vấn.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Thay CRUD in-memory (2.3) bằng DB. Quy tắc nghiệp vụ:
   - Employer chỉ tạo tin cho **employer profile của mình** (tạm thời lấy `employerId` từ tham số; sẽ gắn security ở 2.11).
   - Tạo mới → trạng thái `DRAFT`; `PATCH status`: `DRAFT→OPEN`, `OPEN→CLOSED`, `OPEN→EXPIRED` (hệ thống), `CLOSED→OPEN` (mở lại nếu chưa hết hạn). Sai luồng → exception nghiệp vụ.
   - `expiresAt` mặc định = `postedAt + defaultExpiryDays` (config).
   - Không sửa được tin đã `CLOSED`/`EXPIRED` (trừ mở lại).
2. `GET /api/v1/jobs` hỗ trợ tham số: `province`, `jobType`, `minSalary`, `maxSalary` (khoảng lương **giao nhau** với khoảng của tin), `maxMinYears`, `skills` (danh sách code, ví dụ `skills=PROC_TIG,MAT_ALUMINIUM`), `skillMode=ANY|ALL`, `q` (từ khóa trong tiêu đề, không phân biệt hoa thường/dấu — chọn cách cài đặt và ghi trade-off), `page`, `size`, `sort` (whitelist: `postedAt,desc` mặc định; `salaryMax`).
   - Chỉ trả tin `OPEN` và chưa hết hạn cho người dùng công khai.
3. Dùng `Specification` cho các điều kiện; `skillMode=ALL` cài bằng subquery đếm **hoặc** nhiều `EXISTS`; giải thích lựa chọn. Port ở domain: `JobRepository.search(JobSearchCriteria, PageQuery): PageResult<JobPost>` (kiểu phân trang **của domain**); `Specification`/`PageRequest` chỉ xuất hiện trong adapter.
4. Response phân trang chuẩn `PageResponse<T>(content, page, size, totalElements, totalPages, sort)` (không trả `Page` của Spring trực tiếp).
5. Bộ dữ liệu test ≥ 40 tin, kiểm tra từng bộ lọc và tổ hợp (≥ 20 test). Kiểm tra số truy vấn SQL của trang danh sách ≤ 3.
6. Thêm index hỗ trợ (migration mới) và chứng minh bằng `EXPLAIN` cho truy vấn phổ biến (dán kết quả, giải thích dùng index hay seq scan và vì sao với dữ liệu nhỏ có thể vẫn seq scan).
7. `size > maxPageSize` hoặc `sort=passwordHash` → 400.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] CRUD đầy đủ với quy tắc trạng thái; test cho mọi chuyển trạng thái hợp lệ/không hợp lệ.
- [ ] ≥ 20 test lọc/tìm kiếm, gồm ANY vs ALL cho cùng dữ liệu cho kết quả khác nhau và **đúng**.
- [ ] Tin hết hạn hoặc không `OPEN` không xuất hiện trong tìm kiếm công khai.
- [ ] Trang danh sách không N+1 (≤ 3 query, dán số).
- [ ] `size` vượt giới hạn và sort ngoài whitelist bị từ chối.
- [ ] `EXPLAIN` được dán kèm giải thích.
- [ ] `PageResponse` tự định nghĩa, ổn định cấu trúc JSON.
- [ ] `Pageable`/`Page`/`Specification` chỉ xuất hiện trong `infrastructure.persistence.jpa` (grep hoặc ArchUnit).

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao cần giới hạn `size` và whitelist `sort`? → Chống tải nặng/ DoS, chống sort theo cột không index/nhạy cảm.
2. `Page` và `Slice` khác nhau? → `Page` chạy thêm count; `Slice` chỉ biết có trang tiếp.
3. Làm sao cài "có TẤT CẢ kỹ năng"? → Đếm số kỹ năng khớp = số kỹ năng yêu cầu, hoặc nhiều EXISTS.
4. Khoảng lương "giao nhau" biểu diễn điều kiện thế nào? → `job.salaryMin <= maxSalary AND job.salaryMax >= minSalary`.

---

### Bài 2.10 – Xử lý lỗi toàn cục (ProblemDetail)
**Thời lượng:** 0,5–1 ngày

**Mục tiêu:** Mọi lỗi API có định dạng nhất quán, không lộ nội bộ, có mã tương quan để truy vết.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `@RestControllerAdvice`, `@ExceptionHandler`, `ResponseEntityExceptionHandler`.
- `ProblemDetail` (RFC 9457/7807): `type`, `title`, `status`, `detail`, `instance`, thuộc tính mở rộng.
- Ánh xạ exception → HTTP status; không lộ stack trace/ SQL.
- Correlation/trace id qua MDC và response header.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Port hệ exception từ Giai đoạn 1 (`NotFoundException`, `ConflictException`, `ValidationException`, `InvalidStateTransitionException`...) vào `common.exception`.
2. `GlobalExceptionHandler` xử lý và trả `ProblemDetail`:
   - Lỗi validation (`MethodArgumentNotValidException`, `ConstraintViolationException`, `HttpMessageNotReadableException`, kiểu tham số sai) → 400 với danh sách `errors: [{field, message, rejectedValueMasked?}]` (không echo lại mật khẩu).
   - `NotFoundException` → 404; `ConflictException`/`DataIntegrityViolationException` (unique) → 409 (message thân thiện, không lộ tên constraint nội bộ ra client).
   - `InvalidStateTransitionException` → 409 (hoặc 422 — chọn, ghi quy ước).
   - `AccessDeniedException` → 403; `AuthenticationException` → 401 (hoàn thiện ở 2.11).
   - `ObjectOptimisticLockingFailureException` → 409.
   - Mọi lỗi còn lại → 500 **không lộ chi tiết**, ghi log đầy đủ phía server.
3. Filter gán `X-Request-Id` (nhận từ client hoặc tự sinh UUID), đưa vào MDC, trả ở response header và trong body lỗi (`traceId`).
4. Test cho **từng loại lỗi**: status, `Content-Type: application/problem+json`, các trường bắt buộc.
5. Test chứng minh lỗi 500 **không chứa** stack trace/ tên class nội bộ/ SQL.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Mọi test ở mục 4 xanh (≥ 12 test).
- [ ] Mọi response lỗi có `traceId` khớp với dòng log phía server (dán ví dụ).
- [ ] Test 500 xác nhận không rò rỉ nội bộ.
- [ ] Không có `try/catch` trong controller chỉ để chuyển lỗi (đọc code).
- [ ] Thông báo lỗi tiếng Việt hoặc theo quy ước đã chọn, nhất quán.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao trả `ProblemDetail`? → Chuẩn hóa định dạng lỗi, client dễ xử lý chung.
2. Vì sao 500 không được lộ chi tiết? → Lộ cấu trúc nội bộ tạo điều kiện tấn công.
3. `traceId` giúp gì? → Nối lỗi phía client với log server.
4. Vì sao 409 cho unique violation thay vì 500? → Đây là lỗi do dữ liệu/ trạng thái từ client, không phải lỗi hệ thống.

---

### Bài 2.11 – Spring Security + JWT + phân quyền + kiểm tra quyền sở hữu
**Thời lượng:** 3 ngày (bài khó nhất giai đoạn)

**Mục tiêu:** Bảo vệ API đúng chuẩn: xác thực, phân quyền theo vai trò **và theo quyền sở hữu**, quản lý token an toàn.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Kiến trúc Spring Security 6: `SecurityFilterChain`, filter chain, `Authentication`, `SecurityContext`, `AuthenticationManager`, `UserDetailsService`.
- Băm mật khẩu: BCrypt (cost factor), vì sao không dùng MD5/SHA thuần.
- JWT: cấu trúc (header.payload.signature), claim (`sub`, `iss`, `exp`, `iat`, `roles`), HS256 vs RS256; **không** nhét dữ liệu nhạy cảm vào JWT; xác minh chữ ký, hạn, issuer.
- Access token ngắn hạn + **refresh token** xoay vòng (rotation), lưu **băm** trong DB, thu hồi khi đăng xuất, **phát hiện tái sử dụng** token cũ.
- Phân quyền: `hasRole`, `@EnableMethodSecurity`, `@PreAuthorize`; **IDOR/BOLA** (truy cập đối tượng của người khác).
- CORS (origin cho Next.js), CSRF (vì sao tắt được với API stateless dùng header `Authorization`, và khi nào không), security headers.
- OWASP API Top 10 ở mức nhận biết.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Endpoint: `POST /auth/register` (WORKER/EMPLOYER; tạo luôn profile rỗng), `POST /auth/login`, `POST /auth/refresh`, `POST /auth/logout`.
2. Access token 15 phút (cấu hình), refresh token 7 ngày. Dùng hỗ trợ JWT của Spring Security (resource server) hoặc thư viện đáng tin cậy — **Agent kiểm tra tài liệu hiện hành** — không tự viết thuật toán ký.
3. Bảng `refresh_token(id, user_id, token_hash, family_id, expires_at, revoked_at, replaced_by)`: mỗi lần refresh **cấp token mới và thu hồi token cũ**; nếu token cũ bị dùng lại ⇒ thu hồi **cả họ** (family) và trả 401.
4. Quy tắc truy cập:
   - Công khai: `POST /auth/**`, `GET /jobs`, `GET /jobs/{id}`, `GET /skills`, `/actuator/health`.
   - `ADMIN`: `/admin/**`.
   - `WORKER`: `/workers/me/**`, ứng tuyển, gợi ý.
   - `EMPLOYER`: `/employers/me`, tạo/sửa tin, xem hồ sơ ứng tuyển **của tin mình**.
5. **Kiểm tra quyền sở hữu:** employer chỉ sửa/đóng/xem hồ sơ ứng tuyển của tin **do mình tạo**; worker chỉ xem/rút hồ sơ **của mình**. Cài đặt qua `@PreAuthorize("@jobSecurity.isOwner(#id, authentication)")` hoặc kiểm tra trong service (chọn và giải thích). Truy cập đối tượng người khác trả **404 hay 403**? Chọn một quy ước và nêu lý do (chống dò ID).
6. Gắn security vào các endpoint ở 2.3–2.9 (thay thế tham số `employerId` tạm thời).
7. CORS cho origin cấu hình (`http://localhost:3000`), chỉ method/header cần thiết; cấu hình security headers cơ bản.
8. Không log mật khẩu/ token; không trả `passwordHash` ở bất kỳ DTO nào; login sai trả thông báo **chung** (không nói "email không tồn tại").
9. Test **ma trận phân quyền**: bảng tham số hóa ≥ 25 tổ hợp (endpoint × {ẩn danh, worker, employer khác chủ, employer chủ, admin}) kiểm tra 200/401/403/404 đúng quy ước.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Đăng ký → đăng nhập → gọi API bảo vệ thành công (dán `curl` đầy đủ).
- [ ] Token hết hạn bị từ chối (test dùng `Clock`/TTL ngắn).
- [ ] Token sai chữ ký/sai issuer bị 401.
- [ ] Refresh: token cũ **không dùng lại được**; dùng lại token cũ thu hồi cả họ (test).
- [ ] Logout thu hồi refresh token (test).
- [ ] Ma trận phân quyền ≥ 25 tổ hợp xanh, **bao gồm IDOR** (employer A sửa tin của employer B bị chặn).
- [ ] Mật khẩu lưu BCrypt (kiểm tra DB: không có plaintext) và có test.
- [ ] Không rò rỉ: không có `passwordHash`, token, secret trong response/log (grep + test).
- [ ] CORS chỉ cho origin cấu hình (test với origin lạ).
- [ ] Tài liệu `docs/security-notes.md`: sơ đồ luồng đăng nhập/refresh, 5 mối đe dọa và cách đã chặn.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. JWT có mã hóa dữ liệu không? → Không, chỉ ký; payload đọc được (base64) ⇒ không để dữ liệu nhạy cảm.
2. Vì sao dùng access token ngắn + refresh token? → Giảm thiệt hại khi lộ access token; refresh có thể thu hồi.
3. Refresh token rotation giải quyết gì? → Phát hiện đánh cắp/tái sử dụng token cũ.
4. IDOR là gì và phòng thế nào? → Truy cập đối tượng người khác bằng cách đổi ID; kiểm tra quyền sở hữu ở server cho mọi truy cập.
5. Vì sao CSRF thường tắt với API dùng header `Authorization`? → Trình duyệt không tự đính kèm header đó như cookie; nhưng nếu dùng cookie xác thực thì cần CSRF.
6. Vì sao lưu **băm** refresh token? → Lộ DB không lộ token dùng được.

---

### Bài 2.12 – Luồng ứng tuyển, state machine, giao dịch và đồng thời
**Thời lượng:** 1,5 ngày

**Mục tiêu:** Cài luồng ứng tuyển đúng nghiệp vụ, an toàn khi nhiều người thao tác đồng thời.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `@Transactional`: phạm vi, propagation (`REQUIRED`, `REQUIRES_NEW`), isolation (khái niệm), **rollback mặc định chỉ với unchecked exception**, bẫy **self-invocation**.
- Khóa lạc quan `@Version` vs khóa bi quan; ràng buộc unique làm "chốt chặn cuối".
- State machine đơn giản (enum + bảng chuyển) + bảng lịch sử.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `POST /jobs/{id}/applications` (WORKER): quy tắc từ Bài 1.8 (tin `OPEN`, chưa hết hạn, không trùng); thêm: thợ có `availability = NOT_LOOKING` vẫn được ứng tuyển nhưng ghi nhận (ghi chú trong thiết kế, chọn hành vi); `coverNote ≤ 1000`.
2. `GET /workers/me/applications` (phân trang, lọc theo `status`); `GET /jobs/{id}/applications` (employer chủ tin; phân trang, lọc `status`; **ẩn số điện thoại** của thợ cho đến khi `SHORTLISTED`).
3. `PATCH /applications/{id}/status`: worker chỉ được `WITHDRAWN` hồ sơ của mình; employer chỉ chuyển theo luồng (`SUBMITTED→VIEWED→SHORTLISTED→HIRED`, hoặc `REJECTED` có `reason`); dùng `ApplicationStatus.canTransitionTo`.
4. Bảng `application_status_history(application_id, from_status, to_status, changed_by, changed_at, reason)`; mỗi lần đổi trạng thái ghi lịch sử **trong cùng transaction**.
5. `@Version` trên `application`; hai request đổi trạng thái đồng thời → một cái 409.
6. **Test đồng thời:** 20 thread cùng ứng tuyển một tin bằng cùng một worker ⇒ **đúng 1 thành công**, 19 nhận 409; kết quả DB chỉ có 1 bản ghi (dùng `CountDownLatch` để bắt đầu đồng thời).
7. Tái hiện bẫy rollback: viết method ném **checked exception** trong `@Transactional` không rollback → sửa bằng `rollbackFor` hoặc dùng unchecked; viết test chứng minh. Tái hiện self-invocation làm `@Transactional` không có tác dụng và giải thích.
8. Khi `HIRED` đủ `headcount` thì tin tự `CLOSED` (trong cùng transaction); test.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Test đồng thời xanh ổn định (chạy 5 lần liên tiếp đều xanh).
- [ ] Mọi chuyển trạng thái hợp lệ/không hợp lệ có test; lịch sử được ghi **atomically** (rollback giao dịch → không có lịch sử mồ côi — test).
- [ ] Thợ A không xem được hồ sơ của thợ B; employer khác chủ không xem được (ma trận quyền mở rộng).
- [ ] Số điện thoại bị ẩn trước `SHORTLISTED` (test).
- [ ] Demo + test cho bẫy rollback checked exception và self-invocation.
- [ ] `HIRED` đủ `headcount` ⇒ tin `CLOSED` (test).
- [ ] `docs/transaction-notes.md` mô tả ranh giới transaction của từng use case.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao ràng buộc unique ở DB vẫn cần dù đã kiểm tra trùng trong code? → Kiểm tra-rồi-ghi có race condition; DB là chốt chặn cuối.
2. `@Version` hoạt động thế nào? → Cột version tăng mỗi lần update; update kèm `WHERE version=?` nếu 0 dòng ⇒ xung đột.
3. Vì sao `@Transactional` không chạy khi gọi method cùng class? → Proxy AOP chỉ chặn lời gọi từ bên ngoài bean.
4. Checked exception có rollback mặc định không? → Không; cần `rollbackFor`.

---

### Bài 2.13 – Matching và gợi ý việc làm
**Thời lượng:** 1–1,5 ngày

**Mục tiêu:** Đưa bộ chấm điểm vào hệ thống thật, hiệu quả và tôn trọng quyền riêng tư.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Tiền lọc ở DB rồi chấm điểm trong bộ nhớ (vì sao không chấm toàn bộ trong SQL / không tải toàn bộ bảng).
- Thiết kế DTO giải thích điểm (`breakdown`).
- Quyền riêng tư: dữ liệu liên hệ chỉ hiện khi hợp lý.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `GET /workers/me/recommendations?limit=10`:
   - Tiền lọc: tin `OPEN`, chưa hết hạn, **không** phải tin thợ đã ứng tuyển, và (có ít nhất 1 kỹ năng yêu cầu trùng kỹ năng của thợ **hoặc** cùng tỉnh).
   - Chấm điểm bằng `WeightedScorer` (4 scorer: kỹ năng, kinh nghiệm, địa điểm, lương), loại tin có điểm kỹ năng = 0 (thiếu kỹ năng bắt buộc).
   - Response: `[{job: JobSummaryResponse, score, breakdown: [{name, points, max, explanation}]}]` sắp xếp giảm dần điểm, hòa điểm thì mới nhất trước.
2. `GET /jobs/{id}/candidates?limit=20` (employer chủ tin): top thợ phù hợp; **chỉ** thợ `availability != NOT_LOOKING`; **không** trả điện thoại/email (chỉ `displayName`, tỉnh, kỹ năng, số năm, score).
3. Tối ưu: tổng truy vấn SQL ≤ 4 cho mỗi endpoint; không tải toàn bộ bảng `job_post` (giới hạn ứng viên tiền lọc, ví dụ tối đa 500, ghi chú trade-off).
4. Port toàn bộ test scorer từ Giai đoạn 1 (≥ 15) và thêm test tích hợp endpoint.
5. Đo thời gian với 1.000 tin × 1 thợ (dữ liệu seed) trên máy bạn; ghi vào `docs/matching-notes.md` kèm nhận xét hướng mở rộng (index, cache, tính trước bằng job nền ở GĐ3/6).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Test: thợ thiếu kỹ năng bắt buộc không được gợi ý; thợ đủ thì xuất hiện đúng thứ hạng đoán trước (dataset dựng tay).
- [ ] `breakdown` cộng đúng bằng `score`.
- [ ] Tin đã ứng tuyển không xuất hiện trong gợi ý.
- [ ] Endpoint candidates không lộ thông tin liên hệ; thợ `NOT_LOOKING` bị loại (test).
- [ ] ≤ 4 truy vấn SQL mỗi endpoint (dán số liệu).
- [ ] `docs/matching-notes.md` có số đo thời gian và nêu hạn chế.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao không chấm điểm toàn bộ trong SQL? → Logic phức tạp, khó test/bảo trì; tiền lọc bằng SQL rồi chấm trong bộ nhớ cân bằng tốt.
2. Điều gì xảy ra khi số tin tăng lên 1 triệu? → Cần tính trước/ index tìm kiếm/ engine tìm kiếm; không tiền lọc kiểu hiện tại.
3. Vì sao ẩn thông tin liên hệ của ứng viên? → Quyền riêng tư; chỉ lộ khi có quan hệ ứng tuyển/ shortlist.

---

### Bài 2.14 – Testing chuyên sâu cho Spring
**Thời lượng:** 2 ngày

**Mục tiêu:** Có bộ test tin cậy ở mọi tầng, chạy trên Postgres thật.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Kim tự tháp test: unit (nhanh, nhiều) → slice (`@WebMvcTest`, `@DataJpaTest`) → tích hợp (`@SpringBootTest`) → E2E (ít).
- Testcontainers với `@ServiceConnection` (hoặc `@DynamicPropertySource`); tái sử dụng container giữa test (singleton) để nhanh.
- `MockMvc` (+ `spring-security-test`: `jwt()`, `@WithMockUser`), `@MockitoBean`/`@MockBean` (theo phiên bản), `@Sql`, dọn dữ liệu giữa các test, `@Transactional` trong test và bẫy của nó.
- Test không ổn định (flaky): nguyên nhân và cách tránh.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Thiết lập lớp cơ sở/ cấu hình Testcontainers Postgres dùng chung, khởi động **một lần** cho toàn bộ test tích hợp.
2. Xây bộ test đạt:
   - **Unit** (Mockito): service (`JobApplicationService`, `JobService`, `MatchingService`) ≥ 40 test.
   - **`@WebMvcTest`**: mọi controller, kèm security slice ≥ 30 test.
   - **`@DataJpaTest`**: repository & specification ≥ 20 test.
   - **`@SpringBootTest` end-to-end API**: ≥ 8 kịch bản đầy đủ (ví dụ: đăng ký employer → tạo tin → đăng ký worker → tìm → ứng tuyển → employer shortlist → hired → tin đóng).
3. JaCoCo: **tổng ≥ 75%**, service ≥ 85%; build **fail** nếu dưới ngưỡng.
4. Chạy toàn bộ suite **3 lần liên tiếp**, không flaky; chạy ngẫu nhiên thứ tự (nếu cấu hình được) vẫn xanh.
5. Đột biến thủ công ≥ 6 chỗ ở service/security (ví dụ bỏ kiểm tra chủ sở hữu, bỏ `canTransitionTo`) — mỗi đột biến phải làm test **fail**; ghi vào `docs/mutation-log-api.md`.
6. Báo cáo `docs/test-strategy.md`: tỷ lệ các loại test, thời gian chạy từng nhóm, cách giữ test nhanh.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `./mvnw verify` xanh; số test ≥ 98; JaCoCo đạt ngưỡng (dán báo cáo tóm tắt).
- [ ] Suite chạy 3 lần liên tiếp không flaky (dán kết quả).
- [ ] Mọi test tích hợp dùng Postgres Testcontainers (không H2).
- [ ] ≥ 6 đột biến bị phát hiện.
- [ ] Test không phụ thuộc thứ tự/ dữ liệu chung; mỗi test dọn dữ liệu của nó.
- [ ] `docs/test-strategy.md` đầy đủ.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Khi nào dùng `@WebMvcTest` thay vì `@SpringBootTest`? → Khi chỉ test tầng web (nhanh, nạp ít bean).
2. Bẫy của `@Transactional` trong test? → Test rollback nên che giấu lỗi commit/flush; hành vi khác production (lazy, constraint).
3. Vì sao dùng container singleton? → Giảm thời gian khởi động mỗi lớp test.
4. Test flaky thường do gì? → Thời gian, thứ tự, dữ liệu chung, đồng thời, sleep.

---

### Bài 2.15 – OpenAPI, logging, hardening và vận hành cơ bản
**Thời lượng:** 1 ngày

**Mục tiêu:** API có tài liệu sống, log hữu ích và an toàn, sẵn sàng cho container hóa.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- springdoc-openapi: annotation `@Operation`, `@ApiResponse`, `@Schema`; bảo mật (Bearer) trong tài liệu.
- Logging: SLF4J/Logback, mức log, log có cấu trúc (JSON) ở prod, MDC, **không log PII/secret**.
- Actuator: `health` (liveness/readiness), `metrics`; shutdown êm (`server.shutdown=graceful`).
- HikariCP: kích thước pool, timeout.
- Nén response, giới hạn kích thước request.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Tích hợp springdoc; Swagger UI hoạt động; mô tả mọi endpoint với ví dụ request/response và mã lỗi; có nút Authorize (Bearer).
2. Xuất `docs/api/openapi.json` tự động trong build (hoặc script) và commit.
3. Log định dạng text ở `dev`, **JSON** ở `prod`; mỗi dòng log request có `traceId`, method, path, status, thời gian; **che** header `Authorization`, không log body đăng nhập.
4. Actuator health có `liveness`/`readiness`; cấu hình graceful shutdown (timeout 20s) và chứng minh (gửi request dài rồi tắt app).
5. Cấu hình Hikari hợp lý (giải thích số liệu), giới hạn kích thước request (ví dụ 1MB).
6. `docs/runbook.md`: các lệnh khởi động, biến môi trường bắt buộc, cách đọc log, cách kiểm tra sức khỏe.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Swagger UI mở được; mọi endpoint có mô tả và ví dụ; `docs/api/openapi.json` commit.
- [ ] Log prod là JSON hợp lệ (dán 3 dòng); grep xác nhận không có mật khẩu/ token.
- [ ] `readiness` thành `DOWN` khi DB tắt (thử và dán kết quả).
- [ ] Graceful shutdown được chứng minh.
- [ ] `docs/runbook.md` đủ nội dung.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Liveness và readiness khác nhau thế nào? → Liveness: tiến trình còn sống; readiness: sẵn sàng nhận tải (DB, phụ thuộc).
2. Vì sao log JSON ở production? → Dễ tìm kiếm/ phân tích bằng công cụ log.
3. Vì sao cần graceful shutdown? → Hoàn tất request đang xử lý trước khi tắt, tránh lỗi khi deploy.

---

### Bài 2.16 – Capstone Giai đoạn 2 (bài thi tùy chọn)
**Thời lượng:** 2 ngày

**Bài tập – Phần A: Hoàn thiện**
1. Bộ sưu tập Bruno/Postman `docs/api/weldjobs.collection.json` chạy được một **kịch bản đầu-cuối** (đăng ký → đăng nhập → tạo hồ sơ → tạo tin → tìm kiếm → ứng tuyển → đổi trạng thái → gợi ý), có assertion tự động.
2. Endpoint còn thiếu: `GET/PUT /workers/me`, `PUT /workers/me/skills` (thay thế toàn bộ tập kỹ năng, validate trùng/ không tồn tại), `POST/DELETE /workers/me/certificates`, `GET/PUT /employers/me`, `POST/PUT/DELETE /admin/skills` (xóa mềm khi kỹ năng đang được dùng).
3. README gốc: kiến trúc, cách chạy (Docker Compose + `./mvnw`), biến môi trường, cách chạy test, sơ đồ ERD (Mermaid) và sơ đồ luồng trạng thái ứng tuyển.
4. `docs/stage2-report.md`: 8 câu hỏi phản tư (quyết định thiết kế, điểm yếu còn lại, rủi ro bảo mật còn tồn tại, nếu có 100.000 người dùng thì sao…).

**(Tùy chọn) Bài thi cuối giai đoạn (Agent chủ trì; mỗi tính năng phải có migration + test + bảo mật + tài liệu OpenAPI)**
Agent chọn **3** trong số sau (hoặc tương đương):
1. **Lưu tin yêu thích** (`bookmark`): worker lưu/bỏ lưu tin, danh sách tin đã lưu, không lưu trùng, tin đóng vẫn hiển thị nhưng đánh dấu.
2. **Xóa mềm** tin tuyển dụng (`deleted_at`), ẩn khỏi mọi truy vấn công khai, employer khôi phục trong 30 ngày.
3. **Nhà tuyển dụng đã xác minh** (`verified`): admin xác minh; tin của employer chưa xác minh không hiện công khai; có endpoint admin và test quyền.
4. **Bộ lọc chứng chỉ**: tìm tin yêu cầu chứng chỉ loại X và gợi ý chỉ cho thợ có chứng chỉ còn hiệu lực loại X.
5. **Đếm lượt xem tin** an toàn đồng thời (không mất cập nhật khi 50 request song song) mà không làm chậm API đọc.
6. **Tìm kiếm tên kỹ năng không dấu** (`q=han nhom`) khớp kỹ năng/ tiêu đề có dấu (cân nhắc extension `unaccent`).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Kịch bản đầu-cuối chạy xanh (Agent chạy trên máy người học hoặc yêu cầu dán kết quả toàn bộ).
- [ ] `./mvnw verify` xanh, ≥ 110 test, JaCoCo đạt ngưỡng; chạy 3 lần không flaky.
- [ ] Quét thủ công danh sách kiểm tra bảo mật (Agent đưa 15 câu: IDOR, mass assignment, rò rỉ thông tin, rate limit, secret, CORS, log...) — người học trả lời và chứng minh từng điểm.
- [ ] (Tùy chọn) Bài thi: hoàn thành **cả 3** change request với migration, test, bảo mật, OpenAPI cập nhật.
- [ ] `docs/stage2-report.md` đủ 8 câu; Agent hỏi lại 3 điểm ngẫu nhiên.
- [ ] Người học trả lời đúng ≥ 70% bộ 8 câu hỏi tổng hợp từ bài 2.1–2.15.

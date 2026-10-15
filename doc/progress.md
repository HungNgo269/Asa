# PROGRESS — Bảng tiến độ WeldJobs (Agent cập nhật sau mỗi lần chấm)

> Quy ước: `[ ]` chưa làm · `[x]` đã **PASS** · `[~]` đang làm / còn **nợ mục phụ** (ghi rõ nợ gì) · `[-]` bỏ qua (chỉ bài `(Tùy chọn)` hoặc cả Mốc 3, kèm lý do).
> Agent chỉ in lại **dòng thay đổi** sau mỗi bài; in cả bảng khi người học gõ "tiến độ".

## Ba mốc

| Mốc | Phạm vi | Kết quả dùng được | Thời lượng ước tính* |
|---|---|---|---|
| **Mốc 1 – Backend Java chạy được** (bắt buộc) | GĐ0, GĐ1, GĐ2 | Console app + REST API đầy đủ (auth, CRUD, tìm kiếm, ứng tuyển, test) theo kiến trúc 4 tầng; đủ để phỏng vấn Java/Spring backend | 32–38 ngày (≈ 6.4–7.6 tuần) |
| **Mốc 2 – Sản phẩm chạy trọn vẹn** (bắt buộc) | GĐ3, GĐ4, GĐ5 (trừ 5.6–5.8) | Scheduler/Redis + web Next.js + Docker Compose + CI | 32.5–36 ngày (≈ 6.5–7.2 tuần) (+ 4–6.5 ngày (≈ 0.8–1.3 tuần) nếu làm 5.6–5.8) |
| **Mốc 3 – Nâng cao** (tùy chọn, có thể bỏ cả mốc) | GĐ6, GĐ7 | RabbitMQ (retry/DLQ/outbox) + AI local (Ollama, embedding, pgvector) | 30.5–33.5 ngày (≈ 6.1–6.7 tuần) (+ 1.5 ngày (≈ 0.3 tuần) nếu làm 7.9) |

\* Số ngày làm việc **toàn thời gian**, cộng từ thời lượng từng bài (đã gồm bài 0.2 mới; capstone 3.8, 4.10, 5.8, 6.10 bản gốc không ghi thời lượng nên ước tính 1–2 ngày mỗi bài). Nếu học bán thời gian, nhân tương ứng. **Chất lượng nghiệm thu quan trọng hơn tốc độ.**

**Ghi chú về v2:** GĐ3 nằm trong Mốc 2 (không đẩy sang Mốc 3) vì GĐ4–5 dùng Redis và Docker Compose dựng ở GĐ3. Báo cáo giai đoạn bắt buộc chỉ ở cuối mỗi mốc (xem `00-rules.md` mục 9).

## MỐC 1 – Backend Java chạy được

### Giai đoạn 0
- [x] 0.1 Cài đặt và kiểm tra môi trường
- [x] 0.2 Dựng khung kiến trúc 4 tầng và luật kiến trúc (ArchUnit)
### Giai đoạn 1
- [ ] 1.1 Maven project và Java 21 refresher *(Mới có nháp menu & Text ở com.weldjobs.app; chưa có khung 4 tầng 0.2, chưa có Regions, thiếu 8 test)*
- [ ] 1.2 OOP: lớp, đóng gói, enum, `equals/hashCode/toString`
- [ ] 1.3 Kế thừa, abstract class, interface, polymorphism, composition
- [ ] 1.4 `record`, `var`, `Optional`, sealed types, pattern matching
- [ ] 1.5 Collections
- [ ] 1.6 Generics và Repository in-memory
- [ ] 1.7 Lambda và Stream API
- [ ] 1.8 Exception handling
- [ ] 1.9 File I/O và JSON (Jackson)
- [ ] 1.10 Concurrency: `ExecutorService`, `CompletableFuture`, an toàn luồng
- [ ] 1.11 Testing chuyên sâu: JUnit 5, AssertJ, Mockito
- [ ] 1.12 Capstone Giai đoạn 1 (bài thi tùy chọn)
### Giai đoạn 2
- [ ] 2.1 Khởi tạo project, cấu trúc, Actuator
- [ ] 2.2 IoC / Dependency Injection
- [ ] 2.3 REST controller và DTO
- [ ] 2.4 Validation
- [ ] 2.5 Cấu hình: `application.yml`, profiles, `@ConfigurationProperties`
- [ ] 2.6 PostgreSQL, Docker Compose, Spring Data JPA cơ bản, Flyway căn bản
- [ ] 2.7 Quan hệ JPA, N+1, bảng nối có thuộc tính
- [ ] 2.8 Quản lý migration thực chiến và seed dữ liệu
- [ ] 2.9 CRUD tin tuyển dụng + tìm kiếm động + phân trang
- [ ] 2.10 Xử lý lỗi toàn cục (ProblemDetail)
- [ ] 2.11 Spring Security + JWT + phân quyền + kiểm tra quyền sở hữu
- [ ] 2.12 Luồng ứng tuyển, state machine, giao dịch và đồng thời
- [ ] 2.13 Matching và gợi ý việc làm
- [ ] 2.14 Testing chuyên sâu cho Spring
- [ ] 2.15 OpenAPI, logging, hardening và vận hành cơ bản
- [ ] 2.16 Capstone Giai đoạn 2 (bài thi tùy chọn)

## MỐC 2 – Sản phẩm chạy trọn vẹn
> 5.6–5.8 là tùy chọn (cần VPS/domain). Nếu bỏ qua, ghi `[-]` + lý do; điều kiện kết Mốc 2: `docker compose up` chạy trọn hệ thống ở máy local và CI xanh (bài 5.4).

### Giai đoạn 3
- [ ] 3.1 `@Scheduled`: tự động hết hạn tin tuyển dụng
- [ ] 3.2 `RestClient`/`WebClient`: gọi API ngoài, timeout, retry
- [ ] 3.3 RSS (Rome) và HTML (Jsoup) + chuẩn hóa dữ liệu
- [ ] 3.4 Chống trùng và trích xuất kỹ năng theo luật
- [ ] 3.5 Thu thập song song (`CompletableFuture` / virtual threads) trong Spring
- [ ] 3.6 Redis: cache và rate limit
- [ ] 3.7 Nhật ký import và Docker Compose đầy đủ
- [ ] 3.8 Capstone Giai đoạn 3 (bài thi tùy chọn)
### Giai đoạn 4
- [ ] 4.1 Khởi tạo, cấu trúc và API client có kiểu
- [ ] 4.2 Trang danh sách tin (SSR) + bộ lọc theo URL
- [ ] 4.3 Trang chi tiết tin + SEO + dữ liệu có cấu trúc
- [ ] 4.4 Đăng ký/ đăng nhập và bảo vệ route (BFF + cookie httpOnly)
- [ ] 4.5 Form hồ sơ thợ hàn: multi-select kỹ năng, chứng chỉ
- [ ] 4.6 Khu vực nhà tuyển dụng: tạo/sửa tin, quản lý hồ sơ ứng tuyển
- [ ] 4.7 Ứng tuyển, theo dõi trạng thái, gợi ý việc làm
- [ ] 4.8 Responsive, trạng thái giao diện, accessibility, hiệu năng
- [ ] 4.9 Test frontend và E2E
- [ ] 4.10 Capstone Giai đoạn 4 (bài thi tùy chọn)
### Giai đoạn 5
- [ ] 5.1 Dockerfile cho backend (multi-stage)
- [ ] 5.2 Dockerfile cho Next.js (standalone)
- [ ] 5.3 Docker Compose toàn hệ thống
- [ ] 5.4 CI với GitHub Actions
- [ ] 5.5 Build và đẩy image lên registry
- [ ] 5.6 Reverse proxy, HTTPS và deploy lên VPS *(Tùy chọn)*
- [ ] 5.7 Sao lưu, giám sát, rollback dữ liệu *(Tùy chọn)*
- [ ] 5.8 Capstone Giai đoạn 5 *(Tùy chọn)*

## MỐC 3 – Nâng cao (tùy chọn)
> Có thể bỏ cả mốc (ghi `[-]` từng bài + lý do chung).

### Giai đoạn 6
- [ ] 6.1 Khái niệm và thực hành với RabbitMQ
- [ ] 6.2 Spring AMQP: producer và consumer cơ bản
- [ ] 6.3 Các loại exchange và định tuyến
- [ ] 6.4 Ack, prefetch, publisher confirms, độ bền
- [ ] 6.5 Retry và Dead Letter Queue
- [ ] 6.6 Consumer idempotent
- [ ] 6.7 Outbox pattern
- [ ] 6.8 Áp dụng: thông báo email và pipeline import
- [ ] 6.9 Kiểm thử hệ thống bất đồng bộ
- [ ] 6.10 Capstone Giai đoạn 6 (bài thi tùy chọn)
### Giai đoạn 7
- [ ] 7.1 Cài Ollama, chọn mô hình nhỏ, gọi bằng REST
- [ ] 7.2 Gọi từ Spring: client, timeout, giới hạn đồng thời, health
- [ ] 7.3 Trích xuất kỹ năng thành JSON có cấu trúc (và chống prompt injection)
- [ ] 7.4 Bộ dữ liệu đánh giá và đo chất lượng
- [ ] 7.5 Kết hợp luật + AI, cache và fallback
- [ ] 7.6 Spring AI: so sánh với cách gọi thủ công
- [ ] 7.7 Embedding và pgvector: gợi ý việc làm theo ngữ nghĩa
- [ ] 7.8 Chạy AI bất đồng bộ qua RabbitMQ
- [ ] 7.9 RAG hỏi đáp FAQ *(Tùy chọn)*
- [ ] 7.10 Cầu nối sang dự án dự đoán giá dầu
- [ ] 7.11 Capstone khóa học (bài thi tùy chọn)

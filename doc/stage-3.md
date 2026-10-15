## GIAI ĐOẠN 3 – SCHEDULER, GỌI API NGOÀI, RSS/HTML, REDIS

> **Ghi chú v2 cho file này:** (1) Dạy **từng bước** theo `00-rules.md` — mỗi mục đánh số trong *Bài tập* là một bước, Agent chỉ đưa một bước mỗi lần. (2) Vị trí class theo bảng ở `01-architecture.md` §5; `ArchitectureTest` phải xanh sau mỗi bài. (3) "Bài thi" là **tùy chọn**; báo cáo giai đoạn theo `00-rules.md` mục 9. (4) Mục *Nghiệm thu* gắn *(mục phụ)* hoặc là ghi chú `docs/*.md` có thể ghi nợ `[~]` theo `00-rules.md` mục 4.2.

> **Mục tiêu giai đoạn:** Biến WeldJobs thành hệ thống có **tác vụ nền** và **nhập dữ liệu từ nguồn ngoài**, đồng thời dùng Redis đúng chỗ. Đây cũng là bộ kỹ năng cần cho dự án thu thập tin/dự đoán giá dầu sau này.
>
> **Quy ước pháp lý và đạo đức khi thu thập dữ liệu (bắt buộc đọc)**
> - Chỉ lấy dữ liệu từ nguồn **cho phép**: RSS công khai, API chính thức, hoặc trang có điều khoản/`robots.txt` cho phép. Đọc `robots.txt` và điều khoản sử dụng của từng nguồn **trước khi** viết bộ thu thập.
> - **Không** scrape nền tảng cấm scrape (ví dụ mạng xã hội) và **không** vượt rào đăng nhập/chống bot. Với X: dùng API chính thức (có giới hạn/phí). Với Facebook: không có đường hợp lệ để đọc bài công khai tùy ý — **không làm** trong khóa học này.
> - Giới hạn tốc độ lịch sự (rate limit), đặt `User-Agent` rõ ràng có thông tin liên hệ, tôn trọng `Retry-After`/mã 429.
> - **Không thu thập dữ liệu cá nhân** (số điện thoại, email người đăng); chỉ thông tin tin tuyển dụng công khai ở mức cần thiết. Lưu `sourceUrl` để dẫn nguồn.
> - Trong môi trường học, **dùng máy chủ giả lập (mock server)** do chính bạn dựng làm nguồn chính; nguồn thật chỉ dùng nếu đã kiểm tra điều khoản.

### Bài 3.1 – `@Scheduled`: tự động hết hạn tin tuyển dụng
**Thời lượng:** 1 ngày

**Mục tiêu:** Chạy tác vụ định kỳ an toàn; hiểu cạm bẫy khi chạy nhiều instance.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `@EnableScheduling`, `@Scheduled(fixedRate | fixedDelay | cron, zone=...)`; khác nhau `fixedRate` vs `fixedDelay`; `initialDelay`.
- Pool của scheduler (mặc định **1 thread**) và hệ quả: tác vụ dài chặn tác vụ khác; cấu hình `spring.task.scheduling.pool.size`.
- Chạy nhiều instance: tác vụ chạy **trùng** trên mỗi instance ⇒ cần khóa phân tán (ShedLock) hoặc thiết kế **idempotent**.
- Transaction trong tác vụ nền; xử lý theo lô (batch) để không giữ giao dịch dài.
- Kiểm thử tác vụ định kỳ (gọi trực tiếp method, không chờ lịch).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `JobExpirationJob`: mỗi 10 phút, chuyển các tin `OPEN` có `expires_at < now` sang `EXPIRED`, xử lý theo lô 200 bản ghi/lượt, ghi log số lượng; dùng `Clock`; **idempotent**.
2. Cấu hình cron theo múi giờ `Asia/Ho_Chi_Minh`; thêm job `DailyStatsJob` (cron 02:00) tính số tin mới/ hết hạn/ ứng tuyển trong ngày, lưu vào bảng `daily_stats` (migration mới).
3. Cấu hình pool scheduler ≥ 2 và chứng minh: tác vụ chậm (giả lập sleep 30s) **không** chặn tác vụ nhanh.
4. Chống chạy trùng giữa nhiều instance: dùng **ShedLock** (JDBC provider) hoặc khóa bằng `SELECT ... FOR UPDATE SKIP LOCKED` ở cấp lô; chọn một và giải thích.
5. Test: tạo 500 tin hết hạn + 100 tin còn hạn; gọi method job trực tiếp → đúng 500 chuyển trạng thái, 100 giữ nguyên; chạy lần 2 → không thay đổi; test 2 instance giả lập (2 thread) cùng chạy job cùng lúc không xử lý trùng/ không lỗi.
6. Tắt job bằng property (`weldjobs.jobs.expiration.enabled=false`) và test.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Log cho thấy job chạy đúng lịch (dán ≥ 3 lần chạy).
- [ ] Test 500/100 xanh; chạy lần 2 không đổi (idempotent).
- [ ] Test chạy đồng thời hai worker không xử lý trùng.
- [ ] Chứng minh pool > 1 thread có tác dụng.
- [ ] Có thể tắt/bật bằng cấu hình và có test.
- [ ] `docs/scheduler-notes.md` trả lời: vì sao `fixedDelay` thường an toàn hơn `fixedRate` cho tác vụ chậm; vì sao cần ShedLock.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. `fixedRate` khác `fixedDelay`? → Rate: tính từ lúc bắt đầu lần trước; Delay: từ lúc kết thúc lần trước.
2. Nếu chạy 3 instance, tác vụ chạy mấy lần? → 3, trừ khi có khóa phân tán.
3. Vì sao xử lý theo lô? → Tránh giao dịch dài, giữ khóa lâu, tràn bộ nhớ.

---

### Bài 3.2 – `RestClient`/`WebClient`: gọi API ngoài, timeout, retry
**Thời lượng:** 1,5 ngày

**Mục tiêu:** Gọi dịch vụ bên ngoài **an toàn và bền bỉ**: không treo, không làm sập hệ thống khi nguồn lỗi.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `RestClient` (đồng bộ, hiện đại) vs `WebClient` (reactive) — khi nào dùng cái nào; `RestTemplate` (cũ).
- Timeout kết nối/đọc **bắt buộc đặt** (mặc định có thể là vô hạn).
- Xử lý mã lỗi 4xx/5xx; `onStatus`; giải mã JSON vào record.
- Retry có **backoff lũy thừa + jitter**, chỉ retry lỗi **có thể phục hồi** (5xx, timeout, 429), **không** retry 4xx logic; giới hạn số lần; idempotency.
- Circuit breaker (khái niệm; Resilience4j) — nhận biết.
- Khả năng quan sát: log thời gian/ trạng thái từng lời gọi (không log secret).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Dựng **mock server** (WireMock hoặc Spring Boot app nhỏ `mock-job-source`) giả lập nguồn tin: `GET /jobs?page=n` trả JSON tin tuyển dụng phân trang; có chế độ: chậm, lỗi 500 ngẫu nhiên, trả 429 có `Retry-After`, trả JSON sai định dạng.
2. `ExternalJobApiClient` dùng `RestClient`: timeout kết nối 2s, đọc 5s (cấu hình qua `@ConfigurationProperties`); `User-Agent` rõ ràng; phân trang tự động đến hết.
3. Retry: tối đa 3 lần, backoff 500ms → 1s → 2s có jitter; **chỉ** cho timeout/5xx/429 (tôn trọng `Retry-After`); 4xx khác **không** retry. Dùng Spring Retry hoặc Resilience4j, hoặc tự cài rồi so sánh (chọn một, giải thích).
4. Lỗi → ném `ExternalSourceException` có `source`, `status`, `retryable`; **không** làm sập job.
5. Test với WireMock: (a) thành công nhiều trang; (b) 500 rồi 200 ⇒ thành công sau retry; (c) 500 liên tục ⇒ thất bại sau đúng 3 lần; (d) 404 ⇒ không retry (verify số request = 1); (e) timeout ⇒ retry; (f) 429 + `Retry-After`; (g) JSON sai ⇒ lỗi rõ ràng.
6. Viết `docs/http-client-notes.md`: vì sao timeout mặc định nguy hiểm; vì sao cần jitter; cách chọn số lần retry.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] 7 test (a–g) xanh, có `verify` số lượng request.
- [ ] Không có lời gọi HTTP nào thiếu timeout (grep/ đọc code).
- [ ] Retry **không** diễn ra với 4xx (trừ 429) — test chứng minh.
- [ ] Thời gian chờ giữa các lần retry đúng backoff (test với `Clock`/đo, cho phép sai số hợp lý).
- [ ] Log mỗi lời gọi gồm nguồn, status, thời gian; không chứa secret.
- [ ] Mock server chạy được bằng một lệnh, có README.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao retry không dùng backoff cố định? → Dễ gây "thundering herd"; lũy thừa + jitter phân tán tải.
2. Vì sao không retry mọi lỗi? → Lỗi 4xx do request sai, retry vô ích/gây hại; thao tác không idempotent có thể nhân bản.
3. Circuit breaker giải quyết gì? → Ngừng gọi nguồn đang hỏng để tiết kiệm tài nguyên và phục hồi nhanh.

---

### Bài 3.3 – RSS (Rome) và HTML (Jsoup) + chuẩn hóa dữ liệu
**Thời lượng:** 1,5 ngày

**Mục tiêu:** Đọc nguồn RSS/HTML hợp lệ, chuẩn hóa về mô hình `RawJob` → `JobDraft`.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- RSS 2.0 vs Atom; thư viện Rome; trường `title`, `link`, `pubDate`, `description`, `guid`.
- Jsoup: `connect/parse`, CSS selector, `select`, `text()`, `attr("abs:href")`; **làm sạch HTML** (`Safelist`) để chống XSS khi lưu/hiển thị.
- Mã hóa ký tự, múi giờ trong `pubDate`.
- `robots.txt` và điều khoản — đọc **trước**.
- Chuẩn hóa: bỏ HTML, chuẩn hóa khoảng trắng, chuẩn hóa tên tỉnh (map tên không dấu/ viết tắt → danh sách chuẩn), chuẩn hóa lương (parse "12-18 triệu", "15tr", "$800").

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Mở rộng mock server: `/rss/jobs.xml` (≥ 20 mục, vài mục lỗi cấu trúc, vài mục trùng `guid`), `/html/jobs` (trang danh sách HTML ≥ 20 tin, phân trang, cấu trúc gần giống trang thực tế).
2. `RssJobSource implements JobSource` dùng Rome; `HtmlJobSource` dùng Jsoup; cả hai trả `List<RawJob>` (record có `sourceName, sourceUrl, externalId, title, companyName, locationText, description, postedAt, salaryText`).
3. `JobNormalizer`: `RawJob → JobDraft` — làm sạch HTML bằng allowlist, chuẩn hóa tỉnh (≥ 30 biến thể: "TP.HCM", "Sài Gòn", "HCM", "Da Nang", "Đà nẵng"...), parse lương (≥ 15 mẫu chuỗi lương khác nhau), cắt `description` ≤ 5000, xác định `postedAt` an toàn múi giờ.
4. Bộ test cho chuẩn hóa dùng **file mẫu thực tế mô phỏng** (đặt trong `src/test/resources/samples/`): ≥ 40 test (đặc biệt chuỗi lương và tỉnh).
5. Tuyệt đối **không** lưu HTML thô nguy hiểm: test với payload `<script>alert(1)</script>` và `<img onerror=...>` ⇒ bị loại.
6. Kiểm thử `robots.txt`: viết `RobotsPolicy` đọc `robots.txt` của mock server và **từ chối** truy cập đường dẫn bị cấm (test).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Parse đúng ≥ 90% mục hợp lệ trong RSS/HTML mẫu; mục lỗi cấu trúc bị bỏ qua có log, **không** làm hỏng cả lô.
- [ ] ≥ 40 test chuẩn hóa xanh, gồm biên (lương một vế, "thỏa thuận", tiền tệ lạ).
- [ ] XSS payload bị làm sạch (test).
- [ ] `RobotsPolicy` chặn đường dẫn bị cấm (test).
- [ ] Mã hóa tiếng Việt đúng; múi giờ `pubDate` được xử lý (test với `+0700` và `GMT`).
- [ ] `docs/scraping-ethics.md`: 8 quy tắc bạn sẽ tuân thủ + giải thích vì sao không làm nguồn mạng xã hội.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao phải làm sạch HTML? → Chống XSS và rác định dạng.
2. Vì sao ưu tiên RSS/API hơn scrape HTML? → Ổn định, hợp lệ, ít vỡ khi giao diện đổi.
3. `robots.txt` có tính pháp lý bắt buộc không? → Không hẳn là luật, nhưng là quy ước và bằng chứng thiện chí; tôn trọng cùng điều khoản dịch vụ.

---

### Bài 3.4 – Chống trùng và trích xuất kỹ năng theo luật
**Thời lượng:** 1,5 ngày

**Mục tiêu:** Lưu tin nhập vào hệ thống không trùng lặp và gắn kỹ năng tự động bằng luật (nền tảng để so sánh với AI ở Giai đoạn 7).

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Khóa tự nhiên `(source, external_id)`; băm nội dung (SHA-256) để phát hiện thay đổi; **upsert** (`INSERT ... ON CONFLICT DO UPDATE`).
- Khử trùng giữa **nhiều nguồn** (cùng tin đăng ở nhiều nơi): chuẩn hóa tiêu đề + công ty + tỉnh → fingerprint; độ tương đồng chuỗi (Jaccard trên token / n-gram) ở mức khái niệm.
- Trích xuất theo luật: từ điển từ khóa → mã kỹ năng (đã chuẩn hóa không dấu): "tig" → `PROC_TIG`, "mig/mag" → `PROC_MIG_MAG`, "han nhom" → `MAT_ALUMINIUM`, "inox", "6g", "ong"... xử lý đồng nghĩa, viết tắt, ranh giới từ (tránh "tig" trong "tiger").
- Đo chất lượng: precision/recall (khái niệm).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Migration: bảng `imported_job(id, source, external_id, content_hash, job_id nullable, raw_title, raw_company, ..., first_seen_at, last_seen_at, status)` với unique `(source, external_id)`; liên kết tới `job_post` (tin do hệ thống import có cờ `imported=true`, `source_url`).
2. `ImportService.importDrafts(List<JobDraft>)`: upsert theo `(source, external_id)`; nếu `content_hash` đổi thì cập nhật; nếu không thì chỉ cập nhật `last_seen_at`.
3. Khử trùng liên nguồn: fingerprint = hash(normalize(title) + normalize(company) + province); nếu trùng với tin đã có ⇒ liên kết, không tạo tin mới. Ghi `docs/dedup-notes.md` về trường hợp sai (khác tin nhưng trùng fingerprint) và cách giảm.
4. `RuleBasedSkillExtractor`: từ điển ≥ 60 từ khóa/biến thể → mã kỹ năng (nạp từ file tài nguyên YAML/JSON, **không hardcode** trong code), khớp theo **token/ranh giới từ** sau chuẩn hóa không dấu. Trả `Set<ExtractedSkill(code, matchedText, confidence)>`.
5. **Bộ dữ liệu vàng (golden set):** tự gán nhãn thủ công **≥ 50 mô tả tin** (đa dạng: tiếng Việt có/không dấu, viết tắt, tiếng Anh, lẫn lộn) vào `src/test/resources/golden/skills.jsonl` (mô tả → danh sách mã kỹ năng đúng). Viết test đo **precision và recall** của extractor; mục tiêu ban đầu: precision ≥ 0.85, recall ≥ 0.70 (ghi kết quả thực tế, không chỉnh nhãn cho đẹp).
6. Lưu kỹ năng trích xuất vào `job_required_skill` với cờ `mandatory=false` và `source=RULE` (cột mới), để phân biệt kỹ năng do hệ thống gắn.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Import cùng một lô 2 lần ⇒ không nhân đôi (test); đổi nội dung 1 tin ⇒ cập nhật đúng 1 bản ghi.
- [ ] Hai nguồn đăng cùng một tin ⇒ chỉ có 1 `job_post` (test); 2 tin khác nhau không bị gộp nhầm (test ≥ 5 cặp).
- [ ] Từ điển kỹ năng nằm trong file tài nguyên, có test nạp.
- [ ] Golden set ≥ 50 mẫu do **người học tự gán nhãn**; báo cáo precision/recall được dán; ≥ 10 ca lỗi được phân tích (vì sao sai, cải thiện thế nào).
- [ ] Không có kết quả "tig" khớp trong "tiger", "mig" trong "migrate" (test).
- [ ] `docs/dedup-notes.md` hoàn chỉnh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao cần cả `(source, external_id)` và fingerprint nội dung? → Khóa tự nhiên chống trùng trong một nguồn; fingerprint chống trùng liên nguồn.
2. Precision và recall khác nhau thế nào? → Precision: trong số dự đoán đúng bao nhiêu; recall: trong số thực tế bao nhiêu được tìm thấy.
3. Vì sao tách từ điển ra file? → Thay đổi không cần biên dịch, admin/ người không code chỉnh được.

---

### Bài 3.5 – Thu thập song song (`CompletableFuture` / virtual threads) trong Spring
**Thời lượng:** 1 ngày

**Mục tiêu:** Chạy nhiều nguồn song song trong ứng dụng thật, có giới hạn tài nguyên và cô lập lỗi.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `@Async` + `TaskExecutor` (bean riêng, đặt tên, giới hạn hàng đợi/ rejection policy) vs gọi `CompletableFuture` trực tiếp.
- Virtual threads trong Spring Boot (`spring.threads.virtual.enabled`) — hiệu ứng và hạn chế (pinning, tài nguyên hạ nguồn vẫn có giới hạn như pool kết nối DB).
- Bulkhead: giới hạn đồng thời **mỗi nguồn** (semaphore), timeout tổng.
- Ngữ cảnh: MDC không tự lan sang thread khác — cần truyền.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `ImportOrchestrator.runAll()`: chạy tất cả `JobSource` (RSS, HTML, API mock) **song song**, mỗi nguồn timeout độc lập (cấu hình), cô lập lỗi từng nguồn, tổng hợp `ImportRunResult` (số tin mới/ cập nhật/ trùng/ lỗi theo nguồn).
2. Bean `TaskExecutor` riêng cho import (kích thước, hàng đợi giới hạn); thử bật virtual threads và so sánh thời gian với 10 nguồn mock, mỗi nguồn trễ 1s.
3. Giới hạn đồng thời tối đa 2 yêu cầu/ nguồn bằng `Semaphore` (lịch sự với nguồn).
4. Truyền `traceId` (MDC) sang thread con; test chứng minh log của thread con có cùng `traceId`.
5. Job định kỳ `ImportJob` (mỗi 30 phút, có ShedLock) gọi `runAll()`; **không chạy chồng** khi lần trước chưa xong.
6. Test: 3 nguồn (1 chậm, 1 lỗi, 1 tốt) ⇒ kết quả tốt vẫn lưu; thời gian tổng ≈ nguồn chậm nhất (có biên).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Test 3 nguồn xanh; lỗi một nguồn không ảnh hưởng nguồn khác.
- [ ] So sánh pool vs virtual threads có số liệu trong `docs/concurrency-spring-notes.md`.
- [ ] `traceId` lan truyền đúng (test).
- [ ] Không chạy chồng (test: gọi `runAll` hai lần đồng thời ⇒ lần hai bị bỏ qua với thông báo rõ).
- [ ] Giới hạn đồng thời mỗi nguồn được chứng minh (đếm max concurrent trong mock).

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao cần executor riêng? → Tránh chiếm pool chung của ứng dụng/ request; kiểm soát tài nguyên.
2. Virtual threads có làm DB nhanh hơn không? → Không; vẫn bị giới hạn bởi pool kết nối và tài nguyên hạ nguồn.
3. Vì sao MDC mất trên thread khác? → MDC gắn theo thread; phải sao chép/ truyền thủ công (TaskDecorator).

---

### Bài 3.6 – Redis: cache và rate limit
**Thời lượng:** 1,5–2 ngày

**Mục tiêu:** Dùng Redis đúng mục đích: cache có kiểm soát, giới hạn tốc độ, khóa/ chống trùng nhẹ.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Kiểu dữ liệu Redis: string, hash, set, sorted set; TTL; `SETNX`/`SET NX EX`; `INCR`/`EXPIRE`.
- Spring Cache abstraction (`@Cacheable`, `@CacheEvict`, `@CachePut`, `key`, `condition`, `unless`), cấu hình `RedisCacheManager` (TTL theo cache, serializer **JSON**, không dùng JDK serialization).
- Chiến lược: cache-aside; **cache stampede**; hạn chế dữ liệu cũ (stale); invalidation (khó nhất); không cache dữ liệu theo người dùng nhạy cảm vào khóa chung.
- Rate limit: fixed window vs sliding window vs token bucket.
- `StringRedisTemplate`/`RedisTemplate` khi cần thao tác thấp cấp.
- Redis không phải nguồn dữ liệu chính (mất dữ liệu vẫn chạy được).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Thêm Redis vào Docker Compose (healthcheck, volume tùy chọn); cấu hình Spring Data Redis qua biến môi trường.
2. **Cache danh mục kỹ năng** (`GET /skills`): TTL 1 giờ, evict khi admin sửa; test hit/miss (đếm số lần gọi DB bằng spy/ đếm query).
3. **Cache chi tiết tin** `jobs::{id}` TTL 5 phút, evict khi tin sửa/đóng/hết hạn; **không** cache danh sách tìm kiếm theo mọi tổ hợp (giải thích vì sao; hoặc cache có kiểm soát cho truy vấn phổ biến nhất — chọn và nêu trade-off).
4. Serializer JSON (không JDK serialization), có `type` an toàn; test round-trip với `record`.
5. **Rate limit đăng nhập:** tối đa 5 lần/ 5 phút theo `(IP, email băm)` bằng `INCR + EXPIRE` (hoặc Lua cho nguyên tử); vượt giới hạn → 429 kèm `Retry-After`. Test (Testcontainers Redis) gồm: đúng giới hạn, sau khi hết cửa sổ được thử lại, hai khóa độc lập.
6. **Chống import trùng đồng thời** bằng khóa phân tán đơn giản `SET key value NX EX 600` (kèm token để chỉ chủ khóa mới nhả khóa — mô tả vì sao nhả khóa bằng Lua kiểm tra token); so sánh với ShedLock (3.1).
7. **Stampede:** mô phỏng 50 request đồng thời khi cache hết hạn; giải pháp (khóa theo key/ `sync=true` / TTL jitter); chứng minh số lần tải DB giảm.
8. Khi Redis tắt: ứng dụng **vẫn chạy** (cache lỗi không làm API lỗi) — cài `CacheErrorHandler`, test.
9. `docs/redis-notes.md`: bảng "cái gì nên/không nên cache trong WeldJobs" ≥ 8 dòng, kèm TTL và chiến lược invalidation.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Test cache hit/miss/evict xanh (chứng minh bằng số truy vấn DB).
- [ ] Giá trị trong Redis là JSON đọc được (dán kết quả `GET key`), không phải byte JDK.
- [ ] Rate limit: test 4 kịch bản xanh; header `Retry-After` đúng.
- [ ] Khóa phân tán nhả đúng chủ (test: tiến trình B không nhả được khóa của A).
- [ ] Test stampede cho thấy số lần tải DB ≤ 2 trong 50 request đồng thời.
- [ ] Tắt Redis ⇒ API vẫn trả dữ liệu (test/ bằng chứng).
- [ ] `docs/redis-notes.md` hoàn chỉnh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Cache-aside là gì? → Ứng dụng kiểm tra cache, miss thì đọc DB rồi ghi cache.
2. Vì sao invalidation khó? → Dữ liệu đổi từ nhiều nơi/ nhiều khóa liên quan; dễ lệch; TTL là lưới an toàn.
3. Vì sao không dùng JDK serialization? → Khó đọc, tăng nguy cơ bảo mật (deserialize), không tương thích khi đổi class.
4. `SET NX EX` khác `SETNX` rồi `EXPIRE` thế nào? → Gộp thành một lệnh nguyên tử, tránh khóa vĩnh viễn nếu crash giữa hai lệnh.

---

### Bài 3.7 – Nhật ký import và Docker Compose đầy đủ
**Thời lượng:** 1 ngày

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Bảng `import_run(id, started_at, finished_at, status, trigger)` và `import_run_source(run_id, source, fetched, created, updated, duplicates, failed, error_summary)`; endpoint admin `GET /admin/import-runs?page=` và `GET /admin/import-runs/{id}`.
2. Metric tùy chỉnh (Micrometer): `import.jobs.created{source}`, `import.run.duration`, `import.errors{source}`; xem qua `/actuator/metrics`.
3. `docker-compose.yml` chạy: `postgres`, `redis`, `mock-job-source`, `weldjobs-api` (tạm chạy image dựng thủ công hoặc để `./mvnw spring-boot:run` ngoài; hoàn thiện ở GĐ5). Có `healthcheck` và `depends_on: condition: service_healthy`.
4. Seed + import chạy một lần từ đầu bằng **một** lệnh (`make up` hoặc script) trên máy sạch.
5. Kịch bản demo ghi trong README: khởi động → import → tìm tin đã import → xem lịch sử import.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Máy sạch (hoặc `docker compose down -v`) → một lệnh dựng đủ hệ thống; Agent đọc log xác nhận.
- [ ] Lịch sử import có số liệu đúng so với DB (đối chiếu bằng truy vấn).
- [ ] Metric hiển thị trong `/actuator/metrics` (dán ví dụ).
- [ ] Endpoint admin chỉ ADMIN truy cập (test quyền).

---

### Bài 3.8 – Capstone Giai đoạn 3 (bài thi tùy chọn)
**Bài tập – Phần A**
- Tổng nghiệm thu hệ thống: mock server → import (RSS + HTML + API) → khử trùng → trích xuất kỹ năng → tìm kiếm → gợi ý, tất cả có cache Redis, rate limit đăng nhập, job hết hạn tự động.
- `docs/stage3-report.md`: 6 câu phản tư (độ tin cậy nguồn, rủi ro pháp lý, độ chính xác trích xuất, quyết định cache, điều gì xảy ra khi nguồn đổi cấu trúc, cách giám sát).
- **Cầu nối dự án giá dầu** (`docs/oil-bridge-stage3.md`): ánh xạ từng thành phần giai đoạn này sang hệ thống thu thập tin dầu (nguồn RSS Reuters/EIA/OPEC, API chính thức của X, khử trùng sự kiện, trích xuất thực thể/từ khóa dầu, lịch import, cache, chống trùng). Liệt kê phần **tái sử dụng được nguyên xi** và phần phải **làm lại**.

**(Tùy chọn) Bài thi cuối giai đoạn** — Agent chọn 3:
1. Thêm nguồn thứ 4 (mock) với cấu trúc JSON lồng khác; tích hợp đầy đủ + test + golden set bổ sung.
2. Thêm cơ chế **quarantine**: tin import nghi ngờ (lương bất thường, mô tả quá ngắn, nhiều từ khóa spam) vào trạng thái `PENDING_REVIEW`, admin duyệt/ từ chối bằng endpoint.
3. Thêm **circuit breaker** cho một nguồn (tự ngắt sau N lỗi liên tiếp, thử lại sau X phút, hiển thị trạng thái ở endpoint admin).
4. Cache **top 10 tin mới nhất theo tỉnh** với invalidation đúng khi có tin mới/ hết hạn; chứng minh bằng test.
5. Giới hạn tốc độ API công khai theo IP (sliding window) với header `X-RateLimit-*`.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Tất cả test xanh, độ phủ không giảm so với GĐ2; chạy 3 lần không flaky.
- [ ] Demo hệ thống một lệnh từ máy sạch.
- [ ] `docs/stage3-report.md` và `docs/oil-bridge-stage3.md` đầy đủ, Agent hỏi lại 3 điểm.
- [ ] (Tùy chọn) Bài thi: hoàn thành cả 3 yêu cầu.
- [ ] Trả lời đúng ≥ 70% bộ 12 câu tổng hợp 3.1–3.7.

## GIAI ĐOẠN 6 – MESSAGE QUEUE VỚI RABBITMQ

> **Ghi chú v2 cho file này:** (1) Dạy **từng bước** theo `00-rules.md` — mỗi mục đánh số trong *Bài tập* là một bước, Agent chỉ đưa một bước mỗi lần. (2) Vị trí class theo bảng ở `01-architecture.md` §5; `ArchitectureTest` phải xanh sau mỗi bài. (3) "Bài thi" là **tùy chọn**; báo cáo giai đoạn theo `00-rules.md` mục 9. (4) Mục *Nghiệm thu* gắn *(mục phụ)* hoặc là ghi chú `docs/*.md` có thể ghi nợ `[~]` theo `00-rules.md` mục 4.2.

> **Mục tiêu giai đoạn:** Tách các việc chậm/ không cần đồng bộ (thông báo, nhập tin, tính toán gợi ý, AI) ra khỏi luồng request; đảm bảo **không mất, không làm trùng** khi hệ thống lỗi.
>
> **Quy ước giai đoạn 6**
> - Mọi message là một **sự kiện có hợp đồng**: có `eventId` (UUID), `type`, `occurredAt`, `version`, `payload`; tài liệu hóa trong `docs/event-catalog.md`.
> - Giả định **at-least-once** (có thể nhận trùng). Mọi consumer có tác dụng phụ phải **idempotent**.
> - Không thao tác DB và publish message theo kiểu "hai bước không đồng bộ" nếu không có cơ chế an toàn (xem 6.7).
> - Queue quan trọng phải **durable**, message **persistent**, có DLQ; cấu hình trong code khai báo (declare) nhất quán.
> - Agent phải kiểm tra tài liệu **phiên bản hiện hành** của RabbitMQ và Spring AMQP (tên lớp/ cấu hình có thể đổi), đặc biệt khuyến nghị về **quorum queue**.

### Bài 6.1 – Khái niệm và thực hành với RabbitMQ
**Thời lượng:** 1 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Producer, Consumer, **Exchange**, **Queue**, **Binding**, routing key, virtual host, connection vs channel.
- Vì sao có exchange (producer không gửi thẳng vào queue).
- Ack/ nack/ requeue ở mức khái niệm; durable queue, persistent message.
- So sánh nhanh: RabbitMQ (broker + routing linh hoạt, tiêu thụ xong thì xóa) với Kafka (log phân vùng, lưu lại, replay) và Redis (cache/ stream nhẹ) — khi nào chọn gì cho WeldJobs.
- Các nhu cầu bất đồng bộ của WeldJobs: thông báo email, xử lý import, tính gợi ý, trích xuất kỹ năng bằng AI, cập nhật chỉ mục/embedding.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Thêm `rabbitmq:<bản có management>` vào Compose (healthcheck, volume, user/ mật khẩu không mặc định từ biến môi trường, cổng quản trị chỉ bind `127.0.0.1`).
2. Dùng Management UI **và** CLI (`rabbitmqadmin`/`rabbitmqctl`) để: tạo exchange `demo.direct`, 2 queue, binding bằng routing key, publish tay 5 message, đọc lại bằng "Get messages"; quan sát `Ready` và `Unacked`.
3. Thí nghiệm: (a) publish vào exchange không có binding phù hợp ⇒ message **bị bỏ** (nhận xét); (b) đặt queue non-durable rồi restart broker ⇒ queue/ message mất; (c) queue durable + message persistent ⇒ còn sau restart (so sánh).
4. Viết `docs/mq-concepts.md`: sơ đồ (Mermaid) luồng từ producer → exchange → queue → consumer; bảng 5 use case của WeldJobs và vì sao dùng/ không dùng queue cho từng cái.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Management UI truy cập được (chỉ qua localhost); mật khẩu không phải mặc định.
- [ ] Ảnh chụp/ log cho 3 thí nghiệm (a)(b)(c) với kết luận đúng.
- [ ] `docs/mq-concepts.md` có sơ đồ và bảng use case hợp lý.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao producer gửi vào exchange chứ không vào queue? → Tách producer khỏi cấu trúc consumer; routing linh hoạt.
2. Durable queue có đủ để không mất message? → Không; còn cần message persistent (và với độ bền cao hơn: quorum queue + publisher confirms).
3. Khi nào không nên dùng message queue? → Cần phản hồi đồng bộ ngay; thao tác đơn giản nhanh; độ phức tạp vượt lợi ích.

---

### Bài 6.2 – Spring AMQP: producer và consumer cơ bản
**Thời lượng:** 1,5 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `spring-boot-starter-amqp`; cấu hình `spring.rabbitmq.*`; `RabbitAdmin` khai báo `Queue/Exchange/Binding` bằng bean (`Declarables`).
- `RabbitTemplate.convertAndSend`; `@RabbitListener`; `MessageConverter` JSON (Agent kiểm tra lớp converter hiện hành); header message; `correlationId`.
- Hợp đồng sự kiện bằng `record`, **phiên bản hóa** (`version`), tương thích tiến/ lùi (thêm field optional).
- **`@TransactionalEventListener(phase = AFTER_COMMIT)`** để chỉ publish sau khi giao dịch DB commit; hạn chế của cách này (xem 6.7).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Định nghĩa `record EventEnvelope<T>(UUID eventId, String type, int version, Instant occurredAt, T payload)` và các sự kiện: `JobCreated`, `JobClosed`, `ApplicationSubmitted`, `ApplicationStatusChanged`.
2. Khai báo hạ tầng bằng code: exchange `weldjobs.events` (topic), queue `notify.email` và `audit.log` (durable), binding tương ứng.
3. `JobApplicationService` phát sự kiện nội bộ (`ApplicationEventPublisher`) và `EventPublisher` lắng nghe **sau commit** để gửi lên RabbitMQ.
4. Consumer `AuditLogConsumer` ghi `audit_log(event_id, type, payload, received_at)`; consumer `EmailNotificationConsumer` (tạm chỉ ghi log).
5. Test: (a) ứng tuyển thành công ⇒ có message trong queue; (b) giao dịch rollback ⇒ **không** có message; (c) payload JSON đúng schema (so với mẫu).
6. Thêm trường `correlationId`/`traceId` vào header và log của consumer để truy vết xuyên suốt request → message → consumer.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Ứng tuyển ⇒ thấy message trong Management UI/ consumer log (dán).
- [ ] Test rollback chứng minh không phát hành sự kiện (dán).
- [ ] `traceId` xuất hiện ở log API và log consumer cùng giá trị.
- [ ] Hạ tầng queue/ exchange được khai báo bằng code (xóa tay trên UI, khởi động lại app ⇒ tạo lại).
- [ ] `docs/event-catalog.md` có 4 sự kiện với schema và ví dụ JSON.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao dùng `AFTER_COMMIT`? → Tránh phát sự kiện cho thay đổi chưa/ không được lưu.
2. Hạn chế của `AFTER_COMMIT`? → Nếu app crash hoặc broker lỗi sau commit, sự kiện mất (dual-write) ⇒ cần outbox.
3. Vì sao phiên bản hóa sự kiện? → Producer và consumer triển khai khác thời điểm.

---

### Bài 6.3 – Các loại exchange và định tuyến
**Thời lượng:** 1 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `direct`, `topic` (`*` một từ, `#` nhiều từ), `fanout`, `headers`; default exchange.
- Mô hình **work queue** (nhiều consumer cùng một queue, chia tải) và **pub/sub** (mỗi nhóm consumer một queue riêng).
- Quy ước đặt tên routing key và queue.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Quy ước routing key: `job.created`, `job.closed`, `application.submitted`, `application.status.changed.<STATUS>`.
2. Các queue và binding:
   - `notify.email` ← `application.*`, `job.created`.
   - `matching.index` ← `job.*`.
   - `audit.log` ← `#` (tất cả).
   - `analytics.hired` ← `application.status.changed.HIRED`.
3. Chạy 2 instance consumer của `notify.email` ⇒ chứng minh chia tải vòng tròn; chạy 1 consumer cho mỗi queue khác nhau ⇒ chứng minh mỗi queue nhận **bản sao** sự kiện.
4. Ví dụ `fanout` cho thông báo "bảo trì hệ thống"; ví dụ `headers` (chỉ mô tả bằng văn bản khi nào dùng).
5. Test định tuyến: publish các routing key khác nhau, kiểm tra queue nào nhận (ma trận ≥ 8 ca) bằng Testcontainers RabbitMQ.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Ma trận định tuyến ≥ 8 ca xanh (đúng queue nhận, đúng queue không nhận).
- [ ] Bằng chứng chia tải (log hai consumer xen kẽ) và bằng chứng nhân bản (mỗi queue nhận đủ).
- [ ] Giải thích bằng văn bản: khi nào dùng topic vs direct.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. `*` và `#` khác nhau? → `*` đúng một từ, `#` không hoặc nhiều từ.
2. Muốn nhiều dịch vụ khác nhau đều nhận cùng một sự kiện thì làm gì? → Mỗi dịch vụ một queue riêng bind vào cùng exchange.
3. Muốn tăng tốc xử lý một loại việc thì làm gì? → Thêm consumer cùng queue (competing consumers).

---

### Bài 6.4 – Ack, prefetch, publisher confirms, độ bền
**Thời lượng:** 1,5 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Chế độ ack của Spring AMQP (`AUTO` — ack khi listener thoát thành công/ nack khi ném exception; `MANUAL`); `requeue` và nguy cơ **vòng lặp message độc**.
- `prefetch` (QoS) và ảnh hưởng đến công bằng/ độ trễ; `concurrency`/`max-concurrency`.
- **Publisher confirms** (`publisher-confirm-type`) và **returns** (`mandatory`); xử lý khi bị nack/ trả về.
- Độ bền: durable, persistent, **quorum queue** (đặc tính, đánh đổi); `heartbeat`, tự kết nối lại.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Thí nghiệm "consumer chết giữa chừng": consumer xử lý chậm 10s; `kill -9` giữa chừng ⇒ message được giao lại cho consumer khác (dán log, chú ý `redelivered=true`).
2. Thí nghiệm prefetch: 2 consumer (một nhanh, một chậm) với prefetch 100 vs 1; đo số message mỗi bên xử lý; kết luận.
3. Publisher confirms: bật confirm; publish vào exchange **không tồn tại**/ routing key không khớp với `mandatory=true` ⇒ bắt `ReturnCallback`; log và quyết định xử lý (retry hay báo lỗi).
4. Chuyển `notify.email` thành **quorum queue** (hoặc loại phù hợp theo khuyến nghị hiện hành); ghi rõ khác biệt hành vi, đánh đổi.
5. Cấu hình `AcknowledgeMode` rõ ràng; **cấm requeue vô hạn** (`default-requeue-rejected=false` hoặc `AmqpRejectAndDontRequeueException`).
6. `docs/mq-reliability.md`: bảng "lỗi gì ⇒ hành vi gì ⇒ cách cấu hình" cho ≥ 8 tình huống (broker restart, consumer crash, message độc, mạng đứt, queue đầy…).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Bằng chứng giao lại message sau khi consumer bị `kill -9`.
- [ ] Kết quả prefetch có số liệu và kết luận đúng.
- [ ] Test/ log cho thấy `ReturnCallback` hoạt động với routing key sai.
- [ ] Chứng minh message độc **không** gây vòng lặp vô hạn (dán log đếm số lần).
- [ ] `docs/mq-reliability.md` ≥ 8 tình huống.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Prefetch lớn có hại khi nào? → Consumer chậm giữ nhiều message, các consumer khác rảnh; mất công bằng, tăng độ trễ.
2. Confirm khác ack? → Confirm: broker xác nhận đã nhận từ producer; ack: consumer xác nhận đã xử lý.
3. Vì sao requeue vô hạn nguy hiểm? → Message lỗi quay vòng chiếm tài nguyên, chặn hàng đợi.

---

### Bài 6.5 – Retry và Dead Letter Queue
**Thời lượng:** 2 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- **Dead Letter Exchange (DLX)**: message bị reject/hết TTL/ vượt độ dài ⇒ chuyển sang DLX; header `x-death`.
- Mô hình **retry có trì hoãn** bằng "hàng đợi chờ" (TTL + DLX quay lại queue chính) thay vì retry nóng trong listener.
- Phân loại lỗi: **tạm thời** (retry) vs **vĩnh viễn** (payload sai, nghiệp vụ không thể thực hiện ⇒ DLQ ngay).
- Re-drive (đưa lại từ DLQ) có kiểm soát; cảnh báo khi DLQ có message.
- So sánh retry trong listener (`spring.rabbitmq.listener.simple.retry`) với retry qua hàng đợi chờ.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Thiết kế cho `notify.email`: `notify.email` → (lỗi tạm thời) → `notify.email.retry.5s` → quay về `notify.email`; sau đó `retry.30s`, `retry.5m`; vượt 3 lần ⇒ `notify.email.dlq`.
2. Phân loại exception: `TransientException` (retry), `PermanentException`/ JSON sai (DLQ ngay).
3. Đếm số lần thử qua header (`x-death` count hoặc header tự quản) — không dùng bộ nhớ trong tiến trình.
4. Endpoint admin: `GET /admin/dlq/{queue}` (xem tóm tắt message, che dữ liệu nhạy cảm), `POST /admin/dlq/{queue}/redrive?limit=` (đưa lại vào queue chính, có giới hạn và log kiểm toán), `DELETE` có xác nhận.
5. Metric: số message trong từng DLQ, tổng retry, thời gian từ lần đầu đến thành công; cảnh báo khi DLQ > 0.
6. Test (TTL rút ngắn cho test): (a) handler lỗi tạm thời 2 lần rồi thành công ⇒ xử lý đúng 1 lần cuối cùng; (b) lỗi tạm thời liên tục ⇒ qua 3 lần rồi vào DLQ; (c) payload JSON sai ⇒ vào DLQ ngay, không retry; (d) re-drive thành công sau khi sửa lỗi.
7. `docs/mq-retry-design.md`: sơ đồ, vì sao dùng hàng đợi chờ thay vì `Thread.sleep`/ retry nóng; rủi ro (message "đi lạc", thứ tự).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] 4 test (a–d) xanh, ổn định (chạy 3 lần).
- [ ] Log chứng minh khoảng chờ giữa các lần retry tương ứng cấu hình (dán).
- [ ] Message độc không retry (đếm = 1 lần xử lý).
- [ ] Endpoint DLQ chỉ ADMIN; re-drive có giới hạn và ghi audit.
- [ ] Metric & cảnh báo DLQ hoạt động (dán).
- [ ] `docs/mq-retry-design.md` đầy đủ.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao không `Thread.sleep` trong listener để chờ retry? → Chặn thread consumer, giảm thông lượng, mất khi crash.
2. Thế nào là lỗi vĩnh viễn? Ví dụ? → Retry bao nhiêu cũng không thành: JSON sai, tham chiếu tới thực thể không tồn tại.
3. Re-drive cần lưu ý gì? → Sửa nguyên nhân trước; giới hạn tốc độ; consumer idempotent.

---

### Bài 6.6 – Consumer idempotent
**Thời lượng:** 1 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Vì sao có trùng: giao lại sau lỗi, retry, re-drive, publisher gửi lại.
- Hai hướng: tác dụng phụ **tự nhiên idempotent** (đặt trạng thái = X) hoặc **bảng ghi nhận đã xử lý** `processed_message(consumer, event_id)` với ràng buộc unique, ghi **cùng transaction** với tác dụng phụ trong DB.
- Tác dụng phụ ngoài DB (gửi email) không rollback được ⇒ dùng `notification_log` với unique key và quy trình "đặt chỗ trước (claim) → thực hiện → xác nhận".
- Không đảm bảo thứ tự: consumer chịu được sự kiện đến lệch thứ tự.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Bảng `processed_message(consumer_name, event_id, processed_at, PRIMARY KEY (consumer_name, event_id))`; lớp trợ giúp `IdempotentHandler.execute(consumer, eventId, Runnable)` trong `@Transactional`: nếu `INSERT` vi phạm unique ⇒ bỏ qua êm.
2. Áp dụng cho `AuditLogConsumer` và `AnalyticsHiredConsumer` (tăng bộ đếm `hired_count` theo tỉnh).
3. Test đồng thời: publish **cùng một** `eventId` 20 lần song song với 4 consumer ⇒ tác dụng chỉ xảy ra **1 lần** (đếm đúng).
4. Test lệch thứ tự: `ApplicationStatusChanged(HIRED)` đến trước `ApplicationSubmitted` ⇒ hệ thống không hỏng (quy ước xử lý: chờ/ bỏ qua có kiểm soát/ ghi nhận) — chọn và giải thích.
5. Dọn dẹp bản ghi cũ trong `processed_message` (job định kỳ, giữ N ngày) và lý do chọn N.
6. `docs/mq-idempotency.md`: phân loại 6 consumer của WeldJobs theo cách đạt idempotency.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Test 20 sự kiện trùng song song ⇒ hiệu ứng đúng 1 lần (chạy 5 lần liên tiếp đều đúng).
- [ ] Test lệch thứ tự xanh với quy ước đã nêu.
- [ ] Chứng minh `INSERT` vào `processed_message` và hiệu ứng nằm trong **cùng transaction** (ném lỗi sau hiệu ứng ⇒ cả hai rollback).
- [ ] Job dọn dẹp có test.
- [ ] `docs/mq-idempotency.md` đủ 6 consumer.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. At-least-once nghĩa là gì và hệ quả? → Mỗi message được giao ≥ 1 lần ⇒ có thể trùng ⇒ consumer phải idempotent.
2. Vì sao ghi `processed_message` cùng transaction? → Tránh trạng thái "đã ghi nhận nhưng chưa làm" hoặc ngược lại.
3. Làm sao idempotent với việc gửi email? → Khóa unique trên (event, người nhận) và quy trình claim; chấp nhận hiếm hoi gửi trùng nếu crash giữa chừng hoặc dùng nhà cung cấp hỗ trợ idempotency key.

---

### Bài 6.7 – Outbox pattern
**Thời lượng:** 1,5–2 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- **Dual-write problem:** commit DB và publish broker không nguyên tử ⇒ có thể mất hoặc sai sự kiện.
- Outbox: ghi sự kiện vào bảng `outbox_event` **cùng transaction** với dữ liệu nghiệp vụ; một **relay** đọc bảng và publish, đánh dấu đã gửi.
- Relay đa instance: `SELECT ... FOR UPDATE SKIP LOCKED`; gửi theo lô; confirm trước khi đánh dấu; thứ tự theo aggregate (nhận biết).
- Dọn dẹp bảng; giám sát độ trễ outbox.
- Thay thế nâng cao: CDC (Debezium) — nhận biết, không bắt buộc cài đặt.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Migration `outbox_event(id uuid pk, aggregate_type, aggregate_id, event_type, payload jsonb, created_at, published_at null, attempts int, last_error)` + index phần cho hàng chưa gửi (`WHERE published_at IS NULL`).
2. Thay cách publish `AFTER_COMMIT` (6.2) bằng ghi outbox trong cùng transaction với `JobApplicationService`/`JobService`.
3. `OutboxRelay` (scheduler, mỗi 1–2 giây hoặc kích hoạt khi có ghi mới): lấy lô 100 hàng bằng `SKIP LOCKED`, publish **kèm publisher confirm**, thành công ⇒ set `published_at`; lỗi ⇒ tăng `attempts`, ghi `last_error`, backoff.
4. Job dọn dẹp xóa hàng đã gửi quá N ngày.
5. Kịch bản thất bại có chủ đích (viết thành test/ kịch bản chạy tay có bằng chứng):
   - (a) Tắt RabbitMQ, thực hiện 20 ứng tuyển ⇒ API vẫn thành công, outbox tích lũy; bật lại ⇒ cả 20 sự kiện được gửi **đủ, không mất**.
   - (b) `kill -9` app giữa lúc relay đang gửi ⇒ sau khởi động lại không mất sự kiện (có thể trùng ⇒ consumer idempotent).
   - (c) Hai instance relay chạy cùng lúc ⇒ không xử lý trùng quá mức (chứng minh `SKIP LOCKED`).
6. Metric: số hàng outbox chưa gửi, tuổi hàng cũ nhất; cảnh báo khi > ngưỡng.
7. `docs/mq-outbox.md`: sơ đồ trình tự, so sánh với `AFTER_COMMIT`, đánh đổi (độ trễ, độ phức tạp), khi nào đủ dùng.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Kịch bản (a): 20/20 sự kiện đến đích sau khi bật lại broker (dán số đếm 2 phía).
- [ ] Kịch bản (b): không mất sự kiện (bằng chứng).
- [ ] Kịch bản (c): hai relay không gửi trùng hàng cùng lúc (log).
- [ ] Mọi sự kiện domain đi qua outbox (grep không còn publish trực tiếp trong service).
- [ ] Metric & cảnh báo hoạt động.
- [ ] `docs/mq-outbox.md` đầy đủ.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Outbox đảm bảo điều gì và **không** đảm bảo điều gì? → Đảm bảo không mất sự kiện (at-least-once); không đảm bảo không trùng ⇒ cần idempotent consumer.
2. Vì sao cần `SKIP LOCKED`? → Nhiều relay lấy lô khác nhau không chặn nhau.
3. Vì sao đánh dấu `published_at` sau khi confirm? → Tránh đánh dấu đã gửi khi broker chưa nhận.

---

### Bài 6.8 – Áp dụng: thông báo email và pipeline import
**Thời lượng:** 2 ngày

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Gửi email bằng Spring Mail tới **Mailpit** (dev); template HTML + text bằng Thymeleaf, **tiếng Việt**, có liên kết xem tin/hồ sơ; không chứa thông tin nhạy cảm không cần thiết.
2. Các thông báo:
   - `ApplicationSubmitted` ⇒ email cho employer chủ tin (tóm tắt thợ: kỹ năng, kinh nghiệm; **không** kèm SĐT).
   - `ApplicationStatusChanged` ⇒ email cho thợ (SHORTLISTED/REJECTED/HIRED; lý do từ chối nếu employer nhập).
   - `JobCreated` ⇒ tìm thợ có điểm phù hợp ≥ ngưỡng, `availability != NOT_LOOKING`, đã bật nhận thông báo ⇒ gom thành **một email tóm tắt** mỗi thợ mỗi ngày (digest; dùng Redis/ DB để gom) thay vì gửi từng cái.
3. Tùy chọn của người dùng: `emailNotifications` (bật/tắt từng loại) lưu ở hồ sơ; có liên kết hủy đăng ký an toàn (token ký, hết hạn).
4. Idempotent: `notification_log(event_id, recipient, kind)` unique; chống spam: tối đa N email/ ngày/ người nhận (Redis).
5. Retry/ DLQ theo 6.5 khi SMTP lỗi tạm thời; địa chỉ email sai ⇒ lỗi vĩnh viễn ⇒ DLQ/ đánh dấu bounce.
6. **Chuyển pipeline import (GĐ3) sang hàng đợi:** `import.requested` (mỗi nguồn một message) → `import.fetch` (concurrency 1 mỗi nguồn) → `import.save` (lô); trạng thái `import_run` cập nhật theo tiến độ; nguồn lỗi không chặn nguồn khác.
7. Test tích hợp end-to-end bằng Testcontainers (RabbitMQ + Postgres + Mailpit/GreenMail): ứng tuyển ⇒ email tới đúng người, đúng nội dung, 1 lần.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Mailpit hiển thị email đúng nội dung/ ngôn ngữ cho 3 loại thông báo (ảnh chụp).
- [ ] Gửi trùng sự kiện ⇒ chỉ 1 email (test).
- [ ] Thợ tắt thông báo ⇒ không nhận (test); liên kết hủy đăng ký hoạt động và token hết hạn bị từ chối.
- [ ] Digest gom đúng (nhiều tin trong ngày ⇒ 1 email).
- [ ] SMTP lỗi tạm thời ⇒ retry theo lịch rồi thành công; địa chỉ sai ⇒ vào DLQ ngay.
- [ ] Pipeline import chạy qua hàng đợi, lỗi một nguồn không ảnh hưởng nguồn khác (test).
- [ ] Không có PII không cần thiết trong nội dung/ log.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao gom digest thay vì gửi từng email? → Giảm spam/ chi phí, tăng trải nghiệm.
2. Vì sao hủy đăng ký cần token ký? → Tránh người khác hủy hộ/ đoán link.
3. Vì sao `import.fetch` đặt concurrency 1 mỗi nguồn? → Lịch sự với nguồn, tránh bị chặn.

---

### Bài 6.9 – Kiểm thử hệ thống bất đồng bộ
**Thời lượng:** 1 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Testcontainers RabbitMQ (`@ServiceConnection`), Awaitility (chờ theo điều kiện thay vì `sleep`), dọn queue giữa các test (`purge`), kiểm soát listener (`RabbitListenerEndpointRegistry`) để test xác định.
- Test hợp đồng sự kiện: so schema JSON; kiểm tra tương thích ngược (consumer cũ đọc được sự kiện mới).
- Test tính chất (property): bất kể thứ tự/ trùng, kết quả cuối cùng như nhau (nhận biết).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Bộ test: định tuyến (6.3), retry/DLQ (6.5), idempotency (6.6), outbox (6.7), email (6.8) — tổng ≥ 30 test tích hợp MQ.
2. Awaitility cho mọi chờ đợi; **cấm** `Thread.sleep` trong test (grep).
3. Test hợp đồng: lưu mẫu JSON của từng sự kiện (`src/test/resources/contracts/`); test thêm field mới (optional) không làm consumer hiện tại vỡ.
4. Kiểm tra thời gian chạy bộ test MQ ≤ mức bạn đặt (ghi lại) và giải thích cách giữ nhanh (container dùng chung, TTL rút ngắn).
5. Chạy trong CI (GĐ5) và ổn định 5 lần liên tiếp.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] ≥ 30 test MQ, xanh 5 lần liên tiếp trong CI.
- [ ] Không còn `Thread.sleep` trong test (grep).
- [ ] Test hợp đồng tương thích ngược xanh.
- [ ] `docs/mq-testing.md` mô tả chiến lược.

---

### Bài 6.10 – Capstone Giai đoạn 6 (bài thi tùy chọn)
**Bài tập – Phần A**
1. Kịch bản demo "hỗn loạn có kiểm soát" (Agent giám sát): dưới tải 200 ứng tuyển/ phút giả lập, lần lượt (i) tắt RabbitMQ 2 phút, (ii) kill consumer email, (iii) gửi 5 message độc, (iv) SMTP trả lỗi tạm 30% — kết quả cuối cùng: **không mất sự kiện, không email trùng, DLQ chỉ chứa 5 message độc**, API không có lỗi 5xx do MQ.
2. `docs/event-catalog.md` hoàn chỉnh: mọi sự kiện, schema, producer, consumer, idempotency, retry policy.
3. `docs/stage6-report.md`: 6 câu phản tư (khi nào KHÔNG nên dùng MQ, độ phức tạp thêm vào, vận hành, quan sát, thứ tự sự kiện, so sánh với Kafka cho trường hợp thu thập tin giá dầu quy mô lớn).

**(Tùy chọn) Bài thi** — Agent chọn 3:
1. Thêm sự kiện `JobExpired` (từ job hết hạn) và consumer gửi thông báo cho employer + đóng hồ sơ đang `SUBMITTED` có kiểm soát.
2. Thêm cơ chế **ưu tiên** (priority queue hoặc hai queue) cho thông báo quan trọng (HIRED) so với digest.
3. Thêm **delayed message** (nhắc employer xử lý hồ sơ sau 3 ngày chưa xem) bằng hàng đợi trì hoãn.
4. Giới hạn tốc độ tiêu thụ (throttle) cho consumer gọi dịch vụ ngoài và chứng minh bằng test.
5. Dashboard đơn giản (trang admin hoặc Grafana) hiển thị độ sâu các queue, DLQ, độ trễ outbox.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Kịch bản hỗn loạn đạt cả 4 tiêu chí (bằng chứng số liệu).
- [ ] (Tùy chọn) Bài thi: hoàn thành 3 yêu cầu, có test, tài liệu sự kiện cập nhật.
- [ ] Trả lời đúng ≥ 70% bộ 12 câu tổng hợp 6.1–6.9.

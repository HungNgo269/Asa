## GIAI ĐOẠN 0 – CHUẨN BỊ MÔI TRƯỜNG

> **Ghi chú v2 cho file này:** (1) Dạy **từng bước** theo `00-rules.md` — mỗi mục đánh số trong *Bài tập* là một bước, Agent chỉ đưa một bước mỗi lần. (2) Vị trí class theo bảng ở `01-architecture.md` §5; `ArchitectureTest` phải xanh sau mỗi bài. (3) "Bài thi" là **tùy chọn**; báo cáo giai đoạn theo `00-rules.md` mục 9. (4) Mục *Nghiệm thu* gắn *(mục phụ)* hoặc là ghi chú `docs/*.md` có thể ghi nợ `[~]` theo `00-rules.md` mục 4.2.

### Bài 0.1 – Cài đặt và kiểm tra môi trường
**Thời lượng:** 0,5–1 ngày

**Mục tiêu:** Có môi trường phát triển hoàn chỉnh, tái lập được, dùng suốt khóa học.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- JDK vs JRE vs JVM; LTS là gì; vì sao chọn Java 21.
- `JAVA_HOME`, `PATH`; Maven wrapper (`mvnw`) và vì sao nên dùng.
- Cấu trúc thư mục Maven chuẩn: `src/main/java`, `src/main/resources`, `src/test/java`.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Cài **JDK 21** (ví dụ bản Temurin), **IntelliJ IDEA** (hoặc VS Code + Extension Pack for Java), **Git**, **Docker** (Desktop hoặc Engine) và công cụ gọi API (Bruno/Postman/curl).
2. Tạo repo Git `weldjobs` với `README.md`, `.gitignore` cho Java/Maven/IDE, và một thư mục `docs/`.
3. Chạy PostgreSQL bằng Docker (một container tạm) và kết nối thành công bằng `psql` hoặc DBeaver.
4. Tạo file `docs/environment.md` ghi lại phiên bản từng công cụ và lệnh đã dùng.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `java -version` hiển thị bản 21.x (dán output).
- [ ] `javac -version` cùng bản 21.x.
- [ ] `git --version`, `docker --version`, `docker compose version` có output.
- [ ] `docker run --rm hello-world` chạy thành công.
- [ ] Container Postgres chạy được và truy vấn `SELECT version();` trả kết quả (dán output).
- [ ] Repo có commit đầu tiên, `.gitignore` đã loại `target/`, `.idea/`, `*.iml`.
- [ ] File `docs/environment.md` tồn tại và đầy đủ.

**Câu hỏi kiểm tra (ý chính đáp án)** *(hỏi từng câu một)*
1. JDK, JRE, JVM khác nhau thế nào? → JVM chạy bytecode; JRE = JVM + thư viện chạy; JDK = JRE + công cụ biên dịch/đóng gói (javac, jar...).
2. LTS nghĩa là gì và vì sao dự án nên dùng LTS? → Bản hỗ trợ dài hạn, ổn định, được framework hỗ trợ lâu.
3. Vì sao dùng `mvnw` thay vì Maven cài sẵn? → Cố định phiên bản Maven theo dự án, mọi máy/CI build giống nhau.

**Lỗi hay gặp:** `JAVA_HOME` trỏ sai phiên bản; Docker chưa bật; cổng 5432 đã bị chiếm.

---

### Bài 0.2 – Dựng khung kiến trúc 4 tầng và luật kiến trúc (ArchUnit)
**Thời lượng:** 1 ngày

**Mục tiêu:** Có sẵn khung project đúng chuẩn **trước khi viết dòng nghiệp vụ nào**; luật phụ thuộc giữa các tầng được máy kiểm tra tự động.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Dependency Rule; 4 tầng `presentation` / `application` / `domain` / `infrastructure` (đọc `01-architecture.md` §1–§3).
- Port & Adapter; composition root.
- Maven project layout; ArchUnit: `layeredArchitecture()`, `noClasses().that().resideInAPackage(..).should().dependOnClassesThat()..`.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Đọc `01-architecture.md` §1–§3 rồi tóm tắt bằng lời của bạn trong `docs/architecture-notes.md` (≤ 15 dòng): 4 tầng, hướng mũi tên phụ thuộc, vì sao `domain` không được biết Spring/DB.
2. Tạo Maven project `weldjobs-console` (Java 21, `groupId=com.weldjobs`, có `./mvnw`) trong repo `weldjobs`.
3. Tạo cây package **rỗng** đúng mục 3.2 của `01-architecture.md` (phần dùng cho console: `config`, `shared`, `domain.{model,service,exception}`, `application.port`, `infrastructure.persistence.{inmemory,json}`, `presentation.console`). Mỗi package có `package-info.java` 1–2 dòng mô tả trách nhiệm.
4. Thêm JUnit 5, AssertJ và **ArchUnit** vào `pom.xml`.
5. Viết `ArchitectureTest` với các luật **R1, R3, R4, R5** (mục 8 của `01-architecture.md`) và R2 phần Jackson.
6. Cố ý phá **từng luật** bằng một class tạm (ví dụ trong `domain` import một class của `infrastructure`), xem test đỏ, ghi lại thông báo lỗi, rồi xóa class tạm.
7. Thêm `com.weldjobs.Main` in ra `WeldJobs` và chạy `./mvnw verify` xanh.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Cây package đúng mục 3.2; mỗi package có `package-info.java`.
- [ ] `ArchitectureTest` có ≥ 5 luật (R1, R3, R4, R5, R2-Jackson) và chạy xanh trong `./mvnw verify`.
- [ ] Chứng minh được ≥ 3 luật bằng cách phá thử (dán thông báo đỏ của từng luật).
- [ ] `./mvnw verify` → `BUILD SUCCESS` (dán output cuối).
- [ ] `docs/architecture-notes.md` ≤ 15 dòng, bằng lời của bạn *(mục phụ)*.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Dependency Rule là gì? → Phụ thuộc chỉ trỏ vào trong; tầng trong không biết tầng ngoài tồn tại.
2. Vì sao `domain` không được import Spring/JPA/Jackson? → Để nghiệp vụ thuần, test nhanh không cần framework, đổi hạ tầng không sửa nghiệp vụ.
3. Port và adapter khác nhau thế nào? Ví dụ trong WeldJobs? → Port là interface do tầng trong định nghĩa (`JobRepository`); adapter là cài đặt ở tầng ngoài (`JpaJobRepository`).
4. Composition root là gì và vì sao chỉ nên có một nơi? → Nơi duy nhất `new`/lắp các dependency; giữ các tầng khác không biết cách lắp.

**Lỗi hay gặp:** để `config` hoặc `Main` bị tính vào luật rồi tự vi phạm (cần loại trừ đúng); viết luật quá rộng khiến test luôn xanh (hãy phá thử để chắc luật có tác dụng); đặt `Repository` interface ở `infrastructure`.

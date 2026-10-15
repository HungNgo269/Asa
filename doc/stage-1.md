## GIAI ĐOẠN 1 – JAVA CORE (qua console app "WeldJobs Console")

> **Ghi chú v2 cho file này:** (1) Dạy **từng bước** theo `00-rules.md` — mỗi mục đánh số trong *Bài tập* là một bước, Agent chỉ đưa một bước mỗi lần. (2) Vị trí class theo bảng ở `01-architecture.md` §5; `ArchitectureTest` phải xanh sau mỗi bài. (3) "Bài thi" là **tùy chọn**; báo cáo giai đoạn theo `00-rules.md` mục 9. (4) Mục *Nghiệm thu* gắn *(mục phụ)* hoặc là ghi chú `docs/*.md` có thể ghi nợ `[~]` theo `00-rules.md` mục 4.2.

> **Quy ước giai đoạn 1:** Một Maven project duy nhất `weldjobs-console` (dựng ở bài 0.2) theo **kiến trúc 4 tầng** (`01-architecture.md`). Chưa dùng Spring. Chỉ dùng thêm: JUnit 5, AssertJ, ArchUnit (từ 0.2), Mockito (từ bài 1.11), Jackson (từ bài 1.9). Mỗi bài thêm code vào cùng project, **không tạo project mới**; sau mỗi bài `./mvnw verify` (gồm `ArchitectureTest`) phải xanh. `Main` + `config` là *composition root*: tự lắp (`new`) các dependency — đúng việc mà Spring DI sẽ làm hộ ở Giai đoạn 2.

### Bài 1.1 – Maven project và Java 21 refresher
**Thời lượng:** 0,5 ngày

**Mục tiêu:** Dựng khung project đúng chuẩn; ôn cú pháp Java hiện đại; viết test đầu tiên.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Cấu trúc `pom.xml`: `groupId`, `artifactId`, `version`, `properties` (`maven.compiler.release`), `dependencies`, `build/plugins`.
- Vòng đời Maven: `clean`, `compile`, `test`, `package`.
- Kiểu dữ liệu nguyên thủy vs wrapper; `String` bất biến; `StringBuilder`; `text block`; `switch expression`; `var`.
- `java.time`: `LocalDate`, `LocalDateTime`, `Duration`, `Period`.
- Test tối thiểu với JUnit 5: `@Test`, `assertEquals` (bài 1.11 sẽ học sâu hơn).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Dùng project `weldjobs-console` đã dựng ở bài 0.2 (Java 21, `groupId=com.weldjobs`). Thêm JUnit 5 và `maven-surefire-plugin` phiên bản phù hợp nếu chưa có.
2. Mở rộng `com.weldjobs.Main` (chỉ khởi động) để gọi `com.weldjobs.presentation.console.MainMenu`: in menu chính bằng **text block** (các mục: 1. Thợ hàn, 2. Tin tuyển dụng, 3. Thoát) và đọc lựa chọn bằng `Scanner`; dùng **switch expression** để xử lý lựa chọn.
3. Tạo class `com.weldjobs.shared.Text` với:
   - `static String normalize(String input)`: trim, lowercase, **bỏ dấu tiếng Việt** (dùng `java.text.Normalizer`, nhớ xử lý chữ `đ/Đ`), gộp nhiều khoảng trắng thành một. Ví dụ `"  Hàn   NHÔM  Đồng "` → `"han nhom dong"`.
   - `static boolean containsAnyKeyword(String text, List<String> keywords)` so khớp **sau khi normalize cả hai phía**.
4. Tạo class `com.weldjobs.domain.model.common.Regions` với `static String regionOf(String province)` dùng switch expression, trả về `"BAC"`, `"TRUNG"`, `"NAM"` cho ít nhất 12 tỉnh/thành (bao gồm Hà Nội, Hải Phòng, Quảng Ninh, Đà Nẵng, Quảng Nam, Thanh Hóa, TP.HCM, Bình Dương, Đồng Nai, Bà Rịa-Vũng Tàu, Cần Thơ, Long An) và ném `IllegalArgumentException` với thông báo rõ nếu không biết. Tên tỉnh phải so khớp **không phân biệt dấu/hoa thường** (dùng `Text.normalize`).
5. Viết ≥ 8 test cho `Text` và `Regions`.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `./mvnw clean package` (hoặc `mvn`) thành công, dán output cuối cùng có `BUILD SUCCESS`.
- [ ] `pom.xml` đặt `maven.compiler.release` = 21.
- [ ] `Text.normalize("  Hàn   NHÔM  Đồng ")` trả đúng `"han nhom dong"` (có test).
- [ ] Test có ít nhất các trường hợp: chuỗi rỗng, chỉ khoảng trắng, `null` (quy định rõ hành vi: ném `NullPointerException` hay trả chuỗi rỗng, và test đúng như quy định), chữ `đ`, chữ in hoa có dấu.
- [ ] `Regions.regionOf("ĐÀ NẴNG")` và `regionOf("da nang")` cùng trả `"TRUNG"`.
- [ ] Menu dùng text block và switch expression (không dùng if-else lồng nhau).
- [ ] Có ≥ 8 test, tất cả xanh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao `String` là bất biến và hệ quả khi nối chuỗi trong vòng lặp? → Mỗi lần nối tạo object mới; dùng `StringBuilder`.
2. `==` và `equals` trên `String` khác gì? → `==` so tham chiếu, `equals` so nội dung; string pool.
3. Switch expression khác switch statement ở điểm nào? → Trả về giá trị, không fall-through, kiểm tra đầy đủ với enum/sealed.
4. Vì sao nên truyền `LocalDate` vào method thay vì gọi `LocalDate.now()` bên trong? → Để test được, kiểm soát thời gian.

**Lỗi hay gặp:** quên xử lý `đ`; `Normalizer.Form.NFD` rồi quên xóa dấu kết hợp bằng regex `\p{M}`; dùng `==` để so sánh chuỗi.

---

### Bài 1.2 – OOP: lớp, đóng gói, enum, `equals/hashCode/toString`
**Thời lượng:** 1 ngày

**Mục tiêu:** Mô hình hóa **kỹ năng hàn** đúng cách và xây các lớp miền bất biến, đúng hợp đồng `equals/hashCode`.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Class, constructor, `this`, `static`, `final`, access modifier.
- Encapsulation: field `private final`, không setter vô nghĩa, **validate trong constructor**, immutability.
- `enum` có field, constructor và method; `values()`, `valueOf`, `ordinal` (và vì sao **không** nên dựa vào `ordinal`).
- Hợp đồng `equals/hashCode` (đối xứng, bắc cầu, nhất quán; nếu `a.equals(b)` thì `hashCode` bằng nhau); `toString` hữu ích để debug.
- Defensive copy.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*

*Phần A – Phân tích (làm trước, nộp bằng văn bản trong `docs/design-skills.md`)*
Yêu cầu của chủ dự án: "thợ hàn chọn nhiều kỹ năng: hàn sắt, hàn nhôm, TIG, MIG…". Hãy:
1. Chỉ ra vì sao danh sách này **trộn nhiều chiều khác nhau** (phương pháp hàn vs vật liệu). Đề xuất cách tách chiều (tham khảo `02-project.md` (C.3)).
2. So sánh 2 cách mô hình: (a) một `enum Skill` chứa tất cả giá trị; (b) class `Skill` + `enum SkillCategory` + danh mục (catalog) dữ liệu. Nêu ưu/nhược cho từng cách (khi admin muốn thêm kỹ năng mới thì sao?). Chọn (b) cho dự án và giải thích.

*Phần B – Cài đặt*
Đặt class vào `com.weldjobs.domain.model.<aggregate>` theo bảng ở `01-architecture.md` §5 (ví dụ `Skill`, `SkillCategory`, `SkillCatalog` → `domain.model.skill`; `Worker*`, `Certificate` → `domain.model.worker`):
1. Enum `SkillCategory`: `PROCESS`, `MATERIAL`, `POSITION`, `PRODUCT`, `OTHER` — mỗi giá trị có `displayName` tiếng Việt.
2. Enum `SkillLevel`: `BASIC(1)`, `INTERMEDIATE(2)`, `ADVANCED(3)` với field `weight` và method `isAtLeast(SkillLevel other)` (so sánh theo `weight`, **không** theo `ordinal`).
3. Enum `ApplicationStatus`: `SUBMITTED, VIEWED, SHORTLISTED, REJECTED, HIRED, WITHDRAWN`, với method `Set<ApplicationStatus> allowedNext()` và `boolean canTransitionTo(ApplicationStatus next)`. Luồng hợp lệ:
   - `SUBMITTED` → `VIEWED`, `REJECTED`, `WITHDRAWN`
   - `VIEWED` → `SHORTLISTED`, `REJECTED`, `WITHDRAWN`
   - `SHORTLISTED` → `HIRED`, `REJECTED`, `WITHDRAWN`
   - `REJECTED`, `HIRED`, `WITHDRAWN` là trạng thái cuối (không chuyển tiếp)
4. Các enum còn lại: `Availability`, `JobType`, `JobStatus`, `CertificateType` (giá trị xem `02-project.md` (C.3)).
5. Class `Skill` (**bất biến**): `code` (chữ in hoa + gạch dưới, ví dụ `PROC_TIG`), `name`, `SkillCategory category`. Constructor validate: `code` khớp regex `^[A-Z0-9_]{3,40}$`, `name` không rỗng, `category` không null. `equals/hashCode` **chỉ dựa trên `code`**. `toString` dạng `Skill[PROC_TIG, TIG (GTAW), PROCESS]`.
6. Class `WorkerSkill` (bất biến): `Skill skill`, `int yearsOfExperience` (0–50), `SkillLevel level`. Validate trong constructor.
7. Class `Certificate`: `CertificateType type`, `String code`, `LocalDate issuedDate`, `LocalDate expiryDate` (có thể null = không hết hạn). Method `boolean isValidOn(LocalDate date)` (quy ước rõ: ngày hết hạn **còn hiệu lực trong ngày đó** hay không, ghi trong Javadoc và test cả ngày biên). Validate `expiryDate` không trước `issuedDate`.
8. Class `SkillCatalog` (chỉ cần dữ liệu tĩnh ở bài này): tạo sẵn ≥ 25 kỹ năng thuộc cả 5 nhóm, **bao gồm tối thiểu**: SMAW, MIG/MAG, TIG, FCAW, SAW; thép carbon, inox, nhôm; 1G, 2G, 3G, 4G, 5G, 6G; hàn kết cấu thép, hàn ống, hàn khung nhôm; đọc bản vẽ hàn, cắt plasma. Có `Skill byCode(String)` ném exception khi không tìm thấy.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] File `docs/design-skills.md` trả lời đủ Phần A, có chỉ ra đúng chiều `PROCESS` và `MATERIAL` bị trộn.
- [ ] Mọi field của `Skill`, `WorkerSkill`, `Certificate` là `private final`; không có setter.
- [ ] `new Skill("tig", "TIG", PROCESS)` ném exception (code sai định dạng) — có test.
- [ ] Test chứng minh `equals/hashCode` của `Skill`: phản xạ, đối xứng, hai object cùng `code` khác `name` thì **bằng nhau**, và `hashCode` bằng nhau.
- [ ] `SkillLevel.ADVANCED.isAtLeast(INTERMEDIATE)` là `true`, ngược lại `false` (test); không dùng `ordinal()` trong code.
- [ ] `ApplicationStatus`: test **toàn bộ** ma trận chuyển trạng thái (6×6 = 36 cặp) bằng test tham số hóa hoặc vòng lặp có assert; trạng thái cuối không chuyển được sang đâu.
- [ ] `Certificate.isValidOn` có test: trước ngày cấp, đúng ngày cấp, giữa kỳ, đúng ngày hết hạn, sau ngày hết hạn, `expiryDate = null`.
- [ ] `SkillCatalog` có ≥ 25 kỹ năng, không trùng `code` (có test kiểm tra).
- [ ] `toString` của `Skill` đúng định dạng yêu cầu.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao chỉ dùng `code` trong `equals/hashCode` của `Skill`? → `code` là định danh nghiệp vụ; name có thể đổi; tránh hai object cùng định danh bị coi khác nhau.
2. Điều gì xảy ra nếu override `equals` mà không override `hashCode` rồi bỏ vào `HashSet`? → Phá vỡ hợp đồng, trùng lặp/không tìm thấy.
3. Vì sao không nên dùng `ordinal()` để so sánh mức độ? → Dễ vỡ khi đổi thứ tự khai báo; dùng field `weight` tường minh.
4. Immutability giúp gì khi nhiều thread dùng chung object? → Không cần đồng bộ, không có race condition khi đọc.
5. Vì sao chọn `class Skill + catalog` thay vì `enum Skill`? → Dữ liệu thay đổi được (admin thêm), mở rộng ngành khác mà không sửa code/biên dịch lại.

**Lỗi hay gặp:** để lộ collection nội bộ; `equals` dùng `getClass()` sai cách hoặc quên kiểm tra `null`; dùng `LocalDate.now()` trong `isValidOn`.

---

### Bài 1.3 – Kế thừa, abstract class, interface, polymorphism, composition
**Thời lượng:** 1 ngày

**Mục tiêu:** Biết khi nào dùng kế thừa, interface hay composition; thiết kế bộ chấm điểm mở rộng được (Open/Closed) và một "cổng" thông báo đặt đúng ranh giới tầng.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `extends`, `super`, override (`@Override`), `abstract class` vs `interface`, `default` method, `static` method trong interface.
- Polymorphism lúc chạy (dynamic dispatch); upcasting; vì sao tránh chuỗi `instanceof`.
- **Template Method** (khung thuật toán ở lớp cha, bước thay đổi ở lớp con) và **Strategy**.
- **Composition over inheritance**; Open/Closed Principle; Dependency Inversion (interface thuộc tầng trong, cài đặt ở tầng ngoài).
- `protected`; `final class` / `final method`.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Đặt trong `domain.model.user`: `enum Role { WORKER, EMPLOYER, ADMIN }` và class `User` **final**, bất biến: `UUID id`, `String email` (validate định dạng đơn giản), `Role role`, `Instant createdAt`. **Không** tạo `Worker extends User`. Viết `docs/design-roles.md` (≤ 10 dòng) nêu ≥ 3 lý do dùng composition thay vì kế thừa (gợi ý: đổi/thêm vai trò, vòng đời khác nhau, ánh xạ bảng DB).
2. Aggregate hồ sơ, mỗi cái giữ `UUID userId`: `Worker` (`domain.model.worker`: fullName, phone, province, yearsOfExperience, `Availability`, willingToRelocate, willingToWorkAbroad, `Map<Skill, WorkerSkill>`, `List<Certificate>`; method `displayName()` trả tên rút gọn kiểu `"Nguyễn V.A"` để dùng khi cần ẩn danh) và `Employer` (`domain.model.employer`: companyName, province, contactName).
3. `JobPost` và `JobRequiredSkill` (`domain.model.job`): id, `UUID employerId`, title, description, province, `JobType`, `JobStatus`, minYearsExperience, headcount, `postedAt`, `expiresAt`, danh sách `JobRequiredSkill`. Tạm thời `JobRequiredSkill` là class có `Skill skill`, `boolean mandatory`, `SkillLevel minLevel`.
4. **Strategy + Template Method** trong `domain.service`:
   - `interface JobScorer { ScoreResult score(Worker worker, JobPost job); }` và `ScoreResult` (tạm là class gồm `int points`, `String explanation`).
   - `abstract class BaseJobScorer implements JobScorer`: `score(...)` là **`final`**, gọi `compute(...)` (abstract) rồi **kẹp** điểm vào `[0, maxPoints()]`; `maxPoints()` là abstract.
   - `SkillMatchScorer` (tối đa 60): thiếu **bất kỳ** kỹ năng `mandatory` (hoặc level thấp hơn `minLevel`) → 0 điểm, explanation nêu rõ thiếu gì; đủ → 40 + tối đa 20 theo tỷ lệ kỹ năng "ưu tiên" thợ có.
   - `ExperienceScorer` (tối đa 20): đủ `minYearsExperience` → 20; ít hơn thì tỷ lệ thuận (làm tròn xuống), không âm.
   - `LocationScorer` (tối đa 10): cùng tỉnh 10; khác tỉnh nhưng `willingToRelocate` 5; còn lại 0; job `OVERSEAS` và `willingToWorkAbroad` thì 10.
5. `WeightedScorer implements JobScorer` (**composition**): nhận `List<JobScorer>`, cộng điểm, **gộp explanation**; không biết scorer cụ thể nào.
6. **Chứng minh Open/Closed:** thêm `SalaryScorer` (tối đa 10: lương tối thiểu mong muốn của thợ ≤ lương tối đa của job → 10) **mà không sửa** `WeightedScorer` hay các scorer khác.
7. **Port & Adapter:** `interface Notifier { void send(UUID userId, String message); }` đặt ở `application.port`; `ConsoleNotifier` (in ra console, có prefix) ở `infrastructure`; một bản giả `RecordingNotifier` (trong test) ghi lại tin nhắn. Viết một method nhận `Notifier` và gửi thông báo cho danh sách `Worker`/`Employer` — method này **không** được biết lớp cài đặt cụ thể.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `User` là `final`; không có lớp nào `extends User`; `docs/design-roles.md` có ≥ 3 lý do *(mục phụ)*.
- [ ] `BaseJobScorer.score` là `final`; có test dùng một scorer thử (lớp con trong test) cố tình trả điểm ngoài khoảng để chứng minh kết quả luôn nằm trong `[0, maxPoints()]`.
- [ ] `SkillMatchScorer`: test ≥ 6 tình huống (đủ tất cả, thiếu 1 bắt buộc, đủ bắt buộc thiếu ưu tiên, level thấp hơn yêu cầu, job không có kỹ năng yêu cầu, thợ không có kỹ năng nào).
- [ ] `ExperienceScorer`, `LocationScorer`: mỗi cái ≥ 4 test, gồm cả ca biên.
- [ ] `WeightedScorer` tổng tối đa 100 (với 3 scorer đầu), có test; không `import` scorer cụ thể nào (grep xác nhận).
- [ ] `git diff --stat` khi thêm `SalaryScorer` **không** chạm `WeightedScorer.java` (dán kết quả).
- [ ] Không có `if (x instanceof ...)` trong logic nghiệp vụ.
- [ ] `Notifier` ở `application.port`, `ConsoleNotifier` ở `infrastructure`; test dùng `RecordingNotifier`; `ArchitectureTest` vẫn xanh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Khi nào dùng abstract class, khi nào dùng interface? → Abstract: chia sẻ trạng thái/code chung có quan hệ "is-a" chặt; interface: hợp đồng hành vi, cài nhiều interface cùng lúc.
2. Polymorphism lúc chạy hoạt động ra sao? → JVM chọn method theo kiểu thực của object (dynamic dispatch).
3. Vì sao `WeightedScorer` không nên biết scorer cụ thể? → Phụ thuộc abstraction (DIP), thêm scorer mới không sửa code cũ (OCP).
4. Vì sao `Worker extends User` là thiết kế dở trong dự án này? → Vai trò có thể đổi/thêm, vòng đời hồ sơ khác tài khoản, ánh xạ bảng DB rối, kế thừa ràng buộc chặt; dùng composition (`Worker` giữ `userId`).
5. Template Method trong `BaseJobScorer` giải quyết gì? → Bất biến chung (kẹp điểm) nằm đúng một chỗ; lớp con chỉ lo logic riêng; `final` để không bị override phá.
6. Vì sao `Notifier` nằm ở `application.port` còn cài đặt ở `infrastructure`? → Tầng trong sở hữu hợp đồng, tầng ngoài cài đặt (DIP); đổi console → email không sửa nghiệp vụ.

**Lỗi hay gặp:** để `Worker` biết quá nhiều về cách tính điểm; scorer lặp logic khớp kỹ năng; trả điểm âm; để `Notifier` ở `infrastructure` rồi `application` import ngược vào.

---

### Bài 1.4 – `record`, `var`, `Optional`, sealed types, pattern matching
**Thời lượng:** 0,5–1 ngày

**Mục tiêu:** Dùng các tính năng Java hiện đại đúng chỗ, biết giới hạn của chúng.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `record`: field ngầm `final`, compact constructor để validate, method bổ sung, static factory; record **không** thay thế mọi class.
- `var`: chỉ biến cục bộ, khi kiểu đã rõ từ vế phải.
- `Optional`: dùng làm **kiểu trả về**; `map`, `flatMap`, `orElse`, `orElseGet`, `orElseThrow`, `ifPresentOrElse`; **không** dùng làm field/tham số; không `get()` bừa.
- `sealed interface` + `permits`; `switch` pattern matching (Java 21); record pattern.
- Text block trong test/fixture JSON.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `record SalaryRange(long min, long max, String currency)`: compact constructor validate `min ≥ 0`, `max ≥ min`, `currency` thuộc `{VND, USD, KRW, JPY, AUD, EUR}`. Thêm `boolean contains(long amount)`, `boolean overlaps(SalaryRange other)`, `static SalaryRange vnd(long min, long max)`.
2. `record JobSearchCriteria(String province, JobType jobType, Integer maxMinYears, Set<Skill> skills, SkillMatchMode mode, SalaryRange salary)` + `enum SkillMatchMode { ANY, ALL }`. Tất cả tham số có thể `null` nghĩa là "không lọc"; chuẩn hóa `skills` thành bất biến (`Set.copyOf`) trong compact constructor (null → rỗng). Viết `JobSearchCriteria.empty()`.
3. Chuyển `JobRequiredSkill` thành `record`. Giải thích bằng văn bản (3–5 dòng) vì sao `Worker` và `JobPost` **không** nên là record.
4. `sealed interface ApplicationEvent permits Submitted, Viewed, Shortlisted, Rejected, Hired, Withdrawn`; mỗi loại là record chứa thông tin tối thiểu (`Rejected` có `reason`, `Hired` có `startDate`). Viết `String describe(ApplicationEvent e)` bằng **switch pattern matching** (không `default`; để compiler kiểm tra đầy đủ).
5. Trong `UserRepository` giả (tạm là `Map`): `Optional<User> findByEmail(String)`. Viết 3 đoạn dùng `Optional` đúng cách: (a) lấy email hoặc `"Ẩn danh"`; (b) ném `NoSuchElementException` có thông báo; (c) `ifPresentOrElse` in lời chào hoặc gợi ý đăng ký.
6. Viết `docs/optional-rules.md`: liệt kê 5 quy tắc dùng `Optional` bạn rút ra, mỗi quy tắc kèm ví dụ sai/đúng.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `new SalaryRange(10, 5, "VND")` và `currency = "ABC"` đều ném exception (test).
- [ ] `overlaps` có test cho: tách rời, chạm đúng biên, lồng nhau, trùng hoàn toàn, khác `currency` (quy định rõ: khác tiền tệ thì không so sánh được → ném exception hoặc `false`; ghi quy ước).
- [ ] `JobSearchCriteria` không bị thay đổi từ bên ngoài sau khi tạo (test: sửa `Set` gốc rồi kiểm tra criteria không đổi).
- [ ] `describe(ApplicationEvent)` dùng switch pattern, **không có `default`**, và compiler báo lỗi nếu thêm loại event mới mà chưa xử lý (chứng minh bằng cách thêm thử rồi dán thông báo lỗi biên dịch, sau đó gỡ ra).
- [ ] Không có `optional.get()` trong code (grep xác nhận) và không có `Optional` làm field/tham số.
- [ ] `docs/optional-rules.md` có ≥ 5 quy tắc có ví dụ.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Record khác class thường ở điểm nào, và khi nào **không** nên dùng record? → Bất biến, tự sinh equals/hashCode/toString/accessors; không dùng khi cần kế thừa, trạng thái thay đổi, hoặc entity có định danh/vòng đời (ví dụ JPA entity).
2. Vì sao `Optional` không nên làm tham số/field? → Tăng nhiễu, không serialize tốt, vẫn có thể null; chỉ để biểu diễn "có thể không có" ở kiểu trả về.
3. `orElse` khác `orElseGet` thế nào? → `orElse` luôn tính giá trị mặc định; `orElseGet` chỉ tính khi rỗng.
4. Lợi ích của sealed interface + switch pattern? → Kiểm tra đầy đủ lúc biên dịch, không cần `default`.

---

### Bài 1.5 – Collections
**Thời lượng:** 1 ngày

**Mục tiêu:** Chọn đúng cấu trúc dữ liệu; hiểu độ phức tạp; tránh rò rỉ trạng thái nội bộ.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `List` (`ArrayList`, `LinkedList`), `Set` (`HashSet`, `LinkedHashSet`, `TreeSet`), `Map` (`HashMap`, `LinkedHashMap`, `TreeMap`), `EnumSet`, `EnumMap`, `Deque`/`PriorityQueue`.
- Độ phức tạp thao tác; cách `HashMap` hoạt động (hash bucket, resize) ở mức khái niệm.
- `Comparable` vs `Comparator`; `Collections.sort`, `List.sort`.
- Bất biến: `List.of`, `Set.copyOf`, `Collections.unmodifiableList`, **defensive copy**.
- `Iterator`, `ConcurrentModificationException`, `removeIf`.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Mở rộng `SkillCatalog`: `Map<String, Skill>` theo code, `EnumMap<SkillCategory, List<Skill>>` theo nhóm. Method `List<Skill> byCategory(SkillCategory)` trả về **danh sách không sửa được**.
2. Trong `Worker`: `boolean hasAll(Collection<Skill>)`, `boolean hasAny(Collection<Skill>)`, `Set<Skill> missing(Collection<Skill>)`, `Optional<WorkerSkill> skillOf(Skill)`.
3. Class `WorkerIndex`: **chỉ mục ngược** `Map<Skill, Set<Worker>>`. `add(Worker)`, `remove(Worker)`, `Set<Worker> findAll(Set<Skill> required)` (giao các tập — thợ có **tất cả** kỹ năng), `Set<Worker> findAny(Set<Skill> any)` (hợp các tập). Phải xử lý đúng khi `required` rỗng (quy ước và test).
4. Class `TopN<T>` hoặc dùng `PriorityQueue` để lấy **top 5** việc làm phù hợp nhất cho một thợ mà không sắp xếp toàn bộ danh sách (giải thích độ phức tạp so với sort).
5. **Thí nghiệm hiệu năng nhỏ:** so sánh `ArrayList.contains` và `HashSet.contains` với 100.000 phần tử, 1.000 lần tra cứu; in thời gian; ghi kết luận trong `docs/collections-notes.md` (nêu rõ đây là micro-benchmark thô, không đáng tin tuyệt đối).
6. Chứng minh bằng test: (a) sửa `List` trả về từ `byCategory` ném `UnsupportedOperationException`; (b) sửa `Set` truyền vào constructor của `Worker` sau khi tạo **không** ảnh hưởng `Worker`.
7. Cố ý gây `ConcurrentModificationException` (xóa trong lúc `for-each`), ghi lại và sửa bằng `removeIf` hoặc `Iterator.remove()`.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Không method nào trả về collection nội bộ có thể sửa (xem lại code + test chứng minh).
- [ ] `WorkerIndex` có ≥ 8 test: rỗng, một kỹ năng, nhiều kỹ năng giao nhau không có kết quả, xóa worker thì các tập cập nhật, thêm trùng worker không nhân đôi.
- [ ] `findAll` dùng giao tập hợp hiệu quả (bắt đầu từ tập nhỏ nhất) — giải thích trong comment.
- [ ] `docs/collections-notes.md` có bảng chọn cấu trúc dữ liệu cho ≥ 6 tình huống của WeldJobs và độ phức tạp từng thao tác.
- [ ] Có test tái hiện và sửa `ConcurrentModificationException`.
- [ ] Top-5 hoạt động đúng với dữ liệu < 5 phần tử, đúng 5, và nhiều hơn 5 (test).

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Khi nào `LinkedList` tốt hơn `ArrayList`? → Hiếm; thực tế `ArrayList` thường nhanh hơn do cache locality; `LinkedList` chỉ hợp khi thêm/xóa đầu/giữa với iterator.
2. Vì sao `EnumMap/EnumSet` hiệu quả? → Dựa trên mảng/bitset theo ordinal, nhanh và gọn.
3. `List.of` khác `Arrays.asList` và `new ArrayList<>(...)`? → `List.of` bất biến hoàn toàn (và cấm null); `Arrays.asList` cố định kích thước nhưng sửa phần tử được; `ArrayList` sửa được.
4. Defensive copy là gì và vì sao cần? → Sao chép dữ liệu vào/ra để bên ngoài không phá bất biến nội bộ.
5. Độ phức tạp trung bình của `HashMap.get`, `TreeMap.get`? → O(1) và O(log n).

---

### Bài 1.6 – Generics và Repository in-memory
**Thời lượng:** 1 ngày

**Mục tiêu:** Viết code generic tái sử dụng; dựng tầng repository trừu tượng để sau này thay bằng JPA.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Generic class/interface/method; bounded type (`<T extends Comparable<T>>`); wildcard `? extends` (producer) / `? super` (consumer) — nguyên tắc **PECS**.
- Type erasure và hệ quả (không `new T()`, không `instanceof List<String>`).
- Repository pattern.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `interface Repository<T, ID>` (đặt ở `domain.model.common`): `T save(T)`, `Optional<T> findById(ID)`, `List<T> findAll()`, `boolean existsById(ID)`, `void deleteById(ID)`, `long count()`.
2. `abstract class InMemoryRepository<T, ID> implements Repository<T, ID>` nhận `Function<T, ID> idExtractor`; lưu bằng `LinkedHashMap`; `findAll` trả bản sao bất biến. Đặt ở `infrastructure.persistence.inmemory` (domain **không** biết nó tồn tại).
3. `UserRepository`, `WorkerRepository`, `JobRepository`, `JobApplicationRepository` (interface mở rộng `Repository`, đặt cạnh aggregate trong `domain.model.*` — đây là **port**) và các bản in-memory trong `infrastructure.persistence.inmemory` (**adapter**), với các truy vấn: `findByEmail` (user), `findByProvince` (worker), `findOpenJobs`, `findByWorkerAndJob`, `findByEmployer`.
4. Method generic tiện ích trong `CollectionUtils`: `static <T extends Comparable<T>> Optional<T> max(Collection<T>)`; `static <T, K> Map<K, List<T>> groupBy(Collection<? extends T>, Function<? super T, ? extends K>)`.
5. Thực hành wildcard với phân cấp scorer của bài 1.3: `static void printAll(List<? extends JobScorer> scorers)` và `static void addDefaultScorers(List<? super SkillMatchScorer> target)`; viết comment giải thích vì sao không dùng `List<JobScorer>` / `List<SkillMatchScorer>` trực tiếp.
6. Viết `docs/generics-notes.md`: giải thích PECS bằng ví dụ của chính dự án (không copy từ sách).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `InMemoryRepository` không bị trùng code giữa các repository cụ thể (kiểm tra bằng đọc code).
- [ ] `save` cùng ID thì **ghi đè** (hành vi upsert) — có test; quy ước được ghi Javadoc.
- [ ] `findAll()` trả về danh sách không sửa được và không bị ảnh hưởng khi repository thay đổi sau đó.
- [ ] `max` và `groupBy` có ≥ 4 test mỗi hàm, gồm collection rỗng.
- [ ] Biên dịch được cả hai: `printAll(List<SkillMatchScorer>)` và `addDefaultScorers(List<JobScorer>)`; **không** biên dịch được `addDefaultScorers(List<Skill>)` (dán thông báo lỗi biên dịch đã thử).
- [ ] `docs/generics-notes.md` giải thích PECS đúng.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. PECS là gì? → Producer Extends, Consumer Super: đọc ra từ collection dùng `? extends`, ghi vào dùng `? super`.
2. Type erasure nghĩa là gì và hệ quả? → Thông tin kiểu generic bị xóa lúc chạy; không tạo `T[]`/`new T()`, không `instanceof` với kiểu tham số.
3. Vì sao `List<SkillMatchScorer>` không phải là `List<JobScorer>`? → Generics bất biến (invariant), tránh nhét một scorer khác loại vào list thực chất chứa `SkillMatchScorer`.
4. Vì sao nên có interface `Repository` thay vì dùng `Map` trực tiếp? → Tách nghiệp vụ khỏi cách lưu trữ; sau này thay bằng JPA không đổi service.

---

### Bài 1.7 – Lambda và Stream API
**Thời lượng:** 1 ngày

**Mục tiêu:** Viết truy vấn dữ liệu khai báo bằng Stream; hiểu lazy evaluation và giới hạn.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Functional interface: `Predicate`, `Function`, `BiFunction`, `Supplier`, `Consumer`, `UnaryOperator`; method reference (4 dạng).
- `Comparator.comparing(...).thenComparing(...).reversed()`.
- Stream: `filter`, `map`, `flatMap`, `distinct`, `sorted`, `limit`, `skip`, `anyMatch/allMatch/noneMatch`, `reduce`, `collect`.
- `Collectors`: `toList`, `toSet`, `toMap` (merge function), `groupingBy` (+ downstream), `partitioningBy`, `counting`, `averagingLong`, `summarizingInt`, `joining`, `mapping`.
- `IntStream/LongStream`; `Stream.toList()`; stream chỉ dùng **một lần**; không side-effect.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Tạo `SeedData` sinh dữ liệu **tái lập được** (dùng `new Random(42)`): ≥ 30 tin tuyển dụng (nhiều tỉnh, có/không lương, kỹ năng đa dạng, vài tin hết hạn) và ≥ 50 thợ hàn (kỹ năng/ kinh nghiệm/ chứng chỉ đa dạng; vài chứng chỉ hết hạn).
2. Class `JobAnalytics` (mỗi method **chỉ dùng Stream**, không vòng `for`):
   - a) `List<SkillCount> topDemandedSkills(int n)` — kỹ năng được yêu cầu nhiều nhất (record `SkillCount(Skill skill, long count)`).
   - b) `Map<String, Double> averageMaxSalaryByProvince()` — chỉ tính tin có lương.
   - c) `List<JobPost> jobsRequiringAll(Set<Skill> skills)`.
   - d) `Map<Boolean, List<Worker>> partitionByHavingValidCertificate(LocalDate today)`.
   - e) `Map<ExperienceBand, Long> workersByExperienceBand()` với `enum ExperienceBand { JUNIOR(0–1), MID(2–4), SENIOR(5–9), EXPERT(10+) }`.
   - f) `Map<JobPost, Long> eligibleWorkerCountPerJob(JobScorer scorer)` — số thợ có điểm skill-match > 0.
   - g) `Optional<Entry<Set<Skill>, Long>> mostCommonSkillPair()` — cặp kỹ năng cùng xuất hiện nhiều nhất trong các tin (gợi ý: `flatMap` sinh cặp).
3. `JobSearch.toPredicate(JobSearchCriteria)` trả `Predicate<JobPost>` bằng cách **ghép các điều kiện** có mặt (`and`) — tỉnh, loại việc, kinh nghiệm tối đa, lương giao nhau, kỹ năng (`ANY`/`ALL`), và luôn loại tin không `OPEN` hoặc đã hết hạn tại ngày truyền vào.
4. Viết `docs/stream-notes.md`: cho 3 ví dụ stream **không nên dùng** (ví dụ: side-effect, vòng lặp đơn giản đọc rõ hơn, `parallelStream` sai chỗ) và giải thích.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Mọi method a–g không chứa từ khóa `for`/`while` (grep xác nhận).
- [ ] Mỗi method có ≥ 2 test dựa trên dataset nhỏ **tự dựng tay** (không phụ thuộc `SeedData` ngẫu nhiên) cho kết quả đoán trước được.
- [ ] Với `SeedData` seed 42, kết quả của `topDemandedSkills(3)` luôn **giống nhau** giữa các lần chạy (test).
- [ ] `JobSearch.toPredicate` có ≥ 10 test: từng điều kiện riêng, kết hợp nhiều điều kiện, `ANY` vs `ALL`, criteria rỗng, tin hết hạn bị loại.
- [ ] Không có side-effect trong lambda của `map/filter` (đọc code xác nhận).
- [ ] `docs/stream-notes.md` đủ 3 ví dụ.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Stream lazy nghĩa là gì? → Các thao tác trung gian chỉ chạy khi có thao tác kết thúc; xử lý từng phần tử qua pipeline.
2. `map` khác `flatMap`? → `flatMap` làm phẳng stream lồng nhau.
3. `groupingBy` với downstream collector dùng khi nào? → Để tính tổng hợp theo nhóm (đếm, trung bình, ánh xạ...).
4. Vì sao không nên `parallelStream()` bừa? → Chi phí chia việc, thứ tự, thread-safety, dùng chung ForkJoinPool; chỉ lợi khi dữ liệu lớn và tác vụ CPU độc lập.
5. `Collectors.toMap` gặp key trùng thì sao? → Ném `IllegalStateException` nếu không truyền merge function.

---

### Bài 1.8 – Exception handling
**Thời lượng:** 0,5–1 ngày

**Mục tiêu:** Thiết kế hệ thống exception rõ ràng cho nghiệp vụ; xử lý lỗi đúng chỗ.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Checked vs unchecked; `Error` vs `Exception`; `try/catch/finally`; **try-with-resources** và `AutoCloseable`; multi-catch; **exception chaining** (`cause`); suppressed exception.
- Nguyên tắc: fail fast, không nuốt lỗi, không dùng exception cho luồng điều khiển bình thường, bắt ở tầng có thể xử lý.
- Không lộ thông tin nhạy cảm trong message.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Hệ thống exception (package `domain.exception`): `WeldJobsException extends RuntimeException` (có `errorCode`), `ValidationException`, `NotFoundException(entity, id)`, `ConflictException`, `InvalidStateTransitionException(from, to)`.
2. `JobApplicationService.apply(UUID workerId, UUID jobId, String note, LocalDate today)`:
   - Không tìm thấy thợ/tin → `NotFoundException`.
   - Tin không `OPEN` hoặc đã hết hạn → `ConflictException` (message nêu lý do).
   - Thợ đã ứng tuyển tin này → `ConflictException`.
   - `note` dài quá 1000 ký tự → `ValidationException`.
   - Thành công → lưu `JobApplication` trạng thái `SUBMITTED`.
3. `JobApplicationService.changeStatus(UUID applicationId, ApplicationStatus next)` dùng `canTransitionTo`; sai luồng → `InvalidStateTransitionException`.
4. Tạo một `AutoCloseable` giả `ImportSession` (in log khi mở/đóng) và dùng trong `try-with-resources`; chứng minh `close()` được gọi **ngay cả khi có exception**, và trường hợp cả `try` lẫn `close()` cùng ném lỗi (đọc `getSuppressed()`).
5. Viết method `legacyParse(String)` ném **checked exception** giả lập (ví dụ `IOException`) và bọc lại thành `WeldJobsException` kèm `cause`; test kiểm tra `getCause()`.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Mỗi nhánh lỗi của `apply` có test `assertThrows` + kiểm tra **loại exception** và **nội dung message** có thông tin hữu ích (không rỗng, không chứa dữ liệu nhạy cảm).
- [ ] Không có `catch (Exception e) {}` rỗng; không có `e.printStackTrace()` (grep xác nhận).
- [ ] Test `ImportSession` chứng minh `close()` được gọi khi có exception và đọc được suppressed.
- [ ] Test chứng minh `cause` được giữ nguyên khi bọc exception.
- [ ] `changeStatus` có test cho **tất cả** chuyển trạng thái hợp lệ và ít nhất 6 chuyển không hợp lệ.
- [ ] Trình bày (3–5 dòng) khi nào nên dùng checked, khi nào unchecked trong WeldJobs.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Checked và unchecked khác gì, và vì sao Spring dùng chủ yếu unchecked? → Checked bắt buộc khai báo/bắt; unchecked linh hoạt, tránh rác `throws` ở các tầng giữa; Spring có translation exception.
2. Vì sao phải giữ `cause` khi bọc exception? → Giữ stack trace gốc để debug.
3. `finally` có chạy khi `return` trong `try` không? → Có (trừ khi JVM thoát/ngắt).
4. Suppressed exception là gì? → Lỗi phát sinh khi đóng resource trong try-with-resources bị đính kèm vào lỗi chính.

---

### Bài 1.9 – File I/O và JSON (Jackson)
**Thời lượng:** 0,5–1 ngày

**Mục tiêu:** Lưu/đọc dữ liệu an toàn, đúng mã hóa UTF-8; làm quen Jackson (sẽ dùng xuyên suốt Spring).

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `java.nio.file`: `Path`, `Files.readString/writeString/newBufferedReader/walk`, `StandardCopyOption.ATOMIC_MOVE`; charset `UTF_8`.
- Jackson: `ObjectMapper`, `readValue`, `writeValueAsString`, `TypeReference`, `JavaTimeModule`, annotation `@JsonProperty`, `@JsonIgnore`, `@JsonCreator`; serialize **record**; cấu hình `FAIL_ON_UNKNOWN_PROPERTIES`, định dạng ngày.
- Ghi file **nguyên tử** (ghi file tạm rồi move) để tránh hỏng dữ liệu.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Thêm Jackson (và module JSR-310) vào `pom.xml`.
2. Trong `infrastructure.persistence.json`: `JsonFileStore<D>` (đọc/ghi **nguyên tử** danh sách tài liệu `D` vào `data/<tên>.json`, tên file cấu hình được) và các repository JSON (ví dụ `JsonWorkerRepository implements WorkerRepository`) dùng record `WorkerDocument` + `WorkerMapper` (domain ↔ document). **Annotation Jackson chỉ được xuất hiện ở `Document`/infrastructure, không ở domain.** Xử lý: file chưa tồn tại (tạo mới rỗng), file hỏng (ném exception rõ ràng, **không** ghi đè mất dữ liệu cũ — sao lưu thành `.corrupt` rồi báo).
3. Đảm bảo tiếng Việt có dấu **không bị lỗi** sau khi ghi và đọc lại.
4. `ReportExporter.exportMarkdown(Path out)` xuất báo cáo Markdown gồm: top 5 kỹ năng được yêu cầu, lương trung bình theo tỉnh, số tin theo trạng thái (dùng các method `JobAnalytics`).
5. Cấu hình `ObjectMapper` một chỗ duy nhất (`JsonMapperFactory`) để tái sử dụng.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Test vòng khứ hồi (round-trip) dùng `@TempDir`: ghi → đọc → bằng nhau, với chuỗi tiếng Việt `"Hàn nhôm TIG – Đà Nẵng"`.
- [ ] Domain không `import com.fasterxml.jackson` (ArchUnit); có test mapper domain ↔ document khứ hồi cho ít nhất `Worker` và `JobPost`.
- [ ] Test file không tồn tại, file rỗng, file JSON sai cú pháp (kỳ vọng exception rõ ràng và file gốc được giữ lại dưới dạng `.corrupt`).
- [ ] Không có hardcode đường dẫn tuyệt đối.
- [ ] Ghi nguyên tử: giải thích trong comment vì sao ghi trực tiếp có rủi ro (mất điện giữa chừng) và cách làm của bạn.
- [ ] `ObjectMapper` chỉ khởi tạo ở một nơi; không tạo mới trong vòng lặp.
- [ ] File Markdown xuất ra đọc được, số liệu khớp với kết quả test của `JobAnalytics`.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao phải chỉ rõ charset UTF-8? → Mặc định phụ thuộc hệ điều hành; tránh lỗi font tiếng Việt.
2. `ObjectMapper` có thread-safe không và nên tạo bao nhiêu instance? → Thread-safe sau khi cấu hình; nên dùng lại một instance.
3. Ghi nguyên tử là gì? → Ghi file tạm rồi đổi tên/move (ATOMIC_MOVE) để file đích luôn ở trạng thái hợp lệ.
4. Vì sao `FAIL_ON_UNKNOWN_PROPERTIES` quan trọng khi API thay đổi? → Quyết định việc từ chối hay bỏ qua field lạ; ảnh hưởng tương thích tiến/lùi.

---

### Bài 1.10 – Concurrency: `ExecutorService`, `CompletableFuture`, an toàn luồng
**Thời lượng:** 1–1,5 ngày

**Mục tiêu:** Chạy tác vụ song song đúng cách, hiểu race condition và công cụ tránh nó. Đây là nền cho việc thu thập dữ liệu từ nhiều nguồn ở Giai đoạn 3.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `Thread` vs `ExecutorService` (`newFixedThreadPool`, `shutdown/awaitTermination`), `Future`.
- `CompletableFuture`: `supplyAsync`, `thenApply`, `thenCompose`, `thenCombine`, `allOf`, `anyOf`, `exceptionally`, `handle`, `orTimeout`, `completeOnTimeout`.
- Race condition, visibility, atomicity; `synchronized`, `volatile`, `AtomicInteger/LongAdder`, `ConcurrentHashMap` (`merge`, `computeIfAbsent`), `CopyOnWriteArrayList`.
- Deadlock (nhận biết). **Virtual threads (Java 21):** `Executors.newVirtualThreadPerTaskExecutor()` — khi nào phù hợp (tác vụ I/O chờ nhiều).
- Không block trong common pool; luôn truyền executor rõ ràng cho tác vụ I/O.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. **Race condition có chủ đích:** viết `UnsafeCounter` và test cho 100 thread × 1.000 lần tăng; chứng minh kết quả < 100.000 (có thể cần lặp nhiều lần). Sửa bằng 3 cách: `synchronized`, `AtomicInteger`, `LongAdder`; so sánh và ghi nhận xét.
2. `interface JobSourceClient { String name(); List<RawJob> fetch() throws Exception; }` và 4 bản giả lập: nguồn A (chậm 300ms, thành công), B (chậm 800ms, thành công), C (ném exception), D (chậm 5 giây — sẽ bị timeout). `record RawJob(String source, String title, String company, String province, String description)`.
3. `JobAggregator.fetchAll(List<JobSourceClient>, Duration timeoutPerSource)` dùng `CompletableFuture` song song, **mỗi nguồn timeout độc lập**; trả `record AggregationResult(List<RawJob> jobs, Map<String, String> errorsBySource, Duration totalTime)`. Nguồn lỗi/timeout **không làm hỏng** nguồn khác.
4. Thống kê số tin theo tỉnh từ kết quả bằng `ConcurrentHashMap.merge` khi xử lý song song; chứng minh kết quả đúng với 10.000 `RawJob` và 8 thread.
5. Chạy lại bước 3 với `newVirtualThreadPerTaskExecutor()` và so sánh thời gian/cách viết với pool cố định. Ghi nhận xét vào `docs/concurrency-notes.md`.
6. Đảm bảo executor được `shutdown` đúng (không để chương trình treo).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Test `UnsafeCounter` thể hiện lỗi (kết quả sai) và 3 bản sửa cho kết quả đúng 100.000.
- [ ] `fetchAll` trả về dữ liệu của A và B, **ghi nhận lỗi của C và D** trong `errorsBySource`; không ném exception ra ngoài.
- [ ] Tổng thời gian ≈ nguồn chậm nhất (có timeout) chứ **không phải tổng** các nguồn: test khẳng định `totalTime` nhỏ hơn tổng thời gian tuần tự với biên an toàn hợp lý (ghi rõ vì sao biên đó).
- [ ] Không dùng `Thread.sleep` trong code production (chỉ trong lớp giả lập nguồn).
- [ ] Không dùng `.get()` không timeout; không `join()` trong vòng lặp tuần tự làm mất tính song song (đọc code xác nhận).
- [ ] Chương trình thoát sạch (không còn thread không-daemon treo) — dán bằng chứng.
- [ ] `docs/concurrency-notes.md` so sánh pool cố định và virtual threads, nêu **khi nào không nên** dùng virtual threads.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Race condition là gì? Ví dụ trong WeldJobs? → Kết quả phụ thuộc thứ tự thực thi của nhiều thread (ví dụ đếm lượt xem tin, ứng tuyển trùng).
2. `AtomicInteger` hay `synchronized`? → Atomic cho thao tác đơn giản, không khóa; synchronized cho nhiều biến/bất biến phức hợp.
3. `thenApply` khác `thenCompose`? → `thenCompose` dùng khi hàm trả `CompletableFuture` (tránh lồng).
4. Vì sao phải truyền executor riêng cho I/O? → Tránh làm nghẽn `ForkJoinPool.commonPool()`.
5. Virtual threads giải quyết vấn đề gì? → Rẻ, hàng nghìn thread chờ I/O; không giúp tác vụ CPU-bound.

---

### Bài 1.11 – Testing chuyên sâu: JUnit 5, AssertJ, Mockito
**Thời lượng:** 1 ngày

**Mục tiêu:** Viết test có giá trị, đo độ phủ, và kiểm tra chất lượng test bằng "đột biến" thủ công.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- JUnit 5: `@BeforeEach`, `@Nested`, `@DisplayName`, `@ParameterizedTest` (`@CsvSource`, `@MethodSource`, `@EnumSource`), `@TempDir`, `@Timeout`.
- AssertJ: `assertThat`, `containsExactly`, `extracting`, `assertThatThrownBy`, `usingRecursiveComparison`.
- Mockito: `mock`, `when/thenReturn`, `verify`, `ArgumentCaptor`; khi nào **không** nên mock.
- Mẫu AAA (Arrange–Act–Assert), đặt tên test, test data builder (Object Mother), tiêm `Clock`.
- JaCoCo; độ phủ ≠ chất lượng; mutation testing thủ công.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Refactor: thay mọi `LocalDate.now()/Instant.now()` trong code bằng `Clock` được tiêm vào.
2. Tạo `WorkerBuilder`, `JobBuilder` (test data builder) với giá trị mặc định hợp lệ và method `.withXxx()`.
3. Thêm JaCoCo; cấu hình **ngưỡng độ phủ dòng ≥ 80%** cho package `domain` và `application` (build fail nếu thấp hơn).
4. Viết thêm test để đạt: **tổng ≥ 60 test**, trong đó ≥ 6 test tham số hóa, ≥ 3 test dùng Mockito (ví dụ mock `JobRepository` trong `JobApplicationService`, `verify` không gọi `save` khi lỗi).
5. **Đột biến thủ công:** thực hiện 5 thay đổi cố ý làm sai code (ví dụ đổi `>=` thành `>`, bỏ một điều kiện, đổi `&&` thành `||`, trả sai trạng thái, bỏ validate). Với mỗi thay đổi, ghi lại **test nào bị fail**. Nếu không test nào fail → bổ sung test. Ghi bảng kết quả vào `docs/mutation-log.md`.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `./mvnw verify` thành công, báo cáo JaCoCo cho thấy ≥ 80% dòng ở `domain` và `application` (dán số liệu).
- [ ] ≥ 60 test, ≥ 6 test tham số hóa, ≥ 3 test Mockito có `verify` có ý nghĩa.
- [ ] Không còn gọi `now()` trực tiếp trong `domain`/`application` (grep xác nhận).
- [ ] `docs/mutation-log.md` có ≥ 5 đột biến, mỗi đột biến chỉ rõ test phát hiện; không có đột biến nào "sống sót" (hoặc đã bổ sung test để giết).
- [ ] Không có test phụ thuộc thứ tự chạy, không có test phụ thuộc đồng hồ thật hoặc `sleep`.
- [ ] Tên test mô tả hành vi (ví dụ `apply_whenJobClosed_throwsConflict`).

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Độ phủ 100% có đảm bảo không có bug? → Không; chỉ cho biết dòng đã chạy, không đảm bảo assert đúng.
2. Khi nào không nên mock? → Với value object/domain đơn giản, hoặc khi mock làm test chỉ lặp lại cài đặt.
3. Vì sao tiêm `Clock`? → Kiểm soát thời gian trong test, tránh test không ổn định.
4. FIRST trong test là gì? → Fast, Isolated, Repeatable, Self-validating, Timely.

---

### Bài 1.12 – Capstone Giai đoạn 1 (bài thi tùy chọn)
**Thời lượng:** 1–2 ngày

**Mục tiêu:** Hoàn thiện console app WeldJobs, tổng hợp mọi kiến thức Giai đoạn 1 và chứng minh có thể tự thay đổi code có sẵn.

**Bài tập – Phần A: Hoàn thiện ứng dụng**
Menu console phải làm được (tất cả chạy trên dữ liệu JSON từ bài 1.9):
1. Nạp dữ liệu mẫu (`SeedData`) nếu chưa có dữ liệu.
2. **Đăng ký thợ hàn:** nhập thông tin; **chọn kỹ năng đa lựa chọn theo từng nhóm** (hiển thị đánh số theo nhóm `PROCESS`, `MATERIAL`, `POSITION`, ...; người dùng nhập `1,3,5`; hỗ trợ khoảng `2-4`); nhập số năm và cấp độ cho từng kỹ năng đã chọn; thêm chứng chỉ (tùy chọn).
3. **Tạo tin tuyển dụng:** chọn kỹ năng bắt buộc và ưu tiên, lương (`SalaryRange`), loại việc, hạn đăng.
4. **Tìm tin:** theo tỉnh, loại việc, lương, kinh nghiệm, kỹ năng với `ANY/ALL`; có phân trang đơn giản (trang N, 5 tin/trang) và sắp xếp (mới nhất, lương cao nhất).
5. **Ứng tuyển** và **đổi trạng thái** hồ sơ (kiểm tra luồng hợp lệ).
6. **Gợi ý 5 việc phù hợp nhất** cho một thợ, hiển thị điểm và **giải thích điểm** (explanation từ các scorer).
7. **Xuất báo cáo** Markdown + JSON.
8. **Mô phỏng thu thập** từ nhiều nguồn song song (bài 1.10) và in kết quả/lỗi từng nguồn.
- Mọi input sai phải được xử lý (không crash): nhập chữ thay vì số, chọn ngoài phạm vi, bỏ trống.

**Bài tập – Phần B: Báo cáo (`docs/stage1-report.md`)**
Trả lời bằng lời của bạn (mỗi câu 5–10 dòng):
1. Tóm tắt kiến trúc package và lý do tách như vậy.
2. Ba quyết định thiết kế bạn tự hào nhất và ba điểm bạn muốn cải thiện.
3. Chỗ nào dùng polymorphism/Strategy và lợi ích thực tế?
4. Những lỗi/bug khó nhất bạn gặp và cách tìm ra nguyên nhân.
5. Nếu thay kho JSON bằng cơ sở dữ liệu thật, những lớp nào phải sửa? Vì sao câu trả lời là "chỉ `infrastructure` và `config` (composition root)"? Vẽ sơ đồ phụ thuộc 4 tầng của chính project bạn.
6. Bảng ánh xạ khái niệm Java ↔ NestJS/TypeScript mà bạn đã nhận ra (≥ 8 dòng).

**(Tùy chọn) Bài thi cuối giai đoạn (Agent chủ trì)**
- Agent chọn **3 yêu cầu thay đổi** (change request) **mà người học chưa thấy** từ ngân hàng bên dưới (hoặc tự bổ sung theo tinh thần tương tự). Không giới hạn giờ; dùng thang gợi ý như bình thường (ghi nhận số gợi ý để tự đánh giá), mỗi thay đổi **phải có test**.
- Ngân hàng yêu cầu thay đổi:
  1. Thêm nhóm kỹ năng mới `INSPECTION` (NDT/VT/PT/MT/UT…) và cho phép tin yêu cầu kỹ năng thuộc nhóm này.
  2. Thêm `CertificateScorer`: tin yêu cầu chứng chỉ (ví dụ `AWS` hoặc `ISO_9606`) thì thợ chỉ được điểm nếu có chứng chỉ **còn hiệu lực** tại ngày xét.
  3. Thêm trạng thái `INTERVIEW` giữa `SHORTLISTED` và `HIRED`; cập nhật luồng và toàn bộ test liên quan.
  4. Thêm quy tắc: thợ `NOT_LOOKING` không xuất hiện trong gợi ý/tìm kiếm của nhà tuyển dụng.
  5. Thêm bộ lọc "tin đăng trong N ngày gần đây" vào `JobSearchCriteria`.
  6. Thêm chế độ khớp `AT_LEAST_N` (khớp ít nhất N kỹ năng) cho `SkillMatchMode`.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Mọi chức năng 1–8 chạy được; Agent thử **ít nhất 10 đầu vào xấu** (chữ thay số, rỗng, quá dài, ngoài phạm vi) và app không crash.
- [ ] `./mvnw verify` xanh gồm `ArchitectureTest`; `Main`/`config` là nơi duy nhất `new` các adapter của `infrastructure`.
- [ ] `./mvnw verify` xanh; độ phủ đạt ngưỡng bài 1.11; ≥ 80 test.
- [ ] Repo sạch: `.gitignore` đúng, không commit `target/` hoặc `data/` chứa dữ liệu thật; README hướng dẫn chạy được trong ≤ 5 lệnh.
- [ ] `docs/stage1-report.md` đủ 6 phần, nội dung tự viết (Agent hỏi ngẫu nhiên 3 điểm trong báo cáo để xác nhận người học hiểu).
- [ ] (Tùy chọn) Bài thi: hoàn thành **cả 3** change request, có test, code review không còn lỗi mức nghiêm trọng.
- [ ] Người học tự trả lời đúng ≥ 70% bộ câu hỏi tổng hợp (Agent chọn 5 câu bất kỳ từ các bài 1.1–1.11).

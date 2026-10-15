# 02 — DỰ ÁN XUYÊN SUỐT: WELDJOBS

### C.1. Mô tả sản phẩm
WeldJobs là website kết nối **thợ hàn có kinh nghiệm** với **nhà tuyển dụng** (xưởng cơ khí, công ty xây dựng/đóng tàu, đơn vị tuyển lao động đi nước ngoài). Giai đoạn đầu **chỉ tập trung vào thợ hàn**, nhưng thiết kế phải **dễ mở rộng** sang tiện, phay, CNC, lắp ráp... về sau.

### C.2. Vai trò người dùng
| Vai trò | Quyền chính |
|---|---|
| `WORKER` (thợ hàn) | Tạo/sửa hồ sơ, chọn kỹ năng, tìm việc, ứng tuyển, theo dõi trạng thái |
| `EMPLOYER` (nhà tuyển dụng) | Tạo/sửa/đóng tin tuyển dụng của chính mình, xem và xử lý hồ sơ ứng tuyển |
| `ADMIN` | Quản lý danh mục kỹ năng, duyệt/ẩn tin, quản lý người dùng |

### C.3. Mô hình miền (domain model)

**Kỹ năng hàn** được chia theo **nhiều chiều** (không gộp chung một danh sách phẳng):

| Nhóm (`SkillCategory`) | Ví dụ giá trị |
|---|---|
| `PROCESS` (phương pháp hàn) | Hàn que (SMAW), MIG/MAG (GMAW), TIG (GTAW), Hàn dây lõi thuốc (FCAW), Hàn hồ quang chìm (SAW), Hàn hơi (Oxy-fuel) |
| `MATERIAL` (vật liệu) | Thép carbon (sắt), Inox, Nhôm, Thép hợp kim, Đồng |
| `POSITION` (tư thế hàn) | 1G, 2G, 3G, 4G, 5G, 6G (và 1F–4F cho mối hàn góc) |
| `PRODUCT` (loại sản phẩm/công việc) | Hàn kết cấu thép, Hàn ống/đường ống, Hàn bồn bể/bình áp lực, Hàn khung nhôm/cửa nhôm, Hàn đóng tàu |
| `OTHER` | Đọc bản vẽ hàn, Cắt plasma/gió đá, Mài/làm sạch mối hàn, Kiểm tra ngoại quan (VT) |

> Ví dụ yêu cầu ban đầu của người học: "hàn sắt – nhôm – TIG – MIG…" chính là **trộn chiều `MATERIAL` và `PROCESS`**. Bài học sẽ yêu cầu người học phát hiện và tách chúng ra.

**Các thực thể chính:**

- `User` (tài khoản; **không** chứa thông tin hồ sơ): id, email, passwordHash, role, createdAt, enabled.
- `Worker` (hồ sơ thợ hàn, giữ `userId` trỏ tới `User`; trước đây gọi `WorkerProfile`): fullName, phone, province, yearsOfExperience (tổng), availability (`IMMEDIATE`, `WITHIN_30_DAYS`, `NOT_LOOKING`), willingToRelocate, willingToWorkAbroad, expectedSalaryMin (VND/tháng), bio.
- `WorkerSkill` (bảng nối **có thuộc tính**): worker, skill, yearsOfExperience, level (`BASIC`, `INTERMEDIATE`, `ADVANCED`).
- `Certificate`: worker, type (`AWS`, `ISO_9606`, `ASME_IX`, `JIS`, `VN_NATIONAL`, `OTHER`), code, issuedDate, expiryDate.
- `Employer` (hồ sơ nhà tuyển dụng, giữ `userId`): companyName, taxCode, province, address, contactName, contactPhone, description.
- `JobPost`: employerId, title, description, province, jobType (`FULL_TIME`, `CONTRACT`, `SEASONAL`, `OVERSEAS`), salaryMin, salaryMax, currency, minYearsExperience, headcount, status (`DRAFT`, `OPEN`, `CLOSED`, `EXPIRED`), postedAt, expiresAt.
- `JobRequiredSkill`: job, skill, mandatory (true = bắt buộc, false = ưu tiên), minLevel.
- `JobApplication`: worker, job, coverNote, status (`SUBMITTED`, `VIEWED`, `SHORTLISTED`, `REJECTED`, `HIRED`, `WITHDRAWN`), createdAt.

### C.4. Yêu cầu chức năng tổng quát (MVP)
| Mã | Yêu cầu |
|---|---|
| FR-01 | Đăng ký/đăng nhập, phân quyền theo vai trò |
| FR-02 | Thợ hàn tạo hồ sơ, chọn **nhiều kỹ năng** theo nhóm, kèm số năm kinh nghiệm và cấp độ từng kỹ năng |
| FR-03 | Thợ hàn khai báo chứng chỉ (có ngày hết hạn) |
| FR-04 | Nhà tuyển dụng tạo tin tuyển dụng với danh sách kỹ năng **bắt buộc** và **ưu tiên** |
| FR-05 | Tìm kiếm tin: theo tỉnh, loại việc, khoảng lương, số năm kinh nghiệm tối thiểu, **kỹ năng (khớp bất kỳ / khớp tất cả)**; có phân trang + sắp xếp |
| FR-06 | Thợ hàn ứng tuyển; không ứng tuyển trùng; có thể rút hồ sơ |
| FR-07 | Nhà tuyển dụng đổi trạng thái hồ sơ theo luồng hợp lệ |
| FR-08 | Gợi ý việc làm phù hợp cho thợ hàn (điểm khớp kỹ năng) |
| FR-09 | Tin hết hạn tự động chuyển `EXPIRED` (Giai đoạn 3) |
| FR-10 | Nhập tin từ nguồn ngoài (RSS/HTML/API) một cách hợp lệ (Giai đoạn 3) |
| FR-11 | Thông báo (email) khi có tin phù hợp / có hồ sơ mới (Giai đoạn 6) |
| FR-12 | Tự động trích xuất kỹ năng từ mô tả tin bằng AI local (Giai đoạn 7) |

### C.5. Yêu cầu phi chức năng
- Mật khẩu băm bằng BCrypt; không lộ thông tin nhạy cảm qua API.
- API trả lỗi nhất quán (RFC 7807 `ProblemDetail`).
- Trang tin tuyển dụng **SEO tốt** (lý do chọn Next.js ở Giai đoạn 4).
- Giao diện dùng tốt trên **điện thoại** (thợ hàn thường dùng điện thoại).
- Có test tự động, có CI, deploy được bằng Docker.

> **Ghi chú:** `Worker` có `displayName()` (tên rút gọn kiểu `"Nguyễn V.A"`) dùng khi API phải ẩn danh. Không có `Worker extends User` — xem `01-architecture.md` §5.
> Vị trí từng class trong package: `01-architecture.md` §5. Kiến trúc 4 tầng và luật phụ thuộc: `01-architecture.md`.

---
### D.1. Vì sao chọn **Next.js** (không dạy Angular)
- Trang **danh sách và chi tiết việc làm** cần được Google lập chỉ mục (SEO). Next.js hỗ trợ **SSR/ISR**, `generateMetadata` và dữ liệu có cấu trúc `JobPosting` rất tự nhiên.
- Người học đã có nền Next.js, nên không tốn thời gian học framework mới mà không mang lại lợi ích riêng cho đề tài này.
- Angular mạnh ở ứng dụng nội bộ quy mô lớn; WeldJobs không cần điều đó ở giai đoạn đầu.
- Kết luận: **Giai đoạn 4 dạy Next.js (App Router)**.

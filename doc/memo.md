# MEMO — RÀ SOÁT LỘ TRÌNH VÀ HIỆN TRẠNG DỰ ÁN WELDJOBS

> Cập nhật: 15/10/2026 (sau khi hoàn thành Bài 0.2)  
> Dựa trên: `doc/00-rules.md`, `doc/01-architecture.md`, `doc/stage-0.md`, `doc/stage-1.md`.

---

## 1. Trạng thái các bài học hiện tại

| Bài | Tên bài | Trạng thái | Ghi chú |
|---|---|---|---|
| **0.1** | Cài đặt và kiểm tra môi trường | **PASS (`[x]`)** | Đầy đủ JDK 21, Git, Docker, Postgres 16; đã có `doc/environment.md` và trả lời 3 câu hỏi miệng. |
| **0.2** | Dựng khung kiến trúc 4 tầng và luật ArchUnit | **PASS (`[x]`)** | Đã dựng 15 package chuẩn §3.2, cài đặt 5 luật ArchUnit, test vi phạm thành công, verify xanh và trả lời 4/4 câu hỏi miệng. |
| **1.1** | Maven project và Java 21 refresher | **Chưa đạt (`[ ]`)** | Cần chuyển MainMenu vào `presentation.console`, hoàn thiện `Text` vào `shared`, tạo `Regions` vào `domain.model.common`, viết $\ge 8$ test. |

---

## 2. Kế hoạch tiếp theo cho Bài 1.1

Khi bắt đầu Bài 1.1:
1. **Bước 1/5:** Sử dụng project `weldjobs-console` đã dựng ở 0.2 (đã có JUnit 5 và surefire).
2. **Bước 2/5:** Tạo `com.weldjobs.presentation.console.MainMenu` chuyển code menu console sang đây, và `com.weldjobs.Main` chỉ gọi `MainMenu`.
3. **Bước 3/5:** Tạo `com.weldjobs.shared.Text` (hàm `normalize` bỏ dấu tiếng Việt, xử lý `đ/Đ`, gộp khoảng trắng; và `containsAnyKeyword`).
4. **Bước 4/5:** Tạo `com.weldjobs.domain.model.common.Regions` (`regionOf` dùng switch expression cho 12 tỉnh/thành về 3 miền `BAC`, `TRUNG`, `NAM`).
5. **Bước 5/5:** Viết $\ge 8$ unit tests cho `Text` và `Regions`, chạy `./mvnw verify` xanh toàn bộ (kèm ArchUnit).

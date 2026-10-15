# Tóm tắt Kiến trúc 4 tầng WeldJobs

1. **Bốn tầng và trách nhiệm:**
   - `presentation`: Tiếp nhận request (HTTP/Console), validate format, điều hướng gọi Use Case ở `application`.
   - `application`: Use Case điều phối quy trình, transaction, gọi `domain` và gọi `port` (interface).
   - `domain`: Trái tim nghiệp vụ (Entity, Value Object, Domain Service, Repository Interface). Thuần Java 100%.
   - `infrastructure`: Cài đặt chi tiết kỹ thuật (JPA, DB, Redis, Security/JWT, Mail, Third-party APIs).
2. **Hướng phụ thuộc (chỉ trỏ vào trong):**
   - `presentation` -> `application` -> `domain`
   - `infrastructure` -> `domain` và `infrastructure` -> `application` (cài đặt các interface/port)
   - **Tuyệt đối cấm:** `domain` không biết bất kỳ tầng nào khác. `application` không phụ thuộc `infrastructure`.
3. **Vì sao Domain không được biết Spring / DB:**
   - Giữ nghiệp vụ độc lập hoàn toàn với framework và công nghệ lưu trữ.
   - Chạy Unit Test nghiệp vụ cực nhanh mà không cần khởi động Spring hay kết nối Database.
   - Dễ dàng thay thế hoặc nâng cấp hạ tầng (ví dụ: in-memory -> JSON -> PostgreSQL) mà không phải sửa dòng code nghiệp vụ nào.

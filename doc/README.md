# WeldJobs — Lộ trình học Java + Spring Boot (v2)

Bản v2 chia file gốc (~2.950 dòng) thành các file nhỏ, thêm **kiến trúc 4 tầng ngay từ đầu** và bắt Agent **dạy từng bước**.

## Bộ file

| File | Dùng để làm gì | Dán vào Gemini |
|---|---|---|
| `00-rules.md` | Quy tắc cho Agent: dạy từng bước, nghiệm thu, thang gợi ý | **Luôn** |
| `01-architecture.md` | Kiến trúc chuẩn 4 tầng, bảng vị trí class, luật ArchUnit | **Luôn** |
| `02-project.md` | Mô tả dự án, mô hình miền, yêu cầu chức năng | **Luôn** |
| `progress.md` | Ba mốc + bảng tiến độ | **Luôn** |
| `stage-0.md` … `stage-7.md` | Nội dung bài học từng giai đoạn | **Chỉ giai đoạn đang học** |
| `99-appendix.md` | Quy ước Git/PR, mẫu ADR, ánh xạ NestJS↔Spring, tài liệu tra cứu | Khi cần |

## Cách học
1. Tạo repo `weldjobs/` (cấu trúc ở `01-architecture.md` §3.1), lưu bộ file này vào `weldjobs/docs/roadmap/`.
2. **Phiên đầu:** dán *Prompt khởi động* (mục 1 của `00-rules.md`) + 4 file "Luôn" + `stage-0.md`.
3. **Mỗi phiên sau:** dán 4 file "Luôn" + file giai đoạn đang học + **TÓM TẮT PHIÊN TRƯỚC** (Agent in ở cuối mỗi phiên).
4. Nếu file giai đoạn quá dài (Gemini chậm/quên quy tắc): chỉ dán **bài hiện tại** (từ `### Bài X.Y` đến dòng `---` kế tiếp).
5. Mỗi phiên làm **tối đa 1 bài**. Chỉ khi Agent kết luận **PASS** mới đổi `[ ]` → `[x]` trong `progress.md`.

## Lệnh bạn có thể gõ khi học
| Gõ | Agent làm |
|---|---|
| `tiếp` | sang bước kế tiếp |
| `gợi ý` | gợi ý bậc kế tiếp (thang 5 bậc) |
| `ngắn hơn` / `chậm lại` | rút gọn / chia nhỏ bước hiện tại |
| `tóm tắt` | tóm tắt ≤ 5 dòng chỗ đang học |
| `checklist` | hiện nghiệm thu của bài |
| `tiến độ` | in bảng tiến độ |

## Ba mốc
- **Mốc 1 – Backend Java chạy được** (GĐ0–2): bắt buộc.
- **Mốc 2 – Sản phẩm chạy trọn vẹn** (GĐ3–5, trừ 5.6–5.8): bắt buộc.
- **Mốc 3 – Nâng cao** (GĐ6–7: RabbitMQ, AI local): tùy chọn, có thể bỏ cả mốc.

Chi tiết thời lượng ở `progress.md`.

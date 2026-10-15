## GIAI ĐOẠN 4 – FRONTEND: NEXT.JS (App Router)

> **Ghi chú v2 cho file này:** (1) Dạy **từng bước** theo `00-rules.md` — mỗi mục đánh số trong *Bài tập* là một bước, Agent chỉ đưa một bước mỗi lần. (2) Vị trí class theo bảng ở `01-architecture.md` §5; `ArchitectureTest` phải xanh sau mỗi bài. (3) "Bài thi" là **tùy chọn**; báo cáo giai đoạn theo `00-rules.md` mục 9. (4) Mục *Nghiệm thu* gắn *(mục phụ)* hoặc là ghi chú `docs/*.md` có thể ghi nợ `[~]` theo `00-rules.md` mục 4.2.

> **Vì sao Next.js:** xem mục D.1. Người học đã có nền Next.js, nên giai đoạn này **tập trung vào những gì đặc thù của WeldJobs**: SEO cho tin tuyển dụng, form phức tạp (multi-select kỹ năng), xác thực với backend Spring, dữ liệu máy chủ.
>
> **Quy ước giai đoạn 4**
> - Repo/ thư mục `weldjobs-web`; Next.js bản ổn định mới nhất (Agent kiểm tra), **App Router**, TypeScript `strict`, Tailwind CSS (+ thư viện component như shadcn/ui là tùy chọn).
> - Backend là **nguồn sự thật duy nhất** cho dữ liệu và quyền; frontend không tự "tin" trạng thái đăng nhập.
> - **Token không để trong `localStorage`**. Dùng mô hình BFF: Next.js (Route Handlers/ Server Actions) giữ access/refresh token trong cookie **httpOnly + Secure + SameSite**, gọi Spring bằng header `Authorization` từ phía server.
> - Mọi chuỗi giao diện bằng **tiếng Việt**; mã nguồn/ commit tiếng Anh.

### Bài 4.1 – Khởi tạo, cấu trúc và API client có kiểu
**Thời lượng:** 1–1,5 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Cấu trúc App Router: `app/`, layout lồng nhau, `page.tsx`, `loading.tsx`, `error.tsx`, `not-found.tsx`, route group `(public)`, `(worker)`, `(employer)`.
- Server Components vs Client Components (`"use client"`), ranh giới và chi phí.
- Biến môi trường (`NEXT_PUBLIC_*` vs server-only).
- Sinh kiểu từ OpenAPI (`openapi-typescript`) và client fetch có kiểu; xác thực dữ liệu runtime bằng Zod ở biên.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Khởi tạo project, ESLint + Prettier, `strict: true`, alias `@/`.
2. Sinh kiểu TypeScript từ `docs/api/openapi.json` (script `npm run gen:api`); tạo `lib/api/client.ts` (fetch wrapper phía server: base URL từ env, xử lý `ProblemDetail` → lớp `ApiError` có `status`, `detail`, `fieldErrors`, `traceId`).
3. Layout gốc: header (logo, điều hướng, trạng thái đăng nhập), footer; `not-found.tsx`, `error.tsx` toàn cục (tiếng Việt).
4. Xác thực response bằng Zod ở ít nhất 2 endpoint (danh mục kỹ năng, chi tiết tin) và giải thích vì sao vẫn cần dù đã có kiểu từ OpenAPI.
5. Cấu hình `docker-compose` hoặc script để chạy FE cùng BE cục bộ; CORS không cần thiết khi gọi từ server Next (giải thích).
6. `docs/web-architecture.md`: sơ đồ luồng dữ liệu (Browser → Next → Spring), ranh giới Server/Client Component.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `npm run build` và `npm run lint` và `npm run typecheck` đều xanh.
- [ ] `npm run gen:api` tái tạo kiểu thành công; đổi OpenAPI ⇒ lỗi biên dịch nếu client dùng sai.
- [ ] `ApiError` ánh xạ đúng `ProblemDetail` (test đơn vị với response mẫu).
- [ ] Không có biến bí mật nào bị lộ qua `NEXT_PUBLIC_*` (kiểm tra bundle/ grep).
- [ ] `docs/web-architecture.md` đủ sơ đồ và giải thích.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Server Component khác Client Component thế nào? → Chạy trên server, không gửi JS của nó xuống client; Client Component cần cho tương tác/ hook trạng thái.
2. Vì sao biến `NEXT_PUBLIC_` không được chứa secret? → Nó được nhúng vào bundle công khai.
3. Vì sao dùng Zod dù có kiểu OpenAPI? → Kiểu chỉ có lúc biên dịch; dữ liệu runtime có thể sai.

---

### Bài 4.2 – Trang danh sách tin (SSR) + bộ lọc theo URL
**Thời lượng:** 2 ngày

**Mục tiêu:** Trang tìm việc nhanh, chia sẻ được, SEO tốt: trạng thái bộ lọc nằm trên URL.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Render động với `searchParams`; caching/ revalidate của `fetch` trong Next (`revalidate`, `cache`, `tags`) — Agent kiểm tra hành vi mặc định ở **phiên bản hiện hành**.
- URL là nguồn sự thật của bộ lọc; `useRouter`, `useSearchParams`, `usePathname`; debounce nhập liệu.
- Phân trang theo URL (`?page=`); liên kết `rel="next/prev"` (khái niệm); `Suspense` + streaming + `loading.tsx`.
- Truy cập: nhãn form, thứ tự tab, trạng thái focus.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Trang `/viec-lam` hiển thị danh sách tin từ `GET /jobs`: thẻ tin (tiêu đề, công ty, tỉnh, loại việc, khoảng lương, kỹ năng chính dạng "chip", ngày đăng tương đối).
2. Bộ lọc: tỉnh (select), loại việc, khoảng lương (slider/2 input), số năm kinh nghiệm tối đa, **kỹ năng đa lựa chọn chia theo nhóm** (PROCESS/MATERIAL/POSITION/…, ví dụ: tab hoặc accordion; lựa chọn "Khớp bất kỳ/ Khớp tất cả"), sắp xếp.
3. Mọi bộ lọc đồng bộ **hai chiều** với query string; tải lại trang/ chia sẻ link giữ nguyên bộ lọc; nút "Xóa bộ lọc".
4. Phân trang bằng liên kết thật (`<a>`/`Link`), không chỉ nút JS; hiển thị tổng số kết quả.
5. Trạng thái: loading (skeleton), rỗng ("Không có tin phù hợp" + gợi ý nới lọc), lỗi (hiển thị `traceId` rút gọn cho hỗ trợ).
6. Chiến lược cache: trang danh sách `revalidate` hợp lý (ví dụ 60 giây) — giải thích lựa chọn và ảnh hưởng đến "độ mới" tin.
7. Lighthouse (di động): Performance ≥ 85, Accessibility ≥ 95, SEO ≥ 95 cho trang này (dùng dữ liệu seed).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Dán URL có bộ lọc phức tạp (≥ 4 tham số) vào tab mới ⇒ kết quả giống hệt (Agent kiểm tra).
- [ ] Tắt JavaScript ⇒ danh sách và phân trang vẫn hiển thị và dùng được (SSR thực sự).
- [ ] Skeleton/ rỗng/ lỗi đều hiển thị đúng (chụp màn hình/ mô tả + test).
- [ ] Lighthouse đạt ngưỡng (dán điểm).
- [ ] Không có `useEffect` để gọi API lấy danh sách (dữ liệu lấy ở server).
- [ ] Điều hướng bằng bàn phím được toàn bộ bộ lọc.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao để bộ lọc trên URL? → Chia sẻ/ đánh dấu/ nút Back hoạt động/ SSR theo tham số/ SEO.
2. Vì sao trang danh sách phải render ở server? → Crawler và người dùng chậm thấy nội dung ngay; không phụ thuộc JS.
3. `revalidate` ảnh hưởng thế nào đến trải nghiệm? → Đánh đổi giữa độ mới và tải/ tốc độ.

---

### Bài 4.3 – Trang chi tiết tin + SEO + dữ liệu có cấu trúc
**Thời lượng:** 1,5 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `generateMetadata` (title, description, canonical, Open Graph/ Twitter card), `sitemap.ts`, `robots.ts`.
- Dữ liệu có cấu trúc **JSON-LD `JobPosting`** (các trường chính: `title`, `description`, `datePosted`, `validThrough`, `hiringOrganization`, `jobLocation`, `baseSalary`, `employmentType`) — Agent đối chiếu **tài liệu hướng dẫn hiện hành của Google** vì yêu cầu có thể đổi.
- URL thân thiện (slug) + id; chuyển hướng 301 khi slug đổi; xử lý tin hết hạn (hiện thông báo, vẫn trả 200 hay 410 — chọn và giải thích).
- `generateStaticParams`/ISR cho tin phổ biến (tùy chọn).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Trang `/viec-lam/[slug]-[id]` hiển thị chi tiết: mô tả (HTML đã làm sạch ở BE; FE vẫn chỉ render qua thành phần an toàn), kỹ năng bắt buộc/ ưu tiên (tách nhóm), yêu cầu kinh nghiệm, lương, hạn nộp, nút "Ứng tuyển".
2. `generateMetadata` động đủ title/description/OG/canonical; ảnh OG mặc định.
3. Chèn JSON-LD `JobPosting` hợp lệ; kiểm tra bằng công cụ **Rich Results Test** của Google (dán kết quả/ảnh).
4. `sitemap.xml` động liệt kê tin `OPEN` (phân đoạn nếu nhiều); `robots.txt` chặn khu vực riêng tư.
5. Slug sai/ id đúng ⇒ chuyển hướng về slug chuẩn; id không tồn tại ⇒ `not-found` với mã 404 đúng.
6. Tin hết hạn/ đóng: hiển thị trạng thái rõ ràng + gợi ý tin tương tự (cùng kỹ năng chính).
7. Chống XSS: test với mô tả chứa HTML/script ⇒ không thực thi.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] JSON-LD hợp lệ theo Rich Results Test (dán bằng chứng), đủ trường bắt buộc.
- [ ] `view-source` của trang chi tiết chứa nội dung tin và metadata (không cần JS).
- [ ] Sitemap/ robots hoạt động; tin đóng không có trong sitemap.
- [ ] Redirect slug đúng (test e2e hoặc `curl -I`: 301/308 đúng).
- [ ] 404 trả đúng status code (kiểm tra bằng `curl -I`).
- [ ] Test XSS xanh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. JSON-LD giúp gì? → Công cụ tìm kiếm hiểu cấu trúc tin, đủ điều kiện hiển thị kết quả nâng cao.
2. Vì sao cần canonical? → Tránh nội dung trùng lặp qua nhiều URL.
3. Tin hết hạn nên trả mã gì? → 200 với thông báo, hoặc 410 (đã gỡ vĩnh viễn) — chọn có lý do.

---

### Bài 4.4 – Đăng ký/ đăng nhập và bảo vệ route (BFF + cookie httpOnly)
**Thời lượng:** 2 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Lưu token ở cookie `httpOnly; Secure; SameSite=Lax/Strict` ⇒ JS trình duyệt **không đọc được** (chống XSS đánh cắp token).
- Route Handlers (`app/api/auth/*`), Server Actions, `cookies()`, `middleware.ts`/`proxy` (Agent kiểm tra tên/ cơ chế hiện hành) để bảo vệ route.
- Làm mới token (refresh) trong luồng server, tránh race khi nhiều request cùng lúc làm mới.
- CSRF khi dùng cookie: `SameSite`, kiểm tra `Origin`, token chống CSRF cho thao tác thay đổi dữ liệu.
- Phân quyền ở giao diện chỉ để **trải nghiệm**; quyền thật nằm ở BE.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Trang `/dang-ky` (chọn vai trò thợ hàn/ nhà tuyển dụng), `/dang-nhap`, nút đăng xuất. Hiển thị lỗi theo field từ `ProblemDetail` (kể cả 429 giới hạn đăng nhập với thời gian chờ).
2. Route handler `POST /api/auth/login`: gọi Spring, đặt cookie `access` (ngắn hạn) và `refresh` (dài hạn, `Path` giới hạn), **không** trả token trong body.
3. Hàm `getSession()` ở server: đọc cookie, gọi `GET /workers/me` hoặc `/auth/me` (thêm nếu cần) để lấy vai trò; cache theo request.
4. Middleware bảo vệ: `/ho-so/**` cần WORKER, `/nha-tuyen-dung/**` cần EMPLOYER; chưa đăng nhập ⇒ chuyển hướng `/dang-nhap?next=...` rồi quay lại sau khi đăng nhập (chống open redirect: chỉ cho phép đường dẫn nội bộ).
5. Tự động refresh khi access hết hạn (một lần, tránh vòng lặp); refresh thất bại ⇒ xóa cookie và về đăng nhập.
6. Chống CSRF cho thao tác ghi: kiểm tra `Origin`/`Sec-Fetch-Site` hoặc dùng token; viết test chứng minh request chéo site bị từ chối.
7. Test Playwright: đăng ký → đăng nhập → vào route bảo vệ → đăng xuất → bị chặn.
8. Viết `docs/web-auth-notes.md`: so sánh `localStorage` vs cookie httpOnly (XSS vs CSRF), vì sao chọn BFF.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `document.cookie` trong DevTools **không** thấy token; `localStorage` không chứa token (chụp màn hình).
- [ ] Truy cập `/nha-tuyen-dung` bằng tài khoản worker ⇒ bị chặn; chưa đăng nhập ⇒ chuyển hướng có `next` an toàn; `next=https://evil.com` bị bỏ qua (test).
- [ ] Hết hạn access ⇒ tự refresh thành công và request gốc hoàn tất (test với TTL ngắn).
- [ ] Test CSRF xanh.
- [ ] Playwright kịch bản đăng nhập xanh.
- [ ] `docs/web-auth-notes.md` đúng và đầy đủ.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. `httpOnly` bảo vệ khỏi gì và không bảo vệ khỏi gì? → Chống JS đọc cookie (XSS lấy trộm); không chống CSRF.
2. Open redirect là gì? → Tham số chuyển hướng bị lợi dụng đưa người dùng sang trang độc hại.
3. Vì sao không tin kiểm tra quyền ở frontend? → Có thể bị bỏ qua; BE phải kiểm tra.

---

### Bài 4.5 – Form hồ sơ thợ hàn: multi-select kỹ năng, chứng chỉ
**Thời lượng:** 2,5 ngày

**Mục tiêu:** Giao diện nhập liệu trung tâm của sản phẩm: chọn **nhiều kỹ năng theo nhóm**, kèm số năm và cấp độ từng kỹ năng.

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- React Hook Form + Zod resolver; `useFieldArray` cho danh sách động; kiểm tra phía client **và** hiển thị lỗi server theo field.
- Server Actions vs gọi route handler cho thao tác ghi; `useActionState`/ `useFormStatus` (Agent kiểm tra API hiện hành); chống gửi 2 lần.
- Thiết kế trải nghiệm di động: vùng chạm ≥ 44px, bàn phím số (`inputMode`), chọn nhanh.
- Accessibility cho multi-select tự xây (role, `aria-*`) hoặc dùng thư viện đáng tin cậy.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Trang `/ho-so`: form nhiều bước (3 bước): (1) thông tin cơ bản (họ tên, SĐT, tỉnh, tổng số năm kinh nghiệm, sẵn sàng đi làm/ chuyển nơi/ đi nước ngoài), (2) **kỹ năng**, (3) chứng chỉ + giới thiệu bản thân.
2. **Bước kỹ năng:** hiển thị theo nhóm (PROCESS, MATERIAL, POSITION, PRODUCT, OTHER); chọn nhiều bằng chip/ checkbox; mỗi kỹ năng đã chọn mở rộng ra ô **số năm** (0–50) và **cấp độ** (Cơ bản/ Trung bình/ Thành thạo); có tìm kiếm trong danh mục (không dấu); bắt buộc chọn ≥ 1 kỹ năng PROCESS và ≥ 1 MATERIAL (quy tắc UX — giải thích vì sao).
3. Chứng chỉ: thêm/ xóa nhiều chứng chỉ (loại, mã, ngày cấp, ngày hết hạn); cảnh báo chứng chỉ **sắp hết hạn** (≤ 60 ngày) và **đã hết hạn**.
4. Lưu nháp tự động trong phiên (state), cảnh báo khi rời trang có thay đổi chưa lưu.
5. Gửi qua Server Action → `PUT /workers/me`, `PUT /workers/me/skills`, `POST /workers/me/certificates`; xử lý lỗi theo field; chống gửi đúp; thông báo thành công.
6. Hiển thị **độ hoàn thiện hồ sơ** (ví dụ 0–100%) dựa trên quy tắc rõ ràng.
7. Test: Vitest + Testing Library cho schema Zod (≥ 20 ca) và component multi-select (chọn/ bỏ/ tìm kiếm/ bàn phím); Playwright cho luồng hoàn thiện hồ sơ trên viewport di động.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Chọn 5 kỹ năng, nhập số năm/ cấp độ, lưu thành công; tải lại thấy đúng dữ liệu (Agent kiểm tra bằng thao tác và gọi API xác nhận).
- [ ] Trường hợp lỗi: bỏ trống, số năm âm/ > 50, kỹ năng trùng, ngày hết hạn trước ngày cấp ⇒ lỗi hiển thị đúng field (tiếng Việt).
- [ ] Lỗi từ server (ví dụ SĐT sai định dạng) hiển thị đúng ô nhập.
- [ ] Không gửi đúp khi bấm nhanh nhiều lần (test).
- [ ] Dùng được **hoàn toàn bằng bàn phím** và đọc được bằng trình đọc màn hình ở multi-select (kiểm tra thủ công + axe không lỗi nghiêm trọng).
- [ ] ≥ 20 test Zod + test component + 1 e2e di động xanh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao validate cả client và server? → Client cho trải nghiệm, server cho an toàn.
2. `useFieldArray` dùng khi nào? → Danh sách trường động (chứng chỉ, kỹ năng đã chọn).
3. Vì sao quy tắc "≥ 1 PROCESS và ≥ 1 MATERIAL"? → Thông tin tối thiểu để khớp việc có ý nghĩa; chống hồ sơ rỗng nghĩa.

---

### Bài 4.6 – Khu vực nhà tuyển dụng: tạo/sửa tin, quản lý hồ sơ ứng tuyển
**Thời lượng:** 2,5 ngày

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Trang `/nha-tuyen-dung`: danh sách tin của tôi (lọc theo trạng thái, phân trang), thao tác Đăng/ Đóng/ Mở lại/ Sửa.
2. Form tạo/ sửa tin: tiêu đề, mô tả, tỉnh, loại việc, lương, số lượng, hạn đăng; **chọn kỹ năng bắt buộc và ưu tiên** (cùng thành phần multi-select tái sử dụng từ 4.5, kèm `minLevel`); xem trước tin giống trang công khai; lưu nháp.
3. Bảng hồ sơ ứng tuyển cho từng tin: lọc theo trạng thái, xem tóm tắt thợ (kỹ năng, kinh nghiệm, **điểm phù hợp** từ `/candidates`), đổi trạng thái theo luồng hợp lệ (nút chỉ hiện các bước được phép), nhập lý do khi từ chối; SĐT chỉ hiện khi `SHORTLISTED`.
4. Cập nhật lạc quan (optimistic UI) khi đổi trạng thái, **hoàn tác** khi BE trả lỗi (409 xung đột); thông báo rõ.
5. Dùng một thư viện quản lý dữ liệu phía client (TanStack Query) **hoặc** Server Actions + `revalidatePath/Tag` — chọn một và giải thích.
6. Test: e2e "đăng tin → thợ ứng tuyển (qua API) → employer shortlist → hired".

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Employer A không thấy/ không sửa được tin của employer B qua giao diện **và** qua gọi API trực tiếp (Agent thử đổi id trong URL ⇒ 404/403 hiển thị đúng).
- [ ] Nút trạng thái chỉ hiện bước hợp lệ; thử gọi bước sai bằng `curl` ⇒ BE từ chối (đã kiểm tra ở 2.12), FE hiển thị lỗi.
- [ ] Optimistic update hoàn tác đúng khi lỗi (test mô phỏng 409).
- [ ] Form tin tái sử dụng thành phần chọn kỹ năng (không copy-paste).
- [ ] e2e xanh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Optimistic UI là gì và rủi ro? → Cập nhật giao diện trước khi server xác nhận; phải hoàn tác nếu lỗi.
2. `revalidatePath` dùng để làm gì? → Làm mới dữ liệu cache sau khi ghi.

---

### Bài 4.7 – Ứng tuyển, theo dõi trạng thái, gợi ý việc làm
**Thời lượng:** 1,5 ngày

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Nút **Ứng tuyển** trên trang chi tiết: chưa đăng nhập ⇒ chuyển đăng nhập rồi quay lại; đã ứng tuyển ⇒ hiển thị trạng thái; ô ghi chú ≤ 1000 ký tự có bộ đếm.
2. Trang `/ung-tuyen-cua-toi`: danh sách + trạng thái + dòng thời gian lịch sử trạng thái; nút Rút hồ sơ (xác nhận).
3. Trang `/goi-y`: danh sách gợi ý có **điểm phù hợp và giải thích** (breakdown dạng thanh tiến độ, nêu rõ thiếu kỹ năng nào).
4. Trên danh sách tin (4.2) nếu đã đăng nhập thợ: đánh dấu "Bạn khớp X/Y kỹ năng" (tính ở BE hoặc từ endpoint mới nhỏ — chọn và giải thích).
5. Xử lý 409 (đã ứng tuyển/ tin đóng) bằng thông báo thân thiện.
6. Test e2e: tìm → ứng tuyển → theo dõi → rút.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Ứng tuyển đúp bằng hai tab ⇒ tab thứ hai hiển thị lỗi thân thiện, DB chỉ 1 bản ghi.
- [ ] Breakdown cộng đúng bằng điểm tổng hiển thị.
- [ ] Luồng đăng nhập-rồi-quay-lại giữ nguyên ngữ cảnh.
- [ ] e2e xanh.

---

### Bài 4.8 – Responsive, trạng thái giao diện, accessibility, hiệu năng
**Thời lượng:** 1,5 ngày

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Thiết kế **mobile-first**: kiểm tra ở 360px, 768px, 1280px; bộ lọc ở di động dạng ngăn kéo (drawer); nút chạm lớn.
2. Bộ **trạng thái chuẩn**: loading/ rỗng/ lỗi/ ngoại tuyến cho mọi trang dữ liệu; thành phần `ErrorState` hiển thị `traceId`.
3. Accessibility: tương phản màu (WCAG AA), thứ tự focus, nhãn cho icon, `aria-live` cho thông báo, "skip to content"; chạy axe tự động trên các trang chính (không lỗi nghiêm trọng).
4. Hiệu năng: tối ưu ảnh (`next/image`), font (`next/font`), tách bundle, đo Core Web Vitals (LCP, CLS, INP) bằng Lighthouse/ Web Vitals; mục tiêu LCP < 2.5s ở dữ liệu seed trên mạng giả lập.
5. Đa ngôn ngữ: chuẩn bị `vi` (mặc định) bằng thư viện i18n (ví dụ `next-intl`) hoặc từ điển chuỗi tập trung — không hardcode chuỗi rải rác (tùy chọn thêm `en`).
6. `docs/web-quality.md`: bảng điểm Lighthouse/ axe trước-sau cho 4 trang chính và các thay đổi đã làm.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] 4 trang chính đạt Lighthouse di động: Performance ≥ 85, Accessibility ≥ 95, SEO ≥ 95, Best Practices ≥ 90 (dán số).
- [ ] axe không có vi phạm "serious/critical".
- [ ] Không còn chuỗi giao diện hardcode ngoài từ điển (grep kiểm tra mẫu).
- [ ] Kiểm tra thủ công 3 viewport, ảnh chụp đính kèm.

---

### Bài 4.9 – Test frontend và E2E
**Thời lượng:** 1,5 ngày

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Vitest + Testing Library: ≥ 60 test (schema Zod, hàm tiện ích, component quan trọng: bộ lọc, multi-select, thẻ tin, form).
2. Mock API bằng MSW cho test component; **không** mock quá tay (kiểm tra hành vi thực).
3. Playwright: ≥ 8 kịch bản e2e chạy trên **hệ thống thật** (Compose: BE + Postgres + Redis + FE) cho các luồng: duyệt tin ẩn danh, đăng ký/đăng nhập, hoàn thiện hồ sơ, đăng tin, ứng tuyển, đổi trạng thái, 403/404, giao diện di động.
4. Chạy e2e trên 2 trình duyệt (Chromium + một cái khác) và 2 viewport.
5. Cấu hình để e2e chạy được trong CI (Giai đoạn 5) — headless, artifact trace khi lỗi.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `npm test` xanh, ≥ 60 test; e2e ≥ 8 kịch bản xanh 3 lần liên tiếp.
- [ ] Test không dùng `sleep` cố định (dùng chờ theo điều kiện).
- [ ] Có báo cáo Playwright (HTML) và trace khi lỗi.

---

### Bài 4.10 – Capstone Giai đoạn 4 (bài thi tùy chọn)
**Bài tập – Phần A**
- Demo toàn bộ luồng trên giao diện: ẩn danh tìm việc → đăng ký thợ → hồ sơ nhiều kỹ năng → ứng tuyển → employer duyệt → hired; thợ thấy trạng thái.
- `docs/stage4-report.md`: 6 câu phản tư (SSR vs CSR ở từng trang, rủi ro bảo mật phía web, SEO đã làm gì, điểm yếu UX còn lại, nếu cần thêm ngành khác thì giao diện/ mô hình kỹ năng phải đổi gì, quyết định không dùng Angular).

**(Tùy chọn) Bài thi** — Agent chọn 3:
1. Trang công khai **hồ sơ nhà tuyển dụng** (`/cong-ty/[slug]`) kèm danh sách tin đang mở, SEO đầy đủ.
2. **Lưu tin yêu thích** (cần backend bookmark từ bài thi tùy chọn GĐ2; chưa có thì chọn hạng mục khác) trên giao diện, đồng bộ trạng thái ở danh sách và chi tiết, có optimistic update.
3. **Bộ lọc kỹ năng nhanh** theo chip trên đầu danh sách (ví dụ "TIG", "Nhôm", "6G") đồng bộ URL, đếm số tin theo chip (endpoint facet ở BE).
4. **Trang thống kê** cho employer: số hồ sơ theo trạng thái, biểu đồ đơn giản (không dùng thư viện nặng nếu không cần), có kiểm thử.
5. **Chế độ tối** (dark mode) đủ tương phản, ghi nhớ lựa chọn không gây nhấp nháy (FOUC).

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Demo trọn luồng chạy được từ máy sạch (hướng dẫn trong README).
- [ ] Lighthouse/ axe đạt ngưỡng ở trang mới; test mới xanh; e2e vẫn xanh.
- [ ] `docs/stage4-report.md` đủ 6 câu; Agent hỏi lại 3 điểm.
- [ ] (Tùy chọn) Bài thi: hoàn thành cả 3.
- [ ] Trả lời đúng ≥ 70% bộ 12 câu tổng hợp 4.1–4.9.

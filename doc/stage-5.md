## GIAI ĐOẠN 5 – DOCKER, CI/CD VÀ DEPLOY

> **Ghi chú v2 cho file này:** (1) Dạy **từng bước** theo `00-rules.md` — mỗi mục đánh số trong *Bài tập* là một bước, Agent chỉ đưa một bước mỗi lần. (2) Vị trí class theo bảng ở `01-architecture.md` §5; `ArchitectureTest` phải xanh sau mỗi bài. (3) "Bài thi" là **tùy chọn**; báo cáo giai đoạn theo `00-rules.md` mục 9. (4) Mục *Nghiệm thu* gắn *(mục phụ)* hoặc là ghi chú `docs/*.md` có thể ghi nợ `[~]` theo `00-rules.md` mục 4.2.

> **Mục tiêu giai đoạn:** Từ `git push` đến ứng dụng chạy thật trên server có HTTPS, tự động, an toàn, có thể quay lui.
>
> **Quy ước giai đoạn 5**
> - Không đưa secret vào image, repo hay log. Secret đi qua biến môi trường/ Docker secrets/ GitHub Secrets.
> - Mọi image chạy bằng **user không phải root**.
> - Chỉ cổng **22, 80, 443** mở ra Internet; DB, Redis, RabbitMQ **không** mở ra ngoài.
> - Mọi bước deploy phải **lặp lại được** (không có bước "SSH vào gõ tay" ngoại trừ lần dựng server đầu tiên, và lần đó phải ghi lại thành `docs/server-setup.md`).
> - Hạ tầng thực tế (VPS, tên miền) do người học tự chọn; Agent **không** khẳng định giá cả/ gói dịch vụ cụ thể mà phải để người học tự tra cứu bảng giá hiện hành. Nếu không có VPS/ tên miền, có thể thay bằng máy ảo cục bộ + tên miền giả (hosts) + Cloudflare Tunnel hoặc chứng chỉ tự ký; khi đó ghi rõ phần nào không kiểm chứng được.

### Bài 5.1 – Dockerfile cho backend (multi-stage)
**Thời lượng:** 1 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Image, layer, cache của build; thứ tự lệnh ảnh hưởng cache; `.dockerignore`.
- Multi-stage build: giai đoạn build (JDK + Maven) và giai đoạn chạy (JRE gọn).
- Spring Boot layered jar / giải nén theo lớp (Agent kiểm tra lệnh hiện hành của phiên bản Boot đang dùng) để tối ưu cache.
- User không root, `HEALTHCHECK`, `ENTRYPOINT` dạng exec, xử lý tín hiệu (`SIGTERM`) để graceful shutdown.
- JVM trong container: `-XX:MaxRAMPercentage`, hiểu giới hạn bộ nhớ cgroup.
- So sánh: Dockerfile tự viết vs Cloud Native Buildpacks (`spring-boot:build-image`).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Viết `Dockerfile` multi-stage cho `weldjobs-api`: dùng cache Maven (`--mount=type=cache` hoặc tách bước tải dependency), image chạy dựa trên JRE 21 gọn, user `app` (UID cố định), `HEALTHCHECK` gọi `/actuator/health/readiness`.
2. `.dockerignore` loại `target/`, `.git`, `*.md` không cần, `.env`.
3. Chạy container với giới hạn `--memory=512m`; cấu hình JVM sử dụng phần trăm RAM; chứng minh không bị OOM-kill khi chạy kịch bản tải nhẹ (dán `docker stats`).
4. Chứng minh **graceful shutdown**: gửi request chậm, `docker stop`, request vẫn hoàn tất.
5. Sửa một dòng code, build lại: chứng minh **lớp dependency được cache** (dán log build với `CACHED`).
6. Quét lỗ hổng image bằng Trivy (hoặc công cụ tương đương); ghi lại kết quả và cách xử lý các lỗ hổng mức HIGH/CRITICAL (nâng base image, cập nhật dependency, hoặc ghi chú chấp nhận rủi ro có lý do).
7. Làm thêm bản build bằng buildpacks và ghi bảng so sánh (kích thước, thời gian, khả năng kiểm soát) trong `docs/docker-backend.md`.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] `docker build` thành công; `docker run` khởi động và `HEALTHCHECK` chuyển `healthy` (dán `docker ps`).
- [ ] `docker exec <c> id` cho thấy **không phải root**.
- [ ] Rebuild sau khi đổi 1 dòng code dùng cache lớp dependency (dán log).
- [ ] Graceful shutdown được chứng minh.
- [ ] Báo cáo quét lỗ hổng được dán, có xử lý/ giải thích.
- [ ] `docs/docker-backend.md` có bảng so sánh Dockerfile vs buildpacks và kích thước image.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao multi-stage? → Image chạy không chứa công cụ build ⇒ nhỏ hơn, ít bề mặt tấn công.
2. Vì sao chạy không root? → Giảm thiệt hại nếu container bị chiếm quyền.
3. Vì sao thứ tự lệnh Dockerfile quan trọng? → Cache theo lớp; lớp ít đổi (dependency) nên đặt trước.
4. Vì sao dùng `ENTRYPOINT` dạng exec? → Tiến trình Java nhận trực tiếp `SIGTERM`.

---

### Bài 5.2 – Dockerfile cho Next.js (standalone)
**Thời lượng:** 1 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- `output: "standalone"` trong cấu hình Next; copy `.next/standalone`, `.next/static`, `public`.
- Biến môi trường **lúc build** (bị nhúng vào bundle, ví dụ `NEXT_PUBLIC_*`) vs **lúc chạy** (server-only).
- Layer cache cho `node_modules`; `npm ci`; base image gọn.
- Sharp/ tối ưu ảnh trong container; healthcheck HTTP.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Dockerfile 3 giai đoạn (`deps`, `build`, `runner`), user không root, `HEALTHCHECK` (một route nhẹ `/api/health`).
2. Cấu hình để **cùng một image** chạy ở nhiều môi trường: địa chỉ BE lấy từ biến môi trường **runtime** phía server (không nhúng lúc build). Giải thích bằng ví dụ vì sao `NEXT_PUBLIC_API_URL` lúc build làm hỏng nguyên tắc "một image, nhiều môi trường".
3. Đo kích thước image; so sánh với cách copy cả `node_modules` (ghi bảng).
4. Chạy container, kiểm tra SSR hoạt động (view-source có nội dung) và ảnh tối ưu hoạt động.
5. Chứng minh cache: đổi 1 file nguồn, `npm ci` không chạy lại.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Container `healthy`; `view-source` trang danh sách có dữ liệu thật từ BE trong Compose.
- [ ] Không có secret trong image (kiểm tra bằng `docker history` + duyệt file; `grep` mẫu).
- [ ] User không root (dán bằng chứng).
- [ ] Cùng một image chạy với hai cấu hình BE khác nhau (chứng minh bằng hai lần chạy).
- [ ] Bảng so sánh kích thước trong `docs/docker-web.md`.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao `standalone` giảm kích thước? → Chỉ đóng gói file thật sự cần cho chạy.
2. Vì sao `NEXT_PUBLIC_*` đặt lúc build là vấn đề cho "một image nhiều môi trường"? → Giá trị bị nhúng cứng vào bundle.

---

### Bài 5.3 – Docker Compose toàn hệ thống
**Thời lượng:** 1–1,5 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Compose file: `services`, `networks`, `volumes`, `healthcheck`, `depends_on.condition`, `restart`, `deploy.resources`/`mem_limit`, `profiles`.
- Tách cấu hình: `compose.yaml` (chung) + `compose.override.yaml` (dev) + `compose.prod.yaml` (prod).
- Mạng nội bộ: DB/Redis/RabbitMQ chỉ nằm trên mạng `backend`; chỉ proxy nằm trên mạng công khai.
- Secrets: `secrets:` (file) hoặc biến môi trường từ `.env`; không hardcode.
- Giới hạn log (`logging: options: max-size`).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. `compose.yaml`: `postgres`, `redis`, `rabbitmq` (dịch vụ có thể thêm dần đến GĐ6), `api`, `web`, `proxy` (Caddy/Traefik/nginx — chọn ở 5.6), mọi thứ có `healthcheck` và thứ tự khởi động đúng.
2. Hai mạng: `public` (proxy, web) và `backend` (api, DB, cache, broker). `web` gọi `api` qua tên dịch vụ nội bộ.
3. `compose.prod.yaml`: **không** `ports:` cho DB/Redis/broker; đặt `restart: unless-stopped`; giới hạn bộ nhớ từng dịch vụ; giới hạn log.
4. Dịch vụ phụ trợ chỉ ở dev (profile `tools`): Mailpit (bắt email), pgAdmin hoặc tương đương, mock-job-source.
5. Kịch bản kiểm tra bền vững dữ liệu: `down` (không `-v`) rồi `up` ⇒ dữ liệu còn; `down -v` ⇒ dữ liệu mất (hiểu rõ khác biệt).
6. Kịch bản sự cố: `docker kill` container `api` ⇒ tự khởi động lại; `docker stop postgres` ⇒ `api` readiness `DOWN`, phục hồi khi DB lên lại.
7. Script `make up|down|logs|reset` (hoặc `justfile`) và README ngắn.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Một lệnh dựng toàn bộ từ máy sạch; `docker compose ps` tất cả `healthy`.
- [ ] Kiểm tra từ máy khác/ `nmap` cục bộ: với `compose.prod.yaml`, chỉ proxy lộ cổng (dán bằng chứng).
- [ ] Dữ liệu còn sau `down` (không `-v`).
- [ ] Hai kịch bản sự cố (kill api; stop postgres) được thực hiện và ghi lại.
- [ ] Không có secret thật trong file Compose (grep).
- [ ] README mô tả đủ lệnh.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. `depends_on` thường có đủ để đảm bảo DB sẵn sàng không? → Không; cần `condition: service_healthy` và ứng dụng tự kết nối lại.
2. Vì sao tách mạng? → Giảm bề mặt tấn công, dịch vụ nội bộ không truy cập được từ ngoài.
3. `restart: unless-stopped` khác `always`? → `unless-stopped` không tự khởi động lại nếu bạn đã chủ động dừng.

---

### Bài 5.4 – CI với GitHub Actions
**Thời lượng:** 1,5 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Workflow, job, step, runner; trigger (`push`, `pull_request`, `workflow_dispatch`, thẻ phiên bản); `concurrency` (hủy lượt chạy cũ); `permissions` tối thiểu.
- Cache dependency (Maven/npm); artifacts; matrix (nhận biết).
- Chạy Testcontainers trên runner (Docker có sẵn); dịch vụ phụ trợ (`services:`).
- Branch protection + required status checks.
- Bảo mật workflow: tránh `pull_request_target` nguy hiểm, ghim phiên bản action (SHA), secrets không in ra log.

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Workflow `backend-ci.yml`: checkout → setup Java 21 + cache Maven → `./mvnw verify` (gồm Testcontainers + JaCoCo ngưỡng) → upload báo cáo JaCoCo/ surefire (artifact).
2. Workflow `web-ci.yml`: Node (phiên bản cố định) + cache npm → `npm ci` → lint → typecheck → test → build.
3. Workflow `e2e.yml`: dựng Compose trong CI, chạy Playwright, upload report/trace khi lỗi (chạy trên PR vào `main` hoặc theo nhãn).
4. `concurrency` hủy lượt cũ khi push mới; `permissions: contents: read` mặc định.
5. Bật branch protection cho `main`: bắt buộc PR, bắt buộc CI xanh, không push trực tiếp. **Chứng minh** bằng một PR có test cố ý fail không merge được.
6. Dependabot (hoặc Renovate) cho Maven, npm, GitHub Actions, Docker; một job quét phụ thuộc (dependency review) trên PR.
7. Ghim action theo SHA hoặc phiên bản cụ thể; `actionlint` chạy trong CI hoặc local.
8. Đo thời gian CI trước/ sau khi bật cache; ghi vào `docs/ci-notes.md` cùng 8 quy tắc workflow an toàn.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] CI xanh trên `main` (link lượt chạy).
- [ ] PR có test fail ⇒ bị chặn merge (ảnh chụp/ link).
- [ ] Báo cáo JaCoCo và Playwright được upload làm artifact.
- [ ] Thời gian CI trước/ sau cache có số liệu.
- [ ] `permissions` tối thiểu; secrets không bị in (kiểm tra log).
- [ ] Dependabot cấu hình đúng và có ít nhất 1 PR tự tạo (hoặc cấu hình hợp lệ kèm lý do chưa có).

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao ghim phiên bản/ SHA của action? → Chuỗi cung ứng: tránh bản mới độc hại/ lỗi.
2. `pull_request_target` nguy hiểm thế nào? → Chạy với quyền/ secrets của repo đích trên code từ fork.
3. Vì sao cần required checks? → Không có thì CI chỉ mang tính tham khảo.

---

### Bài 5.5 – Build và đẩy image lên registry
**Thời lượng:** 1 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- GitHub Container Registry (GHCR); xác thực bằng `GITHUB_TOKEN` với `packages: write`.
- Tag chiến lược: `sha-<commit>`, phiên bản ngữ nghĩa (`v1.2.3`), **tránh phụ thuộc `latest` khi deploy**; digest bất biến.
- `docker/build-push-action`, buildx cache, (tùy chọn) đa kiến trúc `amd64/arm64`.
- SBOM/ quét lỗ hổng trong pipeline; ký image (khái niệm, cosign).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Workflow `release.yml` chạy khi đẩy thẻ `v*.*.*`: build image `api` và `web`, gắn thẻ `vX.Y.Z` + `sha-…`, đẩy lên GHCR.
2. Quét Trivy trong pipeline: **fail** khi có lỗ hổng CRITICAL chưa có ngoại lệ được ghi nhận.
3. Thêm nhãn OCI (`org.opencontainers.image.source`, `revision`, `version`) vào image.
4. Xuất SBOM (tùy chọn) và lưu như artifact.
5. Chứng minh có thể `docker pull` đúng phiên bản và chạy lên; ghi lại digest.
6. `docs/release-process.md`: quy trình phát hành, quy ước thẻ, cách đánh dấu phiên bản lỗi.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Đẩy thẻ `v0.1.0` ⇒ hai image xuất hiện trên GHCR với thẻ đúng (ảnh/ link).
- [ ] Pipeline fail khi cố ý dùng base image có lỗ hổng CRITICAL (hoặc giả lập), pass khi sửa (hoặc ghi lại cách kiểm thử khả thi).
- [ ] Image có nhãn OCI đúng (dán `docker inspect`).
- [ ] Pull + run thành công trên máy khác/ sạch.
- [ ] `docs/release-process.md` đủ.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao không deploy bằng `latest`? → Không tái lập được/ không biết đang chạy phiên bản nào; khó rollback.
2. Digest khác tag thế nào? → Digest bất biến trỏ đúng nội dung; tag có thể bị đổi.

---

### Bài 5.6 – Reverse proxy, HTTPS và deploy lên VPS (Tùy chọn)
**Thời lượng:** 2–3 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Reverse proxy (Caddy tự động HTTPS, hoặc Traefik/nginx + Let's Encrypt); chứng chỉ, HSTS, chuyển hướng HTTP→HTTPS, header bảo mật (CSP, X-Content-Type-Options, Referrer-Policy, Permissions-Policy); chuyển tiếp `X-Forwarded-*` và cấu hình Spring tin cậy proxy (`server.forward-headers-strategy`).
- Cứng hóa VPS: người dùng không root + `sudo`, đăng nhập SSH bằng khóa (tắt mật khẩu), đổi/ giới hạn SSH, tường lửa `ufw` (22/80/443), `fail2ban`, tự động cập nhật bảo mật, đồng bộ thời gian.
- DNS: bản ghi A/AAAA, TTL; `www` và gốc.
- Cách deploy: GitHub Actions SSH vào server chạy `docker compose pull && docker compose up -d` với **phiên bản thẻ cụ thể**; môi trường GitHub `production` có **phê duyệt thủ công**; khóa deploy quyền hạn chế.
- Phương án khi không có IP công khai: Cloudflare Tunnel (khái niệm và đánh đổi).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Dựng/ thuê một VPS (hoặc máy ảo) Ubuntu LTS; thực hiện toàn bộ cứng hóa ở trên và ghi `docs/server-setup.md` (từng lệnh, giải thích).
2. Cấu hình DNS cho tên miền (gốc + `www`) trỏ về server (hoặc thay thế bằng phương án đã nêu).
3. Proxy: HTTPS tự động, chuyển hướng HTTP→HTTPS, HSTS, header bảo mật; định tuyến `/api/` về `api`, còn lại về `web` (hoặc dùng subdomain `api.` — chọn và giải thích cookie/CORS/SameSite ảnh hưởng thế nào).
4. Cấu hình Spring tin cậy `X-Forwarded-*` đúng (kiểm tra link tuyệt đối và `https` đúng).
5. Workflow `deploy.yml`: kích hoạt thủ công/ khi release, yêu cầu phê duyệt môi trường `production`; SSH bằng khóa riêng, chạy lệnh deploy với **thẻ phiên bản truyền vào**; kiểm tra sức khỏe sau deploy (gọi `/actuator/health/readiness` qua proxy, thất bại ⇒ workflow fail ⇒ **tự động quay về phiên bản trước**).
6. Biến môi trường production lưu ở server (`/opt/weldjobs/.env`, quyền `600`) hoặc GitHub Environment secrets — tuyệt đối không commit.
7. **Quét từ bên ngoài**: `nmap` vào IP server (từ máy khác) chỉ thấy 22/80/443; kiểm tra SSL (ví dụ SSL Labs) và header (securityheaders-style).
8. Thực hiện 2 lần deploy liên tiếp (v1 → v2) và 1 lần **rollback** về v1; ghi thời gian từng bước.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Truy cập `https://<domain>` thấy ứng dụng, chứng chỉ hợp lệ, HTTP tự chuyển HTTPS (dán `curl -I`).
- [ ] `nmap` từ ngoài chỉ thấy 22/80/443; đăng nhập SSH bằng mật khẩu bị từ chối (dán bằng chứng).
- [ ] Header bảo mật có mặt (dán `curl -I`).
- [ ] Deploy tự động chạy từ GitHub Actions có bước phê duyệt; kiểm tra sức khỏe sau deploy chạy thật.
- [ ] Deploy lỗi (cố ý đẩy phiên bản hỏng) ⇒ tự động quay về phiên bản trước (bằng chứng).
- [ ] Rollback thủ công về v1 hoàn tất ≤ 5 phút (ghi thời gian).
- [ ] Không có secret nào trong repo hoặc log CI.
- [ ] `docs/server-setup.md` đủ để người khác dựng lại.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. Vì sao tắt đăng nhập SSH bằng mật khẩu? → Chống dò mật khẩu; chỉ khóa riêng mới vào được.
2. HSTS làm gì và rủi ro? → Buộc trình duyệt dùng HTTPS; cấu hình sai khó gỡ trong thời hạn max-age.
3. Vì sao deploy bằng thẻ cụ thể thay vì `latest`? → Tái lập và rollback.
4. Cookie `SameSite`/domain bị ảnh hưởng thế nào khi tách subdomain `api.`? → Cùng site (eTLD+1) vẫn là same-site; cần cấu hình domain cookie và CORS đúng.

---

### Bài 5.7 – Sao lưu, giám sát, rollback dữ liệu (Tùy chọn)
**Thời lượng:** 1–1,5 ngày

**Kiến thức cần nắm** *(chỉ dạy phần cần cho bước đang làm)*
- Sao lưu logic (`pg_dump`) vs vật lý (base backup/WAL); RPO/RTO; chiến lược 3-2-1; **mã hóa** bản sao lưu; lưu ngoài máy chủ.
- **Một bản sao lưu chưa từng khôi phục thử thì chưa phải bản sao lưu.**
- Giám sát: uptime (Uptime Kuma hoặc dịch vụ tương tự), cảnh báo qua email/ Telegram; xem metrics Actuator; dung lượng đĩa.
- Quay lui migration: chiến lược "migration tiến" + sao lưu trước khi migrate.
- Quản lý log của Docker (xoay vòng).

**Bài tập** *(mỗi mục đánh số = 1 bước; Agent đưa từng bước một)*
1. Script `backup.sh` chạy theo cron/ container riêng: `pg_dump` định dạng custom, **nén + mã hóa** (ví dụ `age`/`gpg`), đặt tên theo thời gian, giữ N bản (chính sách lưu giữ), đẩy ra nơi khác (object storage hoặc máy khác).
2. Script `restore-drill.sh`: khôi phục bản sao lưu mới nhất vào một DB tạm, chạy kiểm tra (đếm bản ghi các bảng chính, kiểm tra Flyway version), báo kết quả. **Chạy thật** và ghi vào `docs/restore-drill-<ngày>.md`.
3. Trước mỗi deploy có migration: tự động tạo bản sao lưu nhanh.
4. Dựng Uptime Kuma (hoặc tương đương): theo dõi `https://<domain>/` và `/actuator/health` (qua đường công khai an toàn hoặc kiểm tra nội bộ), cảnh báo qua kênh bạn chọn; **gây sự cố có chủ đích** (dừng container) và chứng minh nhận cảnh báo.
5. Cảnh báo dung lượng đĩa > 80% (script hoặc công cụ giám sát).
6. `docs/runbook-incident.md`: 6 tình huống (API chết, DB đầy đĩa, migration lỗi, chứng chỉ hết hạn, lộ secret, deploy hỏng) — mỗi tình huống có triệu chứng, chẩn đoán, xử lý, phòng ngừa.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Bản sao lưu được tạo tự động theo lịch (dán danh sách file + thời gian) và **được mã hóa** (chứng minh không đọc được khi không có khóa).
- [ ] `restore-drill` chạy thành công với số liệu khớp (dán kết quả).
- [ ] Cảnh báo thật được nhận khi dừng dịch vụ (ảnh chụp/ log).
- [ ] Log Docker có giới hạn kích thước.
- [ ] `docs/runbook-incident.md` đủ 6 tình huống.

**Câu hỏi kiểm tra** *(hỏi từng câu một)*
1. RPO và RTO là gì? → Mức dữ liệu chấp nhận mất tối đa / thời gian chấp nhận để phục hồi.
2. Vì sao phải thử khôi phục định kỳ? → Bản sao lưu có thể hỏng/ thiếu mà không ai biết.
3. Vì sao lưu bản sao lưu ở nơi khác? → Mất máy chủ thì mất cả hai.

---

### Bài 5.8 – Capstone Giai đoạn 5 (Tùy chọn)
**Bài tập – Phần A**
1. Chạy một vòng phát hành đầy đủ: PR → CI xanh → merge → gắn thẻ `v1.0.0` → build/push → phê duyệt → deploy → kiểm tra sức khỏe → sao lưu → giám sát hoạt động.
2. **Diễn tập sự cố (game day)** — Agent chọn 3 trong 6: kill container API; DB đầy đĩa giả lập; đẩy phiên bản hỏng; chứng chỉ giả lập hết hạn (kiểm tra kế hoạch); mất kết nối Redis; xóa nhầm dữ liệu rồi khôi phục. Ghi lại thời gian phát hiện và phục hồi.
3. `docs/stage5-report.md`: sơ đồ kiến trúc triển khai, danh sách rủi ro còn lại (ít nhất 8), chi phí vận hành ước tính theo bảng giá **hiện hành người học tự tra**.

**(Tùy chọn) Bài thi** — Agent chọn 3:
1. Thêm môi trường **staging** (compose + domain con) với deploy tự động khi merge vào `main`, production chỉ khi gắn thẻ + phê duyệt.
2. Triển khai **blue-green** hoặc tương đương đơn giản (hai bộ container, chuyển proxy sau khi kiểm tra sức khỏe).
3. Job kiểm tra sao lưu hằng ngày tự động khôi phục thử và báo cáo.
4. Ghim image theo **digest** trong deploy và tự cập nhật qua PR có kiểm duyệt.
5. Thêm giới hạn tốc độ/ chặn bot cơ bản ở tầng proxy cho `/api/auth/*`.

**Nghiệm thu** *(chỉ hiện sau khi xong các bước)*
- [ ] Vòng phát hành đầy đủ chạy được và có bằng chứng từng bước.
- [ ] Game day: 3 sự cố được xử lý, có số đo thời gian và rút kinh nghiệm.
- [ ] (Tùy chọn) Bài thi: hoàn thành 3 yêu cầu.
- [ ] Trả lời đúng ≥ 70% bộ 12 câu tổng hợp 5.1–5.7.

# Thực tập tốt nghiệp

## Quy tắc làm việc nhóm
1. Nhánh main dùng để chứa mã nguồn hoàn thiện.
2. Nhánh dev chứa code được merge từ feature vào, test code.
3. Khi đẩy 1 tính năng mới lên Github: Phải đặt tên nhánh theo format `feature/<Tên tính năng bằng tiếng Anh>`.
4. Mỗi khi bắt đầu code: Chạy `git pull` ở trên Terminal để kéo code mới về.
5. Khi muốn đẩy code mới lên Github: Không được push trực tiếp vào nhánh main mà phải tạo pull request mới, bắt @Copilot review code trước khi merge vào nhánh.
6. Commit message phải có ý nghĩa, làm cho người khác hiểu.
7. Xong 1 feature thì phải commit rồi push lên Github ngay.
8. Workflow phải pass thì mới được merge, pull request.

---

## 🛠️ Yêu cầu môi trường
*   **JDK:** 25 trở lên (`java -version`).
*   **MySQL:** 8 cho cách chạy local.
*   **An toàn thông tin:** Tuyệt đối không lưu mật khẩu hoặc API key trong mã nguồn.

---

## 🚀 Chạy backend bằng VS Code

### Cách 1: Chạy với MySQL local
Mở terminal trong VS Code tại thư mục backend:
```powershell
cd D:\app\Bao-dien-tu
$env:TMDT_JWT_SECRET="dG1kdC1sb2NhbC1zZWNyZXQta2V5LTMyLWJ5dGVzISE="
$env:TMDT_MAIL_USERNAME="local@example.com"
$env:TMDT_MAIL_PASSWORD="local-password"
$env:GEMINI_API_KEY="dummy-local-key"
$env:TMDT_DEMO_CARD_PAYMENT_ENABLED="true"
.\mvnw.cmd spring-boot:run
```
Theo mặc định, backend sẽ dùng MySQL local:
```text
jdbc:mysql://localhost:3306/pthttmdt
```

### Cách 2: Chạy với Azure MySQL
Repo có sẵn file script: `run-backend-azure.ps1`.
Mở terminal trong VS Code tại thư mục backend rồi chạy:
```powershell
cd D:\app\Bao-dien-tu
Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass
.\run-backend-azure.ps1
```
Script sẽ tự động cấu hình các biến môi trường:
*   `TMDT_DB_URL`
*   `TMDT_DB_USERNAME`
*   `TMDT_JWT_SECRET`
*   `TMDT_MAIL_USERNAME`
*   `TMDT_MAIL_PASSWORD`
*   `GEMINI_API_KEY`


Riêng `TMDT_DB_PASSWORD` không được lưu trong GitHub. Khi chạy, script sẽ hỏi mật khẩu Azure MySQL. Nếu không muốn nhập lại mỗi lần, có thể set trước trong terminal:

```powershell
$env:TMDT_DB_PASSWORD="your-azure-mysql-password"
.\run-backend-azure.ps1
```

Backend chạy đúng khi terminal hiện:

```text
Tomcat started on port 8082
```

### Điểm báo RSS trực tiếp

Ứng dụng mobile lấy tin ngoài qua `GET /api/news`. API đọc RSS, chuẩn hóa dữ
liệu và giữ snapshot trong RAM 3 phút để phân trang; dữ liệu không được tạo
thành `Article` và không được lưu vào MySQL. Tham số `refresh=true` dùng cho
thao tác kéo xuống làm mới, nhưng backend giới hạn tối thiểu 30 giây giữa hai
lần tải nguồn để tránh spam RSS.

Ví dụ kiểm tra trên Swagger hoặc trình duyệt:

```text
GET /api/news
GET /api/news?page=1&pageSize=20
GET /api/news?category=technology&page=1&pageSize=20
GET /api/news?source=VnExpress&page=1&pageSize=20
GET /api/news?refresh=true
```

Tiến trình làm nóng cache định kỳ được tắt mặc định. Có thể bật nếu môi trường
triển khai cần chuẩn bị dữ liệu trước (tiến trình này cũng không ghi MySQL):

```powershell
$env:NEWS_RSS_SCHEDULED_REFRESH_ENABLED="true"
```

`run-backend-azure.ps1` luôn đặt `NEWS_RSS_SCHEDULED_REFRESH_ENABLED=false` để
bảo đảm bản demo dùng Azure MySQL không nhập tin Điểm báo vào bảng `Articles`.

### Phân quyền khi đăng ký bản demo

- Web gửi `role=MEMBER` hoặc `role=AUTHOR`; backend cấp đúng vai trò người dùng chọn.
- App chỉ gửi `role=MEMBER`, vì vậy tài khoản đăng ký trên app luôn là người đọc.
- Khi đăng nhập trên web hoặc app, role được đọc lại từ backend/JWT để mở đúng khu vực chức năng.
- Không thể tự đăng ký các quyền `ADMIN`, `CENSOR` hoặc `VIP`.

Chế độ tự đăng ký tác giả đang bật mặc định cho bản demo. Có thể tắt bằng:

```powershell
$env:TMDT_ALLOW_AUTHOR_SELF_REGISTRATION="false"
```

### Thanh toán Visa thử nghiệm trên app

Backend có luồng `demo-card` dành riêng cho việc trình diễn trên app. Luồng này tạo giao dịch chờ, xác nhận một lần trên server rồi kích hoạt thời hạn VIP. API không nhận và không lưu số thẻ, ngày hết hạn hoặc CVV.

Chế độ demo mặc định bị tắt để tránh kích hoạt VIP miễn phí khi deploy. Chỉ bật trong terminal dùng để trình diễn:

```powershell
$env:TMDT_DEMO_CARD_PAYMENT_ENABLED="true"
```

Script Azure giữ chế độ này ở trạng thái tắt nếu không được cấu hình rõ ràng.

VNPay vẫn là luồng thanh toán thật và cần cấu hình đầy đủ `VNPAY_TMN_CODE`, `VNPAY_HASH_SECRET`, `VNPAY_RETURN_URL` và `VNPAY_IPN_URL` công khai.

# MMORPG Server

Dự án server game mini MMORPG sử dụng:

- Java
- MySQL/MariaDB
- WebSocket
- Angular

## Yêu cầu cài đặt

- Java JDK 18
- XAMPP

## Tải và cài đặt

### 1. Cài Java JDK 18

https://www.oracle.com/java/technologies/javase/jdk18-archive-downloads.html

### 2. Cài XAMPP

https://www.apachefriends.org/download.html

Sau khi cài XAMPP:
- bật Apache
- bật MySQL

## Database

Mở phpMyAdmin:

```txt
http://localhost/phpmyadmin
```

- Tạo database tên:

```txt
schema
```

- Import file SQL trong thư mục `sql/`

## Chạy project

Mở file:

```txt
RunServer.bat
```

## Kiểm tra

Truy cập:

```txt
http://localhost:4200
```

## Ghi chú

Nếu bị lỗi:

```txt
Address already in use
hãy tắt process Java cũ hoặc restart máy.
```

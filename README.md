# ¶ Chat Application - API Service
This is my first Cloud Native project. Learn more [Cloud Native](https://www.cncf.io/)

## ¶ Setup local Docker

> Đây là những thiết lập để chạy ứng dụng tại local và sử dụng Docker, chưa liên quan tới Kubernetes

- Để chạy được dự án này bạn cần runtime của Java `17.*`. Chạy lệnh sau để kiểm tra `java --version`.
- Bạn sẽ không cần phải có gradle nằm ngủ trong máy tính của bạn, vì dự án này có một em gái gradle wrapper rồi.  
- Bạn cần có docker và docker compose được cài đặt trên máy tính.  
- Bạn sẽ không cần một SQL Server chạy trên máy tính, thay vào đó bạn sẽ sử dụng _docker-compose_ tại [cncf-chat-deployment](https://github.com/thainguyencoffee/cncf-chat-deployment)
- (bỏ qua nếu đã làm) hoặc nếu bạn bắt đầu từ project này, hãy clone dự án [cncf-chat-deployment](https://github.com/thainguyencoffee/cncf-chat-deployment).
    - `git clone https://github.com/thainguyencoffee/cncf-chat-deployment`
    - Đọc hướng dẫn [tại đây](https://github.com/thainguyencoffee/cncf-chat-deployment/blob/main/README.md) và quay lại sau khi đã làm theo hướng dẫn.
- Clone dự án này `git clone https://github.com/thainguyencoffee/cncf-api-service.git`
- Di chuyển tới root dir và chạy lệnh sau để khởi động **cncf-api-service** `./gradlew bootRun`
- Ứng dụng có sẵn tại http://localhost:9001, 
- Quay lại thiết lập tiếp cho [cncf-edge-service](https://github.com/thainguyencoffee/cncf-edge-service/blob/main/README.md)
- Happy coding.

## ¶ Documentation
> Dự án này chưa bao gồm tại liệu đầy đủ, tham khảo những phần có sẵn
- [C4 Diagram](https://drive.google.com/file/d/1HZdVlWdV6liuWck2N3tHPc_Bf68j1msd/view?usp=sharing)
- [API Specification](https://docs.google.com/spreadsheets/d/1Zn4j5M_qX2QmFMdjc-bPrbw7x_bgyqUGiUQCDnraTQU/edit#gid=427435496)

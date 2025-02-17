# 1. OpenJDK 17 + Nginx 포함된 알파인 리눅스 기반 이미지 사용
FROM openjdk:17-alpine

# 2. JAR 파일 경로 지정
COPY build/libs/gorani.jar /app.jar
# gorani.jar 파일을 컨테이너의 /app.jar 위치에 복사

# 3. Spring Boot 애플리케이션 실행
CMD ["java", "-jar", "/app.jar"]
# Spring Boot 애플리케이션 실행

# 6. 포트 노출 (80: Nginx, 8080: Spring Boot)
EXPOSE 8080
# Spring Boot 애플리케이션은 8080 포트를 사용
EXPOSE 80
## Nginx는 80 포트를 사용
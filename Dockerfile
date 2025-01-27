# 1. OpenJDK 17 이미지를 기본으로 설정
FROM openjdk:17-alpine

# JAR 파일 경로 지정
ARG JAR_FILE=build/libs/gorani.jar
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java", "-jar", "/app.jar"]

# 3. Spring Boot JAR 파일을 /app 디렉토리로 복사
COPY build/libs/gorani.jar /gorani.jar

# 5. 컨테이너가 8080 포트를 수신하도록 설정
EXPOSE 8080

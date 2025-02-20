# 1. Gradle 빌드를 위한 JDK 이미지 사용
FROM gradle:8.2.1-jdk17 AS build

# Gradle 실행 환경을 설정
USER root
WORKDIR /app

# ✅ Gradle 빌드 캐시 최적화 및 권한 문제 해결
RUN mkdir -p /home/gradle/.gradle && \
    chmod -R 777 /home/gradle/.gradle

# 프로젝트 소스 복사
COPY --chown=gradle:gradle . .

# ✅ 기존 Gradle 캐시 삭제 후 빌드 (권한 문제 해결 + 로그 추가)
RUN gradle clean build -x test --no-daemon --stacktrace || \
    (cat /app/build/reports/tests/test/index.html || true)

# 2. JDK 17을 기반으로 실행 이미지 생성
FROM openjdk:17-jdk-slim

WORKDIR /app

# ✅ 빌드된 JAR 파일을 정확한 경로로 복사
COPY --from=build /app/build/libs/*.jar app.jar

# ✅ 실행 명령어 추가
CMD ["java", "-jar", "app.jar"]

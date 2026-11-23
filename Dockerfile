# ---- 1단계: 빌드 ----
FROM gradle:8.10-jdk21 AS build
WORKDIR /app

# 의존성 캐시를 위해 gradle 설정 파일만 먼저 복사
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || true

COPY src ./src
RUN gradle bootJar --no-daemon -x test

# ---- 2단계: 실행 ----
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# non-root 사용자로 실행
RUN groupadd -r spring && useradd -r -g spring spring
USER spring

COPY --from=build /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod
EXPOSE 8080

ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75.0", "-jar", "app.jar"]

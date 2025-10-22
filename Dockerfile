# 1️⃣ 빌드 단계 (멀티 스테이지: JAR 빌드)
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app
COPY . .

RUN chmod +x mvnw

# Maven Wrapper로 빌드 (테스트는 이미 CI에서 하므로 skip)
RUN ./mvnw clean package -DskipTests

# 2️⃣ 실행 단계 (경량 런타임)
FROM eclipse-temurin:21-jre

WORKDIR /app

# 타임존 (한국 기준으로 설정)
ENV TZ=Asia/Seoul

# 빌드 결과물 복사 (target/*.jar → app.jar)
COPY --from=builder /app/target/*.jar app.jar

# 서버 포트 (Spring Boot default: 8080)
EXPOSE 8080

# 프로파일 환경변수 (CI/CD에서 override 가능)
ENV SPRING_PROFILES_ACTIVE=prod

# 실행 명령
ENTRYPOINT ["java", "-jar", "app.jar"]

# 1. Java 17 JDK 이미지 사용
FROM openjdk:17-jdk-slim

# 2. 빌드 단계: Gradle로 jar 만들기
WORKDIR /app
COPY . .
RUN ./gradlew bootJar -x test

# 3. 실행 단계: jar 실행
CMD ["java", "-jar", "/app/build/libs/lululand-0.0.1-SNAPSHOT.jar"]

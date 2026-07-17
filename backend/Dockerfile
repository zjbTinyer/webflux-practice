# ===== 构建阶段 =====
FROM eclipse-temurin:17-jdk-alpine AS build
WORKDIR /workspace
COPY pom.xml .
RUN mvn dependency:go-offline -B 2>/dev/null || true
COPY src src
RUN mvn clean package -DskipTests -q

# ===== 运行阶段 =====
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /workspace/target/*.jar app.jar
EXPOSE 8080

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=30s \
    CMD wget -qO- http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", "-jar", "app.jar", "--spring.profiles.active=docker"]

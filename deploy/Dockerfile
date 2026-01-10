# 通用的Spring Boot微服务Dockerfile
# 使用多阶段构建，减小镜像体积

# 阶段1：构建阶段
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# 复制pom文件并下载依赖（利用Docker缓存）
COPY pom.xml .
COPY hksc-common/pom.xml hksc-common/
RUN mvn dependency:go-offline -B -pl hksc-common

# 复制所有源代码
COPY . .

# 构建指定的服务
ARG SERVICE_NAME
RUN mvn clean package -pl ${SERVICE_NAME} -am -DskipTests

# 阶段2：运行阶段
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# 安装时区数据
RUN apk add --no-cache tzdata && \
    cp /usr/share/zoneinfo/Asia/Shanghai /etc/localtime && \
    echo "Asia/Shanghai" > /etc/timezone && \
    apk del tzdata

# 从构建阶段复制jar包
ARG SERVICE_NAME
COPY --from=builder /build/${SERVICE_NAME}/target/*.jar app.jar

# 创建非root用户运行应用
RUN addgroup -g 1000 appuser && \
    adduser -D -u 1000 -G appuser appuser && \
    chown -R appuser:appuser /app
USER appuser

# 健康检查
HEALTHCHECK --interval=30s --timeout=3s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# 暴露端口（会被docker-compose覆盖）
EXPOSE 8080

# JVM参数优化
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC -XX:MaxGCPauseMillis=200"

# 启动应用
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

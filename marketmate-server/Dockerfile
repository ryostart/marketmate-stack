# ---- build stage ----
FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# 依存キャッシュのために設定系からコピー
COPY gradlew settings.gradle build.gradle /app/
COPY gradle /app/gradle
RUN sed -i 's/\r$//' gradlew && chmod +x gradlew
RUN ./gradlew --version

# ソース投入してビルド（テストは一旦スキップ）
COPY . /app
RUN ./gradlew bootJar -x test

# ---- run stage ----
FROM eclipse-temurin:21-jre
WORKDIR /app

# 生成された jar をコピー（*SNAPSHOT*.jar 前提）
COPY --from=build /app/build/libs/*SNAPSHOT*.jar /app/app.jar

ENV JAVA_OPTS="-XX:+UseContainerSupport -Xms256m -Xmx512m"

EXPOSE 8080
ENTRYPOINT ["sh","-c","java $JAVA_OPTS -Dspring.profiles.active=${SPRING_PROFILES_ACTIVE} -jar /app/app.jar"]

FROM eclipse-temurin:17-jre

WORKDIR /app

COPY target/cartflow-1.0.0.jar app.jar

CMD ["java", "-cp", "app.jar", "com.cartflow.CartFlow"]

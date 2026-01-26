FROM eclipse-temurin:17-jdk-alpine

# Define o diretório de trabalho dentro do container
WORKDIR /app

# Copia o .jar que já foi construído pelo pipeline de CI
COPY target/*.jar app.jar

EXPOSE 8081
# porta de debug
EXPOSE 5005

# Ativa o modo debug
ENV JAVA_OPTS="-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005"

CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
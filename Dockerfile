FROM eclipse-temurin:21-jdk
WORKDIR /gestao 
RUN groupadd dev && useradd -m -g dev matheus
USER matheus
COPY target/*.jar gestao.jar
EXPOSE 8080
CMD ["java","-jar","gestao.jar"]
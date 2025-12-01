FROM eclipse-temurin:21-jre
WORKDIR /gestao 
RUN groupadd dev && useradd -g dev matheus
COPY target/*.jar gestao.jar
USER matheus
EXPOSE 8080
CMD ["java","-jar","gestao.jar"]
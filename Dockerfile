FROM maven:3.6.3-openjdk-8-slim AS build

WORKDIR /src/imagini
COPY ./ ./
RUN mvn clean package

FROM openjdk:8-jre-slim

WORKDIR /opt/imagini
COPY ./entrypoint.sh /var/lib/imagini/entrypoint.sh
COPY --from=build /src/imagini/target/imagini-1.0-SNAPSHOT.jar /var/lib/imagini/imagini-1.0-SNAPSHOT.jar
EXPOSE 8080

ENTRYPOINT /var/lib/imagini/entrypoint.sh


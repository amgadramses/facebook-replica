FROM maven:3.6-jdk-11
COPY src /usr/app/src
COPY target /usr/app/target
COPY pom.xml /usr/app/pom.xml
WORKDIR /usr/app
#RUN mvn package
ENTRYPOINT ["java", "-jar", "target/cache-1.0-USER-APP-SNAPSHOT.jar"]
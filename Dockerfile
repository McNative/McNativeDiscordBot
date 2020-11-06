FROM java:8-jdk-alpine

COPY ./build/McNativeDiscordBot.jar /usr/app/

WORKDIR /usr/app
ENTRYPOINT ["java", "-jar", "McNativeDiscordBot.jar"]1
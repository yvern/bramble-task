FROM clojure as deps
WORKDIR /app
RUN apt update && apt install wget firefox-esr ffmpeg -y
RUN wget https://github.com/mozilla/geckodriver/releases/download/v0.29.1/geckodriver-v0.29.1-linux64.tar.gz
RUN tar -xzf geckodriver* && chmod +x geckodriver && mv geckodriver /usr/local/bin/
COPY deps.edn .
RUN clj -Spath -X:pkg
RUN clj -Spath -M:test:e2e

FROM openjdk:11.0.11-jre-slim as jre
WORKDIR /app


FROM deps as tested
COPY src src
COPY test test
COPY tests.edn .
RUN clj -M:test:e2e --fail-fast


FROM tested as cli-jar
RUN clj -X:pkg:cli

FROM jre as cli
COPY --from=cli-jar /app/scramble.jar .
ENTRYPOINT java -jar scramble.jar


FROM tested as api-jar
RUN clj -X:pkg:api

FROM jre as api
COPY --from=api-jar /app/scramble-api.jar .
ENTRYPOINT java -jar scramble-api.jar
CMD 8080


FROM ghcr.io/graalvm/graalvm-ce:latest as graal
WORKDIR /app
RUN gu install native-image


FROM graal as cli-native
COPY --from=cli-jar /app/scramble.jar .
RUN native-image -jar scramble.jar --initialize-at-build-time --static --no-fallback

FROM scratch as cli-bin
COPY --from=cli-native /app/scramble .
ENTRYPOINT [ "./scramble" ]

FROM clojure as base



WORKDIR /app

COPY deps.edn .

RUN clj -Spath -X:pkg
RUN clj -Spath -M:test

FROM base

COPY src src
COPY test test
COPY tests.edn .

RUN clj -M:test --skip yvern.scramble.e2e
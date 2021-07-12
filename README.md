# Bramble

A Scramble checker

## Functionality

* A core library that checks if given a group of letters (the first argument, as a string), the target word (second argument, also as a string) can or not be formed (the returned boolean).

* A cli application that reads 2 strings from the commandline and displays a message telling the user if with the given pool of letters one can form the target word.

* An HTTP server that listens on a given port, and a post on route `/api` with a body on the shape:
    ```clojure
    {:letters "poolofletters" :word "targetword"}
    ```
    answers with status `200` in the form:
    ```clojure
    {:letters "poolofletters" :word "targetword" :scramble? false}
    ```
    or `400` in the case of a malformed body, with a [malli](https://github.com/metosin/malli) explanation attatched.
    Handles all formats [muuntaja](https://github.com/metosin/muuntaja) handles out of the box.

* A cljs SPA made with [scittle](https://github.com/borkdude/scittle), that is hosted from the same server as the api, and uses such to provide the functionality from the frontend.

## Usage

Run all tests (assuming you have any of the broeser drivers supported by [etaoin](https://github.com/igrishaev/etaoin)):

    $ clojure -M:test
    $ clojure -M:test --watch

skip the browser ones by appending `--skip yvern.scramble.e2e`


Run the cli app:

    $ clojure -M:scramble world word
    Yes we got scramble!


Build runnable uberjar for the cli app (`scramble.jar`):

    $ clojure -X:pkg:cli:no-api


Run the server, both the api and hosting the webapp:

    $ clojure -M:dev-client 8080

when not compiling, the cljs app will be loaded on each request to be served, so you get easy reloading.


Build runnable uberjar for the api and web app (`scramble-api.jar`):

    $ clojure -X:pkg:api

here the cljs app is loaded on compile-time and served from memory.


Appart from the clojure cli, there is also a `Dockerfile` to not only build all of the above, but also provide an anvironment to run all tests and build a single static binary native cli app via graalvm.
Please check the `Dockerfile` in order to use them.

## Choices


* `scittle` instead of a full blown, aot compiled clojurescript project:

    given the simplicity and lack of performance/size criticality of the web app, the interactive and straightforward nature of scittle made development much easier than a more complex setup.

## License

Copyright © 2021 Yuri Vendruscolo da Silveira

Distributed under the Eclipse Public License version 1.0.

Tested with Java 15 and Gradle 6.8.3, might work with other versions.

This project don't include a gradle wrapper, to generate one run:
```shell
gradle wrapper
```

Then, to build:
```shell
  ./gradlew build
```

To run the server using gradle (default port, for development)
```shell
  ./gradlew bootrun
```

To run the built jar
```shell
  java -jar build/libs/beerstock-restapi-1.0.0-SNAPSHOT.jar
```

Useful:
- default port is 8080, possible to change in application.yml or via command line (-Dserver.port=8083 or --server.port=8083)
- access __**/swagger-ui**__ for a quick api overview and manual testing
- access _*/h2*_ for database console
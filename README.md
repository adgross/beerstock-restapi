## Beer Stock REST api

This project aim to create a beer stock back-end server using REST architecture.

- Targets Java 15.  
- Build with Gradle.

### Dependencies

Make sure you have `JDK 15` and `Gradle` installed.

This project don't include a gradle wrapper. If you want to build with system
installed gradle, skip the gradle wrapper generation and build with
`gradle build`.

### Build and run

_The following instructions are expected to be run in the root directory of the
project._

* Generate a gradle wrapper.

        gradle wrapper

* Build the project. This also run tests.

        ./gradlew build

* Run the built jar.

        java -jar build/libs/beerstock-restapi-1.0.0-SNAPSHOT.jar

* Run the server using gradle (default port, for development).

        ./gradlew bootrun

### API
| Method | URI                          | Description                          |
| :------|:-----------------------------|:-------------------------------------|
| GET    | /api/v1/beers                | List all beers                       |
| POST   | /api/v1/beers                | Create a beer                        |
| PUT    | /api/v1/beers/{id}           | Update a beer                        |
| GET    | /api/v1/beers/{id}           | Return a beer by the given id        |
| DELETE | /api/v1/beers/{id}           | Delete a beer by the given id        |
| PATCH  | /api/v1/beers/{id}/decrement | Decrement the beer quantity in stock |
| PATCH  | /api/v1/beers/{id}/increment | Increment the beer quantity in stock |
| GET    | /api/v1/beers/name/{name}    | Return a beer by the given name      |
| DELETE | /api/v1/beers/name/{name}    | Delete a beer by the given name      |

### Default settings
- default port is `8080`, to change it:
  - modify the `application.yml` or
  - add one of the following command line parameter:
`-Dserver.port=8083` or `--server.port=8083`
- access `/swagger-ui` for a quick api overview and manual testing.
- access `/h2` for database console.

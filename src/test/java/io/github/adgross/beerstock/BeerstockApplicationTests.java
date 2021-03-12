package io.github.adgross.beerstock;

import static org.springframework.http.HttpHeaders.CONTENT_TYPE;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.web.reactive.server.WebTestClient;

@SpringBootTest(classes = BeerstockApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class BeerstockApplicationTests {

  @Autowired
  private WebTestClient client;

  private String beerToJson(String name, String brand, int max, int quantity, String type) {
    String json = "{"
        + " \"name\": \"%s\","
        + " \"brand\": \"%s\","
        + " \"max\": %d,"
        + " \"quantity\": %d,"
        + " \"type\": \"%s\""
        + "}";
    return String.format(json, name, brand, max, quantity, type);
  }

  /*
    we are going to test:
    - create one beer
    - update the same beer
    - find the updated beer by name
    - delete the updated beer by name
    - list all (should return empty)
   */
  @Test
  void createUpdateFindAndDeleteBeers() {
    String jsonBeerToCreate = beerToJson("beer", "Test Beer", 100, 50, "LAGER");

    client.post()
        .uri("api/v1/beers")
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .bodyValue(jsonBeerToCreate)
        .exchange()
        .expectStatus().isCreated()
        .expectBody()
        .json(jsonBeerToCreate)
        .jsonPath("$.id").isEqualTo(1);

    String jsonBeerChanged = beerToJson("beer2", "Test Beer", 50, 20, "LAGER");

    client.put()
        .uri("api/v1/beers/{id}", 1)
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .bodyValue(jsonBeerChanged)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(jsonBeerChanged);

    client.get()
        .uri("api/v1/beers/name/{name}", "beer2")
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .json(jsonBeerChanged);

    client.delete()
        .uri("api/v1/beers/name/{name}", "beer2")
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isNoContent()
        .expectBody().isEmpty();

    client.get()
        .uri("api/v1/beers")
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .accept(APPLICATION_JSON)
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0);
  }

  /*
    we are going to test:
    - insert beers via sql
    - list all
    - increment (success)
    - decrement (fail)
    - find by id (all beers)
    - delete by id (all beers)
    - list all should return empty
   */
  @Test
  @Sql(scripts = "/test.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
  void listingIncrementDecrement() {
    client.get()
        .uri("api/v1/beers")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(5);

    client.patch()
        .uri("api/v1/beers/{id}/increment", 3)
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .bodyValue("{\"quantity\": 500}")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.quantity").isEqualTo(5500);

    client.patch()
        .uri("api/v1/beers/{id}/decrement", 2)
        .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
        .bodyValue("{\"quantity\": 10}")
        .exchange()
        .expectStatus().isBadRequest()
        .expectBody()
        .jsonPath("$.message")
        .isEqualTo("Removing 10 beers from Beer ID(2) leads to non existent quantity");

    for (var i = 1; i <= 5; i++) {
      client.get()
          .uri("api/v1/beers/{id}", i)
          .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
          .accept(APPLICATION_JSON)
          .exchange()
          .expectStatus().isOk()
          .expectBody()
          .jsonPath("$.id").isEqualTo(i);

      client.delete().uri("api/v1/beers/{id}", i)
          .header(CONTENT_TYPE, APPLICATION_JSON_VALUE)
          .exchange()
          .expectStatus().isNoContent()
          .expectBody().isEmpty();
    }

    client.get()
        .uri("api/v1/beers")
        .exchange()
        .expectStatus().isOk()
        .expectBody()
        .jsonPath("$.length()").isEqualTo(0);
  }

}

package io.github.adgross.beerstock.controller;

import static io.github.adgross.beerstock.utils.JsonConvertUtils.asJsonString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.enums.BeerType;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
import io.github.adgross.beerstock.exception.BeerStockExceededException;
import io.github.adgross.beerstock.services.BeerService;
import java.util.List;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(BeerController.class)
public class BeerControllerTests {

  private static final String BEER_API_URL_PATH = "/api/v1/beers";
  private static final String BEER_API_URL_PATH_ID = "/api/v1/beers/{id}";
  private static final String BEER_API_URL_PATH_NAME = "/api/v1/beers/name/{name}";
  private static final String BEER_API_URL_PATH_INCREMENT = "/api/v1/beers/{id}/increment";
  private static final String BEER_API_URL_PATH_DECREMENT = "/api/v1/beers/{id}/decrement";
  private static final Long ID_VALID = 1L;
  private static final Long ID_INVALID = 999L;
  private static final String NAME_VALID = "valid";
  private static final String NAME_INVALID = "invalid";


  private final BeerDto.BeerDtoBuilder beerDtoBuilder = new BeerDto(
      1L, "name", "brand", 400, 100, BeerType.FIRKANT).toBuilder();

  @MockBean
  private BeerService beerService;

  @Autowired
  private MockMvc mockMvc;

  @Test
  void listAllWithRegisteredBeers() throws Exception {

  }

  @Test
  void listAllWithoutRegisteredBeers() throws Exception {

  }

  @Test
  void findByIdWithRegisteredId() throws Exception {

  }

  @Test
  void findByIdWithUnregisteredId() throws Exception {

  }

  @Test
  void findByNameWithRegisteredName() throws Exception {

  }

  @Test
  void findByNameWithUnregisteredName() throws Exception {

  }

  @Test
  void createWithValidBeer() throws Exception {
    var validBeer = beerDtoBuilder.build();

    Mockito.when(beerService.createBeer(validBeer)).thenReturn(validBeer);

    mockMvc.perform(post(BEER_API_URL_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(validBeer)))
        .andExpect(status().isCreated())
        // id is managed by JPA, so we should ignore it
        .andExpect(jsonPath("$.name", Matchers.is(validBeer.getName())))
        .andExpect(jsonPath("$.brand", Matchers.is(validBeer.getBrand())))
        .andExpect(jsonPath("$.type", Matchers.is(validBeer.getType().toString())))
        .andExpect(jsonPath("$.max", Matchers.is(validBeer.getMax())))
        .andExpect(jsonPath("$.quantity", Matchers.is(validBeer.getQuantity())));
  }

  void createWithInvalidBeers(List<BeerDto> invalidBeers) throws Exception {
    for (var beer : invalidBeers) {
      mockMvc.perform(post(BEER_API_URL_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(beer)))
          .andExpect(status().isBadRequest());
    }
  }

  @Test
  void createWithAlreadyRegisteredBeer() throws Exception {
    var validBeer = beerDtoBuilder.build();

    Mockito.when(beerService.createBeer(validBeer))
        .thenThrow(BeerAlreadyRegisteredException.class);

    createWithInvalidBeers(List.of(validBeer));
  }

  @Test
  void createWithInvalidName() throws Exception {
    var invalidNameBeers = List.of(
        beerDtoBuilder.name("").build(),
        beerDtoBuilder.name("a".repeat(201)).build(),
        beerDtoBuilder.name(null).build()
    );

    createWithInvalidBeers(invalidNameBeers);
  }

  @Test
  void createWithInvalidBrand() throws Exception {
    var invalidBrandBeers = List.of(
        beerDtoBuilder.brand("").build(),
        beerDtoBuilder.brand("a".repeat(201)).build(),
        beerDtoBuilder.brand(null).build()
    );

    createWithInvalidBeers(invalidBrandBeers);
  }

  @Test
  void createWithOutOfRangeMax() throws Exception {
    var invalidMaxBeers = List.of(
        beerDtoBuilder.max(0).build(),
        beerDtoBuilder.max(501).build()
    );

    createWithInvalidBeers(invalidMaxBeers);
  }

  @Test
  void createWithOutOfRangeQuantity() throws Exception {
    var invalidQuantityBeers = List.of(
        beerDtoBuilder.quantity(-1).build(),
        beerDtoBuilder.quantity(101).build()
    );

    createWithInvalidBeers(invalidQuantityBeers);
  }

  @Test
  void createWithQuantityBiggerThanMax() throws Exception {
    var incorrectBeer = beerDtoBuilder.max(50).quantity(100).build();

    Mockito.when(beerService.createBeer(incorrectBeer))
        .thenThrow(BeerStockExceededException.class);

    createWithInvalidBeers(List.of(incorrectBeer));
  }

  @Test
  void deleteByIdWithRegisteredId() throws Exception {
    Mockito.doNothing().when(beerService).deleteBeer(ID_VALID);

    mockMvc.perform(delete(BEER_API_URL_PATH_ID, ID_VALID)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteByIdWithUnregisteredId() throws Exception {
    Mockito.doNothing().when(beerService).deleteBeer(ID_INVALID);

    mockMvc.perform(delete(BEER_API_URL_PATH_ID, ID_INVALID)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteByNameWithRegisteredName() throws Exception {
    Mockito.doNothing().when(beerService).deleteBeer(NAME_VALID);

    mockMvc.perform(delete(BEER_API_URL_PATH_NAME, NAME_VALID)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  void deleteByNameWithUnregisteredName() throws Exception {
    Mockito.doNothing().when(beerService).deleteBeer(NAME_INVALID);

    mockMvc.perform(delete(BEER_API_URL_PATH_NAME, NAME_INVALID)
        .contentType(MediaType.APPLICATION_JSON))
        .andExpect(status().isNoContent());
  }

  @Test
  void updateWithValidBeer() throws Exception {
    Long idToUpdate = ID_VALID;
    var validBeer = beerDtoBuilder.id(ID_INVALID).build();
    var updatedBeer = validBeer.toBuilder().id(idToUpdate).build();

    Mockito.when(beerService.updateBeer(idToUpdate, validBeer)).thenReturn(updatedBeer);

    mockMvc.perform(put(BEER_API_URL_PATH_ID, idToUpdate)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(validBeer)))
        .andExpect(status().isOk())
        /*
        JSON path "$.id"
        Expected: is <1L>
             but: was <1>
         */
        .andExpect(jsonPath("$.id", Matchers.is((int) (long) idToUpdate)))
        .andExpect(jsonPath("$.name", Matchers.is(validBeer.getName())))
        .andExpect(jsonPath("$.brand", Matchers.is(validBeer.getBrand())))
        .andExpect(jsonPath("$.type", Matchers.is(validBeer.getType().toString())))
        .andExpect(jsonPath("$.max", Matchers.is(validBeer.getMax())))
        .andExpect(jsonPath("$.quantity", Matchers.is(validBeer.getQuantity())));
  }

  @Test
  void updateWithUnregisteredId() throws Exception {
    Long idToUpdate = ID_INVALID;
    var validBeer = beerDtoBuilder.build();

    Mockito.when(beerService.updateBeer(idToUpdate, validBeer))
        .thenThrow(BeerNotFoundException.class);

    mockMvc.perform(put(BEER_API_URL_PATH_ID, idToUpdate)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(validBeer)))
        .andExpect(status().isNotFound());
  }

  void updateWithInvalidBeers(List<BeerDto> invalidBeers) throws Exception {
    for (var beer : invalidBeers) {
      mockMvc.perform(put(BEER_API_URL_PATH_ID, ID_VALID)
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(beer)))
          .andExpect(status().isBadRequest());
    }
  }

  @Test
  void updateWithInvalidName() throws Exception {
    var invalidNameBeers = List.of(
        beerDtoBuilder.name("").build(),
        beerDtoBuilder.name("a".repeat(201)).build(),
        beerDtoBuilder.name(null).build()
    );

    updateWithInvalidBeers(invalidNameBeers);
  }

  @Test
  void updateWithInvalidBrand() throws Exception {
    var invalidBrandBeers = List.of(
        beerDtoBuilder.brand("").build(),
        beerDtoBuilder.brand("a".repeat(201)).build(),
        beerDtoBuilder.brand(null).build()
    );

    updateWithInvalidBeers(invalidBrandBeers);
  }

  @Test
  void updateWithOutOfRangeMax() throws Exception {
    var invalidMaxBeers = List.of(
        beerDtoBuilder.max(0).build(),
        beerDtoBuilder.max(501).build()
    );

    updateWithInvalidBeers(invalidMaxBeers);
  }

  @Test
  void updateWithOutOfRangeQuantity() throws Exception {
    var invalidQuantityBeers = List.of(
        beerDtoBuilder.quantity(-1).build(),
        beerDtoBuilder.quantity(101).build()
    );

    updateWithInvalidBeers(invalidQuantityBeers);
  }

  @Test
  void updateWithQuantityBiggerThanMax() throws Exception {
    var incorrectBeer = beerDtoBuilder.max(50).quantity(100).build();

    Mockito.when(beerService.updateBeer(incorrectBeer.getId(), incorrectBeer))
        .thenThrow(BeerStockExceededException.class);

    updateWithInvalidBeers(List.of(incorrectBeer));
  }

  @Test
  void incrementWithRegisteredIdAndValidQuantity() throws Exception {

  }

  @Test
  void incrementWithRegisteredIdAndInvalidQuantity() throws Exception {

  }

  @Test
  void incrementThatCauseExceedingQuantity() throws Exception {

  }

  @Test
  void decrementWithRegisteredIdAndValidQuantity() throws Exception {

  }

  @Test
  void decrementWithRegisteredIdAndInvalidQuantity() throws Exception {

  }

  @Test
  void decrementThatCauseNonExistentQuantity() throws Exception {

  }

}

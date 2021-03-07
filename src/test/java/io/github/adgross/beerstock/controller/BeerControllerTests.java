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

  private final BeerDto validBeer = new BeerDto(
      2L, "name", "brand", 400, 100, BeerType.FIRKANT);

  private List<BeerDto> getInvalidNameBeers() {
    return List.of(
        validBeer.toBuilder().name("").build(),
        validBeer.toBuilder().name("a".repeat(201)).build(),
        validBeer.toBuilder().name(null).build()
    );
  }

  private List<BeerDto> getInvalidBrandBeers() {
    return List.of(
        validBeer.toBuilder().brand("").build(),
        validBeer.toBuilder().brand("a".repeat(201)).build(),
        validBeer.toBuilder().brand(null).build()
    );
  }

  private List<BeerDto> getInvalidMaxBeers() {
    return List.of(
        validBeer.toBuilder().max(0).build(),
        validBeer.toBuilder().max(501).build()
    );
  }

  private List<BeerDto> getInvalidQuantityBeers() {
    return List.of(
        validBeer.toBuilder().quantity(-1).build(),
        validBeer.toBuilder().quantity(101).build()
    );
  }

  private List<BeerDto> getIncorrectBeers() {
    return List.of(
        validBeer.toBuilder().max(50).quantity(100).build()
    );
  }

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
    Mockito.when(beerService.createBeer(validBeer))
        .thenThrow(BeerAlreadyRegisteredException.class);

    createWithInvalidBeers(List.of(validBeer));
  }

  @Test
  void createWithInvalidName() throws Exception {
    createWithInvalidBeers(getInvalidNameBeers());
  }

  @Test
  void createWithInvalidBrand() throws Exception {
    createWithInvalidBeers(getInvalidBrandBeers());
  }

  @Test
  void createWithOutOfRangeMax() throws Exception {
    createWithInvalidBeers(getInvalidMaxBeers());
  }

  @Test
  void createWithOutOfRangeQuantity() throws Exception {
    createWithInvalidBeers(getInvalidQuantityBeers());
  }

  @Test
  void createWithQuantityBiggerThanMax() throws Exception {
    var incorrectBeers = getIncorrectBeers();
    for (var beer : incorrectBeers) {
      Mockito.when(beerService.createBeer(beer))
          .thenThrow(BeerStockExceededException.class);
    }

    createWithInvalidBeers(incorrectBeers);
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
    updateWithInvalidBeers(getInvalidNameBeers());
  }

  @Test
  void updateWithInvalidBrand() throws Exception {
    updateWithInvalidBeers(getInvalidBrandBeers());
  }

  @Test
  void updateWithOutOfRangeMax() throws Exception {
    updateWithInvalidBeers(getInvalidMaxBeers());
  }

  @Test
  void updateWithOutOfRangeQuantity() throws Exception {
    updateWithInvalidBeers(getInvalidQuantityBeers());
  }

  @Test
  void updateWithQuantityBiggerThanMax() throws Exception {
    var incorrectBeers = getIncorrectBeers();
    for (var beer : incorrectBeers) {
      Mockito.when(beerService.updateBeer(beer.getId(), beer))
          .thenThrow(BeerStockExceededException.class);
    }

    updateWithInvalidBeers(incorrectBeers);
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

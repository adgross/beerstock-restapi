package io.github.adgross.beerstock.controller;

import static io.github.adgross.beerstock.utils.JsonConvertUtils.asJsonString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.dto.QuantityDto;
import io.github.adgross.beerstock.enums.BeerType;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
import io.github.adgross.beerstock.exception.BeerStockExceededException;
import io.github.adgross.beerstock.exception.BeerStockNonExistentQuantityException;
import io.github.adgross.beerstock.services.BeerService;
import java.util.List;
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
  private static final Long ID_VALID = Long.MAX_VALUE - 100;
  private static final Long ID_INVALID = 999L;
  private static final String NAME_VALID = "valid";
  private static final String NAME_INVALID = "invalid";

  private final BeerDto validBeer = new BeerDto(
      1L, "name", "brand", 400, 100, BeerType.FIRKANT);
  private final QuantityDto validQuantity = new QuantityDto(10);
  private final QuantityDto invalidQuantity = new QuantityDto(-1);

  @MockBean
  private BeerService beerService;

  @Autowired
  private MockMvc mockMvc;

  private List<BeerDto> getValidBeers() {
    return List.of(
        new BeerDto(1L, "a", "áéíóúàèìòù ãẽĩõũâêîôûäëïöüçÇ", 400, 100, BeerType.CLASSIC),
        new BeerDto(Long.MAX_VALUE, "x".repeat(199), "y".repeat(199), 100, 1, BeerType.CIRKEL),
        new BeerDto(500L, "áéíóú àèìòùãẽĩõũâêîôûäëïöüçÇ", "e", 10, 0, BeerType.STORMEST)
    );
  }

  private List<BeerDto> getInvalidNameBeers() {
    return List.of(
        validBeer.toBuilder().name("").build(),
        validBeer.toBuilder().name("   ").build(),
        validBeer.toBuilder().name("a".repeat(201)).build(),
        validBeer.toBuilder().name(null).build(),
        validBeer.toBuilder().name("a/bc").build(),
        validBeer.toBuilder().name("a?)b").build()
    );
  }

  private List<BeerDto> getInvalidBrandBeers() {
    return List.of(
        validBeer.toBuilder().brand("").build(),
        validBeer.toBuilder().brand("   ").build(),
        validBeer.toBuilder().brand("a".repeat(201)).build(),
        validBeer.toBuilder().brand(null).build(),
        validBeer.toBuilder().brand("a/bc").build(),
        validBeer.toBuilder().brand("a%b").build()
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
        validBeer.toBuilder().max(50).quantity(100).build(),
        validBeer.toBuilder().max(10).quantity(11).build()
    );
  }

  @Test
  void listAllWithRegisteredBeers() throws Exception {
    List<BeerDto> beers = getValidBeers();

    Mockito.when(beerService.listAll()).thenReturn(beers);

    mockMvc.perform(get(BEER_API_URL_PATH))
        .andExpect(status().isOk())
        .andExpect(content().json(asJsonString(beers)));
  }

  @Test
  void listAllWithoutRegisteredBeers() throws Exception {
    List<BeerDto> emptyList = List.of();

    Mockito.when(beerService.listAll()).thenReturn(emptyList);

    mockMvc.perform(get(BEER_API_URL_PATH))
        .andExpect(status().isOk())
        .andExpect(content().json(asJsonString(emptyList)));
  }

  @Test
  void findByIdWithRegisteredId() throws Exception {
    var beer = validBeer.toBuilder().id(ID_VALID).build();

    Mockito.when(beerService.find(ID_VALID)).thenReturn(beer);

    mockMvc.perform(get(BEER_API_URL_PATH_ID, ID_VALID))
        .andExpect(status().isOk())
        .andExpect(content().json(asJsonString(beer)));
  }

  @Test
  void findByIdWithUnregisteredId() throws Exception {
    Mockito.when(beerService.find(ID_INVALID)).thenThrow(BeerNotFoundException.class);

    mockMvc.perform(get(BEER_API_URL_PATH_ID, ID_INVALID))
        .andExpect(status().isNotFound());
  }

  @Test
  void findByNameWithRegisteredName() throws Exception {
    var beer = validBeer.toBuilder().name(NAME_VALID).build();

    Mockito.when(beerService.find(NAME_VALID)).thenReturn(beer);

    mockMvc.perform(get(BEER_API_URL_PATH_NAME, NAME_VALID))
        .andExpect(status().isOk())
        .andExpect(content().json(asJsonString(beer)));
  }

  @Test
  void findByNameWithUnregisteredName() throws Exception {
    Mockito.when(beerService.find(NAME_INVALID)).thenThrow(BeerNotFoundException.class);

    mockMvc.perform(get(BEER_API_URL_PATH_NAME, NAME_INVALID))
        .andExpect(status().isNotFound());
  }

  @Test
  void createWithValidBeers() throws Exception {
    // id is managed by JPA, so we should ignore it
    for (var beer : getValidBeers()) {
      beer.setId(ID_INVALID);
      var newBeer = beer.toBuilder().id(ID_VALID).build();

      Mockito.when(beerService.createBeer(beer)).thenReturn(newBeer);

      mockMvc.perform(post(BEER_API_URL_PATH)
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(beer)))
          .andExpect(status().isCreated())
          .andExpect(content().json(asJsonString(newBeer)));
    }
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
  void updateWithValidBeers() throws Exception {
    for (var beer : getValidBeers()) {
      Long idToUpdate = ID_VALID;
      var updatedBeer = beer.toBuilder().id(idToUpdate).build();

      Mockito.when(beerService.updateBeer(idToUpdate, beer)).thenReturn(updatedBeer);

      mockMvc.perform(put(BEER_API_URL_PATH_ID, idToUpdate)
          .contentType(MediaType.APPLICATION_JSON)
          .content(asJsonString(beer)))
          .andExpect(status().isOk())
          .andExpect(content().json(asJsonString(updatedBeer)));
    }
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
      mockMvc.perform(put(BEER_API_URL_PATH_ID, beer.getId())
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
    var valueToIncrement = validQuantity;
    var beer = validBeer.toBuilder().id(ID_INVALID).build();
    var newValue = beer.getQuantity() + valueToIncrement.getQuantity();
    var incrementedBeer = beer.toBuilder().id(ID_VALID).quantity(newValue).build();

    Mockito.when(beerService.increment(ID_VALID, valueToIncrement.getQuantity()))
        .thenReturn(incrementedBeer);

    mockMvc.perform(patch(BEER_API_URL_PATH_INCREMENT, ID_VALID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(valueToIncrement)))
        .andExpect(status().isOk())
        .andExpect(content().json(asJsonString(incrementedBeer)));
  }

  @Test
  void incrementWithRegisteredIdAndInvalidQuantity() throws Exception {
    mockMvc.perform(patch(BEER_API_URL_PATH_INCREMENT, ID_VALID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(invalidQuantity)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void incrementThatCauseExceedingQuantity() throws Exception {
    Mockito.when(beerService.increment(ID_VALID, validQuantity.getQuantity()))
        .thenThrow(BeerStockExceededException.class);

    mockMvc.perform(patch(BEER_API_URL_PATH_INCREMENT, ID_VALID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(validQuantity)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void incrementWithUnregisteredId() throws Exception {
    Mockito.when(beerService.increment(ID_INVALID, validQuantity.getQuantity()))
        .thenThrow(BeerNotFoundException.class);

    mockMvc.perform(patch(BEER_API_URL_PATH_INCREMENT, ID_INVALID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(validQuantity)))
        .andExpect(status().isNotFound());
  }

  @Test
  void decrementWithRegisteredIdAndValidQuantity() throws Exception {
    var valueToDecrement = validQuantity;
    var beer = validBeer.toBuilder().id(ID_INVALID).build();
    var newValue = beer.getQuantity() + valueToDecrement.getQuantity();
    var decrementedBeer = beer.toBuilder().id(ID_VALID).quantity(newValue).build();

    Mockito.when(beerService.decrement(ID_VALID, valueToDecrement.getQuantity()))
        .thenReturn(decrementedBeer);

    mockMvc.perform(patch(BEER_API_URL_PATH_DECREMENT, ID_VALID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(valueToDecrement)))
        .andExpect(status().isOk())
        .andExpect(content().json(asJsonString(decrementedBeer)));
  }

  @Test
  void decrementWithRegisteredIdAndInvalidQuantity() throws Exception {
    mockMvc.perform(patch(BEER_API_URL_PATH_DECREMENT, ID_VALID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(invalidQuantity)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void decrementThatCauseNonExistentQuantity() throws Exception {
    Mockito.when(beerService.decrement(ID_VALID, validQuantity.getQuantity()))
        .thenThrow(BeerStockNonExistentQuantityException.class);

    mockMvc.perform(patch(BEER_API_URL_PATH_DECREMENT, ID_VALID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(validQuantity)))
        .andExpect(status().isBadRequest());
  }

  @Test
  void decrementWithUnregisteredId() throws Exception {
    Mockito.when(beerService.decrement(ID_INVALID, validQuantity.getQuantity()))
        .thenThrow(BeerNotFoundException.class);

    mockMvc.perform(patch(BEER_API_URL_PATH_DECREMENT, ID_INVALID)
        .contentType(MediaType.APPLICATION_JSON)
        .content(asJsonString(validQuantity)))
        .andExpect(status().isNotFound());
  }

}

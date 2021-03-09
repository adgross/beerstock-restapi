package io.github.adgross.beerstock.services;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atMostOnce;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.github.adgross.beerstock.entity.Beer;
import io.github.adgross.beerstock.enums.BeerType;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
import io.github.adgross.beerstock.exception.BeerStockExceededException;
import io.github.adgross.beerstock.exception.BeerStockNonExistentQuantityException;
import io.github.adgross.beerstock.mapper.BeerMapper;
import io.github.adgross.beerstock.repository.BeerRepository;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;


@ExtendWith(MockitoExtension.class)
public class BeerServiceTests {

  private static final Long ID_VALID = 1L;
  private static final Long ID_INVALID = Long.MAX_VALUE - 100;
  private static final String NAME_VALID = "valid";
  private static final String NAME_INVALID = "invalid";
  private final Beer validBeer = new Beer(
      1L, "name", "brand", 400, 100, BeerType.FIRKANT);
  private BeerMapper beerMapper = BeerMapper.INSTANCE;

  @Mock
  private BeerRepository beerRepository;

  @InjectMocks
  private BeerService beerService;

  @Test
  void listAllWithRegisteredBeers() {
    List<Beer> listBeers = List.of(
        new Beer(1L, "name1", "brand1", 100, 10, BeerType.FIRKANT),
        new Beer(2L, "name2", "brand2", 50, 5, BeerType.CIRKEL)
    );
    var expectedList = listBeers.stream()
        .map(beerMapper::toDto)
        .collect(Collectors.toList());

    when(beerRepository.findAll()).thenReturn(listBeers);

    var foundList = beerService.listAll();
    assertThat(foundList, is(not(empty())));
    assertThat(foundList, is(equalTo(expectedList)));
    verify(beerRepository, times(1)).findAll();
  }

  @Test
  void listAllWithoutRegisteredBeers() {
    List<Beer> emptyList = List.of();

    when(beerRepository.findAll()).thenReturn(emptyList);

    var foundList = beerService.listAll();
    assertThat(foundList, is(empty()));
    verify(beerRepository, times(1)).findAll();
  }

  @Test
  void findGivenRegisteredName() throws BeerNotFoundException {
    var expectedBeerDto = beerMapper.toDto(validBeer);

    when(beerRepository.findByName(NAME_VALID))
        .thenReturn(Optional.of(validBeer));

    var foundBeer = beerService.find(NAME_VALID);
    assertThat(foundBeer, is(equalTo(expectedBeerDto)));
    verify(beerRepository, times(0)).findAll();
    verify(beerRepository, times(1)).findByName(NAME_VALID);
  }

  @Test
  void findGivenNotRegisteredName() {
    when(beerRepository.findByName(NAME_INVALID)).thenReturn(Optional.empty());

    assertThrows(BeerNotFoundException.class, () -> beerService.find(NAME_INVALID));
    verify(beerRepository, times(0)).findAll();
    verify(beerRepository, times(1)).findByName(NAME_INVALID);
  }

  @Test
  void findGivenRegisteredId() throws BeerNotFoundException {
    var expectedBeerDto = beerMapper.toDto(validBeer);

    when(beerRepository.findById(ID_VALID))
        .thenReturn(Optional.of(validBeer));

    var foundBeer = beerService.find(ID_VALID);
    assertThat(foundBeer, is(equalTo(expectedBeerDto)));
    verify(beerRepository, times(0)).findAll();
    verify(beerRepository, times(1)).findById(ID_VALID);
  }

  @Test
  void findGivenNotRegisteredId() {
    when(beerRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

    assertThrows(BeerNotFoundException.class, () -> beerService.find(ID_INVALID));
    verify(beerRepository, times(0)).findAll();
    verify(beerRepository, times(1)).findById(ID_INVALID);
  }

  @Test
  void createBeerGivenValidBeer()
      throws BeerStockExceededException, BeerAlreadyRegisteredException {
    var beer = validBeer.toBuilder().id(ID_INVALID).name(NAME_VALID).build();
    var inputBeerDto = beerMapper.toDto(beer);
    var savedBeer = beer.toBuilder().id(ID_VALID).build();

    when(beerRepository.findByName(NAME_VALID)).thenReturn(Optional.empty());
    when(beerRepository.save(beer)).thenReturn(savedBeer);

    var createdBeer = beerService.createBeer(inputBeerDto);
    assertThat(createdBeer.getId(), is(equalTo(ID_VALID)));
    assertThat(createdBeer.getName(), is(equalTo(inputBeerDto.getName())));
    assertThat(createdBeer.getQuantity(), is(equalTo(inputBeerDto.getQuantity())));
    assertThat(createdBeer.getMax(), is(equalTo(inputBeerDto.getMax())));
    verify(beerRepository, times(1)).findByName(NAME_VALID);
    verify(beerRepository, times(1)).save(beer);
  }

  @Test
  void createBeerGivenAlreadyRegisteredBeer() {
    var beer = validBeer.toBuilder().name(NAME_INVALID).build();
    var inputBeerDto = beerMapper.toDto(beer);

    when(beerRepository.findByName(NAME_INVALID)).thenReturn(Optional.of(beer));

    assertThrows(BeerAlreadyRegisteredException.class, () -> beerService.createBeer(inputBeerDto));
    verify(beerRepository, times(1)).findByName(NAME_INVALID);
    verify(beerRepository, never()).save(any(Beer.class));
  }

  @Test
  // the implementation might check (quantity <= max) first, and later (if already registered)
  // we should use LENIENT here, so both implementations are valid
  @MockitoSettings(strictness = Strictness.LENIENT)
  void createBeerGivenQuantityBiggerThanMax() {
    var beer = validBeer.toBuilder().name(NAME_VALID).max(10).quantity(50).build();
    var inputBeerDto = beerMapper.toDto(beer);

    when(beerRepository.findByName(NAME_VALID)).thenReturn(Optional.empty());

    assertThrows(BeerStockExceededException.class, () -> beerService.createBeer(inputBeerDto));
    verify(beerRepository, atMostOnce()).findByName(NAME_VALID);
    verify(beerRepository, never()).save(any(Beer.class));
  }

  @Test
  // implementation might check or not if exist
  @MockitoSettings(strictness = Strictness.LENIENT)
  void deleteBeerGivenRegisteredId() {
    var beer = validBeer.toBuilder().build();

    when(beerRepository.findById(ID_VALID)).thenReturn(Optional.of(beer));
    doNothing().when(beerRepository).deleteById(ID_VALID);

    beerService.deleteBeer(ID_VALID);
    verify(beerRepository, atMostOnce()).findById(ID_VALID);
    verify(beerRepository, times(1)).deleteById(ID_VALID);
  }

  @Test
  // implementation might check or not if exist
  @MockitoSettings(strictness = Strictness.LENIENT)
  void deleteBeerGivenNotRegisteredId() {
    when(beerRepository.findById(ID_INVALID)).thenReturn(Optional.empty());
    doNothing().when(beerRepository).deleteById(ID_INVALID);

    beerService.deleteBeer(ID_INVALID);
    verify(beerRepository, atMostOnce()).findById(ID_INVALID);
    verify(beerRepository, atMostOnce()).deleteById(ID_INVALID);
  }

  @Test
  // implementation might check or not if exist
  @MockitoSettings(strictness = Strictness.LENIENT)
  void deleteBeerGivenRegisteredName() {

  }

  @Test
  // implementation might check or not if exist
  @MockitoSettings(strictness = Strictness.LENIENT)
  void deleteBeerGivenNotRegisteredName() {
    when(beerRepository.findByName(NAME_INVALID)).thenReturn(Optional.empty());
    doNothing().when(beerRepository).deleteByName(NAME_INVALID);

    beerService.deleteBeer(NAME_INVALID);
    verify(beerRepository, atMostOnce()).findByName(NAME_INVALID);
    verify(beerRepository, atMostOnce()).deleteByName(NAME_INVALID);
  }

  @Test
  void updateBeerGivenRegisteredId() {

  }

  @Test
  void updateBeerGivenNotRegisteredId() {

  }

  @Test
  void updateBeerGivenQuantityBiggerThanMax(){

  }

  @Test
  void incrementGivenRegisteredId() {

  }

  @Test
  void incrementGivenNotRegisteredId() {

  }

  @Test
  void incrementThatExceeds() {

  }

  @Test
  void decrementGivenRegisteredId() {

  }

  @Test
  void decrementGivenNotRegisteredId() {

  }

  @Test
  void decrementThatCauseNonExistentQuantity() {

  }

}

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
  private final BeerMapper beerMapper = BeerMapper.INSTANCE;

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
    var beer = validBeer.toBuilder().build();

    when(beerRepository.findByName(NAME_VALID)).thenReturn(Optional.of(beer));
    doNothing().when(beerRepository).deleteByName(NAME_VALID);

    beerService.deleteBeer(NAME_VALID);
    verify(beerRepository, atMostOnce()).findByName(NAME_VALID);
    verify(beerRepository, times(1)).deleteByName(NAME_VALID);
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
  void updateBeerGivenRegisteredId()
      throws BeerNotFoundException, BeerStockExceededException, BeerAlreadyRegisteredException {
    var beer = validBeer.toBuilder().id(null).build();
    var savedBeer = beer.toBuilder().id(ID_VALID).build();
    var foundBeer = savedBeer.toBuilder().build();
    var inputBeerDto = beerMapper.toDto(beer);

    when(beerRepository.findById(ID_VALID)).thenReturn(Optional.of(foundBeer));
    when(beerRepository.save(any(Beer.class))).thenReturn(savedBeer);

    var updatedBeer = beerService.updateBeer(ID_VALID, inputBeerDto);
    assertThat(updatedBeer.getId(), is(equalTo(ID_VALID)));
    assertThat(updatedBeer.getName(), is(equalTo(inputBeerDto.getName())));
    assertThat(updatedBeer.getQuantity(), is(equalTo(inputBeerDto.getQuantity())));
    assertThat(updatedBeer.getMax(), is(equalTo(inputBeerDto.getMax())));
    verify(beerRepository, times(1)).findById(ID_VALID);
    verify(beerRepository, times(1)).save(any(Beer.class));
    verify(beerRepository, never()).findByName(any(String.class));
  }


  @Test
  void updateBeerGivenRegisteredIdChangingTheName()
      throws BeerNotFoundException, BeerStockExceededException, BeerAlreadyRegisteredException {
    var beer = validBeer.toBuilder().id(null).build();
    var savedBeer = beer.toBuilder().id(ID_VALID).build();
    var foundBeer = savedBeer.toBuilder().name(NAME_VALID + "old").build();
    var inputBeerDto = beerMapper.toDto(beer);

    when(beerRepository.findById(ID_VALID)).thenReturn(Optional.of(foundBeer));
    when(beerRepository.findByName(inputBeerDto.getName())).thenReturn(Optional.empty());
    when(beerRepository.save(any(Beer.class))).thenReturn(savedBeer);

    var updatedBeer = beerService.updateBeer(ID_VALID, inputBeerDto);
    assertThat(updatedBeer.getId(), is(equalTo(ID_VALID)));
    assertThat(updatedBeer.getName(), is(equalTo(inputBeerDto.getName())));
    assertThat(updatedBeer.getQuantity(), is(equalTo(inputBeerDto.getQuantity())));
    assertThat(updatedBeer.getMax(), is(equalTo(inputBeerDto.getMax())));
    verify(beerRepository, times(1)).findById(ID_VALID);
    verify(beerRepository, times(1)).findByName(any(String.class));
    verify(beerRepository, times(1)).save(any(Beer.class));
  }


  @Test
  void updateBeerGivenNotRegisteredId() {
    var beer = validBeer.toBuilder().id(null).build();
    var inputBeerDto = beerMapper.toDto(beer);

    when(beerRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

    assertThrows(BeerNotFoundException.class,
        () -> beerService.updateBeer(ID_INVALID, inputBeerDto));
    verify(beerRepository, times(1)).findById(ID_INVALID);
    verify(beerRepository, never()).findByName(any(String.class));
    verify(beerRepository, never()).save(any(Beer.class));
  }

  @Test
  void updateBeerGivenQuantityBiggerThanMax() {
    var beer = validBeer.toBuilder().id(null).max(50).quantity(200).build();
    var inputBeerDto = beerMapper.toDto(beer);

    assertThrows(BeerStockExceededException.class,
        () -> beerService.updateBeer(ID_VALID, inputBeerDto));
    verify(beerRepository, never()).findById(any(Long.class));
    verify(beerRepository, never()).findByName(any(String.class));
    verify(beerRepository, never()).save(any(Beer.class));
  }

  @Test
  void updateBeerWhenBeerNameAlreadyExist() {
    var duplicateName = NAME_VALID;
    var beer = validBeer.toBuilder().id(null).name(duplicateName).build();
    var inputBeerDto = beerMapper.toDto(beer);
    var inputId = ID_VALID;
    var foundByIdBeer = validBeer.toBuilder().id(inputId).name(NAME_VALID + "old").build();
    var sameNameBeer = validBeer.toBuilder().id(ID_VALID + 1).name(duplicateName).build();

    when(beerRepository.findById(inputId)).thenReturn(Optional.of(foundByIdBeer));
    when(beerRepository.findByName(duplicateName)).thenReturn(Optional.of(sameNameBeer));

    assertThrows(BeerAlreadyRegisteredException.class,
        () -> beerService.updateBeer(inputId, inputBeerDto));
    verify(beerRepository, times(1)).findById(inputId);
    verify(beerRepository, times(1)).findByName(duplicateName);
    verify(beerRepository, never()).save(any(Beer.class));
  }

  @Test
  void incrementGivenRegisteredId() throws BeerStockExceededException, BeerNotFoundException {
    int inputQuantity = 10;
    var beer = validBeer;
    var oldQuantity = beer.getQuantity();
    var savedBeer = beer.toBuilder().quantity(oldQuantity + inputQuantity).build();

    when(beerRepository.findById(ID_VALID)).thenReturn(Optional.of(beer));
    when(beerRepository.save(any(Beer.class))).thenReturn(savedBeer);

    var incrementedBeer = beerService.increment(ID_VALID, inputQuantity);
    assertThat(incrementedBeer.getId(), is(equalTo(ID_VALID)));
    assertThat(incrementedBeer.getName(), is(equalTo(beer.getName())));
    assertThat(incrementedBeer.getMax(), is(equalTo(beer.getMax())));
    assertThat(incrementedBeer.getQuantity(), is(equalTo(oldQuantity + inputQuantity)));
    verify(beerRepository, times(1)).findById(ID_VALID);
    verify(beerRepository, times(1)).save(any(Beer.class));
  }

  @Test
  void incrementGivenNotRegisteredId() {
    int inputQuantity = 10;

    when(beerRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

    assertThrows(BeerNotFoundException.class,
        () -> beerService.increment(ID_INVALID, inputQuantity));
    verify(beerRepository, times(1)).findById(ID_INVALID);
    verify(beerRepository, never()).save(any(Beer.class));
  }

  @Test
  void incrementThatExceeds() {
    int inputQuantity = 2;
    var beer = validBeer.toBuilder()
        .max(Integer.MAX_VALUE)
        .quantity(Integer.MAX_VALUE - 1).build();

    when(beerRepository.findById(ID_VALID)).thenReturn(Optional.of(beer));

    assertThrows(BeerStockExceededException.class,
        () -> beerService.increment(ID_VALID, inputQuantity));
    verify(beerRepository, times(1)).findById(ID_VALID);
    verify(beerRepository, never()).save(any(Beer.class));
  }

  @Test
  void decrementGivenRegisteredId()
      throws BeerNotFoundException, BeerStockNonExistentQuantityException {
    int inputQuantity = 10;
    var beer = validBeer;
    var oldQuantity = beer.getQuantity();
    var savedBeer = beer.toBuilder().quantity(oldQuantity - inputQuantity).build();

    when(beerRepository.findById(ID_VALID)).thenReturn(Optional.of(beer));
    when(beerRepository.save(any(Beer.class))).thenReturn(savedBeer);

    var decrementedBeer = beerService.decrement(ID_VALID, inputQuantity);
    assertThat(decrementedBeer.getId(), is(equalTo(ID_VALID)));
    assertThat(decrementedBeer.getName(), is(equalTo(beer.getName())));
    assertThat(decrementedBeer.getMax(), is(equalTo(beer.getMax())));
    assertThat(decrementedBeer.getQuantity(), is(equalTo(oldQuantity - inputQuantity)));
    verify(beerRepository, times(1)).findById(ID_VALID);
    verify(beerRepository, times(1)).save(any(Beer.class));
  }

  @Test
  void decrementGivenNotRegisteredId() {
    int inputQuantity = 10;

    when(beerRepository.findById(ID_INVALID)).thenReturn(Optional.empty());

    assertThrows(BeerNotFoundException.class,
        () -> beerService.decrement(ID_INVALID, inputQuantity));
    verify(beerRepository, times(1)).findById(ID_INVALID);
    verify(beerRepository, never()).save(any(Beer.class));
  }

  @Test
  void decrementThatCauseNonExistentQuantity() {
    int inputQuantity = Integer.MAX_VALUE;
    var beer = validBeer.toBuilder()
        .max(Integer.MAX_VALUE - 1)
        .quantity(Integer.MAX_VALUE - 1).build();

    when(beerRepository.findById(ID_VALID)).thenReturn(Optional.of(beer));

    assertThrows(BeerStockNonExistentQuantityException.class,
        () -> beerService.decrement(ID_VALID, inputQuantity));
    verify(beerRepository, times(1)).findById(ID_VALID);
    verify(beerRepository, never()).save(any(Beer.class));
  }

}

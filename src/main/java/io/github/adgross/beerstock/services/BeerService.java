package io.github.adgross.beerstock.services;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.entity.Beer;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
import io.github.adgross.beerstock.exception.BeerStockExceededException;
import io.github.adgross.beerstock.mapper.BeerMapper;
import io.github.adgross.beerstock.repository.BeerRepository;
import java.util.List;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor(onConstructor = @__({@Autowired}))
public class BeerService {

  private final BeerRepository beerRepository;
  private final BeerMapper beerMapper = BeerMapper.INSTANCE;

  public List<BeerDto> listAll() {
    return beerRepository.findAll()
        .stream()
        .map(beerMapper::toDto)
        .collect(Collectors.toList());
  }

  public BeerDto find(String name) throws BeerNotFoundException {
    return beerMapper.toDto(findBeer(name));
  }

  public BeerDto find(Long id) throws BeerNotFoundException {
    return beerMapper.toDto(findBeer(id));
  }

  public BeerDto createBeer(BeerDto beerDto) throws BeerAlreadyRegisteredException {
    String name = beerDto.getName();
    if (!isRegistered(name)) {
      Beer beer = beerMapper.toModel(beerDto);
      Beer savedBeer = beerRepository.save(beer);
      return beerMapper.toDto(savedBeer);
    } else {
      throw new BeerAlreadyRegisteredException(name);
    }
  }

  public void deleteBeer(Long id) throws BeerNotFoundException {
    find(id);
    beerRepository.deleteById(id);
  }

  @Transactional
  public void deleteBeer(String name) throws BeerNotFoundException {
    find(name);
    beerRepository.deleteByName(name);
  }

  public BeerDto increment(Long id, int quantity)
      throws BeerStockExceededException, BeerNotFoundException {
    Beer beerToIncrement = findBeer(id);
    if (beerToIncrement.getQuantity() <= beerToIncrement.getMax() - quantity) {
      beerToIncrement.setQuantity(beerToIncrement.getQuantity() + quantity);
      Beer incrementedBeer = beerRepository.save(beerToIncrement);
      return beerMapper.toDto(incrementedBeer);
    }
    throw new BeerStockExceededException(id, quantity);
  }

  private Beer findBeer(Long id) throws BeerNotFoundException {
    return beerRepository.findById(id)
        .orElseThrow(() -> new BeerNotFoundException(id));
  }

  private Beer findBeer(String name) throws BeerNotFoundException {
    return beerRepository.findByName(name)
        .orElseThrow(() -> new BeerNotFoundException(name));
  }

  private boolean isRegistered(String name) {
    return beerRepository.findByName(name).isPresent();
  }
}
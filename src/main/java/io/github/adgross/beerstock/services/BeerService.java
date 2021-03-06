package io.github.adgross.beerstock.services;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.entity.Beer;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
import io.github.adgross.beerstock.exception.BeerStockExceededException;
import io.github.adgross.beerstock.exception.BeerStockNonExistentQuantityException;
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

  public void deleteBeer(Long id) {
    if (isRegistered(id)) {
      beerRepository.deleteById(id);
    }
  }

  @Transactional
  public void deleteBeer(String name) {
    if (isRegistered(name)) {
      beerRepository.deleteByName(name);
    }
  }

  public BeerDto updateBeer(Long id, BeerDto beerDto) throws BeerNotFoundException {
    Beer beerOld = findBeer(id);
    Beer beerNew = beerMapper.toModel(beerDto);
    beerNew.setId(beerOld.getId());
    Beer savedBeer = beerRepository.save(beerNew);
    return beerMapper.toDto(savedBeer);
  }

  public BeerDto increment(Long id, int quantity)
      throws BeerStockExceededException, BeerNotFoundException {
    Beer beerToIncrement = findBeer(id);
    if (beerToIncrement.getQuantity() <= beerToIncrement.getMax() - quantity) {
      beerToIncrement.setQuantity(beerToIncrement.getQuantity() + quantity);
      Beer incrementedBeer = beerRepository.save(beerToIncrement);
      return beerMapper.toDto(incrementedBeer);
    }
    throw new BeerStockExceededException(id, quantity, beerToIncrement.getMax());
  }

  public BeerDto decrement(Long id, int quantity)
      throws BeerStockNonExistentQuantityException, BeerNotFoundException {
    Beer beerToDecrement = findBeer(id);
    if (beerToDecrement.getQuantity() >= quantity) {
      beerToDecrement.setQuantity(beerToDecrement.getQuantity() - quantity);
      Beer decrementedBeer = beerRepository.save(beerToDecrement);
      return beerMapper.toDto(decrementedBeer);
    }
    throw new BeerStockNonExistentQuantityException(id, quantity);
  }

  private Beer findBeer(Long id) throws BeerNotFoundException {
    return beerRepository.findById(id)
        .orElseThrow(() -> new BeerNotFoundException(id));
  }

  private Beer findBeer(String name) throws BeerNotFoundException {
    return beerRepository.findByName(name)
        .orElseThrow(() -> new BeerNotFoundException(name));
  }

  private boolean isRegistered(Long id) {
    return beerRepository.findById(id).isPresent();
  }

  private boolean isRegistered(String name) {
    return beerRepository.findByName(name).isPresent();
  }
}

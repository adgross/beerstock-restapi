package io.github.adgross.beerstock.controller;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
import io.github.adgross.beerstock.exception.BeerStockExceededException;
import io.github.adgross.beerstock.exception.BeerStockNonExistentQuantityException;
import io.github.adgross.beerstock.services.BeerService;
import java.util.List;
import javax.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/beers")
@AllArgsConstructor(onConstructor = @__(@Autowired))
public class BeerController implements BeerControllerApi {

  private final BeerService beerService;

  @GetMapping
  public List<BeerDto> listAll() {
    return beerService.listAll();
  }

  @GetMapping("/{id}")
  public BeerDto findById(@PathVariable Long id) throws BeerNotFoundException {
    return beerService.find(id);
  }

  @GetMapping("/name/{name}")
  public BeerDto findByName(@PathVariable String name) throws BeerNotFoundException {
    return beerService.find(name);
  }

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BeerDto createBeer(@RequestBody @Valid BeerDto beerDto)
      throws BeerAlreadyRegisteredException, BeerStockExceededException {
    return beerService.createBeer(beerDto);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteById(@PathVariable Long id) {
    beerService.deleteBeer(id);
  }

  @DeleteMapping("/name/{name}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteByName(@PathVariable String name) {
    beerService.deleteBeer(name);
  }

  @PutMapping("/{id}")
  public BeerDto updateBeer(Long id, @RequestBody @Valid BeerDto beerDto)
      throws BeerNotFoundException {
    return beerService.updateBeer(id, beerDto);
  }

  @PatchMapping("/{id}/increment")
  public BeerDto increment(@PathVariable Long id, int quantity)
      throws BeerStockExceededException, BeerNotFoundException {
    return beerService.increment(id, quantity);
  }

  @PatchMapping("/{id}/decrement")
  public BeerDto decrement(@PathVariable Long id, int quantity)
      throws BeerStockNonExistentQuantityException, BeerNotFoundException {
    return beerService.decrement(id, quantity);
  }
}

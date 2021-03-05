package io.github.adgross.beerstock.controller;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
import io.github.adgross.beerstock.services.BeerService;
import java.util.List;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public BeerDto createBeer(BeerDto beerDto) throws BeerAlreadyRegisteredException {
    return beerService.createBeer(beerDto);
  }

  @GetMapping("/{id}")
  public BeerDto findById(@PathVariable Long id) throws BeerNotFoundException {
    return beerService.find(id);
  }

  @GetMapping("/name/{name}")
  public BeerDto findByName(@PathVariable String name) throws BeerNotFoundException {
    return beerService.find(name);
  }

  @DeleteMapping("/{id}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteById(@PathVariable Long id) throws BeerNotFoundException {
    beerService.deleteBeer(id);
  }

  @DeleteMapping("/name/{name}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteByName(@PathVariable String name) throws BeerNotFoundException {
    beerService.deleteBeer(name);
  }
}
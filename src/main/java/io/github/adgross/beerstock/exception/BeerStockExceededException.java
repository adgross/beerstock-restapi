package io.github.adgross.beerstock.exception;

import io.github.adgross.beerstock.dto.BeerDto;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockExceededException extends Exception {

  public BeerStockExceededException(Long id, int quantityToIncrement, int limit) {
    super(String.format("Adding %s beers to Beer ID(%s) exceed its max stock capacity(%s)",
        quantityToIncrement, id, limit));
  }

  public BeerStockExceededException(BeerDto beerDto) {
    super(String.format("Beer with quantity(%s) exceed its max stock capacity (%s)",
        beerDto.getQuantity(), beerDto.getMax()));
  }

}
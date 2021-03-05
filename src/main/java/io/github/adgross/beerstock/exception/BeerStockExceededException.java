package io.github.adgross.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockExceededException extends Exception {

  public BeerStockExceededException(Long id, int quantityToIncrement) {
    super(String.format("Adding %s beers to Beer ID(%s) exceed its stock capacity",
        quantityToIncrement, id));
  }
}
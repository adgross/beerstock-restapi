package io.github.adgross.beerstock.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BeerStockNonExistentQuantityException extends Exception {

  public BeerStockNonExistentQuantityException(Long id, int quantityToDecrement) {
    super(String.format("Removing %s beers from Beer ID(%s) leads to non existent quantity",
        quantityToDecrement, id));
  }
}
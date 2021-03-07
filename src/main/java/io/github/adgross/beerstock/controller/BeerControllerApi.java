package io.github.adgross.beerstock.controller;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
import io.github.adgross.beerstock.exception.BeerStockExceededException;
import io.github.adgross.beerstock.exception.BeerStockNonExistentQuantityException;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;

@Api("Manages beer stock")
public interface BeerControllerApi {

  @ApiOperation(value = "List all beers")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "List of all registered beers"),
  })
  List<BeerDto> listAll();

  @ApiOperation(value = "Beer creation operation")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "201", description = "Beer created"),
      @ApiResponse(responseCode = "400",
          description = "Missing required fields or out of range values")
  })
  BeerDto createBeer(BeerDto beerDto)
      throws BeerAlreadyRegisteredException, BeerStockExceededException;

  @ApiOperation(value = "Return a beer by the given id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Beer found"),
      @ApiResponse(responseCode = "404", description = "Beer with given id not found")
  })
  BeerDto findById(@PathVariable Long id) throws BeerNotFoundException;

  @ApiOperation(value = "Return a beer by the given name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Beer found"),
      @ApiResponse(responseCode = "404", description = "Beer with given name not found")
  })
  BeerDto findByName(@PathVariable String name) throws BeerNotFoundException;

  @ApiOperation(value = "Delete a beer by the given id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Beer has been deleted")
  })
  void deleteById(@PathVariable Long id);

  @ApiOperation(value = "Delete a beer by the given name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Beer has been deleted")
  })
  void deleteByName(@PathVariable String name);

  @ApiOperation(value = "Update a beer")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Beer updated"),
      @ApiResponse(responseCode = "400",
          description = "Missing required fields or out of range values"),
      @ApiResponse(responseCode = "404", description = "Beer with given id not found")
  })
  BeerDto updateBeer(@PathVariable Long id, BeerDto beerDto)
      throws BeerNotFoundException;

  @ApiOperation(value = "Increment the beer quantity in stock")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Beer quantity incremented"),
      @ApiResponse(responseCode = "400",
          description = "Increment value lead to invalid quantity"),
      @ApiResponse(responseCode = "404", description = "Beer with given id not found")
  })
  BeerDto increment(@PathVariable Long id, int quantity)
      throws BeerStockExceededException, BeerNotFoundException;

  @ApiOperation(value = "Decrement the beer quantity in stock")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Beer quantity decremented"),
      @ApiResponse(responseCode = "400",
          description = "Decrement value lead to invalid quantity"),
      @ApiResponse(responseCode = "404", description = "Beer with given id not found")
  })
  BeerDto decrement(@PathVariable Long id, int quantity)
      throws BeerStockNonExistentQuantityException, BeerNotFoundException;

}

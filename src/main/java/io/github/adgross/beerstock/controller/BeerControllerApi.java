package io.github.adgross.beerstock.controller;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.exception.BeerAlreadyRegisteredException;
import io.github.adgross.beerstock.exception.BeerNotFoundException;
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
      @ApiResponse(responseCode = "201", description = "Success beer found in the system"),
      @ApiResponse(responseCode = "400",
          description = "Missing required fields or out of range values")
  })
  BeerDto createBeer(BeerDto beerDto) throws BeerAlreadyRegisteredException;

  @ApiOperation(value = "Return a beer by the given id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Success beer found in the system"),
      @ApiResponse(responseCode = "404", description = "Beer with given id not found.")
  })
  BeerDto findById(@PathVariable Long id) throws BeerNotFoundException;

  @ApiOperation(value = "Return a beer by the given name")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "200", description = "Success beer found in the system"),
      @ApiResponse(responseCode = "404", description = "Beer with given name not found.")
  })
  BeerDto findByName(@PathVariable String name) throws BeerNotFoundException;

  @ApiOperation(value = "Delete a beer by the given id")
  @ApiResponses(value = {
      @ApiResponse(responseCode = "204", description = "Success beer deleted in the system"),
      @ApiResponse(responseCode = "404", description = "Beer with given id not found.")
  })
  void deleteById(@PathVariable Long id) throws BeerNotFoundException;
}

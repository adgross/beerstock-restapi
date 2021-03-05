package io.github.adgross.beerstock.mapper;

import io.github.adgross.beerstock.dto.BeerDto;
import io.github.adgross.beerstock.entity.Beer;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface BeerMapper {

  BeerMapper INSTANCE = Mappers.getMapper(BeerMapper.class);

  Beer toModel(BeerDto beerDto);

  BeerDto toDto(Beer beer);
}

package io.github.adgross.beerstock.dto;

import io.github.adgross.beerstock.enums.BeerType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Max;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BeerDto {

  private Long id;

  @NotNull
  @Size(min = 1, max = 200)
  private String name;

  @NotNull
  @Size(min = 1, max = 200)
  private String brand;

  @NotNull
  @Max(500)
  private int max;

  @NotNull
  @Max(100)
  private int quantity;

  @Enumerated(EnumType.STRING)
  @NotNull
  private BeerType type;
}
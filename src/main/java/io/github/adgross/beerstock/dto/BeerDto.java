package io.github.adgross.beerstock.dto;

import io.github.adgross.beerstock.enums.BeerType;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import javax.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class BeerDto {

  private Long id;

  @NotBlank
  @Size(min = 1, max = 200)
  @Pattern(regexp = "^[A-Za-z0-9áéíóúàèìòùãẽĩõũâêîôûäëïöüçÇ\\s]*$")
  private String name;

  @NotBlank
  @Size(min = 1, max = 200)
  @Pattern(regexp = "^[A-Za-z0-9áéíóúàèìòùãẽĩõũâêîôûäëïöüçÇ\\s]*$")
  private String brand;

  @NotNull
  @Positive
  @Min(1)
  @Max(500)
  private int max;

  @NotNull
  @PositiveOrZero
  @Min(0)
  @Max(100)
  private int quantity;

  @Enumerated(EnumType.STRING)
  @NotNull
  private BeerType type;
}
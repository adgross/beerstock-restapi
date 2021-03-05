package io.github.adgross.beerstock.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BeerType {

  CLASSIC("Classic"),
  CIRKEL("Cirkel"),
  FIRKANT("Firkant"),
  JAVA("Java"),
  ORIGINAL("Original"),
  KORN("Korn"),
  STORMEST("Stormest"),
  VAR("VAR");

  private final String description;
}

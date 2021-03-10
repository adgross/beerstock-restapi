package io.github.adgross.beerstock.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum BeerType {

  LAGER("Lager"),
  ALE("Ale"),
  IPA("IPA"),
  PILSEN("Pilsen"),
  STOUT("Stout"),
  VIENNA("Vienna"),
  WEISS("Weiss"),
  PORTER("Porter"),
  WITBIER("Witbier"),
  TRIPEL("Tripel");

  private final String description;
}

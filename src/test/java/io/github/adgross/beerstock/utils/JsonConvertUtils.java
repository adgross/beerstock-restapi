package io.github.adgross.beerstock.utils;

import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonConvertUtils {

  public static String asJsonString(final Object obj) {
    try {
      return new ObjectMapper().writeValueAsString(obj);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}


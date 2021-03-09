package io.github.adgross.beerstock.repository;

import io.github.adgross.beerstock.entity.Beer;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BeerRepository extends JpaRepository<Beer, Long> {

  Optional<Beer> findByName(String name);

  void deleteByName(String name);
}

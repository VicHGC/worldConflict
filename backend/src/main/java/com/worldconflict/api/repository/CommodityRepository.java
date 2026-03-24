package com.worldconflict.api.repository;

import com.worldconflict.api.entity.Commodity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface CommodityRepository extends JpaRepository<Commodity, Long> {
    Optional<Commodity> findBySymbol(String symbol);
    List<Commodity> findTop10ByOrderByChangePercentDesc();
    List<Commodity> findAllByOrderBySymbolAsc();
}

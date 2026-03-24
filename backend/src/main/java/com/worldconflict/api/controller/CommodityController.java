package com.worldconflict.api.controller;

import com.worldconflict.api.entity.Commodity;
import com.worldconflict.api.entity.CommodityPriceHistory;
import com.worldconflict.api.service.CommodityPriceHistoryService;
import com.worldconflict.api.service.CommodityService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/commodities")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class CommodityController {
    
    private final CommodityService commodityService;
    private final CommodityPriceHistoryService priceHistoryService;
    
    @GetMapping
    public ResponseEntity<List<Commodity>> getAllCommodities() {
        return ResponseEntity.ok(commodityService.getAllCommodities());
    }
    
    @GetMapping("/top")
    public ResponseEntity<List<Commodity>> getTopCommodities() {
        return ResponseEntity.ok(commodityService.getTopCommodities());
    }
    
    @GetMapping("/{symbol}/history")
    public ResponseEntity<List<CommodityPriceHistory>> getPriceHistory(
            @PathVariable String symbol,
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(priceHistoryService.getHistory(symbol, days));
    }
    
    @GetMapping("/history/all")
    public ResponseEntity<Map<String, List<CommodityPriceHistory>>> getAllPriceHistory(
            @RequestParam(defaultValue = "7") int days) {
        return ResponseEntity.ok(priceHistoryService.getAllHistory(days));
    }
}

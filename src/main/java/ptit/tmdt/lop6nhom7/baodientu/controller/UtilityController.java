package ptit.tmdt.lop6nhom7.baodientu.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;
import ptit.tmdt.lop6nhom7.baodientu.dto.utility.ExchangeRateDto;
import ptit.tmdt.lop6nhom7.baodientu.dto.utility.UtilityDataEnvelope;
import ptit.tmdt.lop6nhom7.baodientu.service.ExchangeRateService;
import ptit.tmdt.lop6nhom7.baodientu.service.OptionalUtilityProviderService;

@RestController
@RequestMapping("/api/utilities")
@RequiredArgsConstructor
public class UtilityController {
    private final ExchangeRateService exchangeRateService;
    private final OptionalUtilityProviderService optionalProviderService;

    @GetMapping("/finance/exchange-rates")
    public UtilityDataEnvelope<List<ExchangeRateDto>> exchangeRates() {
        return exchangeRateService.getRates();
    }


    @GetMapping("/finance/gold")
    public Object gold() {
        return optionalProviderService.getGold();
    }

    @GetMapping("/finance/gold/test")
    public Object testGold() {
        try {
            return optionalProviderService.getGold();
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return java.util.Map.of("error", e.toString(), "stackTrace", sw.toString());
        }
    }
}

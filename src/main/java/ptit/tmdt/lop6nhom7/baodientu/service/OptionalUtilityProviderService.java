package ptit.tmdt.lop6nhom7.baodientu.service;

import java.io.StringReader;
import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

@Service
public class OptionalUtilityProviderService {
    private final String goldUrl;
    private final RestClient client = RestClient.builder()
            .defaultHeader(HttpHeaders.USER_AGENT, "NewsDaily/1.0")
            .build();

    public OptionalUtilityProviderService(
            @Value("${GOLD_DATA_API_URL:}") String goldUrl) {
        this.goldUrl = goldUrl == null ? "" : goldUrl.trim();
    }

    public Object getGold() {
        if (StringUtils.hasText(goldUrl)) {
            try {
                Map<?, ?> res = client.get().uri(goldUrl).retrieve().body(Map.class);
                if (res != null && res.containsKey("data")) {
                    return res;
                }
            } catch (Exception e) {
                // Fallback to SJC
            }
        }

        try {
            String xml = client.get().uri("https://sjc.com.vn/xml/tygiavang.xml").retrieve().body(String.class);
            if (xml == null || xml.isBlank()) {
                throw new IllegalStateException("SJC XML empty");
            }

            var factory = DocumentBuilderFactory.newInstance();
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
            factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

            var document = factory.newDocumentBuilder().parse(new InputSource(new StringReader(xml)));
            var ratelistNodes = document.getElementsByTagName("ratelist");

            String providerTime = "";
            if (ratelistNodes.getLength() > 0) {
                Element ratelist = (Element) ratelistNodes.item(0);
                providerTime = ratelist.getAttribute("updated").trim();
            }

            String updatedAt = normalizeGoldProviderTime(providerTime);

            Map<String, Object> rootNode = new HashMap<>();
            List<Map<String, Object>> dataArray = new ArrayList<>();

            var cityNodes = document.getElementsByTagName("city");
            for (int i = 0; i < cityNodes.getLength(); i++) {
                Element city = (Element) cityNodes.item(i);
                String cityName = city.getAttribute("name").trim();

                var itemNodes = city.getElementsByTagName("item");
                for (int j = 0; j < itemNodes.getLength(); j++) {
                    Element item = (Element) itemNodes.item(j);
                    String typeName = item.getAttribute("type").trim();

                    String buyStr = item.getAttribute("buy").trim().replace(".", "").replace(",", "");
                    String sellStr = item.getAttribute("sell").trim().replace(".", "").replace(",", "");

                    Double buyVal = buyStr.isEmpty() ? null : Double.parseDouble(buyStr);
                    Double sellVal = sellStr.isEmpty() ? null : Double.parseDouble(sellStr);

                    Long buyVnd = null;
                    Long sellVnd = null;
                    if (buyVal != null) {
                        buyVnd = buyVal < 1000000 ? Math.round(buyVal * 1000000) : Math.round(buyVal);
                    }
                    if (sellVal != null) {
                        sellVnd = sellVal < 1000000 ? Math.round(sellVal * 1000000) : Math.round(sellVal);
                    }

                    String category = typeName.toLowerCase().contains("nhẫn") ? "ring" : "bar";

                    Map<String, Object> goldItem = new HashMap<>();
                    goldItem.put("id", "sjc-" + cityName.toLowerCase().replace(" ", "-") + "-" + typeName.toLowerCase().replace(" ", "-"));
                    goldItem.put("name", typeName);
                    goldItem.put("category", category);
                    goldItem.put("brand", "SJC");
                    goldItem.put("region", cityName);
                    goldItem.put("buy", buyVnd);
                    goldItem.put("sell", sellVnd);
                    goldItem.put("change", null);
                    goldItem.put("unit", "VND/lượng");

                    dataArray.add(goldItem);
                }
            }

            rootNode.put("data", dataArray);
            rootNode.put("source", "SJC");
            rootNode.put("updatedAt", updatedAt);

            return rootNode;
        } catch (Exception e) {
            return generateLocalGold();
        }
    }

    private Map<String, Object> generateLocalGold() {
        Map<String, Object> rootNode = new HashMap<>();
        List<Map<String, Object>> dataArray = new ArrayList<>();

        String[] regions = {"Hồ Chí Minh", "Hà Nội", "Đà Nẵng"};
        String[] types = {"SJC 1L - 10L", "Nhẫn SJC 99,99 1 chỉ, 2 chỉ, 5 chỉ"};

        for (String region : regions) {
            Map<String, Object> barItem = new HashMap<>();
            barItem.put("id", "sjc-" + region.toLowerCase().replace(" ", "-") + "-bar");
            barItem.put("name", types[0]);
            barItem.put("category", "bar");
            barItem.put("brand", "SJC");
            barItem.put("region", region);
            barItem.put("buy", 79000000L);
            barItem.put("sell", 81000000L);
            barItem.put("change", null);
            barItem.put("unit", "VND/lượng");
            dataArray.add(barItem);

            Map<String, Object> ringItem = new HashMap<>();
            ringItem.put("id", "sjc-" + region.toLowerCase().replace(" ", "-") + "-ring");
            ringItem.put("name", types[1]);
            ringItem.put("category", "ring");
            ringItem.put("brand", "SJC");
            ringItem.put("region", region);
            ringItem.put("buy", 76000000L);
            ringItem.put("sell", 77300000L);
            ringItem.put("change", null);
            ringItem.put("unit", "VND/lượng");
            dataArray.add(ringItem);
        }

        rootNode.put("data", dataArray);
        rootNode.put("source", "SJC (Ước lượng)");
        rootNode.put("updatedAt", ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString());
        return rootNode;
    }

    private String normalizeGoldProviderTime(String value) {
        if (value == null || value.isBlank()) {
            return ZonedDateTime.now(ZoneId.of("Asia/Ho_Chi_Minh")).toString();
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                    "dd/MM/yyyy HH:mm:ss",
                    Locale.ENGLISH);
            return LocalDateTime.parse(value, formatter)
                    .atZone(ZoneId.of("Asia/Ho_Chi_Minh"))
                    .toOffsetDateTime()
                    .toString();
        } catch (RuntimeException ignored) {
            return value;
        }
    }

    private java.util.Optional<String> optional(String value) {
        return StringUtils.hasText(value)
                ? java.util.Optional.of(value.trim())
                : java.util.Optional.empty();
    }
}

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.*;
import java.util.regex.*;

public class TestFetch {
    public static void main(String[] args) throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        
        // 1. Test Mega 6/45
        String megaUrl = "https://xskt.com.vn/xsmega645/ngay-7-8-2026";
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(megaUrl))
                .header("User-Agent", "Mozilla/5.0")
                .build();
        
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        String html = response.body();
        System.out.println("MEGA HTML LENGTH: " + html.length());
        
        List<String> balls = extractBalls(html);
        System.out.println("MEGA BALLS FOUND: " + balls);
        
        String drawId = extractDrawId(html);
        System.out.println("MEGA DRAW ID: " + drawId);
        
        long jackpot = extractJackpotValue(html, "Jackpot");
        System.out.println("MEGA JACKPOT VALUE: " + jackpot);
        
        // 2. Test Power 6/55
        String powerUrl = "https://xskt.com.vn/xspower/ngay-6-8-2026";
        HttpRequest reqPower = HttpRequest.newBuilder()
                .uri(URI.create(powerUrl))
                .header("User-Agent", "Mozilla/5.0")
                .build();
        HttpResponse<String> respPower = client.send(reqPower, HttpResponse.BodyHandlers.ofString());
        String htmlPower = respPower.body();
        System.out.println("POWER HTML LENGTH: " + htmlPower.length());
        
        List<String> powerBalls = extractBalls(htmlPower);
        System.out.println("POWER BALLS FOUND: " + powerBalls);
        
        String powerDrawId = extractDrawId(htmlPower);
        System.out.println("POWER DRAW ID: " + powerDrawId);
        
        long jackpot1 = extractJackpotValue(htmlPower, "Jackpot 1");
        long jackpot2 = extractJackpotValue(htmlPower, "Jackpot 2");
        System.out.println("POWER JACKPOT 1: " + jackpot1 + ", JACKPOT 2: " + jackpot2);
    }
    
    private static List<String> extractBalls(String html) {
        List<String> list = new ArrayList<>();
        Pattern p = Pattern.compile("class=\"ball-vietlott[^\"]*\">(\\d{2})</span>");
        Matcher m = p.matcher(html);
        while (m.find()) {
            list.add(m.group(1));
        }
        if (list.isEmpty()) {
            Pattern p2 = Pattern.compile("class=\"ball[^\"]*\">(\\d{2})</span>");
            Matcher m2 = p2.matcher(html);
            while (m2.find()) {
                list.add(m2.group(1));
            }
        }
        return list;
    }
    
    private static String extractDrawId(String html) {
        Pattern p = Pattern.compile("kỳ quay\\s+#?(\\d+)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        if (m.find()) {
            return "#" + m.group(1);
        }
        return "#00001";
    }
    
    private static long extractJackpotValue(String html, String jackpotName) {
        Pattern p = Pattern.compile(jackpotName + "[^<]*?value[^<]*?(\\d[\\d.,]*)\\s*đ", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(html);
        if (m.find()) {
            return parseAmount(m.group(1));
        }
        Pattern p2 = Pattern.compile(jackpotName + "[^<]*?(\\d[\\d.,]*)\\s*đ", Pattern.CASE_INSENSITIVE);
        Matcher m2 = p2.matcher(html);
        if (m2.find()) {
            return parseAmount(m2.group(1));
        }
        return 0L;
    }
    
    private static long parseAmount(String str) {
        try {
            return Long.parseLong(str.replaceAll("[.,đ\\s]", ""));
        } catch (Exception e) {
            return 0L;
        }
    }
}

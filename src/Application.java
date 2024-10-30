import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;


public class Application {
    private static final String siteURl = "https://api.weather.yandex.ru/v2/forecast";

    public static void main(String[] args) {
        String[][] params = new String[args.length][2];
        params[0][0] = "lat";
        params[0][1] = args[0];
        params[1][0] = "lon";
        params[1][1] = args[1];
        int limit = 0;
        if (args.length > 2) {
            params[2][0] = "limit";
            params[2][1] = args[2];
            limit = Integer.parseInt(args[2]);
        }
        if (limit > 11) {
            System.out.println("Слишком большое значение limit");
            return;
        }
        String taskUrl = formatQuery(siteURl, params);
        String apiKey = System.getenv("YOUR_API_KEY");
        sendRequest(taskUrl, apiKey, limit);
    }

    private static void sendRequest(String URL, String apiKey, int limit) {
        URI uri = URI.create(URL);
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .GET()
                .header("X-Yandex-Weather-Key", apiKey)
                .build();
        double total = 0;
        double count = 1;
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();

            System.out.println("Тело ответа:");
            System.out.print(body);

            //  Находим и печатаем температуру сегодня
            int temp = body.indexOf("\"temp\"");
            int start = body.indexOf(":", temp)+1;
            int end = body.indexOf(",", temp);
            System.out.println("Температура сегодня:");
            System.out.println(Integer.parseInt(body.substring(start, end)));

            //  Находим и считаем средние температуры по прогнозу с  сегодняшнего дня
            for (int i = 0; i < limit; i++) {
                temp = body.indexOf("\"temp_avg\"", end);
                start = body.indexOf(":", temp)+1;
                end = body.indexOf(",", start);
                total += Double.parseDouble(body.substring(start, end));
                count++;
            }
            if (total != 0) {
                System.out.println("Средняня температура по прогнозу:");
                System.out.println(total/count);
            }
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
    }

    private static String formatQuery(String URL, String[][] params) {
        StringBuilder query = new StringBuilder(URL);
        query.append("?");
        for (String[] param : params) {
            query.append("&").append(param[0]).append("=").append(param[1]);
        }
        return query.toString();
    }
}


import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Scanner;

public class BookFinder {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Type hier de naam van het boek in een woord: ");
        String bookName = scanner.nextLine();

        try {
            URL url = new URL("https://www.googleapis.com/books/v1/volumes?q=" + bookName + "&maxResults=10");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");

            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                JSONObject jsonObject = new JSONObject(new JSONTokener(response.toString()));
                JSONArray items = jsonObject.getJSONArray("items");

                for (int i = 0; i < items.length(); i++) {
                    JSONObject item = items.getJSONObject(i);
                    JSONObject volumeInfo = item.getJSONObject("volumeInfo");

                    String title = volumeInfo.optString("title", "");
                    String author = volumeInfo.optJSONArray("authors").optString(0, "");
                    String isbn = volumeInfo.optJSONArray("industryIdentifiers").optJSONObject(0).optString("identifier", "");
                    String releaseDate = volumeInfo.optString("publishedDate", "");

                    if (releaseDate.isEmpty()) {
                        continue;
                    }

                    LocalDate date;
                    try {
                        date = LocalDate.parse(releaseDate, DateTimeFormatter.ISO_DATE);
                    } catch (Exception e) {
                        continue;
                    }

                    String monthInDutch = date.getMonth().getDisplayName(TextStyle.FULL, new Locale("nl"));

                    System.out.println("Titel: " + title);
                    System.out.println("Auteur: " + author);
                    System.out.println("ISBN nummer: " + isbn);
                    System.out.println("Release Datum: " + date.getDayOfMonth() + " " + monthInDutch + " " + date.getYear());
                    System.out.println(" ");
                }
            } else {
                System.out.println("Error: " + responseCode);
            }
            connection.disconnect();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
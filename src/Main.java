import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.file.*;
import java.util.Scanner;

public class Main {
    static final String BASE_URL = "https://lucida.to";
    static String songUrl;
    static final String OUTPUT_DIR = "C:\\Users\\samwi\\OneDrive\\Documents\\Music";

    public static void main(String[] args) {
        while (true){
            System.out.println("Enter song url: ");
            Scanner reader = new Scanner(System.in);
            String newSongUrl = reader.nextLine();

            try{
                download(newSongUrl);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        }
    }

    private static void download(String url) throws Exception{
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

        String encodedUrl = URLEncoder.encode(url, "UTF-8");
        HttpRequest initalLoadRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/?url=" + encodedUrl + "&country=auto"))
                .GET()
                .build();

        String loadBody = String.format(
                "{\"url\":\"%s\"}",
                url
        );

        HttpRequest loadRequest = HttpRequest.newBuilder()  // start building
                .uri(URI.create(BASE_URL + "/api/load"))        // where to send it
                .header("Content-Type", "application/json")      // what format we're sending
                .header("User-Agent", "Mozilla/5.0")             // who we're pretending to be
                .POST(HttpRequest.BodyPublishers.ofString(loadBody)) // method + data to send
                .build();

        HttpResponse<String> loadResponse = client.send(loadRequest,
                HttpResponse.BodyHandlers.ofString()); // use of string so we can get id for poll

        System.out.println("Load response: " + loadResponse.body());



    }


}
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.file.*;
import java.util.Scanner;

public class Main {
    static final String BASE_URL = "https://lucida.to";
    static final String OUTPUT_DIR = "C:\\Users\\samwi\\OneDrive\\Documents\\Music";
    static final HttpClient client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .build();

    public static void main(String[] args) {
        Scanner reader = new Scanner(System.in);
        while (true) {
            System.out.println("Enter song url: ");
            String newSongUrl = reader.nextLine();
            try {
                download(newSongUrl);
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
            }
        }
    }

    private static void download(String url) throws Exception {
        // Step 1 - kick off the download
        String loadBody = String.format(
                "{" +
                        "\"url\":\"%s\"," +
                        "\"metadata\":true," +
                        "\"compat\":false," +
                        "\"private\":false," +
                        "\"downscale\":\"mp3-320\"," +
                        "\"handoff\":true," +
                        "\"account\":{\"type\":\"country\",\"id\":\"auto\"}," +
                        "\"upload\":{\"enabled\":false,\"service\":\"pixeldrain\"}" +
                        "}",
                url
        );

        String encodedPath = URLEncoder.encode("/api/fetch/stream/v2", "UTF-8");

        HttpRequest streamRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/load?url=" + encodedPath))
                .header("Content-Type", "application/json")
                .header("User-Agent", "Mozilla/5.0")
                .POST(HttpRequest.BodyPublishers.ofString(loadBody))
                .build();

        HttpResponse<String> streamResponse = client.send(streamRequest,
                HttpResponse.BodyHandlers.ofString());

        System.out.println("Stream response: " + streamResponse.body());

        // Step 2 - extract server and handoff ID
        String responseBody = streamResponse.body();
        String handoffId = responseBody.split("\"handoff\":\"")[1].split("\"")[0];
        String server = responseBody.split("\"server\":\"")[1].split("\"")[0];

        System.out.println("Handoff ID: " + handoffId);
        System.out.println("Server: " + server);

        // Step 3 - poll until ready
        String encodedPollPath = URLEncoder.encode(
                "/api/fetch/request/" + handoffId, "UTF-8"
        );

        while (true) {
            HttpRequest pollRequest = HttpRequest.newBuilder()
                    .uri(URI.create(BASE_URL + "/api/load?url=" + encodedPollPath + "&force=" + server))
                    .header("User-Agent", "Mozilla/5.0")
                    .GET()
                    .build();

            HttpResponse<String> pollResponse = client.send(pollRequest,
                    HttpResponse.BodyHandlers.ofString());

            System.out.println("Poll response: " + pollResponse.body());

            if (pollResponse.body().contains("\"status\":\"completed\"")) {
                break;
            } else if (pollResponse.body().contains("\"status\":\"error\"")) {
                throw new RuntimeException("Download failed: " + pollResponse.body());
            }

            System.out.println("Waiting...");
            Thread.sleep(2000);
        }

        // Step 4 - download the file
        System.out.println("Downloading...");
        String encodedDownloadPath = URLEncoder.encode(
                "/api/fetch/request/" + handoffId + "/download", "UTF-8"
        );

        HttpRequest downloadRequest = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/load?url=" + encodedDownloadPath + "&force=" + server + "&redirect=true"))
                .header("User-Agent", "Mozilla/5.0")
                .GET()
                .build();

        Files.createDirectories(Paths.get(OUTPUT_DIR));
        Path outputPath = Paths.get(OUTPUT_DIR, handoffId + ".mp3");

        client.send(downloadRequest, HttpResponse.BodyHandlers.ofFile(outputPath));
        System.out.println("Saved to: " + outputPath);
    }
}
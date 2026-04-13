import java.net.http.*;

public class Main {
    static final String BASE_URL = "https://lucida.to";
    static String songUrl;
    static final String OUTPUT_DIR = "C:\\Users\\samwi\\OneDrive\\Documents\\Music";

    public static void main(String[] args) {

    }

    private static void download(String url) throws Exception{
        HttpClient client = HttpClient.newBuilder().followRedirects(HttpClient.Redirect.ALWAYS).build();

        Str
    }


}
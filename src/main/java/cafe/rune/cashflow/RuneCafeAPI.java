package cafe.rune.cashflow;

import com.google.gson.Gson;
import net.runelite.http.api.RuneLiteAPI;
import okhttp3.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import static net.runelite.http.api.RuneLiteAPI.JSON;

public class RuneCafeAPI {
    private final String API_BASE_URL = "https://api.rune.cafe/api/gehistory/";
    private final String apiKey;

    public RuneCafeAPI(String apiKey) {
        this.apiKey = apiKey;
    }

    public void postGEHistorySnapshot(String osrsName,
                                      List<GEHistoryRecord> records,
                                      Consumer<Response> onResponse,
                                      Consumer<Exception> onError) {
        String urlString;
        try {
            urlString = API_BASE_URL + URLEncoder.encode(osrsName, "UTF-8") + "/snapshot";
        } catch(UnsupportedEncodingException e) {
            Logger.getLogger("cafe.rune.cashflow").log(Level.WARNING,"Error encoding osrsname.", e);
            return;
        };

        this.post(urlString, records, onResponse, onError);
    }

    public void postLiveTrade() {

    }

    private void post(String url, Object body, Consumer<Response> onResponse, Consumer<Exception> onError) {
        Gson gson = new Gson();
        Request request = new Request.Builder()
                .header("Authorization", "Bearer " + this.apiKey)
                .header("Content-Type", "application/json")
                .post(RequestBody.create(JSON, gson.toJson(body)))
                .url(HttpUrl.parse(url))
                .build();

        RuneLiteAPI.CLIENT.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e)
            {
                onError.accept(e);
            }

            @Override
            public void onResponse(Call call, Response response)
            {
                onResponse.accept(response);
                response.close();
            }
        });
    }
}

package single;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;

import java.util.Map;

public class CreditSuisseAPI extends APICommon{
    private static String csApiBase;

    static {
        csApiBase = cs_apiBase;
    }

    public static HttpResponse<String> getCookies(Map<String, String> headers) throws UnirestException {
        return Unirest.get("https://www.credit-suisse.com/bin/i18n/getLocalizationValues?getCookies")
                .headers(headers).asString();
    }



}

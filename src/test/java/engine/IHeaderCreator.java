package engine;

import java.util.List;
import java.util.Map;

public interface IHeaderCreator {
    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String CONTENTTYPE_HEADER = "Content-Type";
    public static final String VERSION_HEADER = "Version";
    public static final String DEVICEID_HEADER = "DeviceId";
    public static final String SESSIONID_HEADER = "Session-Id";
    public static final String ACCEPT_HEADER = "Accept";

    public static final String ANY_VALUE = "anyvalue";
    public static final String EMPTY_VALUE = "";
    public static final String CONTENTTYPE_JSON = "application/json";
    public static final String CONTENTTYPE_URLENCODED = "application/x-www-form-urlencoded";

    Map<String, String> createHeader(String headerType);

    default void setRequestBody(Object requestBody) {

    }

    default void setAPIMethod(String apiMethod) {

    }

    default void setUrlParamsList(List<String> urlParamsList) {
    }
}

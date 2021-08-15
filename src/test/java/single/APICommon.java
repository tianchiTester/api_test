package single;

import common.GetEnvironmentVariables;

public class APICommon {
    protected static String baseUrl;
    protected static String protocol;
    protected static String apiBase;
    protected static String cs_baseUrl;
    protected static String cs_apiBase;



    static {
        baseUrl = GetEnvironmentVariables.getInstance().getBaseURL();
        protocol = GetEnvironmentVariables.getInstance().getProtocol();
        apiBase = protocol + "://" + baseUrl;

        cs_baseUrl = GetEnvironmentVariables.getInstance().getCsBaseURL();
        cs_apiBase = protocol + "://" + cs_baseUrl;
    }

}

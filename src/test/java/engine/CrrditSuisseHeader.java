package engine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CrrditSuisseHeader implements IHeaderCreator {
    private static final String cookies_zh = "CsgLanguage=zh; CsgBasePath=/cn/; OptanonAlertBoxClosed=2021-07-17T10:15:00.864Z; _gcl_au=1.1.1811335803.1626516902; _ga=GA1.2.813241594.1626516904; s_fid=3154CA9FE427F222-0B0E15771B5F3AEB; s_ecid=MCMID%7C72782903925848134862970717035689301759; aam_did=76095079776936445972711680289024764516; CsgLanguage=zh; CsgBasePath=/cn/; ClientTLSVersion=tls1.3; CsgLanguage=zh; at_check=true; AMCVS_14CE34B8527836E60A490D44%40AdobeOrg=1; AMCV_14CE34B8527836E60A490D44%40AdobeOrg=1473929765%7CMCMID%7C72782903925848134862970717035689301759%7CMCAAMLH-1629380676%7C11%7CMCAAMB-1629380676%7CRKhpRz8krg2tLO6pguXWp5olkAcUniQYPHaMWWgdJ3xzPWQmdj0y%7CMCOPTOUT-1628783076s%7CNONE%7CvVersion%7C5.2.0%7CMCIDTS%7C18852%7CMCAID%7CNONE; _gid=GA1.2.1812917259.1628775877; s_cc=true; s_sq=%5B%5BB%5D%5D; _gat_UA-49497905-1=1; mbox=PC#07631b3a0afa420ea767d0540b6fc0b9.32_0#1692021469|session#b734501845504b2a90e821f692f6e272#1628777736; RT=\"z=1&dm=credit-suisse.com&si=5ritvdc20xb&ss=ks8z3p2w&sl=0&tt=0\"; OptanonConsent=isIABGlobal=false&datestamp=Thu+Aug+12+2021+21%3A58%3A02+GMT%2B0800+(%E4%B8%AD%E5%9B%BD%E6%A0%87%E5%87%86%E6%97%B6%E9%97%B4)&version=6.17.0&hosts=&consentId=7d39e158-c25f-440b-adbe-8efd261f5cd7&interactionCount=1&landingPath=NotLandingPage&groups=C0001%3A1%2CC0002%3A1%2CC0003%3A1%2CC0004%3A1&geolocation=%3B&AwaitingReconsent=false; _uetsid=7b4807c0fb7311ebaa6667f1afd4d9c0; _uetvid=d8e8f9d0e6e711ebbea26519c2ec9a06";
    private static final String COOKIE_HEADER = "cookie";
    private static final String ACCEPT_LANGUAGE_HEADER = "accept-language";
    private static final String language_zh = "zh-CN";

    private Object requestBody;
    private String apiMethodName;
    private List<String> urlParamList;
    private Executor executor;
    private String token;
    private Map<String, String> header;


    public CrrditSuisseHeader(Executor executor) {
        this.executor = executor;
        //this.token = token;
    }

    @Override
    public Map<String, String> createHeader(String headerType)
    {
        header = new HashMap<>();
        HeaderType type = HeaderType.valueOf(headerType.toUpperCase());

        switch(type)
        {
            case CN:
                header.put(COOKIE_HEADER, cookies_zh);
                header.put(ACCEPT_LANGUAGE_HEADER,language_zh);
                break;

            default :
                header = null;
                break;

        }
        return header;
    }




    @Override
    public void setRequestBody(Object requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public void setAPIMethod(String apiMethodName) {
        this.apiMethodName = apiMethodName;
    }

    @Override
    public void setUrlParamsList(List<String> urlParamList) {
        this.urlParamList = urlParamList;
    }


}

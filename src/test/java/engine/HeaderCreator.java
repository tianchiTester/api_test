package engine;

import testcase.SuiteCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HeaderCreator implements IHeaderCreator {
    protected Map<String, String> header;
    protected String token;
    protected String deviceId;
    protected List<IHeaderCreator> headerCreatorAppendList;

    public HeaderCreator(String token, String deviceId)
    {
        this.token = token;
        this.deviceId = deviceId;
        this.headerCreatorAppendList = new ArrayList<>();
    }

    public void appendHeaderCreator(IHeaderCreator headerCreator)
    {
        this.headerCreatorAppendList.add(headerCreator);
    }

    public Map<String, String> createHeader(String headerType)
    {
        HeaderType type = HeaderType.valueOf(headerType.toUpperCase());

        switch(type)
        {
            case COMMON:
                header= createCommon();
                break;
            case EMPTY:
                header = createEmpty();
                break;
            case EXTRAPROPERTY:
                header = createExtraProperty();
                break;
            case AUTHORIZATIONMISSING:
                header= createAuthorizationMissing();
                break;
            case AUTHORIZATIONEMPTY:
                header= createAuthorizationEmpty();
                break;
            case AUTHORIZATIONINVALID:
                header = createAuthorizationInvalid();
                break;
            case DEVICEIDMISSING:
                header = createDeviceIdMissing();
                break;
            case DEVICEIDEMPTY:
                header = createDeviceIdEmpty();
                break;
            case DEVICEIDINVALID:
                header = createDeviceIdInvalid();
                break;
            case CONTENTTYPEMISSING:
                header = createContentTypeMissing();
                break;
            case CONTENTTYPEEMPTY:
                header = createContentTypeEmpty();
                break;
            case CONTENTTYPEINVALID:
                header = createContentTypeInvalid();
                break;
            case ICONS_AUTHORIZATIONMISSING:
                header = icons_createAuthorizationMissing();
                break;
            case ICONS_AUTHORIZATIONEMPTY:
                header = icons_createAuthorizationEmpty();
                break;
            case ICONS_AUTHORIZATIONINVALID:
                header = icons_createAuthorizationInvalid();
                break;
            case ICONS_DEVICEIDMISSING:
                header = icons_createDeviceIdMissing();
                break;
            case ICONS_DEVICEIDEMPTY:
                header = icons_createDeviceIdEmpty();
                break;
            case ICONS_DEVICEIDINVALID:
                header = icons_createDeviceIdInvalid();
                break;
            default:
                header = createHeaderWithAppendedList(headerType);
                break;
        }

        return header;
    }

    private Map<String, String> createHeaderWithAppendedList(String headerType)
    {
        Map<String, String> result = null;
        for (IHeaderCreator iHeaderCreator: headerCreatorAppendList)
        {
            result = iHeaderCreator.createHeader(headerType);
            if (result != null)
            {
                return result;
            }
        }

        return result;
    }

    public Map<String, String> createEmpty()
    {
        header = new HashMap<>();
        return header;
    }

    public Map<String, String> createCommon()
    {
        if (token == null)
        {
            token = SuiteCommon.GetToken();
        }

        if (deviceId == null)
        {
            deviceId = SuiteCommon.GetSetUpDeviceId();
        }

        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(CONTENTTYPE_HEADER, CONTENTTYPE_JSON);
        header.put(DEVICEID_HEADER, deviceId);

        return header;
    }

    public Map<String, String> createExtraProperty()
    {
        if (token == null)
        {
            token = SuiteCommon.GetToken();
        }

        if (deviceId == null)
        {
            deviceId = SuiteCommon.GetSetUpDeviceId();
        }

        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(CONTENTTYPE_HEADER, CONTENTTYPE_JSON);
        header.put(DEVICEID_HEADER, deviceId);
        header.put(ANY_VALUE, ANY_VALUE);

        return header;
    }

    public Map<String, String> createAuthorizationMissing()
    {
        header = new HashMap<>();
        header.put(DEVICEID_HEADER, deviceId);
        header.put(CONTENTTYPE_HEADER, CONTENTTYPE_JSON);
        return header;
    }

    public Map<String, String> createAuthorizationEmpty()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, EMPTY_VALUE);
        header.put(DEVICEID_HEADER, deviceId);
        header.put(CONTENTTYPE_HEADER, CONTENTTYPE_JSON);
        return header;
    }

    public Map<String, String> createAuthorizationInvalid()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, ANY_VALUE);
        header.put(CONTENTTYPE_HEADER, CONTENTTYPE_JSON);
        header.put(DEVICEID_HEADER, deviceId);

        return header;
    }

    public Map<String, String> createDeviceIdMissing()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(CONTENTTYPE_HEADER, CONTENTTYPE_JSON);
        return header;
    }

    public Map<String, String> createDeviceIdEmpty()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(DEVICEID_HEADER, EMPTY_VALUE);
        header.put(CONTENTTYPE_HEADER, CONTENTTYPE_JSON);
        return header;
    }

    public Map<String, String> createDeviceIdInvalid()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(DEVICEID_HEADER, ANY_VALUE);
        header.put(CONTENTTYPE_HEADER, CONTENTTYPE_JSON);
        return header;
    }

    public Map<String, String> createContentTypeMissing()
    {
        header = new HashMap<>();
        header.put(DEVICEID_HEADER, deviceId);
        header.put(AUTHORIZATION_HEADER, token);
        return header;
    }

    public Map<String, String> createContentTypeEmpty()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(DEVICEID_HEADER, deviceId);
        header.put(CONTENTTYPE_HEADER, EMPTY_VALUE);
        return header;
    }

    public Map<String, String> createContentTypeInvalid()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(DEVICEID_HEADER, deviceId);
        header.put(CONTENTTYPE_HEADER, ANY_VALUE);
        return header;
    }

    //----------------------Icons Header-----------------------
    public Map<String, String> icons_createAuthorizationMissing()
    {
        header = new HashMap<>();
        header.put(DEVICEID_HEADER, deviceId);
        return header;
    }

    public Map<String, String> icons_createAuthorizationEmpty()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, EMPTY_VALUE);
        header.put(DEVICEID_HEADER, deviceId);
        return header;
    }

    public Map<String, String> icons_createAuthorizationInvalid()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, ANY_VALUE);
        header.put(DEVICEID_HEADER, deviceId);
        return header;
    }

    public Map<String, String> icons_createDeviceIdMissing()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        return header;
    }

    public Map<String, String> icons_createDeviceIdEmpty()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(DEVICEID_HEADER, EMPTY_VALUE);
        return header;
    }

    public Map<String, String> icons_createDeviceIdInvalid()
    {
        header = new HashMap<>();
        header.put(AUTHORIZATION_HEADER, token);
        header.put(DEVICEID_HEADER, ANY_VALUE);
        return header;
    }
}

package testcase;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.http.HttpHost;
import org.json.JSONObject;
import org.testng.ITestContext;
import org.testng.annotations.*;
import common.CommonVariables;
import common.GetEnvironmentVariables;
import engine.*;
import report.IsenCustomizedEmailableReporter;
import report.IsenXMLReporter;
import utility.PriorityInterceptor;

import java.io.*;
import java.lang.reflect.Method;
import java.security.NoSuchAlgorithmException;
import java.util.*;

@Listeners({ PriorityInterceptor.class, IsenCustomizedEmailableReporter.class, IsenXMLReporter.class })
public class SuiteCommon{

    protected static Logger logger;

    public static final String POST = "POST";
    public static final String GET = "GET";
    public static final String PATCH = "PATCH";
    public static final String PUT = "PUT";
    public static final String PUTSINGLE = "PUTSINGLE";
    public static final String DELETE = "DELETE";
    public static final String SCENARIO = "SCENARIO";
    public static final String DELETESINGLE = "DELETESINGLE";
    public static final String GETALL = "GETALL";
    public static final String GETEMPTY = "GETEMPTY";
    public static final String FILENAME = "file";
    public static final String REQUESTBODYKEY = "key";
    public static final String PRECONDITION = "PRECONDITION";
    public static final String GETINVALID = "GETINVALID";
    public static final String DELETEINVALIDDEVICES = "DELETEINVALIDDEVICES";
    public static final String POSTDEVICES = "POSTDEVICES";

    protected static String token;
    protected static String hpbptoken;
    private static String deviceId;
    public static String version;

    public static String getVersion() {
        return version;
    }

    public static String setVersion(String newVersion) {
        return version = newVersion;
    }

    protected Map<String, String> header = new HashMap<>();
    protected IHeaderCreator headerCreator;
    protected RequestCreator requestCreator;
    protected Executor executor;

    protected String currentTestCaseName;

    private static ArrayList<String> queryKeys;
    private static JSONObject testResult;

    public static Logger getLogger() {
        return logger;
    }

    /***
     * 1. Retrieve token
     * 2. Post one device and get device id
     * 3. Remove legacy test data: applist and identity
     * 4. Initiates testResult
     * @param context
     * @throws Exception
     */
    @BeforeSuite(alwaysRun = true)
    public void beforeSuite(ITestContext context) throws Exception {
//        logger.info("Retrieving token for API...");
//        Exception currentException = null;
//        try {
//            if (GetEnvironmentVariables.getInstance().getTokenSource().toUpperCase().equals("HPID"))
//            {
//                token = "Bearer " + retrieveTokenFromHPID();
//            }
//            else {
//                token = retrieveToken();
//            }
//        } catch (Exception e) {
//            currentException = e;
//        } finally {
//            if (currentException != null) {
//                logger.fatal("Exception encountered while retrieving token:" + currentException.toString());
//                StackTraceElement[] elements = currentException.getStackTrace();
//                for (StackTraceElement element : elements)
//                {
//                    logger.info("Error:" + element.toString() +
//                            " occured in method:" + element.getMethodName() +
//                            " in line:" + element.getLineNumber());
//                }
//                logger.info("ISEN SERVICE TEST ENDED");
//                throw currentException;
//            }
//        }
//        logger.info("Token retrieved is: " + token);
        testResult = new JSONObject();
        testResult.put(context.getSuite().getName(), new JSONObject());
//        logger.info("exchange hpid token to hpbp token");
//        hpbptoken = "Bearer " + exchangeTokenWithHPID();
    }

    /***
     * Remove test device
     * @param context
     * @throws Exception
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite(ITestContext context){

    }

    /***
     * Creates test object into testResult
     * @param context
     */
    @BeforeTest(alwaysRun = true)
    public void beforeTest(ITestContext context) throws Exception {
        logger.info("Test: " + context.getName() + " has started!");
//        logger.info("Retrieving deviceId through POST method");
        queryKeys = new ArrayList<>();

//        int currentVersion = 1;
        String versionString = context.getName();
//        try {
//            currentVersion = Integer.parseInt(versionString);
//        } catch (NumberFormatException e) {
//            currentVersion = 1;
//        }
//        SuiteCommon.setVersion(String.valueOf(currentVersion));
        testResult.getJSONObject(context.getSuite().getName()).put(context.getName(), new JSONObject());
//        logger.info("exchange hpid token to hpbp token");
//        hpbptoken = "Bearer " + exchangeTokenWithHPID();
    }

    /***
     * Creates class object into testResult
     * @param context
     */
    @BeforeClass(alwaysRun = true)
    public void beforeClass(ITestContext context) throws Exception {
        logger.info("Class: " + this.getClass().getName() + " has started!");
        ResetTestEnvironment(context);

        queryKeys.clear();
        queryKeys.add(context.getSuite().getName());
        queryKeys.add(context.getName());
        logger.info("queryKeys:" + queryKeys.toString());
        JSONObject result = testResult.queryObject(queryKeys);
        result.put(this.getClass().getName(), new JSONObject());
    }

    @BeforeMethod(alwaysRun = true)
    public void beforeMethod(ITestContext context, Method method) {
        this.currentTestCaseName = method.getName();
        logger.info("Method: " + this.currentTestCaseName + " has started!");
    }

    /***
     * Creates method object into testResult and stores method passed/failed verifications count and list
     * @param context
     * @param method
     */
    @AfterMethod(alwaysRun = true)
    public void afterMethod(ITestContext context, Method method) {
        logger.info("Method: " + method.getName() + " has ended!");

        if (executor == null)
            return;
        queryKeys.clear();
        queryKeys.add(context.getSuite().getName());
        queryKeys.add(context.getName());
        queryKeys.add(this.getClass().getName());
        testResult.queryObject(queryKeys).put(method.getName(), new JSONObject());
        queryKeys.clear();
        queryKeys.add(context.getSuite().getName());
        queryKeys.add(context.getName());
        queryKeys.add(this.getClass().getName());
        queryKeys.add(method.getName());
        JSONObject methodResultObject = testResult.queryObject(queryKeys);
        logger.info("queryKeys:" + queryKeys.toString());
        logger.info("methodResultObject:" + methodResultObject.toString(4));
        methodResultObject.put(CommonVariables.MPVC, executor.getMethodPassedVerificationsCount());
        methodResultObject.put(CommonVariables.MFVEC, executor.getMethodFailedVerificationsExpectedCount());
        methodResultObject.put(CommonVariables.MFVNEC, executor.getMethodFailedVerificationsNotExpectedCount());
        methodResultObject.put(CommonVariables.MPV, executor.getMethodPassedVerifications());
        methodResultObject.put(CommonVariables.MFVE, executor.getMethodFailedVerificationsExpected());
        methodResultObject.put(CommonVariables.MFVNE, executor.getMethodFailedVerificationsNotExpected());
    }

    /***
     * 1. Creates class object into testResult and stores class passed/failed verifications count and list
     * 2. Generate JSON format test report
     * @param context
     */
    @AfterClass(alwaysRun = true)
    public void tearDown(ITestContext context) throws IOException {
        logger.info("Class: " + this.getClass().getName() + " has ended!");
        if (executor == null)
            return;
        queryKeys.clear();
        queryKeys.add(context.getSuite().getName());
        queryKeys.add(context.getName());
        queryKeys.add(this.getClass().getName());
        logger.info("queryKeys:" + queryKeys.toString());
        JSONObject classResultObject = testResult.queryObject(queryKeys);
        logger.info("classResultObject:" + classResultObject.toString(4));
        classResultObject.put(CommonVariables.CPVC, executor.getAllPassedVerificationsCount());
        classResultObject.put(CommonVariables.CFVEC, executor.getAllFailedVerificationsExpectedCount());
        classResultObject.put(CommonVariables.CFVNEC, executor.getAllFailedVerificationsNotExpectedCount());
        classResultObject.put(CommonVariables.CPV, executor.getAllPassedVerifications());
        classResultObject.put(CommonVariables.CFVE, executor.getAllFailedVerificationsExpected());
        classResultObject.put(CommonVariables.CFVNE, executor.getAllFailedVerificationsNotExpected());
        if (GetEnvironmentVariables.getInstance().getGenerateCSVReport().equals("true")) {
            executor.generateCSVReport();
        }
    }

    /***
     * Retrieve testResult in SuiteCommon
     * @return testResult
     */
    public static JSONObject getTestResult() {
        return testResult;
    }

    public SuiteCommon(){
        PropertyConfigurator.configure("log4j.properties");

        if (GetEnvironmentVariables.getInstance().getUseProxy()) {
            Unirest.setProxy(new HttpHost("web-proxy.corp.hp.com", 8080));
        }
        logger = Logger.getRootLogger();
        logger.info("ISEN SERVICE TEST STARTED!!");
        logger.info("Initializing suite members...");
        headerCreator = new HeaderCreator(token, deviceId);
        requestCreator = new RequestCreator();
    }

    private void ResetTestEnvironment(ITestContext context) throws Exception {
        logger.info("Resetting test environment");
        headerCreator = new HeaderCreator(token, deviceId);
        header = headerCreator.createHeader(HeaderType.COMMON.toString());
        logger.info("Header for registering device is:" + header.toString());

//        JSONObject setUpDevice = new JSONObject();
//        setUpDevice.put("hardwareId", "123456789");
//        setUpDevice.put("deviceName", "setup device");
//        setUpDevice.put("deviceType", "host");
//        setUpDevice.put("chassisType", "desktop");
//        setUpDevice.put("friendlyName", "Set Up Device");
//
//        logger.info("Registering a device for reset:");
//        HttpResponse<JsonNode> response = DeviceAPI.registerDevice(header, setUpDevice);
//        logger.info("Response code is:" + response.getStatus());
//        if (response.getStatus() != 200)
//        {
//            logger.info("Error for posting device when setting up testing environment with response body");
//            logger.info(response.getBody().toString());
//            throw new Exception("Error for setting up testing envoronment");
//        }
//        Object actualBody = response.getBody().getObject();
//        if (actualBody instanceof JSONObject) {
//            deviceId = ((JSONObject) actualBody).getString("deviceId");
//        }
//        else if (actualBody instanceof JSONArray) {
//            deviceId = ((JSONArray)actualBody).getJSONObject(0).getString("deviceId");
//        }

//        logger.info("Remove all devices bound to the user");
//
//        header.put(IHeaderCreator.DEVICEID_HEADER, deviceId);
//        response = DeviceAPI.getDeviceAll(header);
//        if (response.getBody().isArray())
//        {
//            JSONArray deviceArray = response.getBody().getArray();
//            for (int i = 0; i < deviceArray.length(); i++)
//            {
//                String deviceIdToRemove = deviceArray.getJSONObject(i).getString("deviceId");
//                if (!deviceIdToRemove.equals(deviceId)) {
//                    DeviceAPI.deleteDevice(header, deviceIdToRemove);
//                    logger.info("Device:" + deviceIdToRemove + " has been removed");
//                }
//                else
//                {
//                    logger.info("Remove applist belonging to the device");
//                    AppListAPI.deleteAppList(deviceId, header);
//
//                    logger.info("Remove gametag belonging to the user");
//                    IdentityAPI.deleteIdentity(header);
//                }
//            }
//        }

        // Remove the device posted itself
//        DeviceAPI.deleteDevice(header, deviceId);
//
//        logger.info("Post the device again to get the deviceId");
//        response = DeviceAPI.registerDevice(header, setUpDevice);
//
//        deviceId =  response.getBody().getObject().getString("deviceId");
//        logger.info("Device id posted and retrieved is: " + deviceId);
//
//        logger.info("Suite: " + context.getSuite().getName() + " has started!");
//        logger.info("Initiates testResult to store all results for test");

    }

    public static String GetToken()
    {
        return  token;
    }

    public static String GetHPBPToken()
    {
        return  hpbptoken;
    }

    public static String GetSetUpDeviceId()
    {
        return  deviceId;
    }

    public String retrieveToken () throws UnirestException, UnsupportedEncodingException {
        //Send out request with user name, password and jsession
        String baseUrl = GetEnvironmentVariables.getInstance().getBaseURL();
        String protocol = GetEnvironmentVariables.getInstance().getProtocol();
        String apiBase = protocol + "://" + baseUrl;

        String signinUrl = apiBase + "/idm/users_sign_in";
        Map<String, Object> queryParams = new HashMap<>();
        queryParams.put("userName", GetEnvironmentVariables.getInstance().getOmenUserName());
        queryParams.put("credentials", GetEnvironmentVariables.getInstance().getOmenPassword());
        queryParams.put("authority", "hptpm/basic");
        queryParams.put("customAttributes", "?continue=" + apiBase + "/api/v1/oauth2/authorize&response_type=code&client_id=89d9fef0-58c4-43e4-8c94-c3a7957cb4a2&redirect_uri=" + protocol + "%3a%2f%2f" + baseUrl + "%2fusers%2fauth%2fidm%2fcallback&scope=sign_in&state=new");
        queryParams.put("locale", "en");
        HttpResponse<String> result = Unirest.post(signinUrl).queryString(queryParams).asString();

        String aidValue = result.getHeaders().get("Set-Cookie").get(result.getHeaders().get("Set-Cookie").size() - 2);

        int start = aidValue.indexOf("=", 0);
        int end = aidValue.indexOf(";", 0);
        return aidValue.substring(start + 1, end);
    }


}



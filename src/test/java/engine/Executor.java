package engine;

import bsh.EvalError;
import bsh.Interpreter;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import common.GetEnvironmentVariables;
import testcase.SuiteCommon;
import utility.FileOperator;
import utility.OmenConstants;
import utility.ResultChecker;
import utility.SuiteSoftAssert;

import java.io.*;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static engine.IHeaderCreator.VERSION_HEADER;


public class Executor {
    private JSONObject testData;
    private Map<String, Method> methodPool;
    private Map<String, String> variables;
    private SuiteSoftAssert softAssert;
    private String currentAPI;
    private Logger logger;
    private IHeaderCreator headerCreator;
    private Interpreter bshInterpreter;
    private ArrayList<JSONObject> methodPassedVerifications;
    private ArrayList<JSONObject> methodFailedVerificationsExpected;
    private ArrayList<JSONObject> methodFailedVerificationsNotExpected;
    private ArrayList<JSONObject> allPassedVerifications;
    private ArrayList<JSONObject> allFailedVerificationsExpected;
    private ArrayList<JSONObject> allFailedVerificationsNotExpected;


    private boolean checkCorrelationId;

    /***
     * 1. Read test data form .json file
     * 2. Initiates all required members
     * @param apiName
     * @throws FileNotFoundException
     */
    public Executor(String apiName) throws IOException {
        String dataFile = GetEnvironmentVariables.getInstance().getProjectRootDirectory() + "\\resources\\" + apiName + ".json";

        InputStream is = new FileInputStream(dataFile);
        String jsonTxt = IOUtils.toString(is, "UTF-8");
        testData = new JSONObject(jsonTxt);

        currentAPI = apiName;
        methodPool = new HashMap<>();
        variables = new HashMap<>();
        softAssert = new SuiteSoftAssert();
        logger = SuiteCommon.getLogger();
        headerCreator = new HeaderCreator(SuiteCommon.GetToken(), SuiteCommon.GetSetUpDeviceId());
        bshInterpreter = new Interpreter();
        methodPassedVerifications = new ArrayList<>();
        methodFailedVerificationsExpected = new ArrayList<>();
        methodFailedVerificationsNotExpected = new ArrayList<>();
        allPassedVerifications = new ArrayList<>();
        allFailedVerificationsExpected = new ArrayList<>();
        allFailedVerificationsNotExpected = new ArrayList<>();
        checkCorrelationId = true;
    }

    public void setCheckCorrelationId(boolean value) {
        checkCorrelationId = value;
    }

    public Interpreter getBshInterpreter() {
        return bshInterpreter;
    }

    public Map<String, String> getVariables() {
        return variables;
    }

    /***
     * Get passed verifications for current method
     * @return verifications list of String format
     */
    public String getMethodPassedVerifications() {
        return constructVerificationString(methodPassedVerifications);
    }

    /***
     * Get expected failed verifications for current method
     * @return verifications list of String format
     */
    public String getMethodFailedVerificationsExpected() {
        return constructVerificationString(methodFailedVerificationsExpected);
    }

    /***
     * Get failed verifications for current method
     * @return verifications list of String format
     */
    public String getMethodFailedVerificationsNotExpected() {
        return constructVerificationString(methodFailedVerificationsNotExpected);
    }

    /***
     * Get passed verifications for all methods
     * @return verifications list of String format
     */
    public String getAllPassedVerifications() {
        return constructVerificationString(allPassedVerifications);
    }

    /***
     * Get expected failed verifications for all methods
     * @return verifications list of String format
     */
    public String getAllFailedVerificationsExpected() {
        return constructVerificationString(allFailedVerificationsExpected);
    }

    /***
     * Get failed verifications for all methods
     * @return verifications list of String format
     */
    public String getAllFailedVerificationsNotExpected() {
        return constructVerificationString(allFailedVerificationsNotExpected);
    }

    /***
     * Build a verifications list string separated by comma
     * @param verifications any verification list
     * @return
     */
    public String constructVerificationString(ArrayList<JSONObject> verifications) {
        StringBuilder verificationString = new StringBuilder();

        for (int i = 0; i < verifications.size(); i++) {
            JSONObject verification = verifications.get(i);
            String verificationName = verification.names().getString(0);
            verificationString.append(verificationName);

            String bracketString = "";

            if (verification.getJSONObject(verificationName).getString("Result").toLowerCase().equals("fail") && !verification.getJSONObject(verificationName).getString("RelatedBug").trim().equals("")) {
                if (!bracketString.equals("")) {
                    bracketString += ",";
                }
                bracketString += "<a href=\"https://hpswapps.visualstudio.com/PSSW_Omen/_workitems/edit/" + verification.getJSONObject(verificationName).getString("RelatedBug").trim()
                        + "\" target=\"_Blank\">" + verification.getJSONObject(verificationName).getString("RelatedBug").trim() + "</a>";
            }

            if (!verification.getJSONObject(verificationName).getString("Comment").trim().equals("")) {
                if (!bracketString.equals("")) {
                    bracketString += ",";
                }
                bracketString += verification.getJSONObject(verificationName).getString("Comment").trim();
            }

            if (!bracketString.equals("")) {
                verificationString.append("(").append(bracketString).append(")");
            }

            if (i != (verifications.size() - 1)) {
                verificationString.append(",");
            }
        }

        return verificationString.toString();
    }

    /***
     * Get passed verifications count for current method
     * @return passed verifications count
     */
    public int getMethodPassedVerificationsCount() {
        return methodPassedVerifications.size();
    }

    /***
     * Get expected failed verifications count for current method
     * @return failed verifications count
     */
    public int getMethodFailedVerificationsExpectedCount() {
        return methodFailedVerificationsExpected.size();
    }

    /***
     * Get failed verifications not expected count for current method
     * @return failed verifications not expected count
     */
    public int getMethodFailedVerificationsNotExpectedCount() {
        return methodFailedVerificationsNotExpected.size();
    }

    /***
     * Get passed verifications count for all methods
     * @return passed verifications count
     */
    public int getAllPassedVerificationsCount() {
        return allPassedVerifications.size();
    }

    /***
     * Get expected failed verifications count for all methods
     * @return failed verifications count
     */
    public int getAllFailedVerificationsExpectedCount() {
        return allFailedVerificationsExpected.size();
    }

    /***
     * Get failed verifications count not expected for all methods
     * @return failed verifications count
     */
    public int getAllFailedVerificationsNotExpectedCount() {
        return allFailedVerificationsNotExpected.size();
    }

    public void addMethodToPool(String methodName, Method apiMethod) {
        methodPool.put(methodName, apiMethod);
    }

    public void setHeaderCreator(IHeaderCreator headerCreator) {
        this.headerCreator = headerCreator;
    }

    public IHeaderCreator getHeaderCreator() {
        return this.headerCreator;
    }

    public void checkCaseResult() {
        if (methodFailedVerificationsNotExpected.size() == 0 && methodFailedVerificationsExpected.size() == 0) {
            return;
        }
        softAssert.assertAll();
    }

    public boolean executeVerification(String verificationName, JSONObject verification, Method apiMethod) {
        boolean verificationPass = true;

        try {
            /*** Extract verification members from JSON test data ***/

            // Extract "UrlParams"
            String urlParams = null;
            ArrayList<String> urlParamsList = new ArrayList<>();

            if (verification.has("UrlParams")) {
                urlParams = verification.getString("UrlParams");
                urlParams = preProcessRequestString(urlParams);
                verification.put("UrlParams", urlParams);
                logger.info("url parameters:" + urlParams);
            }
            if (urlParams != null && urlParams.trim().length() > 0) {
                urlParamsList.addAll(Arrays.asList(urlParams.split(",")));
                if (urlParams.endsWith(",")) {
                    urlParamsList.add("");
                }
            }
            headerCreator.setUrlParamsList(urlParamsList);

            // Extract "RequestBody"
            JSONObject requestBodyObject = null;
            JSONArray requestBodyArray = null;
            Object requestBody = null;
            if (verification.has("RequestBody")) {
                requestBody = verification.get("RequestBody");
            }
            if ((requestBody instanceof JSONObject)) {
                requestBodyObject = (JSONObject) requestBody;
                requestBodyObject = new JSONObject(preProcessRequestString(requestBodyObject.toString()));
                verification.put("RequestBody", requestBodyObject);
                logger.info("request body:" + requestBodyObject.toString(4));
            } else if ((requestBody instanceof JSONArray)) {
                requestBodyArray = (JSONArray) requestBody;
                requestBodyArray = new JSONArray(preProcessRequestString(requestBodyArray.toString()));
                verification.put("RequestBody", requestBodyArray);
                logger.info("request array:" + requestBodyArray.toString(4));
            }

            // Extract "ExpectedCode"
            String expectedCode = null;
            if (verification.has("ExpectedCode")) {
                expectedCode = verification.getString("ExpectedCode");
                verification.put("ExpectedCode", preProcessRequestString(expectedCode));
                logger.info("excepted code:" + expectedCode);
            }

            // Extract "ExpectedBody"
            JSONObject expectedBodyObject = null;
            JSONArray expectedBodyArray = null;
            Object expectedBody = null;
            if (verification.has("ExpectedBody")) {
                expectedBody = verification.get("ExpectedBody");
            }
            if ((expectedBody instanceof JSONObject)) {
                expectedBodyObject = (JSONObject) expectedBody;
                verification.put("ExpectedBody", expectedBodyObject);
                logger.info("expected body:" + expectedBodyObject.toString(4));
            } else if ((expectedBody instanceof JSONArray)) {
                expectedBodyArray = (JSONArray) expectedBody;
                verification.put("ExpectedBody", expectedBodyArray);
                logger.info("expected array:" + expectedBodyArray.toString(4));
            }

            // Extract "VerificationMode"
            String verificationMode = null;
            if (verification.has("VerificationMode")) {
                verificationMode = verification.getString("VerificationMode");
                logger.info("verification mode:" + verificationMode);
            }

            // Extract "QueryString"
            String queryString = null;
            Map<String, Object> queryParams = new HashMap<>();
            if (verification.has("QueryString")) {
                queryString = verification.getString("QueryString");
                queryString = preProcessRequestString(queryString);
                logger.info("query string:" + queryString);
                verification.put("QueryString", queryString);
            }

            if (queryString != null && !queryString.equals("")) {
                String[] queryArray = queryString.split(";");
                for (int i = 0; i < queryArray.length; i++) {
                    String query = queryArray[i].trim();
                    int signal = query.indexOf("=");
                    String key = query.substring(0, signal);
                    Object value = query.substring(signal + 1);
                    queryParams.put(key, value);
                }
            }

            // Extract "HeaderParams"
            String headers = null;
            Map<String, Object> headerParams = new HashMap<>();
            if (verification.has("HeaderParams")) {
                headers = verification.getString("HeaderParams");
                headers = preProcessRequestString(headers);
                logger.info("Header Params:" + headers);
                verification.put("HeaderParams", headers);
            }

            if (headers != null && !headers.equals("")) {
                String[] headerArray = headers.split(";");
                for (int i = 0; i < headerArray.length; i++) {
                    String header = headerArray[i].trim();
                    int signal = header.indexOf("=");
                    String key = header.substring(0, signal);
                    Object value = header.substring(signal + 1);
                    headerParams.put(key, value);
                }
            }

            // Extract "HeaderType"
            String headerType = null;
            Map<String, String> header = new HashMap<>();
            if (verification.has("HeaderType")) {
                headerType = verification.getString("HeaderType");
                logger.info("header type:" + headerType);
                // Creating HMAC header, Request Body is required
                headerCreator.setRequestBody(requestBodyObject == null ? requestBodyArray : requestBodyObject);
                headerCreator.setAPIMethod(apiMethod.getName());
                header = headerCreator.createHeader(headerType);
                //If test with awareness data, set the version from verification name
                if (verificationName.startsWith("A-")) {
                    header.put(VERSION_HEADER, verificationName.substring(verificationName.length() - 1));
                }
                //If test with other API, set the version with test name in test suite
                else {
                    //header.put(VERSION_HEADER, SuiteCommon.getVersion());
                }

                for (Map.Entry<String, Object> entry : headerParams.entrySet()
                ) {
                    header.put(entry.getKey(), entry.getValue().toString());
                }

                logger.info("header content:" + header.toString());
                verification.put("HeaderContent", header.toString());
            }

            /*** Constructs api method parameter array and execute method***/
            Object[] methodParamArray = null;
            ArrayList<Object> methodParamList = new ArrayList<>();

            boolean headerAdded = false;

            Class[] parameterTypes = apiMethod.getParameterTypes();

            for (int i = 0, urlParamIndex = 0; i < parameterTypes.length; i++) {
                if (parameterTypes[i].getName().equals("java.util.Map")) {
                    if (!headerAdded) {
                        methodParamList.add(header);
                        headerAdded = true;
                    } else {
                        methodParamList.add(queryParams);
                    }
                } else if (parameterTypes[i].getName().equals("org.json.JSONObject")) {
                    methodParamList.add(requestBodyObject);
                } else if (parameterTypes[i].getName().equals("org.json.JSONArray")) {
                    methodParamList.add(requestBodyArray);
                } else if (parameterTypes[i].getName().equals("java.lang.String")) {
                    if (urlParamsList.size() == 0) {
                        methodParamList.add("");
                    }

                    if (urlParamIndex < urlParamsList.size()) {
                        methodParamList.add(urlParamsList.get(urlParamIndex));
                        urlParamIndex++;
                    } else if (urlParamIndex == urlParamsList.size() && urlParamsList.size() > 0) {
                        methodParamList.add("");
                    }
                }
            }

            methodParamArray = methodParamList.toArray();


            //Thread.sleep(3000);
            Object response = apiMethod.invoke(null, methodParamArray);

            /*** Verify result and record them in JSON array  ***/

            // verify "Correlation-ID" exists in header
            if (((HttpResponse) response).getHeaders() != null) {
                HashMap<String, List<String>> actualHeader = ((HttpResponse) response).getHeaders();
                Exception ex = new Exception("No valid \'Correlation-ID\' is contained within a failure response");
                String uuidRegex = "^[0-9a-f]{8}-[0-9a-f]{4}-[1-5][0-9a-f]{3}-[0-9a-f]{4}-[0-9a-f]{12}$";
                if (checkCorrelationId) {
                    if (actualHeader.containsKey("Correlation-ID")) {
                        String correlationIDValue = actualHeader.get("Correlation-ID").get(0);
                        if (!Pattern.matches(uuidRegex, correlationIDValue)) {
                            verification.put("Exception", ex.getMessage());
                            logger.info("There is no valid Correlation-ID for verificationName: " + verificationName);
                            throw ex;
                        } else {
                            verification.put("Correlation-ID", correlationIDValue);
                            logger.info("Correlation-ID for verificationName: " + verificationName + " is: " + correlationIDValue);
                        }
                    } else {
                        logger.info("There is no Correlation-ID for verificationName: " + verificationName);
//                    throw ex;
                    }
                }
            }


            // verify response code
            String actualCode = String.valueOf(((HttpResponse) response).getStatus());

            logger.info("actual code:" + actualCode);
            verification.put("ActualCode", actualCode);
            softAssert.assertEquals(actualCode, expectedCode, constructAssertionMessage(verificationName, "Response code is not equal to what is expected", null, null));
            if (!softAssert.getLatestCheck()) {
                verificationPass = false;
                logger.warn("Verification fail: expected code is: " + expectedCode + ", actual code is: " + actualCode);
            }


            expectedBody = verification.get("ExpectedBody");

            // Operates when response type is "HttpResponse<String>"
            if (((HttpResponse) response).getBody() != null && ((HttpResponse) response).getBody().getClass().getName().equals("java.lang.String")) {
                if (expectedBody != null && expectedBody.toString() != "null") {

                    String responseBody = (((HttpResponse) response).getBody()).toString();

                    //save response to file
                    //  FileOperator.writeContentToFile("C:\\Output\\homepage\\" + verificationName.replace("/","-"), (((HttpResponse) response).getBody()).toString(),true);
                    // FileOperator.writeContentToFile("C:\\Output\\homepage\\" + verificationName.replace("/","-")+"header", (((HttpResponse) response).getHeaders()).toString(),true);

                    for (int i = 0; i < ((JSONObject) expectedBody).length(); i++) {
                        String propertyValue = (((JSONObject) expectedBody).get("key" + i)).toString();
                        propertyValue = preProcessRequestString(propertyValue);
                        if (verificationMode.toLowerCase().equals("contains")) {
                            softAssert.assertTrue(responseBody.contains(propertyValue), constructAssertionMessage(verificationName, "Response body is not the same as what expected",
                                    expectedBody, responseBody));
                            if (!softAssert.getLatestCheck()) {
                                verificationPass = false;
                            }
                        } else if (verificationMode.toLowerCase().equals("notcontains")) {
                            softAssert.assertFalse(responseBody.contains(propertyValue), constructAssertionMessage(verificationName, "Response body is not the same as what expected",
                                    expectedBody, responseBody));
                            if (!softAssert.getLatestCheck()) {
                                verificationPass = false;
                            }
                        } else if (verificationMode.toLowerCase().equals("notcontainstext")) {
                            String parsedExpectedBody = responseBody.replaceAll("http[^,]*,","");
                            softAssert.assertFalse(parsedExpectedBody.contains(propertyValue), constructAssertionMessage(verificationName, "Response body is not the same as what expected",
                                    expectedBody, responseBody));
                            if (!softAssert.getLatestCheck()) {
                                verificationPass = false;
                            }
                        } else if (verificationMode.toLowerCase().equals("randomcontains")) {
                            softAssert.assertTrue(responseBody.contains(propertyValue), constructAssertionMessage(verificationName, "Response body is not the same as what expected",
                                    expectedBody, responseBody));
                            if (softAssert.getLatestCheck()) {
                                break;
                            }
                            if (!softAssert.getLatestCheck() && i == (((JSONObject) expectedBody).length() - 1)) {
                                verificationPass = false;
                            }
                        }

                    }
                }
            }
            // Operates when response type is "HttpResponse<JsonNode>"
            else if (((HttpResponse) response).getBody() != null && ((HttpResponse) response).getBody().getClass().getName().equals("com.mashape.unirest.http.JsonNode")) {
                HttpResponse<JsonNode> responseJsonNode = (HttpResponse<JsonNode>) response;
                // verify response body
                JSONObject responseBodyObject = null;
                JSONArray responseBodyArray = null;
                Object responseBody = null;
                if (responseJsonNode.getBody().isArray()) {
                    responseBody = responseBodyArray = responseJsonNode.getBody().getArray();
                    logger.info("actual body:" + responseBodyArray.toString(4));
                } else {

                    responseBody = responseBodyObject = responseJsonNode.getBody().getObject();
                    logger.info("actual body:" + responseBodyObject.toString(4));

                }
                verification.put("ActualBody", responseBody);

                if (expectedBodyObject != null || expectedBodyArray != null) {
                    retrieveAndStoreVariables(expectedBody, responseBody);
                    if (expectedBody instanceof JSONObject) {
                        expectedBody = new JSONObject(preProcessRequestString(expectedBody.toString()));
                    } else if (expectedBody instanceof JSONArray) {
                        expectedBody = new JSONArray(preProcessRequestString(expectedBody.toString()));
                    }

                    if (verificationMode.equals("Identical")) {
                        softAssert.assertTrue(ResultChecker.matchIdentical(responseBody, expectedBody), constructAssertionMessage(verificationName, "Response body is not the same as what expected",
                                expectedBody, responseBody));
                        if (!softAssert.getLatestCheck()) {
                            verificationPass = false;
                            logger.warn("Verification fail: actual body is not the same with what expected.");
                        }
                    } else if (verificationMode.startsWith("Format")) {
                        softAssert.assertTrue(ResultChecker.matchJSONFormat(responseBody, expectedBody), constructAssertionMessage(verificationName, "Response body should have a valid format as expected",
                                expectedBody, responseBody));
                        if (!softAssert.getLatestCheck()) {
                            verificationPass = false;
                            logger.warn("Verification fail: actual body is not what expected.");
                        }
                        if (verificationMode.contains("-")) {
                            String[] identicalProperties = verificationMode.substring(7).split(",");
                            softAssert.assertTrue(ResultChecker.matchJSONFormat(responseBody, expectedBody, identicalProperties), constructAssertionMessage(verificationName, "Response body should have a valid format as expected",
                                    expectedBody, responseBody));
                            if (!softAssert.getLatestCheck()) {
                                verificationPass = false;
                                logger.warn("Verification fail: some properties values are different:" + identicalProperties.toString());
                            }
                        }
                    } else if (verificationMode.startsWith(OmenConstants.VERIFICATION_MODE_ARRAY_FORMAT)) {
                        softAssert.assertTrue(ResultChecker.matchJSONFormat(verificationMode, responseBody, expectedBody), constructAssertionMessage(verificationName, "Response body should have a valid format as expected",
                                expectedBody, responseBody));
                        if (!softAssert.getLatestCheck()) {
                            verificationPass = false;
                            logger.warn("Verification fail: actual body is not what expected.");
                        }
                        if (verificationMode.contains("-")) {
                            String[] identicalProperties = verificationMode.substring(OmenConstants.VERIFICATION_MODE_ARRAY_FORMAT.length()).split(",");
                            softAssert.assertTrue(ResultChecker.matchJSONFormat(responseBody, expectedBody, identicalProperties), constructAssertionMessage(verificationName, "Response body should have a valid format as expected",
                                    expectedBody, responseBody));
                            if (!softAssert.getLatestCheck()) {
                                verificationPass = false;
                                logger.warn("Verification fail: some properties values are different:" + identicalProperties.toString());
                            }
                        }
                    } else if (verificationMode.toLowerCase().equals("contains")) {
                        softAssert.assertTrue(ResultChecker.contains(responseBody, expectedBody), constructAssertionMessage(verificationName, "Response body should have a valid format as expected",
                                expectedBody, responseBody));
                        if (!softAssert.getLatestCheck()) {
                            verificationPass = false;
                            logger.warn("Verification fail: actual body is not what expected.");
                        }
                    } else if (verificationMode.toLowerCase().equals("notcontains")) {
                        softAssert.assertTrue(ResultChecker.notContains(responseBody, expectedBody), constructAssertionMessage(verificationName, "Response body should have a valid format as expected",
                                expectedBody, responseBody));
                        if (!softAssert.getLatestCheck()) {
                            verificationPass = false;
                            logger.warn("Verification fail: actual body should not contains the content in expected");
                        }
                    }  else if (verificationMode.toLowerCase().equals("noduplicates")) {
                        softAssert.assertTrue(ResultChecker.noDuplicates(responseBody, expectedBody), constructAssertionMessage(verificationName, "Response body should have a valid format as expected",
                                expectedBody, responseBody));
                        if (!softAssert.getLatestCheck()) {
                            verificationPass = false;
                            logger.warn("Verification fail: actual body contains duplicates of keys in expected");
                        }
                    } else if (verificationMode.toLowerCase().equals("notcontainstext")) {
                        String parsedExpectedBody = responseBody.toString().replaceAll("http[^,]*,","\",");
                        responseBody = responseBodyArray = new JSONArray(parsedExpectedBody);
                        softAssert.assertTrue(ResultChecker.notContainsText(responseBody, (JSONObject)expectedBody), constructAssertionMessage(verificationName, "Response body should have a valid format as expected",
                                expectedBody, responseBody));
                        if (!softAssert.getLatestCheck()) {
                            verificationPass = false;
                            logger.warn("Verification fail: actual body should not contains the content in expected");
                        }
                    }
                }
            }


            if (verificationPass) {
                verification.put("Result", "Pass");
            } else {
                verification.put("Result", "Fail");
            }
        } catch (Exception ex) {
            verificationPass = false;
            String exceptionMessage = ex.toString();
            exceptionMessage = (exceptionMessage != null) ? exceptionMessage : "Not clear with the exception, please check";

            verification.put("Exception", exceptionMessage);
            logger.warn("Exception found: " + exceptionMessage + " Cause:" + ex.getCause());
            softAssert.assertTrue(false, constructAssertionMessage(verificationName, "Exception found: " + exceptionMessage, null, null));
            verification.put("Result", "Fail");
        }

        return verificationPass;
    }

    /***
     * 1.Execute method
     * 2.Check result
     * 3.Save result
     * @param method
     */
    public void executeMethod(String method) {
        logger.info("============================================================");
        logger.info("Current method:" + method);
        JSONObject verifications = testData.getJSONObject(method);
        ArrayList<String> verificationKeys = new ArrayList<>();

        for (Iterator<String> it = verifications.keys(); it.hasNext(); ) {
            verificationKeys.add(it.next());
        }

        methodPassedVerifications.clear();
        methodFailedVerificationsExpected.clear();
        methodFailedVerificationsNotExpected.clear();
        for (Iterator<String> it = verifications.keys(); it.hasNext(); ) {
            String verificationName = it.next();
            if (!verificationName.startsWith("A-")) {

                /*** stores all verifications starting with '.v' into a list***/
                if (verificationKeys.toString().contains(verificationName.substring(0, ((verificationName.indexOf(".v") >= 0) ? verificationName.indexOf(".v") : verificationName.length())) + ".v")) {
                    ArrayList<Integer> verificationVersions = new ArrayList<>();
                    for (String key : verificationKeys
                    ) {
                        if (key.contains(verificationName + ".v")) {
                            String versionString = key.substring(key.indexOf(".v") + 2);
                            int versionNumber = 0;
                            if ((versionNumber = Integer.valueOf(versionString)) > 0) {
                                verificationVersions.add((versionNumber));
                            }
                        }
                    }

                    /*** do not run current verification if it contains .v but not equal to current version ***/
                    if (verificationName.contains(".v")) {
                        String versionString = verificationName.substring(verificationName.indexOf(".v") + 2);
                        int versionNumber = 0;
                        if ((versionNumber = Integer.valueOf(versionString)) > 0) {
                            if (!(Integer.valueOf(SuiteCommon.getVersion()) == versionNumber)) {
                                logger.info(verificationName + " will not be run for this version");
                                continue;
                            }
                        }
                    }
                    /*** do not run current verification if it doesn't contain .v but there are .v verifications that of current running api version***/
                    else {
                        if (verificationVersions.contains(Integer.valueOf(SuiteCommon.getVersion()))) {
                            logger.info(verificationName + " will not be run for this version, there is an exactly mapped one");
                            continue;
                        }
                    }
                }
            }

            logger.info("============================================================");
            logger.info("Verification-" + verificationName + "-started");
            Method apiMethod;
            if (verificationName.contains("-Switch-")) {
                String newMethodName = verificationName.substring(verificationName.indexOf("-Switch-") + 8);
                if (newMethodName.contains(".v")) {
                    newMethodName = newMethodName.substring(0, newMethodName.indexOf(".v"));
                }
                apiMethod = methodPool.get(newMethodName);
            } else {
                apiMethod = methodPool.get(method);
            }
            logger.info("API method used:" + apiMethod.getName());

            JSONObject verification = verifications.getJSONObject(verificationName);
            if (GetEnvironmentVariables.getInstance().getExcludeDisabled()) {
                if (verification.getBoolean("Disabled")) {
                    continue;
                }
            }

            boolean result = executeVerification(verificationName, verification, apiMethod);
            if (result) {
                JSONObject passedVerification = new JSONObject().put(verificationName, verification);
                methodPassedVerifications.add(passedVerification);
                allPassedVerifications.add(passedVerification);
                logger.info("Verification-" + verificationName + "-PASS");
            } else {
                if (!GetEnvironmentVariables.getInstance().getPassFailedWithBug()) {
                    JSONObject failedVerificationsNotExpected = new JSONObject().put(verificationName, verification);
                    methodFailedVerificationsNotExpected.add(failedVerificationsNotExpected);
                    allFailedVerificationsNotExpected.add(failedVerificationsNotExpected);
                    logger.info("Verification-" + verificationName + "-FAIL");
                } else {
                    if (verification.getString("RelatedBug").trim().equals("")) {
                        JSONObject failedVerificationsNotExpected = new JSONObject().put(verificationName, verification);
                        methodFailedVerificationsNotExpected.add(failedVerificationsNotExpected);
                        allFailedVerificationsNotExpected.add(failedVerificationsNotExpected);
                        logger.info("Verification-" + verificationName + "-FAIL");
                    } else {
                        JSONObject failedVerificationsExpected = new JSONObject().put(verificationName, verification);
                        methodFailedVerificationsExpected.add(failedVerificationsExpected);
                        allFailedVerificationsExpected.add(failedVerificationsExpected);
                        logger.info("Verification-" + verificationName + "-FAIL");
                    }
                }
            }
        }
    }

    public Object getActualBody(String methodName, String verification) {
        return testData.getJSONObject(methodName).getJSONObject(verification).get("ActualBody");
    }

    public String constructAssertionMessage(String verificationName, String message, Object expectedBody, Object actualBody) {
        StringBuilder assertionMessage = new StringBuilder();
        assertionMessage.append(verificationName + ": " + message + ":");
        if (expectedBody instanceof JSONObject || expectedBody instanceof JSONArray) {
            assertionMessage.append("\n");
            assertionMessage.append("expected body is:\n");
            if (expectedBody instanceof JSONObject) {
                assertionMessage.append(((JSONObject) expectedBody).toString(4));
            } else {
                assertionMessage.append(((JSONArray) expectedBody).toString(4));
            }

        }

        if (actualBody instanceof JSONObject || actualBody instanceof JSONArray) {
            assertionMessage.append("\n");
            assertionMessage.append("actual body is:\n");
            if (actualBody instanceof JSONObject) {
                assertionMessage.append(((JSONObject) actualBody).toString(4));
            } else {
                assertionMessage.append(((JSONArray) actualBody).toString(4));
            }
        }

        return assertionMessage.toString();
    }

    public void generateCSVReport() throws IOException {
        /*** Generate summary in logger first ***/
        logger.info("Total verification number:" + (allPassedVerifications.size() + allFailedVerificationsExpected.size() + allFailedVerificationsNotExpected.size()));
        logger.info("Passed verification number:" + allPassedVerifications.size());
        logger.info("Expected failed verification number:" + allFailedVerificationsExpected.size());
        logger.info("Not expected failed verification number:" + allFailedVerificationsNotExpected.size());

        logger.info(("Passed verifications:") + getAllPassedVerifications());
        logger.info(("Expected failed verifications:") + getAllFailedVerificationsExpected());
        logger.info(("Not expected failed verifications:") + getAllFailedVerificationsNotExpected());

        /*** Create report folder and report file ***/
        String reportFolderPath = GetEnvironmentVariables.getInstance().getProjectRootDirectory() + "\\reports\\" + currentAPI;
        File reportFolder = new File(reportFolderPath);
        if (!reportFolder.exists() || !reportFolder.isDirectory()) {
            reportFolder.mkdirs();
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String reportFilePath = reportFolderPath + "\\" + currentAPI + "_" + "v" + SuiteCommon.getVersion()
                + "_" + GetEnvironmentVariables.getInstance().getCurrentStack().toLowerCase() + "_" + dateFormat.format(new Date()) + ".csv";

        if (!FileOperator.createFile(reportFilePath)) {
            return;
        }

        /*** Write test result into report file ***/
        FileOperator.writeContentToFile(reportFilePath, "Method,Verification,Exception,VerificationMode,HeaderType,HeaderContent,HeaderParams,UrlParams,QueryString,RequestBody,ExpectedCode,ActualCode,ExpectedBody,ActualBody,Result,RelatedBug,CorrelationID,Comment,Report Exception", true);
        Iterator<String> methodKeys = testData.keys();
        while (methodKeys.hasNext()) {
            String methodName = methodKeys.next();
            Object methodData = testData.get(methodName);
            if (methodData instanceof JSONObject) {
                Iterator<String> verificationKeys = ((JSONObject) methodData).keys();
                while (verificationKeys.hasNext()) {
                    String verificationName = verificationKeys.next();
                    JSONObject verification = ((JSONObject) methodData).getJSONObject(verificationName);
                    if (verification.has("Result")) {
                        String dataLine = methodName + "," + "\"" + verificationName + "\"" + "," + buildVerificationDataLine(verification);
                        FileOperator.writeContentToFile(reportFilePath, dataLine, true);
                    }
                }
            }
        }

        /*** Write summary info to report file ***/
        FileOperator.writeContentToFile(reportFilePath, "Passed verification number:," + allPassedVerifications.size(), true);
        FileOperator.writeContentToFile(reportFilePath, "Expected failed verification number:," + allFailedVerificationsExpected.size(), true);
        FileOperator.writeContentToFile(reportFilePath, "Not expected failed verification number:," + allFailedVerificationsNotExpected.size(), true);
        FileOperator.writeContentToFile(reportFilePath, "Passed verifications:," + "\"" + getAllPassedVerifications() + "\"", true);
        FileOperator.writeContentToFile(reportFilePath, "Expected failed verifications:," + "\"" + getAllFailedVerificationsExpected() + "\"", true);
        FileOperator.writeContentToFile(reportFilePath, "Not expected failed verifications:," + "\"" + getAllFailedVerificationsNotExpected() + "\"", true);

        /*** Open file after execution ***/
        if (GetEnvironmentVariables.getInstance().getOpenCSVReport().toLowerCase().equals("true")) {
            FileOperator.openFile(reportFilePath);
        }
    }

    /***
     * Returns the variable name that is contained in shell "{__StoreAsVariable($XXXX)}"
     * @param propertyValue The property or string that contains variable name, returns "" if not found
     * @return The variable name List<String>
     */
    public List<String> getMappedVariableNames(String propertyValue) {
        List<String> mappedVariabls = new ArrayList<>();
        String bshRegex = "\\$\\{__StoreAsVariable\\(\\$.+?\\)}";
        Pattern pattern = Pattern.compile(bshRegex);
        Matcher m = pattern.matcher(propertyValue);
        while (m.find()) {
            String variableName = m.group().replace("${__StoreAsVariable($", "");
            variableName = variableName.replace(")}", "");
            mappedVariabls.add(variableName);
        }

        return mappedVariabls;
    }

    /***
     * Replace all variables contained in the string by all variables stored, excluding '${__StoreAsVariable()}
     * @param target
     * @return
     */
    public String replaceStringWithVariables(String target) {
        String result = target;

        List<String> storeSentences = new ArrayList<>();
        String bshRegex = "\\$\\{__StoreAsVariable\\(\\$.+?\\)}";
        Pattern pattern = Pattern.compile(bshRegex);
        Matcher m = pattern.matcher(target);
        while (m.find()) {
            String matchedStoreSentence = m.group();
            storeSentences.add(matchedStoreSentence);
            result = result.replaceFirst(bshRegex, "\\${__StoreAsVariable(\\$VARIABLE)}");
        }

        for (Map.Entry<String, String> entry : variables.entrySet()) {
            String name = entry.getKey();
            String value = entry.getValue();

            result = result.replace(name, value);
        }

        String variableRegex = "\\$\\{__StoreAsVariable\\(\\$VARIABLE\\)}";
        for (String storeSentence : storeSentences
        ) {
            storeSentence = storeSentence.replace("$", "\\$");
            result = result.replaceFirst(variableRegex, storeSentence);
        }

        return result;
    }

    /***
     * Retrieve all BeanShell code in a string and store that into bshSegments
     * Replace all BeanShell code with a "$bshCode"
     * @param codeString
     * @return the codeString that has been manipulated
     */
    public String executeBeanShellCodes(String codeString) throws EvalError {
        if (codeString == null || codeString.equals("")) {
            return "";
        }

        String bshRegex = "\\$\\{__BeanShell\\(.+?\\)}";
        Pattern pattern = Pattern.compile(bshRegex);
        Matcher m = pattern.matcher(codeString);
        while (m.find()) {
            String bshCode = m.group().replace("${__BeanShell(", "");
            bshCode = bshCode.replace(")}", "");

            String importString = "import common.*;import engine.*;import report.*;" +
                    "import signintool.*;import testcase.*;import utility.*;";
            bshCode = importString + bshCode;
            String bshCodeForExecution = bshCode;
            Object bshResult = bshInterpreter.eval(bshCodeForExecution);
            String bshResultString = (bshResult == null) ? "" : bshResult.toString();
            codeString = codeString.replaceFirst(bshRegex, bshResultString);
        }

        return codeString;
    }


    /***
     * Pre process request string before putting into api command using executing BeanShell, replace variables
     * @param input The input string including UrlParams, QueryString and RequestBody property value
     * @return The request string that has been processed
     */
    public String preProcessRequestString(String input) throws EvalError {
        String result;

        result = replaceStringWithVariables(input);
        result = executeBeanShellCodes(result);


        return result;
    }


    /***
     * Check if shell ${__StoreAsVariable($XXXX)} can be found within a expected JSONObject/JSONArray and then gets the mapping value from the actual JSON response
     * @param target The expected JSONObject/JSONArray that contains variable storing shell
     * @param actual The actual response body
     */
    public void retrieveAndStoreVariables(Object target, Object actual) {

        if (!(target instanceof JSONObject) && !(target instanceof JSONArray)) {
            return;
        }

        if (!(actual instanceof JSONObject) && !(actual instanceof JSONArray)) {
            return;
        } else {
            if (target instanceof JSONObject && actual instanceof JSONObject) {
                JSONObject targetObject = (JSONObject) target;
                JSONObject actualObject = (JSONObject) actual;

                for (Object key : targetObject.keySet()) {
                    String keyStr = (String) key;
                    Object keyValue = targetObject.get(keyStr);
                    if (keyValue instanceof String) {
                        List<String> mappedVariablesNames = getMappedVariableNames((String) keyValue);
                        String variableName = mappedVariablesNames.size() > 0 ? mappedVariablesNames.get(0) : "";
                        if (variableName != "") {
                            Object actualKeyValue = actualObject.get(keyStr);
                            if (actualKeyValue instanceof String) {
                                variables.put("$" + variableName, (String) actualKeyValue);
                            }
                        }
                    } else if (keyValue instanceof JSONObject) {
                        Object actualKeyValue = actualObject.get(keyStr);
                        if (actualKeyValue instanceof JSONObject) {
                            retrieveAndStoreVariables(keyValue, actualKeyValue);
                        }
                    }
                }
            } else if (target instanceof JSONArray && actual instanceof JSONArray) {
                JSONArray targetArray = (JSONArray) target;
                JSONArray actualArray = (JSONArray) actual;
                if (targetArray.length() != actualArray.length()) return;

                for (int i = 0; i < targetArray.length(); i++) {
                    retrieveAndStoreVariables(targetArray.get(i), actualArray.get(i));
                }
            }
        }
    }


    /***
     *  Stores a common variable into testData and replace all variables with the specified value
     * @param name variable name
     * @param value variable value
     * ***/
    public void storeCommonVariable(String name, String value) {
        variables.put(name, value);
    }


    /***
     * Constructs one data line for CSV report file
     * @param verification one verification in JSON object
     * @return The line string
     */
    public String buildVerificationDataLine(JSONObject verification) {
        StringBuilder dataLine = new StringBuilder();
        try {
            dataLine.append(verification.has("Exception") ? "\"" + verification.getString("Exception") + "\"" + "," : ",");
            dataLine.append(verification.has("VerificationMode") ? "\"" + verification.getString("VerificationMode") + "\"" + "," : ",");
            dataLine.append(verification.has("HeaderType") ? "\"" + verification.getString("HeaderType") + "\"" + "," : ",");
            dataLine.append(verification.has("HeaderContent") ? "\"" + verification.getString("HeaderContent") + "\"" + "," : ",");
            dataLine.append(verification.has("HeaderParams") ? "\"" + verification.getString("HeaderParams") + "\"" + "," : ",");
            dataLine.append(verification.has("UrlParams") ? "\"" + verification.getString("UrlParams") + "\"" + "," : ",");
            dataLine.append(verification.has("QueryString") ? "\"" + verification.getString("QueryString") + "\"" + "," : ",");
            Object requestBody = null;
            String requestBodyString = "";
            if (verification.has("RequestBody")) {
                requestBody = verification.get("RequestBody");
            }
            if (requestBody instanceof JSONObject) {
                requestBodyString = ((JSONObject) requestBody).toString();
            } else if (requestBody instanceof JSONArray) {
                requestBodyString = ((JSONArray) requestBody).toString();
            }
            dataLine.append("\"" + requestBodyString.replace("\"", "\"\"") + "\"" + ",");
            dataLine.append(verification.has("ExpectedCode") ? verification.getString("ExpectedCode") + "," : ",");
            dataLine.append(verification.has("ActualCode") ? verification.getString("ActualCode") + "," : ",");
            Object expectedBody = null;
            String expectedBodyString = "";
            if (verification.has("ExpectedBody")) {
                expectedBody = verification.get("ExpectedBody");
            }

            if (expectedBody instanceof JSONObject) {
                expectedBodyString = ((JSONObject) expectedBody).toString();
            } else if (expectedBody instanceof JSONArray) {
                expectedBodyString = ((JSONArray) expectedBody).toString();
            }

            dataLine.append("\"" + expectedBodyString.replace("\"", "\"\"") + "\"" + ",");

            Object actualBody = null;
            String actualBodyString = "";
            if (verification.has("ActualBody")) {
                actualBody = verification.get("ActualBody");
            }
            if (actualBody instanceof JSONObject) {
                actualBodyString = ((JSONObject) actualBody).toString();
            } else if (actualBody instanceof JSONArray) {
                actualBodyString = ((JSONArray) actualBody).toString();
            } else if (actualBody instanceof String) {
                actualBodyString = actualBody.toString();
            }

            dataLine.append("\"" + actualBodyString.replace("\"", "\"\"") + "\"" + ",");


            dataLine.append(verification.getString("Result") + ",");
            dataLine.append("\"" + verification.getString("RelatedBug").replace("\"", "\"\"") + "\"" + ",");
            dataLine.append(verification.has("Correlation-ID") ? "\"" + verification.getString("Correlation-ID") + "\"" + "," : ",");
            dataLine.append("\"" + verification.getString("Comment").replace("\"", "\"\"") + "\"");
        } catch (Exception ex) {
            dataLine.append("Exception:" + ex.getMessage());
        }
        return dataLine.toString();
    }
}
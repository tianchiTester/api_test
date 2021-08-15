package engine;

import org.json.JSONArray;
import org.json.JSONObject;
import common.GetEnvironmentVariables;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class RequestCreator {
    protected Map<String, String> testDataPool;
    protected JSONObject testData;

    public void initiateTestDataPool(String fileName)
    {
        testDataPool = new HashMap<>();

        String csvFile = GetEnvironmentVariables.getInstance().getProjectRootDirectory() + "\\resources\\" + fileName + ".csv";
        String separator = GetEnvironmentVariables.getInstance().getCSVSeparator();
        String line = null;

        try (BufferedReader br = new BufferedReader(new FileReader(csvFile))) {
            while ((line = br.readLine()) != null) {
                String[] data = line.split(separator);
                String key = removeUselessCharacters(data[0]) + removeUselessCharacters(data[1]);
                String value = removeUselessCharacters(data[2]);
                testDataPool.put(key, value);
            }
        }
        catch (IOException e){
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    /***
     * 1. Extract request body from testData
     * 2. Send out request with body and API specified
     * 3. Verify response got
     * @param method
     * @param verification
     * @param apiName
     */
    public void executeVerification(String method, String verification, String apiName)
    {


    }

    /***
     *Create a request object containing single JSON format object based on the request data file
     * @param method the first column in the data file
     * @param caseName the second column in the data file
     * @return the JSON format object
     */
    public JSONObject createObject(String method, String caseName){
        String jsonValue = testDataPool.get(method + caseName);

        JSONObject requestBody = new JSONObject();
        String value = removeUselessCharacters(jsonValue);
        String[] properties = value.split(GetEnvironmentVariables.getInstance().getPropertySeparator());
        for (int i = 0; i < properties.length; i++) {
            String[] propertyPair = removeUselessCharacters(properties[i]).split(GetEnvironmentVariables.getInstance().getPropertyPairSeparator());
            if (propertyPair.length == 2) {
                requestBody.put(removeUselessCharacters(propertyPair[0]), removeUselessCharacters(propertyPair[1]));
            }
            else if (propertyPair.length == 1){
                requestBody.put(removeUselessCharacters(propertyPair[0]), "");
            }
        }

        return requestBody;
    }

    /***
     * Create a request object containing array of JSON objects based on test data file
     * @param method the first column in file
     * @param caseName the second column in file
     * @return a request object of JSONArray
     */
    public JSONArray createArray(String method, String caseName){
        String jsonValue = testDataPool.get(method + caseName);
        JSONArray requestBody = new JSONArray();
        String jsonArraySeparator =GetEnvironmentVariables.getInstance().getArraySeparator();
        String[] valueArray = jsonValue.split(jsonArraySeparator);
        for (int i = 0; i < valueArray.length; i++)
        {
            JSONObject jsonObject = new JSONObject();
            String value = removeUselessCharacters(valueArray[i]);
            String[] properties = value.split(GetEnvironmentVariables.getInstance().getPropertySeparator());
            for (int j = 0; j < properties.length; j++)
            {
                String[] propertyPair = removeUselessCharacters(properties[j]).split(GetEnvironmentVariables.getInstance().getPropertyPairSeparator());
                jsonObject.put(removeUselessCharacters(propertyPair[0]), removeUselessCharacters(propertyPair[1]));
            }
            requestBody.put(jsonObject);
        }

        return requestBody;
    }

    /***
     * Create an array list containing the request objects whose keys starting with method + caseNameAsPrefix
     * @param method the first column in the data file
     * @param caseNameAsPrefix the prefix of the second column like "OK-" in "OK-duplicateIds"
     * @param forArray whether each request object contains array of JSON objects
     * @return an array list containing the request JSON format objects
     */
    public ArrayList createObjects(String method, String caseNameAsPrefix, boolean... forArray)
    {
        boolean createArray = false;
        if (forArray.length > 0 && forArray[0] == true) createArray = true;

        ArrayList requestObjects = null;
        if (!createArray)
        {
            requestObjects = new ArrayList<JSONObject>();
        }
        else
        {
            requestObjects = new ArrayList<JSONArray>();
        }

        Iterator iterator = testDataPool.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry entry = (Map.Entry) iterator.next();
            String currentKey = entry.getKey().toString();
            if (currentKey.contains(method + caseNameAsPrefix))
            {
                if (!createArray) {
                    JSONObject newObject = createObject(method, currentKey.substring(method.length()));
                    requestObjects.add(newObject);
                }
                else
                {
                    JSONArray newArray = createArray(method, currentKey.substring(method.length()));
                    requestObjects.add(newArray);
                }
            }
        }

        return requestObjects;
    }

    private String removeUselessCharacters(String input)
    {
        String result = null;
        result = input.replace("\\t", "");
        result = result.trim();
        return result;
    }

}

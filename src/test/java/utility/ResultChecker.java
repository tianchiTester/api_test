package utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Iterator;

public class ResultChecker {
    public static boolean matchJSONArrayFormat(JSONArray actualFormat, JSONArray expectedFormat, String... identicalProperties) {
        boolean matched = true;

        if (actualFormat.length() != expectedFormat.length()) {
            matched = false;
            return matched;
        }

        for (int i = 0; i < actualFormat.length(); i++) {
            if (!matchJSONObjectFormat(actualFormat.getJSONObject(i), expectedFormat.getJSONObject(i), identicalProperties)) {
                matched = false;
                return matched;
            }
        }

        return matched;
    }

    /**
     * Match JSON structure only in array
     * @param actualFormat - Actual format
     * @param expectedFormat - Expected format
     * @param identicalProperties - properties
     * @return true/false
     */
    public static boolean matchJSONArrayFormatStructure(JSONArray actualFormat, JSONArray expectedFormat, String... identicalProperties) {
        boolean matched = true;
        if (!matchJSONObjectFormat(actualFormat.getJSONObject(0), expectedFormat.getJSONObject(0), identicalProperties)) {
            matched = false;
            return matched;
        }
        return matched;
    }

    public static boolean matchJSONArrayFormatWithJSONObject(JSONArray actualFormat, JSONObject expectedFormat, String... identicalProperties) {
        boolean matched = true;

        for (int i = 0; i < actualFormat.length(); i++) {
            if (!matchJSONObjectFormat(actualFormat.getJSONObject(i), expectedFormat, identicalProperties)) {
                matched = false;
                break;
            }
        }

        return matched;
    }

    public static boolean matchJSONObjectFormat(JSONObject actualFormat, JSONObject expectedFormat, String... identicalProperties) {
        boolean matched = true;

        ArrayList<String> actualList = new ArrayList<String>();
        ArrayList<String> expectedList = new ArrayList<String>();
        Iterator<String> expectedKeys = expectedFormat.keys();
        Iterator<String> actualKeys = actualFormat.keys();

        while (actualKeys.hasNext()) {
            actualList.add(actualKeys.next());
        }

        while (expectedKeys.hasNext()) {
            expectedList.add(expectedKeys.next());
        }

        if (actualList.size() != expectedList.size() && identicalProperties.length == 0) {
            matched = false;
            return matched;
        } else {
            for (int i = 0; i < actualList.size(); i++) {
                String currentKey = actualList.get(i);
                for (int j = 0; j < expectedList.size(); j++) {
                    if (currentKey.equals(expectedList.get(j))) {
                        if (identicalProperties.length > 0) {
                            for (int k = 0; k < identicalProperties.length; k++) {
                                if (identicalProperties[k].equals(currentKey)) {
                                    if (!(actualFormat.get(currentKey).toString().equals(expectedFormat.get(currentKey).toString()))) {
                                        matched = false;
                                        return matched;
                                    }
                                }
                            }
                        }
                        matched = true;
                        break;
                    }
                    matched = false;
                }
            }
        }

        return matched;
    }

    public static boolean notContainsText(Object actualValue, JSONObject expected) {
        //Iterate all keys in expected
        Iterator<String> keys = expected.keys();
        while (keys.hasNext()) {
            //If actual has the key, then compare the value
            Object expectedValue = expected.get(keys.next());
            //If the value is the equal, then return false
            if(actualValue.toString().contains(expectedValue.toString()))
            {
                return false;
            }
        }
        //Compare all keys in expected, if all keys contains in actual and the key values are equal, then return true
        return true;
    }

    public static boolean matchIdentical(Object actualBody, Object expectedBody) {
        if (actualBody == null) {
            return false;
        }

        if (!(actualBody instanceof JSONObject) && !(actualBody instanceof JSONArray)) {
            return false;
        }

        if (!(expectedBody instanceof JSONObject) && !(expectedBody instanceof JSONArray)) {
            return false;
        }

        if (actualBody instanceof JSONObject && expectedBody instanceof JSONArray) {
            if (((JSONArray) expectedBody).length() == 1) {
                if (((JSONArray) expectedBody).getJSONObject(0).similar(actualBody)) {
                    return true;
                }
            }

            return false;
        }

        if (actualBody instanceof JSONObject && expectedBody instanceof JSONObject) {
            return ((JSONObject) actualBody).similar(expectedBody);
        }

        if (actualBody instanceof JSONArray && expectedBody instanceof JSONObject) {
            for (int i = 0; i < ((JSONArray) actualBody).length(); i++) {
                if (!((JSONArray) actualBody).getJSONObject(i).similar(expectedBody)) {
                    return false;
                }
            }

            return true;
        }

        if (actualBody instanceof JSONArray && expectedBody instanceof JSONArray) {
            return ((JSONArray) actualBody).similar(expectedBody);
        }

        return false;
    }

    private static boolean matchJSONFormatByType(Object actualFormat, Object expectedFormat, String verificationMode, String... identicalProperties) {
        if (actualFormat == null) {
            return false;
        }

        if (!(actualFormat instanceof JSONObject) && !(actualFormat instanceof JSONArray)) {
            return false;
        }

        if (!(expectedFormat instanceof JSONObject) && !(expectedFormat instanceof JSONArray)) {
            return false;
        }

        if (actualFormat instanceof JSONObject && expectedFormat instanceof JSONArray) {
            return false;
        }

        if (actualFormat instanceof JSONObject && expectedFormat instanceof JSONObject) {
            return matchJSONObjectFormat((JSONObject) actualFormat, (JSONObject) expectedFormat, identicalProperties);
        }

        if (actualFormat instanceof JSONArray && expectedFormat instanceof JSONObject) {
            return matchJSONArrayFormatWithJSONObject((JSONArray) actualFormat, (JSONObject) expectedFormat, identicalProperties);
        }

        if (actualFormat instanceof JSONArray && expectedFormat instanceof JSONArray) {
            if (OmenConstants.VERIFICATION_MODE_ARRAY_FORMAT.equals(verificationMode)) {
                return matchJSONArrayFormatStructure((JSONArray) actualFormat, (JSONArray) expectedFormat);
            }
            return matchJSONArrayFormat((JSONArray) actualFormat, (JSONArray) expectedFormat);
        }

        return false;
    }

    public static boolean matchJSONFormat(Object actualFormat, Object expectedFormat, String... identicalProperties) {
        return matchJSONFormatByType(actualFormat, expectedFormat, null, identicalProperties);
    }

    public static boolean matchJSONFormat(String verificationMode, Object actualFormat, Object expectedFormat, String... identicalProperties) {
        return matchJSONFormatByType(actualFormat, expectedFormat, verificationMode, identicalProperties);
    }


    public static boolean contains(Object actualBody, Object expectedBody) {
        if (actualBody == null) {
            return false;
        }

        if (!actualBody.getClass().equals(expectedBody.getClass())) {
            return false;
        }

        if (actualBody instanceof JSONObject && expectedBody instanceof JSONObject) {
            return ((JSONObject) actualBody).similar(expectedBody);
        }

        if (actualBody instanceof JSONArray && expectedBody instanceof JSONArray) {
            Iterator expected = ((JSONArray) expectedBody).iterator();
            while (expected.hasNext()) {
                if (!(actualBody.toString().contains(expected.next().toString()))) {
                    return false;
                }
            }
            return true;
        }

        if (actualBody.toString().contains(expectedBody.toString())) {
            return true;
        }
        return false;
    }

    public static boolean notContains(Object actualBody, Object expectedBody) {
        if (actualBody == null) {
            return false;
        }

        if (actualBody instanceof JSONObject && expectedBody instanceof JSONObject) {
            return ((JSONObject) actualBody).similar(expectedBody);
        }

        if (actualBody instanceof JSONArray && expectedBody instanceof JSONArray) {
            Iterator expected = ((JSONArray) expectedBody).iterator();
            while (expected.hasNext()) {
                if ((actualBody.toString().contains(expected.next().toString()))) {
                    return false;
                }
            }
            return true;
        }
        if (actualBody.toString().contains(expectedBody.toString())) {
            return false;
        }
        return false;
    }

    public boolean contains(JSONObject actual, JSONObject expected) {

        //Iterate all keys in expected
        Iterator<String> keys = expected.keys();
        while (keys.hasNext()) {
            //If actual has the key, then compare the value
            if (actual.has(keys.next())) {
                Object actualValue = actual.get(keys.next());
                Object expectedValue = expected.get(keys.next());
                //If the value is not the equal, then return false
                if (!actualValue.equals(expectedValue)) {
                    return false;
                }
            }
            //If actual doesn't have the key, return false
            else {
                return false;
            }

        }
        //Compare all keys in expected, if all keys contains in actual and the key values are equal, then return true
        return true;
    }

    public static boolean noDuplicates(Object actualBody, Object expectedBody) {
        int dupeCount = 0;
        Iterator actual = ((JSONArray) actualBody).iterator();
        Iterator expected = ((JSONArray) expectedBody).iterator();
        while(expected.hasNext()) {
            String expectedStr = expected.next().toString();
            //This is a hacky fix, we will add a proper fix down the line
            expectedStr=expectedStr.replace("{","");
            expectedStr=expectedStr.replace("}","");
            while (actual.hasNext()) {
                String actualStr = actual.next().toString();
                if (actualStr.contains(expectedStr.toString())) {
                    dupeCount = dupeCount + 1;
                }
            }
        }

        if(dupeCount>1){
            return false;
        }
        return true;
    }
}
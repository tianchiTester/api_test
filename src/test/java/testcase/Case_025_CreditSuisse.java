package testcase;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import common.GetEnvironmentVariables;
import engine.CrrditSuisseHeader;
import engine.Executor;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.Map;

public class Case_025_CreditSuisse extends  SuiteCommon{
    @BeforeClass
    public void setUp() throws IOException, ClassNotFoundException, NoSuchMethodException {
        executor = new Executor(GetEnvironmentVariables.getInstance().getCreditSuisseJsonFileName());
        String targetAPIClass = "single.CreditSuisseAPI";
        Class<?> creditSuisseAPI = Class.forName(targetAPIClass);
        Method  getCookies = creditSuisseAPI.getMethod("getCookies", Map.class);
        executor.addMethodToPool(GET, getCookies);
        CrrditSuisseHeader crrditSuisseHeader = new CrrditSuisseHeader(executor);
        executor.setHeaderCreator(crrditSuisseHeader);
    }

    @Test(groups = {GET})
    public void getLocalizationValues_getCookies() {
        logger.info("Executing test case: " + currentTestCaseName);
        logger.info("Target API: GET /bin/i18n/getLocalizationValues?getCookies");
        executor.executeMethod(GET);
        executor.checkCaseResult();
    }

}

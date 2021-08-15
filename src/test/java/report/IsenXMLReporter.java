package report;

import org.json.JSONObject;
import org.testng.IReporter;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.Reporter;
import org.testng.internal.Utils;
import org.testng.reporters.XMLReporterConfig;
import org.testng.reporters.XMLStringBuffer;
import org.testng.reporters.XMLSuiteResultWriter;
import org.testng.util.TimeUtils;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import testcase.SuiteCommon;
import common.CommonVariables;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.util.*;

/**
 * The main entry for the XML generation operation
 */
public class IsenXMLReporter implements IReporter {

    public static final String FILE_NAME = "xmlresult-report.xml";
    private static final String JVM_ARG = "testng.report.xml.name";


    private final XMLReporterConfig config = new XMLReporterConfig();
    private XMLStringBuffer rootBuffer;

    private ArrayList<String> queryKeys = new ArrayList<>();
    private JSONObject allSuiteResult;

    public int getTestPassedVerificationsCount(String suiteName, String testName)
    {
        int result = 0;
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        testResult.keySet();
        for (Iterator<String> it = testResult.keys(); it.hasNext(); ) {
            result += testResult.getJSONObject(it.next()).getInt(CommonVariables.CPVC);
        }
        return result;
    }

    public int getTestFailedVerificationsExpectedCount(String suiteName, String testName)
    {
        int result = 0;
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        testResult.keySet();
        for (Iterator<String> it = testResult.keys(); it.hasNext(); ) {
            result += testResult.getJSONObject(it.next()).getInt(CommonVariables.CFVEC);
        }
        return result;
    }

    public int getTestFailedVerificationsNotExpectedCount(String suiteName, String testName)
    {
        int result = 0;
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        testResult.keySet();
        for (Iterator<String> it = testResult.keys(); it.hasNext(); ) {
            result += testResult.getJSONObject(it.next()).getInt(CommonVariables.CFVNEC);
        }
        return result;
    }

    public int getClassPassedVerificationsCount(String suiteName, String testName, String className)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        return testResult.getInt(CommonVariables.CPVC);
    }

    public int getClassFailedVerificationsExpectedCount(String suiteName, String testName, String className)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        return testResult.getInt(CommonVariables.CFVEC);
    }

    public int getClassFailedVerificationsNotExpectedCount(String suiteName, String testName, String className)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        return testResult.getInt(CommonVariables.CFVNEC);
    }

    public int getMethodPassedVerificationsCount(String suiteName, String testName, String className, String methodName)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getInt(CommonVariables.MPVC);
    }

    public int getMethodFailedVerificationsExpectedCount(String suiteName, String testName, String className, String methodName)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getInt(CommonVariables.MFVEC);
    }

    public int getMethodFailedVerificationsNotExpectedCount(String suiteName, String testName, String className, String methodName)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getInt(CommonVariables.MFVNEC);
    }

    public String getMethodPassedVerifications(String suiteName, String testName, String className, String methodName)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getString(CommonVariables.MPV);
    }

    public String getMethodFailedVerificationsExpected(String suiteName, String testName, String className, String methodName)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getString(CommonVariables.MFVE);
    }

    public String getMethodFailedVerificationsNotExpected(String suiteName, String testName, String className, String methodName)
    {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getString(CommonVariables.MFVNE);
    }



    @Override
    public void generateReport(List<XmlSuite> xmlSuites, List<ISuite> suites,
                               String outputDirectory) {
        File directory = new File("");
        outputDirectory = directory.getAbsolutePath();
        String xmlReportFilePath = outputDirectory + "\\xmlresult-report.xml";
        config.setOutputDirectory(outputDirectory);

        allSuiteResult = SuiteCommon.getTestResult();
        try {
            generateXMLFile(xmlReportFilePath, suites);
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        // Calculate passed/failed/skipped
//        int passed = 0;
//        int failed = 0;
//        int skipped = 0;
//        int ignored = 0;
//        for (ISuite s : suites) {
//            Map<String, ISuiteResult> suiteResults = s.getResults();
//            for (ISuiteResult sr : suiteResults.values()) {
//                ITestContext testContext = sr.getTestContext();
//                passed += getTestPassedVerificationsCount(testContext.getSuite().getName(), testContext.getName());
//                failed += testContext.getFailedTests().size();
//                skipped += testContext.getSkippedTests().size();
//                ignored += testContext.getExcludedMethods().size();
//            }
//        }
//
//        rootBuffer = new XMLStringBuffer();
//        Properties p = new Properties();
//        p.put("passed", passed);
//        p.put("failed", failed);
//        p.put("skipped", skipped);
//        p.put("ignored", ignored);
//        p.put("total", passed + failed + skipped + ignored);
//        rootBuffer.push(XMLReporterConfig.TAG_TESTNG_RESULTS, p);
//        //writeReporterOutput(rootBuffer);
//        for (ISuite suite : suites) {
//            writeSuite(suite);
//        }
//        rootBuffer.pop();
//        Utils.writeUtf8File(config.getOutputDirectory(), fileName(), rootBuffer, null /* no prefix */);

    }

    private void generateXMLFile(String filePath, List<ISuite> suites) throws ParserConfigurationException, TransformerException {
        DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
        Document document = documentBuilder.newDocument();

        //Create testng-results root element
        Element testngResultsRoot = document.createElement(XMLReporterConfig.TAG_TESTNG_RESULTS);
        document.appendChild(testngResultsRoot);

        for (ISuite suite : suites) {
            //Create suite nodes
            Element suiteNode = document.createElement(XMLReporterConfig.TAG_SUITE);
            suiteNode.setAttribute(XMLReporterConfig.ATTR_NAME, suite.getName());
            testngResultsRoot.appendChild(suiteNode);

            //Create test nodes
            for (XmlTest test : suite.getXmlSuite().getTests())
            {
                Element testNode = document.createElement(XMLReporterConfig.TAG_TEST);
                testNode.setAttribute(XMLReporterConfig.ATTR_NAME, test.getName());
                suiteNode.appendChild(testNode);

                //Create class nodes
                for (XmlClass xmlClass : test.getClasses())
                {
                    Element classNode = document.createElement(XMLReporterConfig.TAG_CLASS);
                    classNode.setAttribute(XMLReporterConfig.ATTR_NAME, xmlClass.getName());
                    testNode.appendChild(classNode);

                    //Create method nodes
                    queryKeys.clear();
                    queryKeys.add(suite.getName());
                    queryKeys.add(test.getName());
                    queryKeys.add(xmlClass.getName());
                    Iterator<String> methods = allSuiteResult.queryObject(queryKeys).keys();
                    while (methods.hasNext())
                    {
                        String methodName = methods.next();
                        if (methodName.equals(CommonVariables.CFVEC) || methodName.equals(CommonVariables.CFVE) || methodName.equals(CommonVariables.CFVNEC)
                            || methodName.equals(CommonVariables.CFVNE) || methodName.equals(CommonVariables.CPVC) || methodName.equals(CommonVariables.CPV))
                        {
                            continue;
                        }
                        Element methodNode = document.createElement(XMLReporterConfig.TAG_TEST_METHOD);
                        methodNode.setAttribute(XMLReporterConfig.ATTR_NAME, methodName);

                        queryKeys.clear();
                        queryKeys.add(suite.getName());
                        queryKeys.add(test.getName());
                        queryKeys.add(xmlClass.getName());
                        queryKeys.add(methodName);
                        int failedExpectedCount = getMethodFailedVerificationsExpectedCount(suite.getName(), test.getName(),
                                xmlClass.getName(), methodName);
                        int failedNotExpectedCount = getMethodFailedVerificationsNotExpectedCount(suite.getName(), test.getName(),
                            xmlClass.getName(), methodName);
                        int passedCount = getMethodPassedVerificationsCount(suite.getName(), test.getName(),
                                xmlClass.getName(), methodName);
                        if (failedExpectedCount + failedNotExpectedCount > 0)
                        {
                            methodNode.setAttribute(XMLReporterConfig.ATTR_STATUS, XMLReporterConfig.TEST_FAILED);
                        }
                        else if (passedCount > 0)
                        {
                            methodNode.setAttribute(XMLReporterConfig.ATTR_STATUS, XMLReporterConfig.TEST_PASSED);
                        }
                        else
                        {
                            methodNode.setAttribute(XMLReporterConfig.ATTR_STATUS, XMLReporterConfig.TEST_SKIPPED);
                        }

                        classNode.appendChild(methodNode);
                    }
                }
            }
        }

        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
        DOMSource domSource = new DOMSource(document);
        StreamResult streamResult = new StreamResult(new File(filePath));
        transformer.transform(domSource, streamResult);
    }

    private static final String fileName() {
        return System.getProperty(JVM_ARG, FILE_NAME);
    }

    private void writeReporterOutput(XMLStringBuffer xmlBuffer) {
        // TODO: Cosmin - maybe a <line> element isn't indicated for each line
        xmlBuffer.push(XMLReporterConfig.TAG_REPORTER_OUTPUT);
        List<String> output = Reporter.getOutput();
        for (String line : output) {
            if (line != null) {
                xmlBuffer.push(XMLReporterConfig.TAG_LINE);
                xmlBuffer.addCDATA(line);
                xmlBuffer.pop();
            }
        }
        xmlBuffer.pop();
    }

    private void writeSuite(ISuite suite) {
        switch (config.getFileFragmentationLevel()) {
            case XMLReporterConfig.FF_LEVEL_NONE:
                writeSuiteToBuffer(rootBuffer, suite);
                break;
            case XMLReporterConfig.FF_LEVEL_SUITE:
            case XMLReporterConfig.FF_LEVEL_SUITE_RESULT:
                File suiteFile = referenceSuite(rootBuffer, suite);
                writeSuiteToFile(suiteFile, suite);
                break;
            default:
                throw new AssertionError("Unexpected value: " + config.getFileFragmentationLevel());
        }
    }

    private void writeSuiteToFile(File suiteFile, ISuite suite) {
        XMLStringBuffer xmlBuffer = new XMLStringBuffer();
        writeSuiteToBuffer(xmlBuffer, suite);
        File parentDir = suiteFile.getParentFile();
        suiteFile.getParentFile().mkdirs();
        if (parentDir.exists() || suiteFile.getParentFile().exists()) {
            Utils.writeUtf8File(parentDir.getAbsolutePath(), FILE_NAME, xmlBuffer.toXML());
        }
    }

    private File referenceSuite(XMLStringBuffer xmlBuffer, ISuite suite) {
        String relativePath = suite.getName() + File.separatorChar + FILE_NAME;
        File suiteFile = new File(config.getOutputDirectory(), relativePath);
        Properties attrs = new Properties();
        attrs.setProperty(XMLReporterConfig.ATTR_URL, relativePath);
        xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_SUITE, attrs);
        return suiteFile;
    }

    private void writeSuiteToBuffer(XMLStringBuffer xmlBuffer, ISuite suite) {
        xmlBuffer.push(XMLReporterConfig.TAG_SUITE, getSuiteAttributes(suite));
        //writeSuiteGroups(xmlBuffer, suite);

        Map<String, ISuiteResult> results = suite.getResults();
        XMLSuiteResultWriter suiteResultWriter = new XMLSuiteResultWriter(config);
        for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
            suiteResultWriter.writeSuiteResult(xmlBuffer, result.getValue());
        }

        xmlBuffer.pop();
    }

    private void writeSuiteGroups(XMLStringBuffer xmlBuffer, ISuite suite) {
        xmlBuffer.push(XMLReporterConfig.TAG_GROUPS);
        Map<String, Collection<ITestNGMethod>> methodsByGroups = suite.getMethodsByGroups();
        for (Map.Entry<String, Collection<ITestNGMethod>> entry : methodsByGroups.entrySet()) {
            Properties groupAttrs = new Properties();
            groupAttrs.setProperty(XMLReporterConfig.ATTR_NAME, entry.getKey());
            xmlBuffer.push(XMLReporterConfig.TAG_GROUP, groupAttrs);
            Set<ITestNGMethod> groupMethods = getUniqueMethodSet(entry.getValue());
            for (ITestNGMethod groupMethod : groupMethods) {
                Properties methodAttrs = new Properties();
                methodAttrs.setProperty(XMLReporterConfig.ATTR_NAME, groupMethod.getMethodName());
                methodAttrs.setProperty(XMLReporterConfig.ATTR_METHOD_SIG, groupMethod.toString());
                methodAttrs.setProperty(XMLReporterConfig.ATTR_CLASS, groupMethod.getRealClass().getName());
                xmlBuffer.addEmptyElement(XMLReporterConfig.TAG_METHOD, methodAttrs);
            }
            xmlBuffer.pop();
        }
        xmlBuffer.pop();
    }

    private Properties getSuiteAttributes(ISuite suite) {
        Properties props = new Properties();
        props.setProperty(XMLReporterConfig.ATTR_NAME, suite.getName());

        // Calculate the duration
        Map<String, ISuiteResult> results = suite.getResults();
        Date minStartDate = new Date();
        Date maxEndDate = null;
        // TODO: We could probably optimize this in order not to traverse this twice
        for (Map.Entry<String, ISuiteResult> result : results.entrySet()) {
            ITestContext testContext = result.getValue().getTestContext();
            Date startDate = testContext.getStartDate();
            Date endDate = testContext.getEndDate();
            if (minStartDate.after(startDate)) {
                minStartDate = startDate;
            }
            if (maxEndDate == null || maxEndDate.before(endDate)) {
                maxEndDate = endDate != null ? endDate : startDate;
            }
        }
        // The suite could be completely empty
        if (maxEndDate == null) {
            maxEndDate = minStartDate;
        }
        addDurationAttributes(config, props, minStartDate, maxEndDate);
        return props;
    }

    /**
     * Add started-at, finished-at and duration-ms attributes to the <suite> tag
     */
    public static void addDurationAttributes(XMLReporterConfig config, Properties attributes,
                                             Date minStartDate, Date maxEndDate) {

        String startTime = TimeUtils.timeInUTC(minStartDate.getTime(), config.getTimestampFormat());
        String endTime = TimeUtils.timeInUTC(maxEndDate.getTime(), config.getTimestampFormat());
        long duration = maxEndDate.getTime() - minStartDate.getTime();

        attributes.setProperty(XMLReporterConfig.ATTR_STARTED_AT, startTime);
        attributes.setProperty(XMLReporterConfig.ATTR_FINISHED_AT, endTime);
        attributes.setProperty(XMLReporterConfig.ATTR_DURATION_MS, Long.toString(duration));
    }

    private Set<ITestNGMethod> getUniqueMethodSet(Collection<ITestNGMethod> methods) {
        Set<ITestNGMethod> result = new LinkedHashSet<>();
        for (ITestNGMethod method : methods) {
            result.add(method);
        }
        return result;
    }

    /**
     * @deprecated Unused
     */
    @Deprecated
    public int getFileFragmentationLevel() {
        return config.getFileFragmentationLevel();
    }

    /**
     * @deprecated Unused
     */
    @Deprecated
    public void setFileFragmentationLevel(int fileFragmentationLevel) {
        config.setFileFragmentationLevel(fileFragmentationLevel);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public int getStackTraceOutputMethod() {
        return config.getStackTraceOutputMethod();
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public void setStackTraceOutputMethod(int stackTraceOutputMethod) {
        config.setStackTraceOutputMethod(stackTraceOutputMethod);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public String getOutputDirectory() {
        return config.getOutputDirectory();
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public void setOutputDirectory(String outputDirectory) {
        config.setOutputDirectory(outputDirectory);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public boolean isGenerateGroupsAttribute() {
        return config.isGenerateGroupsAttribute();
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public void setGenerateGroupsAttribute(boolean generateGroupsAttribute) {
        config.setGenerateGroupsAttribute(generateGroupsAttribute);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public boolean isSplitClassAndPackageNames() {
        return config.isSplitClassAndPackageNames();
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public void setSplitClassAndPackageNames(boolean splitClassAndPackageNames) {
        config.setSplitClassAndPackageNames(splitClassAndPackageNames);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public String getTimestampFormat() {
        return config.getTimestampFormat();
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public void setTimestampFormat(String timestampFormat) {
        config.setTimestampFormat(timestampFormat);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public boolean isGenerateDependsOnMethods() {
        return config.isGenerateDependsOnMethods();
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public void setGenerateDependsOnMethods(boolean generateDependsOnMethods) {
        config.setGenerateDependsOnMethods(generateDependsOnMethods);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public void setGenerateDependsOnGroups(boolean generateDependsOnGroups) {
        config.setGenerateDependsOnGroups(generateDependsOnGroups);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public boolean isGenerateDependsOnGroups() {
        return config.isGenerateDependsOnGroups();
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public void setGenerateTestResultAttributes(boolean generateTestResultAttributes) {
        config.setGenerateTestResultAttributes(generateTestResultAttributes);
    }

    /**
     * @deprecated Use #getConfig() instead
     */
    @Deprecated
    public boolean isGenerateTestResultAttributes() {
        return config.isGenerateTestResultAttributes();
    }

    public XMLReporterConfig getConfig() {
        return config;
    }
}

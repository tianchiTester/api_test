package report;

import org.json.JSONObject;
import org.testng.*;
import org.testng.collections.Lists;
import org.testng.internal.Utils;
import org.testng.log4testng.Logger;
import org.testng.xml.XmlSuite;
import testcase.SuiteCommon;
import common.CommonVariables;
import utility.FileOperator;
import common.GetEnvironmentVariables;
import utility.SimpleTool;

import java.io.*;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Reported designed to render self-contained HTML top down view of a testing
 * suite.
 *
 * @author Paul Mendelson
 * @version $Revision: 719 $
 * @since 5.2
 */
public class IsenCustomizedEmailableReporter implements IReporter {
    private static final Logger LOG = Logger.getLogger(IsenCustomizedEmailableReporter.class);

    protected PrintWriter writer;

    protected List<IsenCustomizedEmailableReporter.SuiteResult> suiteResults = Lists.newArrayList();

    // Reusable buffer
    private StringBuilder buffer = new StringBuilder();

    private ArrayList<String> queryKeys = new ArrayList<>();
    private JSONObject allSuiteResult;
    private Boolean showPass;
    private Boolean allPass;

    public int getTestPassedVerificationsCount(String suiteName, String testName) {
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

    public int getTestFailedVerificationsExpectedCount(String suiteName, String testName) {
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

    public int getTestFailedVerificationsNotExpectedCount(String suiteName, String testName) {
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

    public int getClassPassedVerificationsCount(String suiteName, String testName, String className) {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        return testResult.getInt(CommonVariables.CPVC);
    }

    public int getClassFailedVerificationsExpectedCount(String suiteName, String testName, String className) {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        return testResult.getInt(CommonVariables.CFVEC);
    }

    public int getClassFailedVerificationsNotExpectedCount(String suiteName, String testName, String className) {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        JSONObject testResult = allSuiteResult.queryObject(queryKeys);
        return testResult.getInt(CommonVariables.CFVNEC);
    }

    public int getMethodPassedVerificationsCount(String suiteName, String testName, String className, String methodName) {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getInt(CommonVariables.MPVC);
    }

    public int getMethodFailedVerificationsExpectedCount(String suiteName, String testName, String className, String methodName) {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getInt(CommonVariables.MFVEC);
    }

    public int getMethodFailedVerificationsNotExpectedCount(String suiteName, String testName, String className, String methodName) {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getInt(CommonVariables.MFVNEC);
    }

    public String getMethodPassedVerifications(String suiteName, String testName, String className, String methodName) {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getString(CommonVariables.MPV);
    }

    public String getMethodFailedVerificationsExpected(String suiteName, String testName, String className, String methodName) {
        queryKeys.clear();
        queryKeys.add(suiteName);
        queryKeys.add(testName);
        queryKeys.add(className);
        queryKeys.add(methodName);
        JSONObject result = allSuiteResult.queryObject(queryKeys);
        return result.getString(CommonVariables.MFVE);
    }

    public String getMethodFailedVerificationsNotExpected(String suiteName, String testName, String className, String methodName) {
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
        showPass = GetEnvironmentVariables.getInstance().getShowPassForHTML();

        try {
            File directory = new File("");
            outputDirectory = directory.getAbsolutePath();
            writer = createWriter(outputDirectory);
        } catch (IOException e) {
            LOG.error("Unable to create output file", e);
            return;
        }
        for (ISuite suite : suites) {
            suiteResults.add(new IsenCustomizedEmailableReporter.SuiteResult(suite));
        }

        allSuiteResult = SuiteCommon.getTestResult();
        writeDocumentStart();
        writeHead();
        writeBody();
        writeDocumentEnd();

        writer.close();
        if (GetEnvironmentVariables.getInstance().getOpenHTMLReport().toLowerCase().equals("true")) {
            FileOperator.openFile(outputDirectory + "/emailable-report.html");
        }
    }

    protected PrintWriter createWriter(String outdir) throws IOException {
        new File(outdir).mkdirs();
        return new PrintWriter(new BufferedWriter(new FileWriter(new File(
                outdir, "emailable-report.html"))));
    }

    protected void writeDocumentStart() {
        writer.println("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.1//EN\" \"http://www.w3.org/TR/xhtml11/DTD/xhtml11.dtd\">");
        writer.print("<html xmlns=\"http://www.w3.org/1999/xhtml\">");
    }

    protected void writeHead() {
        writer.print("<head>");
        writer.print("<title>ISEN Service Report - " + GetEnvironmentVariables.getInstance().getCurrentStack() + "</title>");
        writeStylesheet();
        writer.print("</head>");
    }

    protected void writeStylesheet() {
        writer.print("<style type=\"text/css\">");
        writer.print("table {margin-bottom:10px;border-collapse:collapse;empty-cells:show;word-break:break-all;word-wrap:break-all}");
        writer.print("th,td {border:1px solid #009;padding:.25em .5em}");
        writer.print("th {vertical-align:center}");
        writer.print("td {vertical-align:top}");
        writer.print("table a {font-weight:bold}");
        writer.print(".stripe td {background-color: #E6EBF9}");
        writer.print(".num {text-align:right}");
        writer.print(".passedodd td {background-color: #3F3}");
        writer.print(".passedeven td {background-color: #0A0}");
        writer.print(".skippedodd td {background-color: #DDD}");
        writer.print(".skippedeven td {background-color: #CCC}");
        writer.print(".failedodd td,.attn {background-color: #F33}");
        writer.print(".failedeven td,.stripe .attn {background-color: #D00}");
        writer.print(".stacktrace {white-space:pre;font-family:monospace}");
        writer.print(".tdverificationslimit {width:300}");
        writer.print(".totop {font-size:85%;text-align:center;border-bottom:2px solid #000}");
        writer.print("</style>");
    }

    protected void writeBody() {
        writer.print("<body>");

        writeSuiteSummary();
        if (allPass && !showPass)
        {
            // not write report body in this situation
        }
        else {
            writeScenarioSummary();
            writeScenarioDetails();
        }

        writer.print("</body>");
    }

    protected void writeDocumentEnd() {
        writer.print("</html>");
    }

    protected void writeSuiteSummary() {
        NumberFormat integerFormat = NumberFormat.getIntegerInstance();
        NumberFormat decimalFormat = NumberFormat.getNumberInstance();

        int totalPassedTests = 0;
        int totalSkippedTests = 0;
        int totalFailedTests = 0;
        long totalDuration = 0;
        int totalPassedVerifications = 0;
        int totalFailedVerificationsExpected = 0;
        int totalFailedVerificationsNotExpected = 0;
        int totalFailedVerifications = 0;

        for (IsenCustomizedEmailableReporter.SuiteResult suiteResult : suiteResults) {
            for (IsenCustomizedEmailableReporter.TestResult testResult : suiteResult.getTestResults()) {
                int testFailedVerificationsNotExpectedCount = getTestFailedVerificationsNotExpectedCount(suiteResult.getSuiteName(), testResult.getTestName());
                totalFailedVerificationsNotExpected += testFailedVerificationsNotExpectedCount;
                int testFailedVerificationsExpectedCount = getTestFailedVerificationsExpectedCount(suiteResult.getSuiteName(), testResult.getTestName());
                totalFailedVerificationsExpected += testFailedVerificationsExpectedCount;
                totalFailedVerifications = totalFailedVerificationsNotExpected + totalFailedVerificationsExpected;
            }

        }


//        writer.print("<h2 style=\"text-align: center\">ISEN Service Report</h2>");
//        writer.print("<h3 style=\"text-align: center\">Stack:" + GetEnvironmentVariables.getInstance().getCurrentStack() + "</h3>");
        if (totalFailedVerifications == 0) {
            allPass = true;
            writer.print("<h3 style=\"text-align:center; color:#3F3\">" + "PASS" + "</h3>");
        } else {
            allPass = false;
            writer.print("<h3 style=\"text-align:center; color:#F33\">" + "FAIL" + "</h3>");
        }
        writer.print("<table>");
        writer.print("<tr>");
        writer.print("<th>Version</th>");
        writer.print("<th># Passed Methods</th>");
        writer.print("<th># Skipped Methods</th>");
        writer.print("<th># Failed Methods</th>");
        writer.print("<th>Time</th>");
        writer.print("<th>Passed Verifications</th>");
        writer.print("<th>Passed Verifications Ratio</th>");
        writer.print("<th>Failed Verifications Not Expected</th>");
        writer.print("<th>Failed Verifications Expected</th>");
        writer.print("</tr>");

        int testIndex = 0;
        for (IsenCustomizedEmailableReporter.SuiteResult suiteResult : suiteResults) {
            writer.print("<tr><th colspan=\"9\">");
            writer.print(Utils.escapeHtml(suiteResult.getSuiteName()));
            writer.print("</th></tr>");

            for (IsenCustomizedEmailableReporter.TestResult testResult : suiteResult.getTestResults()) {
                int passedTests = testResult.getPassedTestCount();
                int skippedTests = testResult.getSkippedTestCount();
                int failedTests = testResult.getFailedTestCount();
                long duration = testResult.getDuration();

                writer.print("<tr");
                if ((testIndex % 2) == 1) {
                    writer.print(" class=\"stripe\"");
                }
                writer.print(">");

                buffer.setLength(0);
                writeTableData(buffer.append("<a href=\"#t").append(testIndex)
                        .append("\">")
                        .append(Utils.escapeHtml("v" + testResult.getTestName()))
                        .append("</a>").toString());
                writeTableData(integerFormat.format(passedTests), "num");
                writeTableData(integerFormat.format(skippedTests),
                        (skippedTests > 0 ? "num attn" : "num"));
                writeTableData(integerFormat.format(failedTests),
                        (failedTests > 0 ? "num attn" : "num"));
                System.out.println("Duration of " + testResult.getTestName() + " is:" + duration);
                writeTableData(SimpleTool.convertMillisecondsToMinutesAndSeconds(duration), "num");
                int testPassedVerificationsCount = getTestPassedVerificationsCount(suiteResult.getSuiteName(), testResult.getTestName());
                totalPassedVerifications += testPassedVerificationsCount;
                int testFailedVerificationsExpectedCount = getTestFailedVerificationsExpectedCount(suiteResult.getSuiteName(), testResult.getTestName());
                totalFailedVerificationsExpected += testFailedVerificationsExpectedCount;
                int testFailedVerificationsNotExpectedCount = getTestFailedVerificationsNotExpectedCount(suiteResult.getSuiteName(), testResult.getTestName());

                int testTotalVerificationsCount = testPassedVerificationsCount + testFailedVerificationsExpectedCount + testFailedVerificationsNotExpectedCount;
                writeTableData(String.valueOf(testPassedVerificationsCount));
                writeTableData(generatePercentageString(testPassedVerificationsCount, testTotalVerificationsCount, 2));
                writeTableData(integerFormat.format(testFailedVerificationsNotExpectedCount),
                        (testFailedVerificationsNotExpectedCount > 0 ? "num attn" : "num"));
                writeTableData(String.valueOf(testFailedVerificationsExpectedCount));

                writer.print("</tr>");

                totalPassedTests += passedTests;
                totalSkippedTests += skippedTests;
                totalFailedTests += failedTests;
                totalDuration += duration;

                testIndex++;
            }
        }

        // Print totals if there was more than one test
        if (testIndex > 1) {
            writer.print("<tr>");
            writer.print("<th>Total</th>");
            writeTableHeader(integerFormat.format(totalPassedTests), "num");
            writeTableHeader(integerFormat.format(totalSkippedTests),
                    (totalSkippedTests > 0 ? "num attn" : "num"));
            writeTableHeader(integerFormat.format(totalFailedTests),
                    (totalFailedTests > 0 ? "num attn" : "num"));
            writeTableHeader(SimpleTool.convertMillisecondsToMinutesAndSeconds(totalDuration), "num");
            writeTableHeader(integerFormat.format(totalPassedVerifications), "num");
            writeTableHeader(generatePercentageString(totalPassedVerifications, (totalPassedVerifications
                    + totalFailedVerificationsExpected + totalFailedVerificationsNotExpected), 2), "num");
            writeTableHeader(integerFormat.format(totalFailedVerificationsNotExpected),
                    (totalFailedVerificationsNotExpected > 0 ? "num attn" : "num"));
            writeTableHeader(integerFormat.format(totalFailedVerificationsExpected), "num");
            writer.print("</tr>");
        }

        writer.print("</table>");
    }

    /**
     * Writes a summary of all the test scenarios.
     */
    protected void writeScenarioSummary() {

        writer.print("<table>");
        writer.print("<thead>");
        writer.print("<tr>");
        writer.print("<th>Class</th>");
        writer.print("<th>Method</th>");
        writer.print("<th>Start</th>");
        writer.print("<th>Time</th>");
        writer.print("<th width=\"100\">Failed Verifications Count and Ratio(Not Expected)</th>");
        writer.print("<th>Failed Verifications(Not Expected)</th>");
        writer.print("<th width=\"100\">Failed Verifications Count and Ratio(Expected)</th>");
        writer.print("<th>Failed Verifications(Expected)</th>");
        if (showPass) {
            writer.print("<th width=\"100\">Passed Verifications Count and Ratio</th>");
            writer.print("<th>Passed Verifications</th>");
        }
        writer.print("</tr>");
        writer.print("</thead>");

        int testIndex = 0;
        int scenarioIndex = 0;
        for (IsenCustomizedEmailableReporter.SuiteResult suiteResult : suiteResults) {
            if (showPass) {
                writer.print("<tbody><tr><th colspan=\"10\">");
            } else {
                writer.print("<tbody><tr><th colspan=\"8\">");
            }
            writer.print(Utils.escapeHtml(suiteResult.getSuiteName()));
            writer.print("</th></tr></tbody>");

            for (IsenCustomizedEmailableReporter.TestResult testResult : suiteResult.getTestResults()) {
                writer.print("<tbody id=\"t");
                writer.print(testIndex);
                writer.print("\">");

                String testName = "v" + Utils.escapeHtml(testResult.getTestName());

                scenarioIndex += writeScenarioSummary(testName
                                + " &#8212; failed (configuration methods)",
                        testResult.getFailedConfigurationResults(), "failed",
                        scenarioIndex);
                scenarioIndex += writeScenarioSummary(testName
                                + " &#8212; failed", testResult.getFailedTestResults(),
                        "failed", scenarioIndex);
                scenarioIndex += writeScenarioSummary(testName
                                + " &#8212; skipped (configuration methods)",
                        testResult.getSkippedConfigurationResults(), "skipped",
                        scenarioIndex);
                scenarioIndex += writeScenarioSummary(testName
                                + " &#8212; skipped",
                        testResult.getSkippedTestResults(), "skipped",
                        scenarioIndex);
                if (showPass) {
                    scenarioIndex += writeScenarioSummary(testName
                                    + " &#8212; passed", testResult.getPassedTestResults(),
                            "passed", scenarioIndex);
                }

                writer.print("</tbody>");

                testIndex++;
            }
        }

        writer.print("</table>");
    }

    /**
     * Writes the scenario summary for the results of a given state for a single
     * test.
     */
    private int writeScenarioSummary(String description,
                                     List<IsenCustomizedEmailableReporter.ClassResult> classResults, String cssClassPrefix,
                                     int startingScenarioIndex) {
        int scenarioCount = 0;
        if (!classResults.isEmpty()) {
            if (showPass) {
                writer.print("<tr><th colspan=\"10\">");
            } else {
                writer.print("<tr><th colspan=\"8\">");
            }
            writer.print(description);
            writer.print("</th></tr>");

            String suiteName = "";
            String testName = "";
            String className = "";

            int scenarioIndex = startingScenarioIndex;
            int classIndex = 0;
            for (IsenCustomizedEmailableReporter.ClassResult classResult : classResults) {
                String cssClass = cssClassPrefix
                        + ((classIndex % 2) == 0 ? "even" : "odd");

                buffer.setLength(0);

                int scenariosPerClass = 0;
                int methodIndex = 0;
                for (IsenCustomizedEmailableReporter.MethodResult methodResult : classResult.getMethodResults()) {
                    List<ITestResult> results = methodResult.getResults();
                    int resultsCount = results.size();
                    assert resultsCount > 0;

                    ITestResult firstResult = results.iterator().next();

                    String methodName = Utils.escapeHtml(firstResult
                            .getMethod().getMethodName());
                    long start = firstResult.getStartMillis();
                    long duration = firstResult.getEndMillis() - start;

                    // The first method per class shares a row with the class
                    // header
                    if (methodIndex > 0) {
                        buffer.append("<tr class=\"").append(cssClass)
                                .append("\">");

                    }

                    Date startDateTime = new Date(start);
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss:SSS");
                    String formattedStartDateTime = formatter.format(startDateTime);
                    suiteName = firstResult.getTestContext().getSuite().getName();
                    testName = firstResult.getTestContext().getName();
                    className = firstResult.getTestClass().getName();
                    int methodPassedVerificationsCount = getMethodPassedVerificationsCount(suiteName, testName, className, methodName);
                    String methodPassedVerifications = getMethodPassedVerifications(suiteName, testName, className, methodName);
                    int methodFailedVerificationsExpectedCount = getMethodFailedVerificationsExpectedCount(suiteName, testName, className, methodName);
                    String methodFailedVerificationsExpected = getMethodFailedVerificationsExpected(suiteName, testName, className, methodName);
                    int methodFailedVerificationsNotExpectedCount = getMethodFailedVerificationsNotExpectedCount(suiteName, testName, className, methodName);
                    String methodFailedVerificationsNotExpected = getMethodFailedVerificationsNotExpected(suiteName, testName, className, methodName);

                    int methodTotalVerificationsCount = methodPassedVerificationsCount + methodFailedVerificationsExpectedCount + methodFailedVerificationsNotExpectedCount;
                    String passRatioString = generatePercentageString(methodPassedVerificationsCount, methodTotalVerificationsCount, 2);
                    String failExpectedRatioString = generatePercentageString(methodFailedVerificationsExpectedCount, methodTotalVerificationsCount, 2);
                    String failNotExpectedRatioString = generatePercentageString(methodFailedVerificationsNotExpectedCount, methodTotalVerificationsCount, 2);

                    // Write the timing information with the first scenario per
                    // method
                    if (showPass)
                        buffer.append("<td><a href=\"#m").append(scenarioIndex).append("\" width=\"100\">")
                                .append(methodName).append("</a></td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"100\">")
                                .append(formattedStartDateTime).append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\">")
                                .append(SimpleTool.convertMillisecondsToMinutesAndSeconds(duration)).append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"100\">")
                                .append(methodFailedVerificationsNotExpectedCount).append("&nbsp(").append(failNotExpectedRatioString).append(")").append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"400\">")
                                .append(methodFailedVerificationsNotExpected).append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"100\">")
                                .append(methodFailedVerificationsExpectedCount).append("&nbsp(").append(failExpectedRatioString).append(")").append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"400\">")
                                .append(methodFailedVerificationsExpected).append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"100\">")
                                .append(methodPassedVerificationsCount).append("&nbsp(").append(passRatioString).append(")").append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"400\">")
                                .append(methodPassedVerifications).append("</td></tr>");
                    else {
                        buffer.append("<td><a href=\"#m").append(scenarioIndex).append("\" width=\"100\">")
                                .append(methodName).append("</a></td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"100\">")
                                .append(formattedStartDateTime).append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\">")
                                .append(SimpleTool.convertMillisecondsToMinutesAndSeconds(duration)).append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"100\">")
                                .append(methodFailedVerificationsNotExpectedCount).append("&nbsp(").append(failNotExpectedRatioString).append(")").append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"400\">")
                                .append(methodFailedVerificationsNotExpected).append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"100\">")
                                .append(methodFailedVerificationsExpectedCount).append("&nbsp(").append(failExpectedRatioString).append(")").append("</td>")
                                .append("<td rowspan=\"").append(resultsCount).append("\" width=\"400\">")
                                .append(methodFailedVerificationsExpected).append("</td></tr>");
                    }

                    scenarioIndex++;

                    // Write the remaining scenarios for the method
                    for (int i = 1; i < resultsCount; i++) {
                        buffer.append("<tr class=\"").append(cssClass)
                                .append("\">").append("<td><a href=\"#m")
                                .append(scenarioIndex).append("\">")
                                .append(methodName).append("</a></td></tr>");
                        scenarioIndex++;
                    }

                    scenariosPerClass += resultsCount;
                    methodIndex++;
                }


                // Write the test results for the class
                int classPassedVerificationsCount = getClassPassedVerificationsCount(suiteName, testName, className);
                int classFailedVerificationsExpectedCount = getClassFailedVerificationsExpectedCount(suiteName, testName, className);
                int classFailedVerificationsNotExpectedCount = getClassFailedVerificationsNotExpectedCount(suiteName, testName, className);
                int classTotalVerificationsCount = classPassedVerificationsCount + classFailedVerificationsExpectedCount + classFailedVerificationsNotExpectedCount;
                String classVerificationsPassRatio = generatePercentageString(classPassedVerificationsCount, classTotalVerificationsCount, 2);

                writer.print("<tr class=\"");
                writer.print(cssClass);
                writer.print("\">");
                writer.print("<td rowspan=\"");
                writer.print(scenariosPerClass);
                writer.print("\">");
                writer.print(Utils.escapeHtml(classResult.getClassName()) + "<br />");
                writer.print("&nbsp<br />");
                writer.print(classPassedVerificationsCount + "/" + classTotalVerificationsCount + "&nbsp(" + classVerificationsPassRatio + ")" + "<br />");
                writer.print("</td>");
                writer.print(buffer);

                classIndex++;
            }
            scenarioCount = scenarioIndex - startingScenarioIndex;
        }
        return scenarioCount;
    }

    public String generatePercentageString(double numerator, double denominator, int decimalPlaces) {
        StringBuilder formatBuilder = new StringBuilder("0.");
        for (int i = 0; i < decimalPlaces; i++) {
            formatBuilder.append("0");
        }
        DecimalFormat df = new DecimalFormat(formatBuilder.toString());
        String ratioString = df.format(numerator * 100 / denominator) + "%";
        return ratioString;
    }

    /**
     * Writes the details for all test scenarios.
     */
    protected void writeScenarioDetails() {
        int scenarioIndex = 0;
        for (IsenCustomizedEmailableReporter.SuiteResult suiteResult : suiteResults) {
            for (IsenCustomizedEmailableReporter.TestResult testResult : suiteResult.getTestResults()) {
                writer.print("<h2>");
                writer.print(Utils.escapeHtml("v" + testResult.getTestName()));
                writer.print("</h2>");

                scenarioIndex += writeScenarioDetails(
                        testResult.getFailedConfigurationResults(),
                        scenarioIndex);
                scenarioIndex += writeScenarioDetails(
                        testResult.getFailedTestResults(), scenarioIndex);
                scenarioIndex += writeScenarioDetails(
                        testResult.getSkippedConfigurationResults(),
                        scenarioIndex);
                scenarioIndex += writeScenarioDetails(
                        testResult.getSkippedTestResults(), scenarioIndex);
                scenarioIndex += writeScenarioDetails(
                        testResult.getPassedTestResults(), scenarioIndex);
            }
        }
    }

    /**
     * Writes the scenario details for the results of a given state for a single
     * test.
     */
    private int writeScenarioDetails(List<IsenCustomizedEmailableReporter.ClassResult> classResults,
                                     int startingScenarioIndex) {
        int scenarioIndex = startingScenarioIndex;
        for (IsenCustomizedEmailableReporter.ClassResult classResult : classResults) {
            String className = classResult.getClassName();
            for (IsenCustomizedEmailableReporter.MethodResult methodResult : classResult.getMethodResults()) {

                List<ITestResult> results = methodResult.getResults();
                assert !results.isEmpty();

                String label = Utils
                        .escapeHtml(className
                                + "#"
                                + results.iterator().next().getMethod()
                                .getMethodName());
                for (ITestResult result : results) {
                    if (!result.isSuccess()) {
                        writeScenario(scenarioIndex, label, result);
                        scenarioIndex++;
                    }
                }
            }
        }

        return scenarioIndex - startingScenarioIndex;
    }

    /**
     * Writes the details for an individual test scenario.
     */
    private void writeScenario(int scenarioIndex, String label,
                               ITestResult result) {
        writer.print("<h3 id=\"m");
        writer.print(scenarioIndex);
        writer.print("\">");
        writer.print(label);
        writer.print("</h3>");

        writer.print("<table class=\"result\">");

        // Write test parameters (if any)
        Object[] parameters = result.getParameters();
        int parameterCount = (parameters == null ? 0 : parameters.length);
        if (parameterCount > 0) {
            writer.print("<tr class=\"param\">");
            for (int i = 1; i <= parameterCount; i++) {
                writer.print("<th>Parameter #");
                writer.print(i);
                writer.print("</th>");
            }
            writer.print("</tr><tr class=\"param stripe\">");
            for (Object parameter : parameters) {
                writer.print("<td>");
                writer.print(Utils.escapeHtml(Utils.toString(parameter)));
                writer.print("</td>");
            }
            writer.print("</tr>");
        }

        // Write reporter messages (if any)
        List<String> reporterMessages = Reporter.getOutput(result);
        if (!reporterMessages.isEmpty()) {
            writer.print("<tr><th");
            if (parameterCount > 1) {
                writer.print(" colspan=\"");
                writer.print(parameterCount);
                writer.print("\"");
            }
            writer.print(">Messages</th></tr>");

            writer.print("<tr><td");
            if (parameterCount > 1) {
                writer.print(" colspan=\"");
                writer.print(parameterCount);
                writer.print("\"");
            }
            writer.print(">");
            writeReporterMessages(reporterMessages);
            writer.print("</td></tr>");
        }

        // Write exception (if any)
        Throwable throwable = result.getThrowable();
        if (throwable != null) {
            writer.print("<tr><th");
            if (parameterCount > 1) {
                writer.print(" colspan=\"");
                writer.print(parameterCount);
                writer.print("\"");
            }
            writer.print(">");
            writer.print((result.getStatus() == ITestResult.SUCCESS ? "Expected Exception"
                    : "Exception"));
            writer.print("</th></tr>");

            writer.print("<tr><td");
            if (parameterCount > 1) {
                writer.print(" colspan=\"");
                writer.print(parameterCount);
                writer.print("\"");
            }
            writer.print(">");
            writeStackTrace(throwable);
            writer.print("</td></tr>");
        }

        writer.print("</table>");
        writer.print("<p class=\"totop\"><a href=\"#summary\">back to summary</a></p>");
    }

    protected void writeReporterMessages(List<String> reporterMessages) {
        writer.print("<div class=\"messages\">");
        Iterator<String> iterator = reporterMessages.iterator();
        assert iterator.hasNext();
        if (Reporter.getEscapeHtml()) {
            writer.print(Utils.escapeHtml(iterator.next()));
        } else {
            writer.print(iterator.next());
        }
        while (iterator.hasNext()) {
            writer.print("<br/>");
            if (Reporter.getEscapeHtml()) {
                writer.print(Utils.escapeHtml(iterator.next()));
            } else {
                writer.print(iterator.next());
            }
        }
        writer.print("</div>");
    }

    protected void writeStackTrace(Throwable throwable) {
        writer.print("<div class=\"stacktrace\">");
        writer.print(Utils.stackTrace(throwable, true)[0]);
        writer.print("</div>");
    }

    /**
     * Writes a TH element with the specified contents and CSS class names.
     *
     * @param html       the HTML contents
     * @param cssClasses the space-delimited CSS classes or null if there are no
     *                   classes to apply
     */
    protected void writeTableHeader(String html, String cssClasses) {
        writeTag("th", html, cssClasses);
    }

    /**
     * Writes a TD element with the specified contents.
     *
     * @param html the HTML contents
     */
    protected void writeTableData(String html) {
        writeTableData(html, null);
    }

    /**
     * Writes a TD element with the specified contents and CSS class names.
     *
     * @param html       the HTML contents
     * @param cssClasses the space-delimited CSS classes or null if there are no
     *                   classes to apply
     */
    protected void writeTableData(String html, String cssClasses) {
        writeTag("td", html, cssClasses);
    }

    /**
     * Writes an arbitrary HTML element with the specified contents and CSS
     * class names.
     *
     * @param tag        the tag name
     * @param html       the HTML contents
     * @param cssClasses the space-delimited CSS classes or null if there are no
     *                   classes to apply
     */
    protected void writeTag(String tag, String html, String cssClasses) {
        writer.print("<");
        writer.print(tag);
        if (cssClasses != null) {
            writer.print(" class=\"");
            writer.print(cssClasses);
            writer.print("\"");
        }
        writer.print(">");
        writer.print(html);
        writer.print("</");
        writer.print(tag);
        writer.print(">");
    }

    /**
     * Groups {@link IsenCustomizedEmailableReporter.TestResult}s by suite.
     */
    protected static class SuiteResult {
        private final String suiteName;
        private final List<IsenCustomizedEmailableReporter.TestResult> testResults = Lists.newArrayList();

        public SuiteResult(ISuite suite) {
            suiteName = suite.getName();
            for (ISuiteResult suiteResult : suite.getResults().values()) {
                testResults.add(new IsenCustomizedEmailableReporter.TestResult(suiteResult.getTestContext()));
            }
        }

        public String getSuiteName() {
            return suiteName;
        }

        /**
         * @return the test results (possibly empty)
         */
        public List<IsenCustomizedEmailableReporter.TestResult> getTestResults() {
            return testResults;
        }
    }

    /**
     * Groups {@link IsenCustomizedEmailableReporter.ClassResult}s by test, type (configuration or test), and
     * status.
     */
    protected static class TestResult {
        /**
         * Orders test results by class name and then by method name (in
         * lexicographic order).
         */
        protected static final Comparator<ITestResult> RESULT_COMPARATOR = new Comparator<ITestResult>() {
            @Override
            public int compare(ITestResult o1, ITestResult o2) {
                int result = o1.getTestClass().getName()
                        .compareTo(o2.getTestClass().getName());
                if (result == 0) {
                    result = (o1.getStartMillis() - o2.getStartMillis() >= 0) ? 1 : -1;
                }
                return result;
            }
        };

        private final String testName;
        private final List<IsenCustomizedEmailableReporter.ClassResult> failedConfigurationResults;
        private final List<IsenCustomizedEmailableReporter.ClassResult> failedTestResults;
        private final List<IsenCustomizedEmailableReporter.ClassResult> skippedConfigurationResults;
        private final List<IsenCustomizedEmailableReporter.ClassResult> skippedTestResults;
        private final List<IsenCustomizedEmailableReporter.ClassResult> passedTestResults;
        private final int failedTestCount;
        private final int skippedTestCount;
        private final int passedTestCount;
        private final long duration;
        private final String includedGroups;
        private final String excludedGroups;

        public TestResult(ITestContext context) {
            testName = context.getName();

            Set<ITestResult> failedConfigurations = context
                    .getFailedConfigurations().getAllResults();
            Set<ITestResult> failedTests = context.getFailedTests()
                    .getAllResults();
            Set<ITestResult> skippedConfigurations = context
                    .getSkippedConfigurations().getAllResults();
            Set<ITestResult> skippedTests = context.getSkippedTests()
                    .getAllResults();
            Set<ITestResult> passedTests = context.getPassedTests()
                    .getAllResults();

            failedConfigurationResults = groupResults(failedConfigurations);
            failedTestResults = groupResults(failedTests);
            skippedConfigurationResults = groupResults(skippedConfigurations);
            skippedTestResults = groupResults(skippedTests);
            passedTestResults = groupResults(passedTests);

            failedTestCount = failedTests.size();
            skippedTestCount = skippedTests.size();
            passedTestCount = passedTests.size();

            duration = context.getEndDate().getTime()
                    - context.getStartDate().getTime();

            includedGroups = formatGroups(context.getIncludedGroups());
            excludedGroups = formatGroups(context.getExcludedGroups());
        }

        /**
         * Groups test results by method and then by class.
         */
        protected List<IsenCustomizedEmailableReporter.ClassResult> groupResults(Set<ITestResult> results) {
            List<IsenCustomizedEmailableReporter.ClassResult> classResults = Lists.newArrayList();
            if (!results.isEmpty()) {
                List<IsenCustomizedEmailableReporter.MethodResult> resultsPerClass = Lists.newArrayList();
                List<ITestResult> resultsPerMethod = Lists.newArrayList();

                List<ITestResult> resultsList = Lists.newArrayList(results);

                Collections.sort(resultsList, RESULT_COMPARATOR);
                Iterator<ITestResult> resultsIterator = resultsList.iterator();
                assert resultsIterator.hasNext();

                ITestResult result = resultsIterator.next();
                resultsPerMethod.add(result);

                String previousClassName = result.getTestClass().getName();
                String previousMethodName = result.getMethod().getMethodName();
                while (resultsIterator.hasNext()) {
                    result = resultsIterator.next();

                    String className = result.getTestClass().getName();
                    if (!previousClassName.equals(className)) {
                        // Different class implies different method
                        assert !resultsPerMethod.isEmpty();
                        resultsPerClass.add(new IsenCustomizedEmailableReporter.MethodResult(resultsPerMethod));
                        resultsPerMethod = Lists.newArrayList();

                        assert !resultsPerClass.isEmpty();
                        classResults.add(new IsenCustomizedEmailableReporter.ClassResult(previousClassName,
                                resultsPerClass));
                        resultsPerClass = Lists.newArrayList();

                        previousClassName = className;
                        previousMethodName = result.getMethod().getMethodName();
                    } else {
                        String methodName = result.getMethod().getMethodName();
                        if (!previousMethodName.equals(methodName)) {
                            assert !resultsPerMethod.isEmpty();
                            resultsPerClass.add(new IsenCustomizedEmailableReporter.MethodResult(resultsPerMethod));
                            resultsPerMethod = Lists.newArrayList();

                            previousMethodName = methodName;
                        }
                    }
                    resultsPerMethod.add(result);
                }
                assert !resultsPerMethod.isEmpty();
                resultsPerClass.add(new IsenCustomizedEmailableReporter.MethodResult(resultsPerMethod));
                assert !resultsPerClass.isEmpty();
                classResults.add(new IsenCustomizedEmailableReporter.ClassResult(previousClassName,
                        resultsPerClass));
            }
            return classResults;
        }

        public String getTestName() {
            return testName;
        }

        /**
         * @return the results for failed configurations (possibly empty)
         */
        public List<IsenCustomizedEmailableReporter.ClassResult> getFailedConfigurationResults() {
            return failedConfigurationResults;
        }

        /**
         * @return the results for failed tests (possibly empty)
         */
        public List<IsenCustomizedEmailableReporter.ClassResult> getFailedTestResults() {
            return failedTestResults;
        }

        /**
         * @return the results for skipped configurations (possibly empty)
         */
        public List<IsenCustomizedEmailableReporter.ClassResult> getSkippedConfigurationResults() {
            return skippedConfigurationResults;
        }

        /**
         * @return the results for skipped tests (possibly empty)
         */
        public List<IsenCustomizedEmailableReporter.ClassResult> getSkippedTestResults() {
            return skippedTestResults;
        }

        /**
         * @return the results for passed tests (possibly empty)
         */
        public List<IsenCustomizedEmailableReporter.ClassResult> getPassedTestResults() {
            return passedTestResults;
        }

        public int getFailedTestCount() {
            return failedTestCount;
        }

        public int getSkippedTestCount() {
            return skippedTestCount;
        }

        public int getPassedTestCount() {
            return passedTestCount;
        }

        public long getDuration() {
            return duration;
        }

        public String getIncludedGroups() {
            return includedGroups;
        }

        public String getExcludedGroups() {
            return excludedGroups;
        }

        /**
         * Formats an array of groups for display.
         */
        protected String formatGroups(String[] groups) {
            if (groups.length == 0) {
                return "";
            }

            StringBuilder builder = new StringBuilder();
            builder.append(groups[0]);
            for (int i = 1; i < groups.length; i++) {
                builder.append(", ").append(groups[i]);
            }
            return builder.toString();
        }
    }

    /**
     * Groups {@link IsenCustomizedEmailableReporter.MethodResult}s by class.
     */
    protected static class ClassResult {
        private final String className;
        private final List<IsenCustomizedEmailableReporter.MethodResult> methodResults;

        /**
         * @param className     the class name
         * @param methodResults the non-null, non-empty {@link IsenCustomizedEmailableReporter.MethodResult} list
         */
        public ClassResult(String className, List<IsenCustomizedEmailableReporter.MethodResult> methodResults) {
            this.className = className;
            this.methodResults = methodResults;
        }

        public String getClassName() {
            return className;
        }

        /**
         * @return the non-null, non-empty {@link IsenCustomizedEmailableReporter.MethodResult} list
         */
        public List<IsenCustomizedEmailableReporter.MethodResult> getMethodResults() {
            return methodResults;
        }
    }

    /**
     * Groups test results by method.
     */
    protected static class MethodResult {
        private final List<ITestResult> results;

        /**
         * @param results the non-null, non-empty result list
         */
        public MethodResult(List<ITestResult> results) {
            this.results = results;
        }

        /**
         * @return the non-null, non-empty result list
         */
        public List<ITestResult> getResults() {
            return results;
        }
    }
}

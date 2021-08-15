package common;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

public class GetEnvironmentVariables {
	private static Object lockObject = new Object();
	private static GetEnvironmentVariables environmentVariables = null;
	private String baseUrl = null;
	private String currentStack = null;
	private String omenUserName = null;
	private String omenPassword = null;
	private String csvSeparator = null;
	private String arraySeparator = null;
	private String propertySeparator = null;
	private String propertyPairSeparator = null;

	private String protocol = null;

	private String generateReport=null;
	private String stagingStack=null;


	Properties properties = null;


	public static GetEnvironmentVariables getInstance() {
		if (environmentVariables == null) {
			synchronized (lockObject) {
				if (environmentVariables == null) {
					environmentVariables = new GetEnvironmentVariables();
				}
			}
		}
		return (environmentVariables);
	}
	
	private GetEnvironmentVariables() {

		FileInputStream fileInputStream = null;
		try {
			fileInputStream = new FileInputStream(new File(getProjectRootDirectory() + "\\resources\\environment.properties"));
			properties = new Properties();
			properties.load(fileInputStream);
			currentStack = properties.getProperty("CurrentStack").toUpperCase();
			if (currentStack == null)
			{
				Exception e = new Exception("No stack is got, please check environment.properties");
				throw e;
			}
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (fileInputStream != null){
			try {
				fileInputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}

	public String getBaseURL(){
		baseUrl = properties.getProperty(currentStack);
		return baseUrl;
	}

	public String getProtocol()	{
		protocol = properties.getProperty("Protocol");
		return protocol;
	}

    public boolean getExcludeDisabled() {
        return Boolean.parseBoolean(properties.getProperty("ExcludeDisabled"));
    }

    public boolean getPassFailedWithBug() {
        return Boolean.parseBoolean(properties.getProperty("PassFailedWithBug"));
    }

    public boolean getUseProxy()
	{
		return Boolean.parseBoolean(properties.getProperty("UseProxy"));
	}

    public String getOmenUserName(){
		omenUserName = properties.getProperty("OmenUserName-" + currentStack);
		return omenUserName;
	}

	public String getOmenPassword(){
		omenPassword = properties.getProperty("OmenPassword");
		return omenPassword;
	}

	public String getCurrentStack(){
		currentStack = properties.getProperty("CurrentStack").toUpperCase();
		return currentStack;
	}

	public String getProjectRootDirectory(){
		String projectDirectory = null;
		try {
			File directory = new File("");
			projectDirectory = directory.getCanonicalPath();
		}
		catch (IOException e){
			e.printStackTrace();
		}

		return projectDirectory;
	}

	public String getCSVSeparator(){
		if (csvSeparator == null)
		{
			csvSeparator = properties.getProperty("DataSeparator");
		}

		return separatorUnifier(csvSeparator);
	}

	public String getArraySeparator(){
		if (arraySeparator == null)
		{
			arraySeparator = properties.getProperty("ArraySeparator");
		}

		return separatorUnifier(arraySeparator);
	}

	public String getPropertySeparator(){
		if (propertySeparator == null)
		{
			propertySeparator = properties.getProperty("PropertySeparator");
		}

		return separatorUnifier(propertySeparator);
	}

	public String getPropertyPairSeparator(){
		if (propertyPairSeparator == null)
		{
			propertyPairSeparator = properties.getProperty("PropertyPairSeparator");
		}

		return separatorUnifier(propertyPairSeparator);
	}

	public String getTimeErrorValue(){
		return properties.getProperty("TimeError_"+getCurrentStack());
	}

	public String getGenerateCSVReport(){
		if (generateReport == null)
		{
			generateReport = properties.getProperty("GenerateCSVReport");
		}

		return generateReport;
	}

	public String getOpenCSVReport(){
		return properties.getProperty("OpenCSVReport");
	}

	public String getOpenHTMLReport(){
		return properties.getProperty("OpenHTMLReport");
	}

	public boolean getShowPassForHTML(){
		return Boolean.parseBoolean(properties.getProperty("ShowPassForHTML"));
	}

	public String getStagingStack(){
		if (stagingStack == null)
		{
			stagingStack = properties.getProperty("Staging");
		}

		return stagingStack;
	}

	public String getBeanShellPrefix()
	{
		return properties.getProperty("BeanShellPrefix");
	}

	public String getBeanShellSubfix()
	{
		return properties.getProperty("BeanShellSubfix");
	}

	public String separatorUnifier(String input){
		String result;
		result = input.replace("|", "\\|");
		result = result.replace(".", "\\.");

		return result;
	}



	public String getCsBaseURL(){
		baseUrl = properties.getProperty("cs_baseUrl");
		return baseUrl;
	}

	public String getCreditSuisseJsonFileName(){
		return properties.getProperty("CreditSuisseJsonFileName");
	}

}

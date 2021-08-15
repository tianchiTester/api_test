package utility;
import org.apache.log4j.Logger;

public class SimpleTool {
    public static String getCurrentMethodName()
    {
        String nameofCurrMethod = new Throwable()
                .getStackTrace()[2]
                .getMethodName();
        return nameofCurrMethod;
    }

    public static void logCurrentMethodName(Logger logger)
    {
        logger.info("Entering method:" + getCurrentMethodName());
    }

    public static String convertMillisecondsToMinutesAndSeconds(long milliSeconds)
    {
        long minutes = (milliSeconds / 1000) / 60;
        long seconds = (milliSeconds / 1000) % 60;

        if (minutes <= 0)
        {
            return seconds + "s";
        }
        else
        {
            return minutes + "m" + seconds + "s";
        }
    }
}

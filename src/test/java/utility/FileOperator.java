package utility;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class FileOperator {

    public static boolean createFile(String fileName) {
        boolean result = false;
        File accountFile = new File(fileName);
        if (accountFile.exists()) {
            try {
                accountFile.delete();
                result = accountFile.createNewFile();
            } catch (Exception e) {
                // TODO: handle exception
                e.printStackTrace();
            }
        } else {
            try {
                result = accountFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                try {
                    accountFile.delete();
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
            }
        }

        return result;
    }

    public static void writeContentToFile(String fileName, String newContent, boolean isAppend) throws IOException {
        FileWriter writer = new FileWriter(fileName, isAppend);
        try {
            writer.write(newContent);
            writer.write("\n");
            writer.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void openFile(String fileName)
    {
        try
        {
            Desktop desk= Desktop.getDesktop();
            File reportFile = new File(fileName);
            desk.open(reportFile);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

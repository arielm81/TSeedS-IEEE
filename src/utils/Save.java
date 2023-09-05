package utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import twitter4j.Status;
import twitter4j.json.DataObjectFactory;


@SuppressWarnings("deprecation")
public class Save
{
  private static Save instance = null;
  private static String lastFile = null;
  

  private Save() {}
  

  public static Save getInstance()
  {
    if (instance == null) {
      instance = new Save();
    }
    return instance;
  }
  
  public void saveStatus(String filePath, Status status) {
    IO io = new IO();
    
    String toWrite = "";
    toWrite = toWrite + DataObjectFactory.getRawJSON(status);
    
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd, HH");
    String fileName = sdf.format(new Date()) + "hs";
    String totalName = filePath + fileName + ".json";
    
    if (io.fileExists(totalName)) {
      io.appendToFile(totalName, ", " + toWrite);
    } else {
      if (lastFile != null) {
        io.appendToFile(lastFile, "]");
      }
      io.writeToFileNew(totalName, "[" + toWrite);
    }
    lastFile = totalName;
  }
  
  public void saveResults(String filePath, String results)
  {
    IO io = new IO();
    io.writeToFileNew(filePath, results);
  }
}

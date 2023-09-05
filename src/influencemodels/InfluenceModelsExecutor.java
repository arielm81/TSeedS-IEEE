package influencemodels;

import java.io.BufferedReader;

public class InfluenceModelsExecutor
{
  public InfluenceModelsExecutor() {}
  
  public void execute()
  {
    try {
      ProcessBuilder builder = new ProcessBuilder(new String[] { "InfluenceModels", "-c", "config_maxinf_CD.txt" });
      builder.redirectErrorStream(true);
      Process process = builder.start();
      BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
      while (reader.readLine() != null) {}
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
    }
  }
  
  public void executeDir(String config_dir)
  {
    try {
      ProcessBuilder builder = new ProcessBuilder(new String[] { "InfluenceModels", "-c", config_dir });
      builder.redirectErrorStream(true);
      Process process = builder.start();
      BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
      while (reader.readLine() != null) {}
    }
    catch (Exception e)
    {
      System.out.println(e.getMessage());
    }
  }
  
  public void runFirstScanDir(String config_dir){
	  try {
	      ProcessBuilder builder = new ProcessBuilder(new String[] { "InfluenceModels", "-c", config_dir });
	      builder.redirectErrorStream(true);
	      Process process = builder.start();
	      BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
	      while (reader.readLine() != null) {}
	    }
	    catch (Exception e)
	    {
	      System.out.println(e.getMessage());
	    }
  }
  
  public void runFirstScan(){
	  try {
	      ProcessBuilder builder = new ProcessBuilder(new String[] { "InfluenceModels", "-c", "config_training_scan1.txt" });
	      builder.redirectErrorStream(true);
	      Process process = builder.start();
	      BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
	      while (reader.readLine() != null) {}
	    }
	    catch (Exception e)
	    {
	      System.out.println(e.getMessage());
	    }
  }
  
  public void runTrueSpread(){
	  try {
	      ProcessBuilder builder = new ProcessBuilder(new String[] { "InfluenceModels", "-c", "config_true_spread.txt" });
	      builder.redirectErrorStream(true);
	      Process process = builder.start();
	      BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
	      while (reader.readLine() != null) {}
	    }
	    catch (Exception e)
	    {
	      System.out.println(e.getMessage());
	    }
  }
  
  public void runTrueSpreadDir(String config_dir){
	  try {
	      ProcessBuilder builder = new ProcessBuilder(new String[] { "InfluenceModels", "-c", config_dir });
	      builder.redirectErrorStream(true);
	      Process process = builder.start();
	      BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
	      while (reader.readLine() != null) {}
	    }
	    catch (Exception e)
	    {
	      System.out.println(e.getMessage());
	    }
  }
  
  public void runSecondScanDir(String config_dir){
	  try {
	      ProcessBuilder builder = new ProcessBuilder(new String[] { "InfluenceModels", "-c", config_dir });
	      builder.redirectErrorStream(true);
	      Process process = builder.start();
	      BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
	      while (reader.readLine() != null) {}
	    }
	    catch (Exception e)
	    {
	      System.out.println(e.getMessage());
	    }
  }
  
  public void runSecondScan(){
	  try {
	      ProcessBuilder builder = new ProcessBuilder(new String[] { "InfluenceModels", "-c", "config_training_scan2.txt" });
	      builder.redirectErrorStream(true);
	      Process process = builder.start();
	      BufferedReader reader = new BufferedReader(new java.io.InputStreamReader(process.getInputStream()));
	      while (reader.readLine() != null) {}
	    }
	    catch (Exception e)
	    {
	      System.out.println(e.getMessage());
	    }
  }
}

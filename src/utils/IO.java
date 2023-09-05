package utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;


public class IO
{
  private String charset;
  
  public IO()
  {
    this("UTF-8");
  }
  
  public IO(String charset) {
    this.charset = charset;
  }
  

  public String getCharset()
  {
    return charset;
  }
  
  public void setCharset(String charset) {
    this.charset = charset;
  }
  

  public void writeToFileOld(String path, String toWrite)
  {
    try
    {
      PrintWriter writer = new PrintWriter(path, charset);
      writer.print(toWrite);
      writer.close();
    } catch (FileNotFoundException e) {
      e.printStackTrace();
    } catch (UnsupportedEncodingException e) {
      e.printStackTrace();
    }
  }
  

  public void writeToFileNew(String path, String toWrite)
  {
    //System.out.println("LLEGA A WRITE FO FILE NEW");
    File file = new File(path);
    
    try
    {
      FileWriter fw = new FileWriter(file, true);
      
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(toWrite);
      
      bw.close();
    } catch (Exception e1) {
      System.out.println(e1.getMessage());
    }
  }
  
  public void writeToFileNew(String path, String toWrite, Boolean append){
    //System.out.println("LLEGA A WRITE FO FILE NEW");
    File file = new File(path);
    	try {
    		if (!append)
    			file.getParentFile().mkdirs();
    	  
	      FileWriter fw = new FileWriter(file, append);
	      
	      BufferedWriter bw = new BufferedWriter(fw);
	      bw.write(toWrite);
	      
	      bw.close();
	    } catch (Exception e1) {
	      System.out.println(e1.getMessage());
	    }	
  }
  
  public void appendToFile(String path, String toWrite)
  {
    File file = new File(path);
    
    try
    {
      FileWriter fw = new FileWriter(file, true);
      
      BufferedWriter bw = new BufferedWriter(fw);
      bw.write(toWrite);
      
      bw.close();
    } catch (Exception e1) {
      System.out.println(e1.getMessage());
    }
  }
  
  public String readFromFile(String path){
	  
    String out = "";
    Path pathToRead = Paths.get(path, new String[0]);
    BufferedReader reader = null;
    try{
      reader = Files.newBufferedReader(pathToRead, Charset.forName(charset));
      String line = null;
      while ((line = reader.readLine()) != null) {
        out = out + line + "\n";
      }
      reader.close();
    } catch (Exception e) {
        System.out.println(e.getMessage());
    }
    
    return out;
  }
  
  public boolean fileExists(String totalName) {
    return new File(totalName).isFile();
  }
}

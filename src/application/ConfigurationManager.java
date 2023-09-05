package application;

import filters.FiltroAbs;
import filters.LanguageFilter;
import filters.OrFilter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;
import twitter4j.FilterQuery;


public class ConfigurationManager
{
  FiltroAbs filter;
  FilterQuery fq;
  Long time_filter;
  Integer update_window;
  Integer training_window;
  private static final String[] DEFAULT_CATEGORY = { "futbol" };
  private static final Long DEFAULT_TIME = Long.valueOf(3600000L);
  private static final Integer DEFAULT_TRAINING_TIME = Integer.valueOf(600000);
  private static final Integer DEFAULT_UPDATE_TIME = Integer.valueOf(60000);
  
  private static final String DEFAULT_LANGUAGE = "und";
  
  private static final String OLDERTIME_FIELD = "OlderTime";
  private static final String UPDATETIME_FIELD = "UpdateWindow";
  private static final String TRAININGTIME_FIELD = "TrainingWindow";
  private static final String CATEGORY_FIELD = "CategoryFilter";
  private static final String LANGUAGE_FIELD = "LanguageFilter";
  private String config_dir = "initconfig.txt";
  private Hashtable<String, String> properties;
  
  public ConfigurationManager() {}
  
  private String category_dir;
  private String language_dir;
  
  public void configure() {
    properties = new Hashtable<String, String>();
    time_filter = DEFAULT_TIME;
    update_window = DEFAULT_UPDATE_TIME;
    training_window = DEFAULT_TRAINING_TIME;
    fq = new FilterQuery();
    category_dir = null;
    language_dir = null;
    filter = null;
    readInitConfig();
    if (language_dir != null) {
      readLanguageFilters();
    } /*else
      filter = new LanguageFilter(DEFAULT_LANGUAGE);
      */
    if (category_dir != null)
      readCategoryFilters();
    else
      fq.track(DEFAULT_CATEGORY);
  }
  
  
  private void readInitConfig() { File file = new File(config_dir);
    if (file.exists()) {
      try {
        FileInputStream fstream = null;
        fstream = new FileInputStream(file);
        @SuppressWarnings("resource")
		Scanner br = new Scanner(new InputStreamReader(fstream));
        while (br.hasNext()) {
          String strLine = br.nextLine();
          String field = strLine.split(" : ")[0];
          String value = strLine.split(" : ")[1];
          properties.put(field, value);
        }
        fstream.close();
      } catch (IOException e) {
        e.printStackTrace();
        return;
      }
      
    } else {
      return;
    }
    if (properties.containsKey(OLDERTIME_FIELD))
		convertTime(OLDERTIME_FIELD);
	if (properties.containsKey(UPDATETIME_FIELD))
		convertTime(UPDATETIME_FIELD);
	if (properties.containsKey(TRAININGTIME_FIELD))
		convertTime(TRAININGTIME_FIELD);
	if (properties.containsKey(CATEGORY_FIELD))
		category_dir = properties.get(CATEGORY_FIELD);
	if (properties.containsKey(LANGUAGE_FIELD))
		language_dir = properties.get(LANGUAGE_FIELD);
  }
  
  private void convertTime(String field) {
    try {
      if (!field.equals("OlderTime")) {
    	  Integer t = Integer.valueOf(properties.get(field));
		  t = t*60*1000; // se pasa de minutos a milisegundos
		  switch(field){
			case UPDATETIME_FIELD: update_window = t; break;
			case TRAININGTIME_FIELD: training_window = t; break;
			default: break;
		  }
      }
      else {
        Long t = Long.valueOf((String)properties.get(field));
        t = Long.valueOf(t.longValue() * 60L * 1000L);
        time_filter = t;
      }
    } catch (NumberFormatException n) {
      n.getMessage();
    }
  }
  
  public Long getFilterTime() { return time_filter; }
  
  public FiltroAbs getFilter() {
    return filter;
  }
  
  public FilterQuery getFilterQuery() { return fq; }
  
  public Integer getUpdate_window() {
    return update_window;
  }
  
  public Integer getTraining_window() { return training_window; }
  
  private void readLanguageFilters() {
    File file = new File(language_dir);
    if (file.exists()) {
      ArrayList<FiltroAbs> al = new ArrayList<FiltroAbs>();
      try {
        FileInputStream fstream = null;
        fstream = new FileInputStream(file);
        @SuppressWarnings("resource")
		Scanner br = new Scanner(new InputStreamReader(fstream));
        while (br.hasNext()) {
          String strLine = br.nextLine();
          LanguageFilter fl = new LanguageFilter(strLine);
          if (!al.isEmpty()) {
            OrFilter fo = new OrFilter((FiltroAbs)al.get(0), fl);
            al.remove(0);
            al.add(fo);
          } else {
            al.add(fl);
          }
        }
        fstream.close();
        if (!al.isEmpty()) {
          filter = ((FiltroAbs)al.get(0));
        }/* else
          filter = new LanguageFilter(DEFAULT_LANGUAGE);
          */
      } catch (IOException e) {
        e.printStackTrace();
        /*
        filter = new LanguageFilter(DEFAULT_LANGUAGE);
        */
        return;
      }
    }
  }
  
  private void readCategoryFilters() { File file = new File(category_dir);
    if (file.exists()) {
      ArrayList<String> al = new ArrayList<String>();
      try {
        FileInputStream fstream = null;
        fstream = new FileInputStream(file);
        @SuppressWarnings("resource")
		Scanner br = new Scanner(new InputStreamReader(fstream));
        while (br.hasNext()) {
          String strLine = br.nextLine();
          al.add(strLine);
        }
        fstream.close();
        String[] keywords = (String[])al.toArray(new String[0]);
        fq.track(keywords);
        System.out.println("KEYWORDS LIST");
        for(String k : keywords)
        	System.out.println(k);
      } catch (IOException e) {
        e.printStackTrace();
        fq.track(DEFAULT_CATEGORY);
        return;
      }
    }
  }
}

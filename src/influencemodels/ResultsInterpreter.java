package influencemodels;

import actionlog.ActionLog;
import database.DBManager;
import infoprocess.User;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Date;
import java.util.Vector;
import seed.HistoricSeedsManager;
import socialgraph.GraphVisualization;
import socialgraph.SocialGraph;


public class ResultsInterpreter{
  
  private Integer budget;
  private Double threshold;
  private Integer totalactions;
  private Integer totalusers;
  private Double totalmarginf;
  private Date timestamp_init;
  private Date timestamp_end;
 
  private static ResultsInterpreter instance = null; 
  
  
  public static ResultsInterpreter getInstance(){
	  if (instance == null)
		  instance = new ResultsInterpreter();
	  return instance;
  }
  
  private ResultsInterpreter(){
	  budget = 0;
	  threshold = 0d;
	  totalactions = 0;
	  totalusers = 0;
	  totalmarginf = 0d;
	  timestamp_init = null;
	  timestamp_end = null;
  }
  
  public void saveTrueSpreadInDB(Integer id, String root, String metric){
	  try{
		  @SuppressWarnings("resource")
		  BufferedReader br = new BufferedReader(new FileReader(root+metric+"/seedsx50.txt_PCCov.txt"));
		  String line = null;
		  int pos_in_seed = 1;
		  while (((line = br.readLine()) != null) && line != ""){
			  String[] linea = line.split(" ");
			  DBManager.getInstance().updateTrueSpreadSeed(id, pos_in_seed, Double.valueOf(linea[3]), metric);
			  pos_in_seed++;
		  }
	  }catch(FileNotFoundException e){
		  e.printStackTrace();
		  return;
	  }catch(IOException e){
		  e.printStackTrace();
		  return;
	  }
  }
  
  public void saveResultsInDB(){
	  try {
		  Integer idset = DBManager.getInstance().insertSeedSet(timestamp_init, timestamp_end, budget, 0d, totalusers, totalactions);
		  @SuppressWarnings("resource")
		  BufferedReader br = new BufferedReader(new FileReader("auto_generated/maxinf_CD/PCCov_0_0.001.txt"));
		  String line = null;
	      int pos_in_seed = 1;
		  while (((line = br.readLine()) != null) && line != ""){
			  String[] linea = line.split(" ");
			  Integer user_id = Integer.valueOf(Integer.parseInt(linea[0]));
			  totalmarginf = Double.valueOf(linea[1]);
			  DBManager.getInstance().insertSeed(idset, pos_in_seed, Double.valueOf(linea[2]), DBManager.getInstance().getUserByIdNorm(user_id));
			  pos_in_seed++;
		  }
		  DBManager.getInstance().updateSeedSet(idset, totalmarginf);
	  } catch (FileNotFoundException e1) {
		e1.printStackTrace();
		return;
	  }catch (IOException e) {
		e.printStackTrace();
		return;
	  }	  
  }
  
  @SuppressWarnings("resource")
  public void saveExecConfig(){
	    String filename = "config_maxinf_CD.txt";
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException e2) {
			e2.printStackTrace();
			return;
		}
	    String line = null;
	    try {
			while ((line = br.readLine()) != null){
				if (line.contains("truncation_threshold")){
					setThreshold(Double.valueOf(getOption(line)));
				}else if (line.contains("budget")){
					setBudget(Integer.valueOf(getOption(line)));
				}
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			return;
		}
  }
  
  private String getOption(String line){
	  return line.split(" : ")[1];
  }
  
  public Vector<String[]> getResultsAsCells()
  {
    Vector<String[]> cells = new Vector<String[]>();
    try {
      Vector<String> maxInfUsers = new Vector<String>();
      @SuppressWarnings("resource")
	  BufferedReader br = new BufferedReader(new FileReader("maxinf_CD/PCCov_0_0.001.txt"));
      String line = null;
      Date d = new Date();
      int pos_in_seed = 1;
      while ((line = br.readLine()) != null)
      {
        String[] linea = line.split(" ");
        Integer user_id = Integer.valueOf(Integer.parseInt(linea[0]));
        User user = SocialGraph.getInstance().getUser(user_id.intValue());
        maxInfUsers.add(user.getScreenName());
        String[] cell = { user.getScreenName(), linea[2], String.valueOf(user.getNumberOfTweets()), String.valueOf(user.getNumberOfRetweets()), String.valueOf(user.getNumberOfReplies()), String.valueOf(user.getRetweeted()), String.valueOf(user.getReplied()) };
        
        user.addInfoSeedSet(linea[2], d, pos_in_seed);
        HistoricSeedsManager.getInstance().addSeed(user);
        cells.add(cell);
        pos_in_seed++;
      }
      GraphVisualization.getInstance().setMaxInfUsers(maxInfUsers);
    }
    catch (IOException localIOException) {}
    return cells;
  }
  
  public String getResultsAsText()
  {
    String line = null;
    String results = "";
    try {
      Vector<String> maxInfUsers = new Vector<String>();
      @SuppressWarnings("resource")
	  BufferedReader br = new BufferedReader(new FileReader("maxinf_CD/PCCov_0_0.001.txt"));
      while ((line = br.readLine()) != null)
      {
        String[] linea = line.split(" ");
        Integer user_id = Integer.valueOf(Integer.parseInt(linea[0]));
        User user = SocialGraph.getInstance().getUser(user_id.intValue());
        maxInfUsers.add(user.getScreenName());
        results = results + "User: " + user.getScreenName() + " Marginal Influenciability: " + linea[2] + " Tweets: " + user.getNumberOfTweets() + " Retweets: " + user.getNumberOfRetweets() + " Replies: " + user.getNumberOfReplies() + " Retweeted: " + user.getRetweeted() + " Replied: " + user.getReplied() + "\n";
      }
      
      results = results + "Total Users: " + SocialGraph.getInstance().getNroUsers() + '\n';
      return results + "Total Interactions: " + ActionLog.getInstance().getNroEntries() + '\n';
    }
    catch (Exception e) {
      System.out.println(e.getMessage());
    }
    return results;
  }

public Integer getBudget() {
	return budget;
}

public void setBudget(Integer budget) {
	this.budget = budget;
}

public Double getThreshold() {
	return threshold;
}

public void setThreshold(Double threshold) {
	this.threshold = threshold;
}

public Integer getTotalactions() {
	return totalactions;
}

public void setTotalactions(Integer totalactions) {
	this.totalactions = totalactions;
}

public Integer getTotalusers() {
	return totalusers;
}

public void setTotalusers(Integer totalusers) {
	this.totalusers = totalusers;
}

public Double getTotalmarginf() {
	return totalmarginf;
}

public void setTotalmarginf(Double totalmarginf) {
	this.totalmarginf = totalmarginf;
}
public Date getTimestamp_init() {
	return timestamp_init;
}

public void setTimestamp_init(Date timestamp_init) {
	this.timestamp_init = timestamp_init;
}

public Date getTimestamp_end() {
	return timestamp_end;
}

public void setTimestamp_end(Date timestamp_end) {
	this.timestamp_end = timestamp_end;
}
}

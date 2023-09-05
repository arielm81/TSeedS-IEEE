package influencemodels;

import actionlog.ActionLog;
import actionlog.LogEntry;
import database.DBManager;
import infoprocess.User;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;
import socialgraph.SocialGraph;
import utils.IO;
import utils.Pair;




public class FilesBuilder
{
  public static final String dir_root = "auto_generated_TSeedSComplete/";
  public static final String dir_training = "training/";
  public static final String dir_training_scan1 = "scan1/";
  public static final String dir_training_scan2 = "scan2/";
  public static final String dir_trueSpread = "config_true_spreadx";
  public static final String dir_scan1 = "config_training_scan1.txt";
  public static final String dir_scan2 = "config_training_scan2.txt";
  public static final String dir_graph = "graph.txt";
  public static final String dir_action = "actionslog.txt";
  public static final String dir_trainaction = "actions_in_training.txt";
  public static final String dir_testaction = "actions_in_testing.txt";
  public static final String dir_seeds = "seedsx";
  
  private SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  private SimpleDateFormat sdfolder = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
  
  public FilesBuilder() {}
  
  public Boolean buildFirstScan(Date inicio, Date fin){
	  IO io = new IO();
	   
	  ResultsInterpreter.getInstance().setTimestamp_init(inicio);
	  ResultsInterpreter.getInstance().setTimestamp_end(fin);
	  
	  
	  //archivo graph.txt
	  //userid userid time   time is in secs but zero if no info available
	  String graphFileDir = dir_root+dir_graph;
	  
	  ResultSet graphResult = DBManager.getInstance().executeQueryResult("SELECT s.millis, u.iduser_norm AS user_follower_norm, t.iduser_norm AS user_followed_norm "
			  														   + "FROM socialgraph s "
			  														   + "LEFT JOIN user u ON s.user_follower = u.iduser "
			  														   + "LEFT JOIN user t ON s.user_followed = t.iduser "
			  														   + "WHERE s.date < '"+sdl.format(fin)+"' "
			  														   + "ORDER BY s.millis ASC;"); 
	
	  if (graphResult!=null){
		  String graphText = "";
		  try {
			  /*
			   Long inicial = 0L;
			   if (graphResult.next()){
				   inicial = graphResult.getLong("millis")/1000L;
				   graphText += graphResult.getLong("user_follower_norm") +" "+graphResult.getLong("user_followed_norm")+" "+0+"\n";
			   }
			   */
			   while (graphResult.next()){
				   //Long millis = (graphResult.getLong("millis")/1000L)-inicial;
				   if (graphResult.getLong("user_follower_norm") != graphResult.getLong("user_followed_norm"))
					   graphText += graphResult.getLong("user_follower_norm") +" "+graphResult.getLong("user_followed_norm")+" "+0+"\n";
			   }
			   io.writeToFileOld(graphFileDir, graphText);
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	  }else{
		  System.out.println("Not possible to build first scan graph file.");
		  return false;
	  }
	  //archivo actionslog.txt
	  //user action time
	  String actionLogDir = dir_root+dir_action;
	  //archivo actions_in_training.txt 
	  //action
	  String trainActionsDir = dir_root+dir_trainaction;
	  
	  ResultSet actionsResult = DBManager.getInstance().executeQueryResult( "SELECT u.iduser_norm, al.tweet_idtweet, al.millis, al.type FROM actionlog al "
																		  + "RIGHT JOIN (SELECT a.tweet_idtweet, a.created_at FROM actionlog a "
																		  + "WHERE a.type='TWEET' AND a.created_at > '"+sdl.format(inicio)+"' AND a.created_at < '"+sdl.format(fin)+"') t "
																		  + "ON al.tweet_idtweet = t.tweet_idtweet "
																		  + "LEFT JOIN user u ON al.user_iduser = u.iduser "
																		  + "WHERE al.created_at < '"+sdl.format(fin)+"' "
																		  + "ORDER BY al.tweet_idtweet, al.millis ASC;");
	  
	  if (actionsResult!=null){
		  String actionLogText = "";
		  String trainActionText = "";
		  Hashtable<Long,Long> trainActionId = new Hashtable<Long,Long>();
		  Vector<Long> userId = new Vector<Long>();
		  try {
			   Integer rows = 0;
			   Long inicial = 0L;
			   if (actionsResult.next()){
				   Long tweetid= actionsResult.getLong("tweet_idtweet");
				   if (!trainActionId.containsKey(tweetid)){
					   trainActionId.put(tweetid, Long.valueOf(trainActionId.size()+1));
					   trainActionText += trainActionId.get(tweetid)+"\n";
				   }
				   inicial = actionsResult.getLong("millis")/1000L;
				   actionLogText += actionsResult.getLong("iduser_norm")+" "+trainActionId.get(tweetid)+" "+0+"\n";
				   if (!userId.contains(actionsResult.getLong("iduser_norm")))
					   userId.add(actionsResult.getLong("iduser_norm"));
				   rows++;
			   }
			   while (actionsResult.next()){
				   Long tweetid= actionsResult.getLong("tweet_idtweet");
				   if (!trainActionId.containsKey(tweetid)){
					   trainActionId.put(tweetid, Long.valueOf(trainActionId.size()+1));
					   trainActionText += trainActionId.get(tweetid)+"\n";
				   }
				   Long millis = (actionsResult.getLong("millis")/1000L)-inicial;
				   actionLogText += actionsResult.getLong("iduser_norm")+" "+trainActionId.get(tweetid)+" "+millis+"\n";
				   if (!userId.contains(actionsResult.getLong("iduser_norm")))
					   userId.add(actionsResult.getLong("iduser_norm"));
				   rows++;
			   }
			   io.writeToFileOld(actionLogDir, actionLogText);
			   io.writeToFileOld(trainActionsDir, trainActionText);
			   ResultsInterpreter.getInstance().setTotalactions(rows);
			   ResultsInterpreter.getInstance().setTotalusers(userId.size());
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	  }else{
		  System.out.println("Not possible to build first scan actions files.");
		  return false;
	  }
	  return true;
  }

@SuppressWarnings("unchecked")
  public void makeFiles()
  {
    String directorio = "training/";
    SocialGraph sg = SocialGraph.getInstance();
    ActionLog al = ActionLog.getInstance();
    Vector<Pair<Integer, Integer>> edges = (Vector<Pair<Integer, Integer>>) sg.getEdges().clone();
    Vector<LogEntry> entries = (Vector<LogEntry>)al.getLogEntries().clone();
    Vector<User> users = sg.getUsers();
    try {
      File f2 = new File(directorio + "actionsFile.txt");
      File f3 = new File(directorio + "trainingActionsFile.txt");
      String actionLogToText = "";
      String actionsToText = "";
      Long olderTime = getOlderTime(entries);
      
      int lastActionID = -1;
      for (LogEntry entry : entries) {
        int actionID = entry.getAction().getId().intValue();
        actionLogToText = actionLogToText + entry.getUserID_normal() + " " + actionID + " " + entry.getTime_normal(olderTime.longValue()) + "\n";
        if (actionID != lastActionID) {
          actionsToText = actionsToText + actionID + "\n";
          lastActionID = actionID;
        }
      }
      FileWriter fw2 = new FileWriter(f2);
      fw2.write(actionLogToText);
      fw2.close();
      FileWriter fw3 = new FileWriter(f3);
      fw3.write(actionsToText);
      fw3.close();
    } catch (IOException e) {
      System.out.println("Problem generating Actions Files: " + e.getMessage());
    }
    try
    {
      File f1 = new File(directorio + "userInflFile.txt");
      String usersToText = "";
      DecimalFormat df = new DecimalFormat("#.####");
      for (User u : users) {
        if (u.getNumberOfTotalActionsPerformed() > 0) {
          Float inf = Float.valueOf(u.getNumberOfActionsNotInit() / u.getNumberOfTotalActionsPerformed());
          usersToText = usersToText + u.getId_normal() + " " + u.getNumberOfTotalActionsPerformed() + " " + u.getNumberOfActionsNotInit() + " " + df.format(inf) + "\n";
        }
      }
      FileWriter fw1 = new FileWriter(f1);
      fw1.write(usersToText);
      fw1.close();
    } catch (IOException e) {
      System.out.println("Problem generating User Infl File: " + e.getMessage());
    }
    

    try
    {
      File f2 = new File(directorio + "graphFile.txt");
      
      Long olderTime = getOlderTime(entries);
      Hashtable<String, Long> edges_differences = new Hashtable<String, Long>();
      Hashtable<String, Integer> edges_counts = new Hashtable<String, Integer>();
      int last_action_id = -1;
      int original_user_id = -1;
      long original_time = -1L;
      int actual_action_id; for (LogEntry e : entries)
      {
        actual_action_id = e.getAction().getId().intValue();
        int actual_user_id = e.getUserID_normal().intValue();
        long actual_time = e.getTime_normal(olderTime.longValue()).longValue();
        if (actual_action_id == last_action_id)
        {

          if (!e.isInterEntry()) {
            String key = original_user_id + "-" + actual_user_id;
            if (edges_differences.containsKey(key)) {
              long difference = ((Long)edges_differences.get(key)).longValue() + (actual_time - original_time);
              edges_differences.put(key, Long.valueOf(difference));
              int counts = ((Integer)edges_counts.get(key)).intValue();
              edges_counts.put(key, Integer.valueOf(counts + 1));
            } else {
              long difference = actual_time - original_time;
              edges_differences.put(key, Long.valueOf(difference));
              edges_counts.put(key, Integer.valueOf(1));
            }
          } else {
            String key = e.getInter_user().getId_normal() + "-" + actual_user_id;
            long inter_time = e.getTimeInter_normal(olderTime.longValue()).longValue();
            if (edges_differences.containsKey(key)) {
              long difference = ((Long)edges_differences.get(key)).longValue() + (actual_time - inter_time);
              edges_differences.put(key, Long.valueOf(difference));
              int counts = ((Integer)edges_counts.get(key)).intValue();
              edges_counts.put(key, Integer.valueOf(counts + 1));
            } else {
              long difference = actual_time - inter_time;
              edges_differences.put(key, Long.valueOf(difference));
              edges_counts.put(key, Integer.valueOf(1));
            }
          }
        } else {
          last_action_id = actual_action_id;
          original_user_id = actual_user_id;
          original_time = actual_time;
        }
      }
      
      String edgeToText = "";
      for (Pair<Integer,Integer> edge : edges) {
        String key = edge.getFirst() + "-" + edge.getSecond();
        String inversa = edge.getSecond() + "-" + edge.getFirst();
        long average_1_2 = 0L;
        long average_2_1 = 0L;
        if (edges_differences.containsKey(key))
          average_1_2 = ((Long)edges_differences.get(key)).longValue() / ((Integer)edges_counts.get(key)).intValue();
        if (edges_differences.containsKey(inversa))
          average_2_1 = ((Long)edges_differences.get(inversa)).longValue() / ((Integer)edges_counts.get(inversa)).intValue();
        if (average_1_2 > average_2_1) {
          edgeToText = edgeToText + edge.getFirst() + " " + edge.getSecond() + " " + average_1_2 + " " + average_2_1 + " 0\n";
        } else
          edgeToText = edgeToText + edge.getSecond() + " " + edge.getFirst() + " " + average_2_1 + " " + average_1_2 + " 0\n";
      }
      FileWriter fw2 = new FileWriter(f2);
      fw2.write(edgeToText);
      fw2.close();
    } catch (IOException e) {
      System.out.println("Problem generating GraphFile: " + e.getMessage());
    }
  }
  
  private Long getOlderTime(Vector<LogEntry> entries) {
    long older = ((LogEntry)entries.get(0)).getTime().longValue();
    for (LogEntry e : entries) {
      if (e.getTime().longValue() < older)
        older = e.getTime().longValue();
    }
    return Long.valueOf(older);
  }
  
  public boolean buildFirstScanForTrueSpread(Date iniciodate, Date findate){
	  IO io = new IO();
	  
	  String folderFormat = sdfolder.format(findate)+"/";
	  
	  File dir = new File(dir_root+folderFormat);
	  dir.mkdir();
	  
	  File dir_train = new File(dir_root+folderFormat+dir_training);
	  dir_train.mkdir();
	  
	  File dir_scan_1 = new File(dir_root+folderFormat+dir_training+dir_training_scan1);
	  dir_scan_1.mkdir();
	  
	  File dir_scan_2 = new File(dir_root+folderFormat+dir_training+dir_training_scan2);
	  dir_scan_2.mkdir();
	  
	  createConfigFiles(folderFormat);
	  
	  //archivo graph.txt
	  //userid userid time   time is in secs but zero if no info available
	  String graphFileDir = dir_root+folderFormat+dir_graph;
	  //archivo actionslog.txt
	  //user action time
	  String actionLogDir = dir_root+folderFormat+dir_action;
	  //archivo actions_in_training.txt 
	  //action
	  String trainActionsDir = dir_root+folderFormat+dir_trainaction;
	  //archivo actions_in_testing.txt 
	  //action
	  String testActionsDir = dir_root+folderFormat+dir_testaction;
	  
	  ResultSet actionsResult = DBManager.getInstance().executeQueryResult("SELECT al.user_iduser AS influenciado, u.iduser_norm, al.tweet_idtweet, al.millis, al.type, t.user_iduser AS original, u2.iduser_norm "
																	   + "FROM actionlog al "
																	   + "RIGHT JOIN (SELECT a.tweet_idtweet, a.created_at, a.user_iduser "
																	               + "FROM actionlog a "
																	               + "LEFT JOIN user u ON a.user_iduser = u.iduser "
																	               + "WHERE a.type='TWEET' "
																	               + "AND a.created_at > '"+sdl.format(iniciodate)+"' "
																	               + "AND a.created_at < '"+sdl.format(findate)+"') "
																	               + "t ON al.tweet_idtweet = t.tweet_idtweet "
																	   + "LEFT JOIN user u ON al.user_iduser = u.iduser "
																	   + "LEFT JOIN user u2 ON t.user_iduser = u2.iduser "
																	   + "WHERE al.created_at < '"+sdl.format(findate)+"' "
																	   + "ORDER BY al.tweet_idtweet, al.millis ASC;"); 
	
	  if (actionsResult != null){
		  Vector<Pair<Long,Long>> edges = new Vector<Pair<Long,Long>>();
		  Hashtable<Long,Long> trainActionId = new Hashtable<Long,Long>();
		  Long inicial = 0L;
		  
		  String graphText = "";
		  String actionLogText = "";
		  String trainActionText = "";
		  String testActionText = "";
		  
		  try {
			   if (actionsResult.next()){
				   Long tweetid= actionsResult.getLong("al.tweet_idtweet");
				   if (!trainActionId.containsKey(tweetid)){
					   trainActionId.put(tweetid, Long.valueOf(trainActionId.size()+1));
					   trainActionText += trainActionId.get(tweetid)+"\n";
				   }
				   inicial = actionsResult.getLong("al.millis")/1000L;
				   actionLogText += actionsResult.getLong("u.iduser_norm")+" "+trainActionId.get(tweetid)+" "+0+"\n";
			   }
			   
			   while (actionsResult.next()){
				   String type = actionsResult.getString("al.type");
				   Long influenciado_norm = actionsResult.getLong("u.iduser_norm");
				   Long tweet_id= actionsResult.getLong("al.tweet_idtweet");
				   
				   if (!trainActionId.containsKey(tweet_id)){
					   trainActionId.put(tweet_id, Long.valueOf(trainActionId.size()+1));
					   trainActionText += trainActionId.get(tweet_id)+"\n";
				   }
				   Long millis = (actionsResult.getLong("al.millis")/1000L) - inicial;
				   actionLogText += influenciado_norm+" "+trainActionId.get(tweet_id)+" "+millis+"\n";
				   
				   if (type.equals("RETWEET")){
					   Long original_norm = actionsResult.getLong("u2.iduser_norm");
					   if (! influenciado_norm.equals(original_norm)){
						   Pair<Long,Long> edge = new Pair<Long,Long>(influenciado_norm, original_norm);
						   Pair<Long,Long> edgereverse = new Pair<Long,Long>(original_norm, influenciado_norm);
						   if (!edges.contains(edge) && !edges.contains(edgereverse)){
							   graphText += influenciado_norm+" "+original_norm+" "+0+"\n";
							   edges.add(edge);
						   }
					   }
				   }
			   }
			   testActionText = trainActionText;
			   
			   io.writeToFileNew(graphFileDir, graphText, false);
			   io.writeToFileNew(actionLogDir, actionLogText, false);
			   io.writeToFileNew(trainActionsDir, trainActionText, false);
			   io.writeToFileNew(testActionsDir, testActionText, false);
			   
			   InfluenceModelsExecutor ime = new InfluenceModelsExecutor();
			   ime.runFirstScanDir(dir_root+folderFormat+dir_scan1);
			   ime.runSecondScanDir(dir_root+folderFormat+dir_scan2);
			   
			   return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	return false;
  }
  
  private void createConfigFiles(String folder){
	  String scan1FileDir = dir_root+folder+dir_scan1;
	  String scan2FileDir = dir_root+folder+dir_scan2;
	  
	  String scan1Text = "# Config file for learning paramters (Scan 1)"+"\n";
	  scan1Text += "phase : 1"+"\n";
	  scan1Text += "computeUserInf : 0"+"\n";
	  scan1Text += "graphFile : "+dir_root+folder+dir_graph+"\n";
	  scan1Text += "actionsFile : "+dir_root+folder+dir_action+"\n";
	  scan1Text += "outdir : "+dir_root+folder+dir_training+dir_training_scan1+"\n";
	  scan1Text += "maxTuples : 0"+ "\n";
	  scan1Text += "trainingActionsFile : "+dir_root+folder+dir_trainaction+"\n";
	  
	  String scan2Text = "# Config file for learning paramters (Scan 2)"+"\n";
	  scan2Text += "phase : 1"+"\n";
	  scan2Text += "computeUserInf : 1"+"\n";
	  scan2Text += "training_dir : "+dir_root+folder+dir_training+dir_training_scan1+"\n";
	  scan2Text += "actionsFile : "+dir_root+folder+dir_action+"\n";
	  scan2Text += "outdir : "+dir_root+folder+dir_training+dir_training_scan2+"\n";
	  scan2Text += "maxTuples : 0"+ "\n";
	  scan2Text += "trainingActionsFile : "+dir_root+folder+dir_trainaction+"\n";
	  
	  
	  
	  IO io = new IO();
      io.writeToFileNew(scan1FileDir, scan1Text, false);
	  io.writeToFileNew(scan2FileDir, scan2Text, false);
	  
  }
  
  public void createTrueSpreadConfigFiles(Date findate){
	  String root = dir_root+sdfolder.format(findate)+"/";
	  createTrueSpreadConfigFile("marginalinfluence", root, 50);
	  createTrueSpreadConfigFile("retweetimpact", root, 50);
	  createTrueSpreadConfigFile("socialnetworkpotential", root, 50);
	  createTrueSpreadConfigFile("closeness", root, 50);
  }
  
  private void createTrueSpreadConfigFile(String metric, String root, Integer seeds){
	  String trueSpreadDir = root+metric+"/"+dir_trueSpread+seeds+".txt";
	  String trueSpreadText = "# Config file for computing true spread under CD model."+"\n";
	  trueSpreadText += "phase : 14"+"\n";
	  trueSpreadText += "graphFile : "+root+dir_training+dir_training_scan2+"edgesCounts.txt"+"\n";
	  trueSpreadText += "actionsFile : "+root+dir_action+"\n";
	  trueSpreadText += "userInflFile : "+root+dir_training+dir_training_scan2+"usersCounts.txt"+"\n";
	  trueSpreadText += "seedFileName : "+root+metric+"/"+dir_seeds+seeds+".txt"+"\n";
	  trueSpreadText += "outdir : "+root+metric+"/"+"\n";
	  trueSpreadText += "maxTuples : 0"+ "\n";
	  trueSpreadText += "trainingActionsFile : "+root+dir_trainaction+"\n";
	  trueSpreadText += "propModel : PC"+"\n";
	  trueSpreadText += "testingActionsFile : "+root+dir_testaction+"\n";
	  
	  IO io = new IO();
	  io.writeToFileNew(trueSpreadDir, trueSpreadText, false);
  }
  
  public Boolean dumpRankingFiles(Date findate){
	  String rankingDir = dir_root+sdfolder.format(findate)+"/";
	  	  
	  if (dumpSeeds(rankingDir, "retweetimpact",findate)
	   && dumpSeeds(rankingDir, "closeness",findate)
	   && dumpSeeds(rankingDir, "marginalinfluence",findate)
	   && dumpSeeds(rankingDir, "socialnetworkpotential",findate))
		  return true;
	  
	  return false;
  }
  
  private Boolean dumpSeeds(String dir, String metric, Date findate){
	  ResultSet seeds = getRanking(findate, metric);
	  try {
		  if (seeds!=null){
			  String metricDir = dir+metric+"/";
			  File metricFolder = new File(metricDir);
			  metricFolder.mkdir();
			  
			  String seedsx10 = metricDir+"seedsx10.txt";
			  String seedsx20 = metricDir+"seedsx20.txt";
			  String seedsx30 = metricDir+"seedsx30.txt";
			  String seedsx40 = metricDir+"seedsx40.txt";
			  String seedsx50 = metricDir+"seedsx50.txt";
			  
			  String textx10 = "";
			  String textx20 = "";
			  String textx30 = "";
			  String textx40 = "";
			  String textx50 = "";
			  
			  int i = 1;
			  while (seeds.next()){
				 Integer iduser_norm = seeds.getInt("u.iduser_norm"); 
				 if (i<=10)
					 textx10 += iduser_norm+"\n";
				 if (i<=20)
					 textx20 += iduser_norm+"\n";
				 if (i<=30)
					 textx30 += iduser_norm+"\n";
				 if (i<=40)
					 textx40 += iduser_norm+"\n";
				 if (i<=50)
					 textx50 += iduser_norm+"\n";
				 
				 i++;
			  }
			  
			  IO io = new IO();
			  /*
		      io.writeToFileNew(seedsx10, textx10, false);
		      io.writeToFileNew(seedsx20, textx20, false);
		      io.writeToFileNew(seedsx30, textx30, false);
		      io.writeToFileNew(seedsx40, textx40, false);
		      */
		      io.writeToFileNew(seedsx50, textx50, false);
		      
		      return true;
		  }
	    } catch (SQLException e) {
			e.printStackTrace();
		}
	 return false;  
  }
  
  private ResultSet getRanking(Date findate, String metric){
	  String query = "SELECT s."+metric+"_idseedset, s.position, u.iduser_norm FROM "+metric+"seed s LEFT JOIN "+metric+" m ON m.idseedset = s."+metric+"_idseedset "+"LEFT JOIN user u ON s.user_iduser = u.iduser "+"WHERE m.timeend = '"+sdl.format(findate)+"' ORDER BY s.position ASC;";
	  return DBManager.getInstance().executeQueryResult(query);
  }
}

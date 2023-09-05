package actionlog;

import infoprocess.User;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;

import database.DBManager;
import socialgraph.SocialGraph;
import twitter4j.Status;

public class ActionLog
{
  private static ActionLog instance = null;
  private Vector<LogEntry> entries = null;
  private Integer lastActionID;
  private Hashtable<Long, Integer> actionsID = null;
  
  private ActionLog() {
	entries = new Vector<LogEntry>();
    actionsID = new Hashtable<Long, Integer>();
    lastActionID = Integer.valueOf(1);
  }
  
  public static ActionLog getInstance() {
    if (instance == null)
      instance = new ActionLog();
    return instance;
  }
  
  public void removeAll() {
	entries.removeAllElements();
    actionsID.clear();
    lastActionID = Integer.valueOf(1);
  }
  
  public void addTweet(long id_user, long id_tweet, String text, long time, String place, String lang) {
	  if (!actionsID.containsKey(Long.valueOf(id_tweet))) {
	      actionsID.put(Long.valueOf(id_tweet), lastActionID);
	      Action action = new Action(lastActionID.intValue(), text, "TWEET");
	      User user = SocialGraph.getInstance().getUserOrigId(id_user);
	      user.addAction(action, Long.valueOf(time));
	      LogEntry e = new LogEntry(user, user.getId_normal(), Long.valueOf(time), action);
	      entries.add(e);
	      lastActionID = Integer.valueOf(lastActionID.intValue() + 1);
	      //INSERT ACTION
	      Date date = new Date(time);
	      DBManager.getInstance().insertTweet(id_tweet, text, place, lang, date);
	      DBManager.getInstance().insertAction(date, "TWEET", id_tweet, id_user);
	      //FINISH INSERT
    }
  }
  
  public void addTweetDB(long id_user, long id_tweet, String text, long time, String place, String lang) {
	  Date date = new Date(time);
      DBManager.getInstance().insertTweet(id_tweet, text, place, lang, date);
      DBManager.getInstance().insertAction(date, "TWEET", id_tweet, id_user);
  }
  
  private int getPos(long id, long time) {
	int pos = -1;
    if (actionsID.containsKey(Long.valueOf(id))) {
      pos = getActionPos(((Integer)actionsID.get(Long.valueOf(id))).intValue(), time).intValue();
    } else {
      actionsID.put(Long.valueOf(id), lastActionID);
      lastActionID = Integer.valueOf(lastActionID.intValue() + 1);
    }
    return pos;
  }
  
  public void addInteraction(String interactionType, long origUser_id, long origTweet_id, String tweetText, long origTime, long infUser_id, long infTime, User inter_user, Long time_inter_interaction, String place, String lang) {
	int pos = getPos(origTweet_id, infTime);
    Action action_tweet = new Action(((Integer)actionsID.get(Long.valueOf(origTweet_id))).intValue(), tweetText, "TWEET");
    Action interaction = new Action(((Integer)actionsID.get(Long.valueOf(origTweet_id))).intValue(), tweetText, interactionType);
       
    User inf_user = SocialGraph.getInstance().getUserOrigId(infUser_id);
    
    if (!inf_user.hasMadeAction(interaction)) {
      inf_user.addAction(interaction, Long.valueOf(infTime));
      LogEntry e_interaction = null;
      if (inter_user != null) {
        e_interaction = new LogEntry(inf_user, inf_user.getId_normal(), Long.valueOf(infTime), interaction, inter_user, time_inter_interaction);
      } else
        e_interaction = new LogEntry(inf_user, inf_user.getId_normal(), Long.valueOf(infTime), interaction);
      User orig_user = SocialGraph.getInstance().getUserOrigId(origUser_id);
      if (pos == -1) {
        orig_user.addAction(action_tweet, Long.valueOf(origTime));
        LogEntry e_original = new LogEntry(orig_user, orig_user.getId_normal(), Long.valueOf(origTime), action_tweet);
        entries.add(e_original);
        entries.add(e_interaction);
      } else {
        entries.add(pos, e_interaction);
      }
      orig_user.addInteraction(interactionType);
      if (inter_user != null)
        inter_user.addInteraction(interactionType);
    }
    
    //INSERTS EN BD
    //original
    Date origDate = new Date(origTime);
    DBManager.getInstance().insertTweet(origTweet_id, tweetText, place, lang, origDate);
    DBManager.getInstance().insertAction(origDate, "TWEET", origTweet_id, origUser_id); //0L por ser original
    //fin original
    // inf user
    Date infDate = new Date(infTime);
    DBManager.getInstance().insertAction(infDate, interactionType, origTweet_id, infUser_id); //infTime - origTime
    // fin inf user
    // inter user
    if (inter_user!=null){
	    Date interDate = new Date(time_inter_interaction);
	    DBManager.getInstance().insertAction(interDate, interactionType, origTweet_id, inter_user.getId()); //time_inter_interaction - origTime
    }
    // fin inter user
    //FIN INSERTS
  }
  
  public void addInteractionDB(String interactionType, long origUser_id, long origTweet_id, String tweetText, long origTime, long infUser_id, long infTime, User inter_user, Long time_inter_interaction, String place, String lang){
	    //original
	    Date origDate = new Date(origTime);
	    DBManager.getInstance().insertTweet(origTweet_id, tweetText, place, lang, origDate);
	    DBManager.getInstance().insertAction(origDate, "TWEET", origTweet_id, origUser_id); //0L por ser original
	    //fin original
	    // inf user
	    Date infDate = new Date(infTime);
	    DBManager.getInstance().insertAction(infDate, interactionType, origTweet_id, infUser_id); //infTime - origTime
	    // fin inf user
	    // inter user
	    if (inter_user!=null){
		    Date interDate = new Date(time_inter_interaction);
		    DBManager.getInstance().insertAction(interDate, interactionType, origTweet_id, inter_user.getId()); //time_inter_interaction - origTime
	    }
	    // fin inter user
  }
  
  private Integer getActionPos(int id_tweet, long time_retweet) {
    int pos = -1;
    int aux = 0;
    for (LogEntry e : entries) {
      if (e.getAction().getId().intValue() == id_tweet) {
        if (e.getTime().longValue() > time_retweet) {
          return Integer.valueOf(aux);
        }
        pos = aux;
      }
      else if ((e.getAction().getId().intValue() != id_tweet) && (pos != -1)) {
        return Integer.valueOf(aux); }
      aux++;
    }
    return Integer.valueOf(aux);
  }
  
  public Vector<LogEntry> getLogEntries() {
	  return entries;
  }
  
  public Integer getNroEntries(){
    return Integer.valueOf(entries.size());
  }
}

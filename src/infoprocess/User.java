package infoprocess;

import actionlog.Action;
import java.util.Date;
import java.util.Vector;
import seed.SeedInfo;
import utils.Pair;

public class User
{
  private Long id;
  private Integer id_normal;
  private String screenName;
  private Vector<Integer> actions_id;
  private Integer nro_retweeted;
  private Integer nro_replied;
  private Integer nro_retweets;
  private Integer nro_tweets;
  private Integer nro_replies;
  private Vector<Pair<Action, Long>> actionsPerformed;
  private Vector<SeedInfo> seed_ocurrs;
  
  public User(Long id, Integer id_normal, String screenName)
  {
    this.id = id;
    this.id_normal = id_normal;
    this.screenName = screenName;
    actions_id = new Vector<Integer>();
    actionsPerformed = new Vector<Pair<Action, Long>>();
    seed_ocurrs = null;
    nro_tweets = Integer.valueOf(0);
    nro_retweets = Integer.valueOf(0);
    nro_replies = Integer.valueOf(0);
    nro_retweeted = Integer.valueOf(0);
    nro_replied = Integer.valueOf(0);
  }
  
  public Long getId() { return id; }
  
  public void addAction(Action action, Long time) {
    actions_id.add(action.getId());
    actionsPerformed.add(new Pair<Action,Long>(action, time));
    if (action.getType().equals("Tweet")) {
      nro_tweets = nro_tweets++;
    } else if (action.getType().equals("Retweet")) {
      nro_retweets = nro_retweets++;
    } else if (action.getType().equals("Reply_To"))
      nro_replies = nro_replies++;
  }
  
  public String getScreenName() { return screenName; }
  
  public Integer getId_normal() {
    return id_normal;
  }
  
  public void setId_normal(Integer id_normal) { this.id_normal = id_normal; }
  
  public boolean hasMadeAction(Action action) {
    if (actions_id.contains(action.getId()))
      return true;
    return false;
  }
  
  public void addInfoSeedSet(String margInf, Date time, int pos_in_seed) {
	if (seed_ocurrs == null)
		seed_ocurrs = new Vector<SeedInfo>();
    SeedInfo si = new SeedInfo(margInf, Integer.valueOf(pos_in_seed), time);
    seed_ocurrs.add(si);
  }
  
  public Vector<SeedInfo> getSeedInfo() { return seed_ocurrs; }
  
  public boolean inSeedSetAnyTime() {
    if (seed_ocurrs != null)
      return true;
    return false;
  }
  
  public Vector<Pair<Action, Long>> getActionsPerformed() { return actionsPerformed; }
  
  public Integer getNumberOfTweets() {
    return nro_tweets;
  }
  
  public Integer getNumberOfRetweets() { return nro_retweets; }
  
  public Integer getNumberOfReplies() {
    return nro_replies;
  }
  
  public Integer getReplied() { return nro_replied; }
  

  public Integer getRetweeted() { return nro_retweeted; }
  
  public void addInteraction(String interactionType) {
    if (interactionType == "Retweet") {
      nro_retweeted = nro_retweeted++;
    } else if (interactionType == "Reply_To")
      nro_replied = nro_replied++;
  }
  
  public int getNumberOfTotalActionsPerformed() { return nro_replies.intValue() + nro_tweets.intValue() + nro_retweets.intValue(); }
  
  public int getNumberOfActionsNotInit() {
    return nro_replies.intValue() + nro_retweets.intValue();
  }
}

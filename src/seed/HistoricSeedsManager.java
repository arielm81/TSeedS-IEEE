package seed;

import infoprocess.User;
import java.util.Hashtable;
import java.util.Vector;

public class HistoricSeedsManager
{
  private Hashtable<String, User> historicSeeds;
  private static HistoricSeedsManager instance = null;
  
  private HistoricSeedsManager() {
    historicSeeds = new Hashtable<String, User>();
  }
  
  public static HistoricSeedsManager getInstance() {
    if (instance == null)
      instance = new HistoricSeedsManager();
    return instance;
  }
  
  public Vector<String> getHistoricSeeds() {
    Vector<String> toReturn = new Vector<String>(historicSeeds.keySet());
    return toReturn;
  }
  
  public void addSeed(User seed) {
    String screenName = seed.getScreenName();
    if (!historicSeeds.containsKey(screenName))
      historicSeeds.put(screenName, seed);
  }
  
  public User getSeed(String name) { return (User)historicSeeds.get(name); }
}

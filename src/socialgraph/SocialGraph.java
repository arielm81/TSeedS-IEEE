package socialgraph;

import infoprocess.User;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import utils.Pair;

public class SocialGraph
{
  private static SocialGraph instance = null;
  private Hashtable<Long, Integer> users_normal;
  private Hashtable<Integer, User> users;
  private Vector<Pair<Integer, Integer>> edges;
  private Integer lastUserId;
  private Vector<String> stringEdges;
  
  private SocialGraph() {
    users = new Hashtable<Integer, User>();
    users_normal = new Hashtable<Long, Integer>();
    edges = new Vector<Pair<Integer,Integer>>();
    stringEdges = new Vector<String>();
    lastUserId = 1;
  }
  
  public Vector<User> getUsers() {
	Vector<User> users_return = new Vector<User>();
    Enumeration<User> en = users.elements();
    while (en.hasMoreElements()) {
      users_return.add(0, (User)en.nextElement());
    }
    return users_return;
  }
  
  public int getNroUsers() { return users_normal.size(); }
  
  public static SocialGraph getInstance() {
    if (instance == null)
      instance = new SocialGraph();
    return instance;
  }
  
  public void addEdge(long id, long id2, boolean isInter) {
    int id_normalizado = ((Integer)users_normal.get(Long.valueOf(id))).intValue();
    int id2_normalizado = ((Integer)users_normal.get(Long.valueOf(id2))).intValue();
    
    if ((!stringEdges.contains(id_normalizado + "-" + id2_normalizado)) && (!stringEdges.contains(id2_normalizado + "-" + id_normalizado))) {
      stringEdges.add(id_normalizado + "-" + id2_normalizado);
      Pair<Integer, Integer> arista = new Pair<Integer,Integer>(id_normalizado, id2_normalizado);
      edges.add(arista);
    }
  }
  




  public void addUser(Long id, String screenName)
  {
    if (!users_normal.containsKey(id)) {
      users_normal.put(id, lastUserId);
      
      User u = new User(id, lastUserId, screenName);
      users.put(lastUserId, u);
      lastUserId = lastUserId++;
    }
  }
  
  public Integer getUserNormalID(long id) {
	  return users_normal.get(id);
  }
  
  public User getUserOrigId(long id) {
    if (users_normal.containsKey(id))
      return (User)users.get(users_normal.get(id));
    return null;
  }
  
  public User getUser(int id) {
	  if (users.containsKey(id))
		  return users.get(id);
    return null;
  }
  
  public void printUsers() {
	Enumeration<Integer> e = users.keys();
    while (e.hasMoreElements()) {
      User u = users.get(e.nextElement());
      System.out.println(u.getScreenName());
    }
  }
  
  public Vector<Pair<Integer, Integer>> getEdges() { return edges; }
  

  public Enumeration<Integer> getUsersIds() { return users.keys(); }
  
  public User getUserByName(String screenName) {
    Enumeration<User> en = users.elements();
    while (en.hasMoreElements()) {
      User u = en.nextElement();
      if (u.getScreenName().equals(screenName))
        return u;
    }
    return null;
  }
  
  public void removeAll() {
	users_normal.clear();
    users.clear();
    edges.removeAllElements();
    lastUserId = 1;
    stringEdges.removeAllElements();
  }
}

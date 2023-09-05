package socialgraph;

import java.util.Vector;
import java.awt.Color;
import java.awt.Paint;
import org.apache.commons.collections15.Transformer;

public class VertexColorTransformer implements Transformer<String,Paint> {
  Vector<String> users_tweets;
  Vector<String> users_retweets;
  Vector<String> inter_users;
  Vector<String> maxInfUsers;
  
  public VertexColorTransformer(Vector<String> users_tweets, Vector<String> user_retweets, Vector<String> maxInfUsers, Vector<String> inter_users)
  {
    this.users_tweets = users_tweets;
    users_retweets = user_retweets;
    this.maxInfUsers = maxInfUsers;
    this.inter_users = inter_users;
  }
  
  public Paint transform(String vertexName) {
    if (maxInfUsers.contains(vertexName))
      return Color.BLUE;
    if (inter_users.contains(vertexName))
      return Color.GREEN;
    if (users_tweets.contains(vertexName))
      return Color.CYAN;
    return Color.RED;
  }
}

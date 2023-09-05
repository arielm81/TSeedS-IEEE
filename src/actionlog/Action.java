package actionlog;

public class Action
{
  public static final String TWEET = "TWEET";
  public static final String RETWEET = "RETWEET";
  public static final String REPLY = "REPLY";
  private Integer id;
  private String tweet;
  private String type;
  
  public Action(int id_tweet, String text, String type) {
    id = Integer.valueOf(id_tweet);
    tweet = text;
    this.type = type;
  }
  
  public String getTweet() {
    return tweet;
  }
  
  public void setTweet(String tweet) {
    this.tweet = tweet;
  }
  
  public Integer getId() {
    return id;
  }
  
  public void setId(int id_tweet) {
    id = Integer.valueOf(id_tweet);
  }
  
  public String getType() {
    return type;
  }
}

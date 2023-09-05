package infoprocess;

import actionlog.ActionLog;
import java.util.List;
import socialgraph.SocialGraph;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;
import twitterinfo.TwitterMonitor;

public class InfoProcessor extends Thread
{
  private TwitterMonitor monitor;
  private Twitter twitter;
  private SocialGraph sg;
  private ActionLog actionLog;
  
  public InfoProcessor(TwitterMonitor m, Twitter t)
  {
    monitor = m;
    twitter = t;
    sg = SocialGraph.getInstance();
    actionLog = ActionLog.getInstance();
  }
  
  public void run()
  {
    for (;;) {
      if (monitor.hasInteraction()) {
        processInteraction(monitor.getInteraction());
      } else {
        try {
          Thread.sleep(5000L);
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
      }
    }
  }
  
  private void processInteraction(Status status) {
    User user = status.getUser();
    if (status.isRetweet()) {
      sg.addUser(Long.valueOf(user.getId()), user.getScreenName());
      Status rtwStatus = status.getRetweetedStatus();
      User rtwUser = rtwStatus.getUser();
      lookForRetweetsChain(user, rtwUser, rtwStatus.getCreatedAt().getTime(), status.getCreatedAt().getTime(), rtwStatus.getId(), rtwStatus.getText(), null);
    } else if (status.getInReplyToStatusId() > 0L) {
      try {
        Status reply = twitter.showStatus(status.getInReplyToStatusId());
        if (reply == null) return;
        sg.addUser(Long.valueOf(user.getId()), user.getScreenName());
        addNormal("Reply_To", reply.getUser(), user, Long.valueOf(reply.getId()), reply.getText(), Long.valueOf(reply.getCreatedAt().getTime()), Long.valueOf(status.getCreatedAt().getTime()));

      }
      catch (TwitterException e) {}

    }
    else
    {
      sg.addUser(Long.valueOf(user.getId()), user.getScreenName());
      actionLog.addTweet(user.getId(), status.getId(), status.getText(), status.getCreatedAt().getTime());
    }
  }
  
  private void lookForRetweetsChain(User user_who_retweet, User user_who_tweet, long time_tweet, long time_retweet, long tweet_id, String tweet_text, List<Status> retweets) { Twitter twitter = null;
    try {      
      if (twitter == null) {
        System.out.println("Agregado normal 1");
        addNormal("Retweet", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet));
        return;
      }
      Relationship r = twitter.showFriendship(user_who_retweet.getId(), user_who_tweet.getId());
      if (r.isSourceFollowingTarget())
      {
        addNormal("Retweet", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet));
        
        return;
      }
      
      if (retweets == null) {
        if (twitter == null) {
          System.out.println("Agregado normal 2");
          addNormal("Retweet", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet));
          return;
        }
        retweets = twitter.getRetweets(tweet_id);
      }
      
      sg.addUser(Long.valueOf(user_who_tweet.getId()), user_who_tweet.getScreenName());
      if ((!retweets.isEmpty()) && 
        (!isLastMajor(retweets, Long.valueOf(time_retweet)))) {
        int pos = 0;
        for (Status retweet : retweets) {
          User inter_user = retweet.getUser();
          if (inter_user.getId() != user_who_retweet.getId()) {
            long time_inter_retweet = retweet.getCreatedAt().getTime();
            if (time_inter_retweet < time_retweet) {
              if (twitter == null) {
                System.out.println("Agregado normal 3");
                addNormal("Retweet", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet));
                return;
              }
              twitter4j.Relationship relation = null;
              try {
                relation = twitter.showFriendship(user_who_retweet.getId(), inter_user.getId());
              } catch (TwitterException te) {
                System.out.println("Agregado normal 4 - tras excepcion");
                addNormal("Retweet", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet));
                return;
              }
              if (relation.isSourceFollowingTarget())
              {
                sg.addUser(Long.valueOf(inter_user.getId()), inter_user.getScreenName());
                sg.addEdge(user_who_retweet.getId(), inter_user.getId(), true);
                
                actionLog.addInteraction("Retweet", user_who_tweet.getId(), tweet_id, tweet_text, time_tweet, user_who_retweet.getId(), time_retweet, sg.getUser(sg.getUserNormalID(inter_user.getId()).intValue()), Long.valueOf(time_inter_retweet));
                

                List<Status> retwts = retweets.subList(pos, retweets.size() - 1);
                lookForRetweetsChain(inter_user, user_who_tweet, time_tweet, time_inter_retweet, tweet_id, tweet_text, retwts);
                return;
              }
            }
          }
          pos++;
        }
      }
      
      addNormal("Retweet", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet));
    }
    catch (TwitterException e) {
      System.out.println("Agregado normal 5 - tras excepcion");
      addNormal("Retweet", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet));
    }
  }
  
  private void addNormal(String interactionType, User user_who_tweet, User user_who_interact, Long tweet_id, String tweet_text, Long time_tweet, Long time_interact) {
    sg.addUser(Long.valueOf(user_who_tweet.getId()), user_who_tweet.getScreenName());
    sg.addEdge(user_who_interact.getId(), user_who_tweet.getId(), false);
    actionLog.addInteraction(interactionType, user_who_tweet.getId(), tweet_id.longValue(), tweet_text, time_tweet.longValue(), user_who_interact.getId(), time_interact.longValue(), null, null);
  }
  
  private boolean isLastMajor(List<Status> retweets, Long time) {
    if (((Status)retweets.get(retweets.size() - 1)).getCreatedAt().getTime() > time.longValue())
      return true;
    return false;
  }
}

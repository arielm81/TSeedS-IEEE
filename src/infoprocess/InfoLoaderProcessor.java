package infoprocess;

import actionlog.ActionLog;
import database.DBManager;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import socialgraph.SocialGraph;
import twitter4j.JSONArray;
import twitter4j.JSONObject;
import twitter4j.RateLimitStatus;
import twitter4j.Relationship;
import twitter4j.Status;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.User;

public class InfoLoaderProcessor extends Thread
{
  private Twitter twitter;
  private SocialGraph sg;
  private ActionLog actionLog;
  
  private Long peticiones;
  private Long esperas;
  
  public InfoLoaderProcessor(Twitter twitter)
  {
    this.twitter = twitter;
    this.sg = SocialGraph.getInstance();
    this.actionLog = ActionLog.getInstance();
    this.setPeticiones(0L);
    this.setEsperas(0L);
  }
  
  public void loadFollowers(File f){
	  try{
		  FileReader fr = new FileReader(f);
		  @SuppressWarnings("resource")
		  BufferedReader br = new BufferedReader(fr);
	      String line = br.readLine();
	      JSONArray jsonArray = new JSONArray(line);
	      if (jsonArray != null){
	    	  for (int i = 0; i < jsonArray.length(); i++) {
	              try {
	                if (!jsonArray.get(i).toString().equals("null")) {
	                  JSONObject jsonTweet = jsonArray.getJSONObject(i);
	                  
	                  if (!jsonTweet.toString().equals("null")) {
	                    Status status = twitter4j.TwitterObjectFactory.createStatus(jsonTweet.toString());
	                    processFollowers(status);
	                  }
	                }
	              } catch (Exception e) {
	                System.out.println("Error en Tweet pero continuo ejecutando los demas. Error: "+e.getMessage());
	                e.printStackTrace();
	              }
	            }
	      }
	  }catch(Exception e){
		  e.printStackTrace();
	  }
  }
  
  public void load(File f)
  {
    //System.out.println("INGRESO A LOAD DE INFO LOADER PROCESSOR con File: "+f.getName());
    try {
      FileReader fr = new FileReader(f);
      @SuppressWarnings("resource")
	  BufferedReader br = new BufferedReader(fr);
      String line = br.readLine();
      try {
        JSONArray jsonArray = new JSONArray(line);
        //System.out.print(" Bien escrito.");
        if (jsonArray != null) {
          for (int i = 0; i < jsonArray.length(); i++) {
            try {
              if (!jsonArray.get(i).toString().equals("null")) {
                JSONObject jsonTweet = jsonArray.getJSONObject(i);
                
                if (!jsonTweet.toString().equals("null")) {
                  Status status = twitter4j.TwitterObjectFactory.createStatus(jsonTweet.toString());
                  processInteractionDB(status);
                }
              }
            } catch (Exception e) {
              System.out.println("Error en Tweet pero continuo ejecutando los demas. Error: "+e.getMessage());
              e.printStackTrace();
            }
          }
        }
      } catch (Exception e) {
        System.out.println("Falla parseando el array. ERROR: " + e.getMessage());
      }
      return;
    } catch (Exception e) { System.out.println("EXCEPTION: " + e.getMessage());
    }
  }
  
  private void insertUser(User user, Boolean inter){
	  String lang = user.getLang();
	  String location = user.getLocation();
	  DBManager.getInstance().insertUser(user.getId(), user.getScreenName(), user.getName(), null, null, null, null, null, null, null, null, lang, location, inter);
  }
  
  public void updateFollowers(){
	  String endpoint = "/users/show/:id";
	  String query = "SELECT iduser, screen_name FROM user WHERE followers = 0 ;";
	  ResultSet actionsResult = DBManager.getInstance().executeQueryResult(query);
	  ArrayList<Long> otherDBIDS = DBManager.getInstance().getOtherDBIDS();
	  if (actionsResult!=null){
		  try {
			  Integer queriesLeft = cantRequests(endpoint) - 3;
			  //boolean cumple = false;
			  while(actionsResult.next()){
				  Long iduser = actionsResult.getLong("iduser");
				  String sname = actionsResult.getString("screen_name");
				  /*
				   if (sname.equals("AnthonyL_49"))
					  cumple = true;
				  */
				  if (!otherDBIDS.contains(iduser)){
					  try {
						//if (cumple){
							if (queriesLeft<2)
								queriesLeft = cantRequests(endpoint);
							User user = twitter.showUser(iduser);
							System.out.println("User = "+sname+" Followers = "+ user.getFollowersCount());
							DBManager.getInstance().updateUserFollowers(iduser, user.getFollowersCount());
							queriesLeft--;
						//}
					  } catch (TwitterException e) {
						System.out.println("error con iduser = "+iduser+" screen name = "+sname);
						e.printStackTrace();
						queriesLeft--;
					  }
			  	  }
			  }
		  } catch (SQLException e) {
			  e.printStackTrace();
		  }
	  }else System.out.println("actions result is null");
	  
  }
  
  private Integer cantRequests(String endpoint){
	  try{
		  Map<String ,RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus();
		  RateLimitStatus limit_status = rateLimitStatus.get("/application/rate_limit_status");
	      RateLimitStatus status = rateLimitStatus.get(endpoint);
	      if (limit_status.getRemaining()<1 || status.getRemaining()<1){
        	 try {
        		int max_time = limit_status.getSecondsUntilReset();
        		if (limit_status.getRemaining()<1 && status.getRemaining()<1){
        			if (status.getSecondsUntilReset() > max_time)
        				max_time = status.getSecondsUntilReset();
        		}else if (status.getRemaining()<1){
        			max_time = status.getSecondsUntilReset();
        		}
        		Thread.sleep(max_time*1000L); //va en milisegundos
				System.out.println("Finish Waiting "+new Date());
				Map<String ,RateLimitStatus> newRateLimitStatus = twitter.getRateLimitStatus();
				RateLimitStatus newStatus = newRateLimitStatus.get(endpoint);
				System.out.println("Ahora tengo "+newStatus.getRemaining());
				return newStatus.getRemaining();
        	 } catch (InterruptedException e) {
 				System.out.println("Interrupted Exception: "+e.getMessage());
 			}
        }else{
        	return status.getRemaining();
        }
	 }catch(TwitterException te){
		 te.printStackTrace();
         System.out.println("Failed to get rate limit status: " + te.getMessage());
	 }
	 return 0;
  }
  
  private Boolean isAvailableTwitter(String endpoint){
	 try{
		 Map<String ,RateLimitStatus> rateLimitStatus = twitter.getRateLimitStatus();
		 peticiones++;
		 System.out.println("peticion de "+endpoint);
		 RateLimitStatus limit_status = rateLimitStatus.get("/application/rate_limit_status");
		 RateLimitStatus status = rateLimitStatus.get(endpoint);
		 
		 System.out.println(" LIMIT STATUS");
		 System.out.println(" Limit: " + limit_status.getLimit());
         System.out.println(" Remaining: " + limit_status.getRemaining());
         System.out.println(" ResetTimeInSeconds: " + limit_status.getResetTimeInSeconds());
         System.out.println(" SecondsUntilReset: " + limit_status.getSecondsUntilReset());
		 
		 System.out.println(" STATUS "+endpoint);
		 System.out.println(" Limit: " + status.getLimit());
         System.out.println(" Remaining: " + status.getRemaining());
         System.out.println(" ResetTimeInSeconds: " + status.getResetTimeInSeconds());
         System.out.println(" SecondsUntilReset: " + status.getSecondsUntilReset());
         
		 if (limit_status.getRemaining()<1 || status.getRemaining()<1){
        	 try {
        		int max_time = limit_status.getSecondsUntilReset();
        		if (limit_status.getRemaining()<1 && status.getRemaining()<1){
        			if (status.getSecondsUntilReset() > max_time)
        				max_time = status.getSecondsUntilReset();
        		}else if (status.getRemaining()<1){
        			max_time = status.getSecondsUntilReset();
        		}
        		
        		System.out.println("Start Waiting "+new Date());
        		esperas++;
				Thread.sleep(max_time*1000L); //va en milisegundos
				System.out.println("Finish Waiting "+new Date());
				return true;
				
			} catch (InterruptedException e) {
				System.out.println("Interrupted Exception: "+e.getMessage());
			}
         }else{
        	 return true;
         }
        	 /*
         for (String endpoint : rateLimitStatus.keySet()) {
             RateLimitStatus status = rateLimitStatus.get(endpoint);
             System.out.println("Endpoint: " + endpoint);
             System.out.println(" Limit: " + status.getLimit());
             System.out.println(" Remaining: " + status.getRemaining());
            
             System.out.println(" ResetTimeInSeconds: " + status.getResetTimeInSeconds());
             System.out.println(" SecondsUntilReset: " + status.getSecondsUntilReset());
         }
         */
  	 }catch (TwitterException te) {
	         te.printStackTrace();
	         System.out.println("Failed to get rate limit status: " + te.getMessage());
  	 }
	 System.out.println("IsAvailable false");
	 return false;
  }
  
  private void processFollowers(Status status){
	  User user = status.getUser();
	  if (DBManager.getInstance().getFollowers(user.getId()) == 0){
		  DBManager.getInstance().updateUserFollowers(user.getId(), user.getFollowersCount());
		  //System.out.println("User: "+user.getId()+ " Followers: "+user.getFollowersCount());
		  if (status.isRetweet()){
			  Status rtwStatus = status.getRetweetedStatus();
		      User rtwUser = rtwStatus.getUser();
		      DBManager.getInstance().updateUserFollowers(rtwUser.getId(), rtwUser.getFollowersCount());
			  //System.out.println("User: "+rtwUser.getId()+ " Followers: "+rtwUser.getFollowersCount());
		  }
	  }
  }
  
  private void processInteractionDB(Status status){
	User user = status.getUser();
	if (status.isRetweet()){
		insertUser(user,false);
		 Status rtwStatus = status.getRetweetedStatus();
	      User rtwUser = rtwStatus.getUser();
	      String place = null;
	      if (rtwStatus.getPlace()!=null)
	    	  place = rtwStatus.getPlace().getName();
	      lookForRetweetsChainDB(user, rtwUser, rtwStatus.getCreatedAt().getTime(), status.getCreatedAt().getTime(), rtwStatus.getId(), rtwStatus.getText(), null, place,rtwStatus.getLang());
	} else if (status.getInReplyToStatusId() > 0L) {
		try {
	    	isAvailableTwitter("/statuses/show/:id");
	        Status reply = twitter.showStatus(status.getInReplyToStatusId());
	        if (reply == null) return;
	        insertUser(user, false);
	        String place = null;
	        if (reply.getPlace()!=null)
	      	  place = reply.getPlace().getName();
	        addNormalDB("REPLY", reply.getUser(), user, Long.valueOf(reply.getId()), reply.getText(), Long.valueOf(reply.getCreatedAt().getTime()), Long.valueOf(status.getCreatedAt().getTime()), place, reply.getLang());
		}catch(TwitterException e){
			 e.printStackTrace();
	    	 System.out.println("Twitter Exception en Reply: "+ e.getMessage()); 
		}
	}else{
		  insertUser(user,false);
	      String place = null;
	      if (status.getPlace()!=null)
	    	  place = status.getPlace().getName();
	      actionLog.addTweetDB(user.getId(), status.getId(), status.getText(), status.getCreatedAt().getTime(), place, status.getLang());
	}
  }

  private void lookForRetweetsChainDB(User user_who_retweet, User user_who_tweet, long time_tweet, long time_retweet, long tweet_id, String tweet_text, List<Status> retweets, String place, String lang) {
	  try {
	      isAvailableTwitter("/friendships/show");
	      Relationship r = twitter.showFriendship(user_who_retweet.getId(), user_who_tweet.getId());
	      if (r.isSourceFollowingTarget())	      {
	        addNormalDB("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet), place, lang);
	        return;
	      }
	      if (retweets == null) {
	        isAvailableTwitter("/statuses/retweets/:id");
	        retweets = twitter.getRetweets(tweet_id);
	      }
	      insertUser(user_who_tweet,false);
	      if ((!retweets.isEmpty()) && 
	        (!isLastMajor(retweets, Long.valueOf(time_retweet)))) {
	        int pos = 0;
	        for (Status retweet : retweets) {
	          User inter_user = retweet.getUser();
	          if (inter_user.getId() != user_who_retweet.getId()) {
	            long time_inter_retweet = retweet.getCreatedAt().getTime();
	            if (time_inter_retweet < time_retweet) {
	              Relationship relation = null;
	              try {
	            	isAvailableTwitter("/friendships/show");
	                relation = twitter.showFriendship(user_who_retweet.getId(), inter_user.getId());
	              } catch (TwitterException te) {
	            	te.printStackTrace();
	            	System.out.println("Twitter Exception en Relation 2: "+te.getMessage());
	                addNormalDB("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet),place,lang);
	                return;
	              }
	              if (relation.isSourceFollowingTarget()){
	                insertUser(inter_user,true);
	                DBManager.getInstance().insertEdge(user_who_retweet.getId(), inter_user.getId(), retweet.getCreatedAt());
	                actionLog.addInteractionDB("RETWEET", user_who_tweet.getId(), tweet_id, tweet_text, time_tweet, user_who_retweet.getId(), time_retweet, sg.getUser(sg.getUserNormalID(inter_user.getId()).intValue()), Long.valueOf(time_inter_retweet), place, lang);
	                List<Status> retwts = retweets.subList(pos, retweets.size() - 1);
	                lookForRetweetsChainDB(inter_user, user_who_tweet, time_tweet, time_inter_retweet, tweet_id, tweet_text, retwts, place, lang);
	                return;
	              }
	            }
	          }
	          pos++;
	        }
	      }
	      addNormalDB("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet),place,lang);
	    }
	    catch (TwitterException e) {
	      e.printStackTrace();
	      System.out.println("Twitter Exception en Relation 1: "+e.getMessage());
	      addNormalDB("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet),place,lang);
	    }
  }

  private void addNormalDB(String interactionType, User user_who_tweet, User user_who_interact, Long tweet_id, String tweet_text, Long time_tweet, Long time_interact, String place, String lang) {
	  insertUser(user_who_tweet, false);
	  DBManager.getInstance().insertEdge(user_who_interact.getId(), user_who_tweet.getId(), new Date(time_interact));
	  actionLog.addInteractionDB(interactionType, user_who_tweet.getId(), tweet_id.longValue(), tweet_text, time_tweet.longValue(), user_who_interact.getId(), time_interact.longValue(), null, null, place, lang);	  
  }
  
  
  private void processInteraction(Status status){
    User user = status.getUser();
    if (status.isRetweet()) {
      sg.addUser(Long.valueOf(user.getId()), user.getScreenName());
      //INSERT USER
      insertUser(user,false);
      //FINISH INSERT
      Status rtwStatus = status.getRetweetedStatus();
      User rtwUser = rtwStatus.getUser();
      String place = null;
      if (rtwStatus.getPlace()!=null)
    	  place = rtwStatus.getPlace().getName();
      lookForRetweetsChain(user, rtwUser, rtwStatus.getCreatedAt().getTime(), status.getCreatedAt().getTime(), rtwStatus.getId(), rtwStatus.getText(), null, place,rtwStatus.getLang());
    } else if (status.getInReplyToStatusId() > 0L) {
      try {
    	isAvailableTwitter("/statuses/show/:id");
        Status reply = twitter.showStatus(status.getInReplyToStatusId());
        if (reply == null) return;
        sg.addUser(Long.valueOf(user.getId()), user.getScreenName());
        //INSERT USER
        insertUser(user, false);
        //FINISH INSERT
        String place = null;
        if (reply.getPlace()!=null)
      	  place = reply.getPlace().getName();
        addNormal("REPLY", reply.getUser(), user, Long.valueOf(reply.getId()), reply.getText(), Long.valueOf(reply.getCreatedAt().getTime()), Long.valueOf(status.getCreatedAt().getTime()), place, reply.getLang());

      }
      catch (TwitterException e) {
    	  e.printStackTrace();
    	  System.out.println("Twitter Exception en Reply: "+ e.getMessage()); 
      }
    }
    else
    {
      sg.addUser(Long.valueOf(user.getId()), user.getScreenName());
      //INSERT USER
      insertUser(user,false);
      //FINISH INSERT
      String place = null;
      if (status.getPlace()!=null)
    	  place = status.getPlace().getName();
      actionLog.addTweet(user.getId(), status.getId(), status.getText(), status.getCreatedAt().getTime(), place, status.getLang());
    }
  }
  
  private void lookForRetweetsChain(User user_who_retweet, User user_who_tweet, long time_tweet, long time_retweet, long tweet_id, String tweet_text, List<Status> retweets, String place, String lang) {
    try {
      if (twitter == null) {
        //System.out.println("Agregado normal 1");
        addNormal("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet), place, lang);
        return;
      }
      isAvailableTwitter("/friendships/show");
      Relationship r = twitter.showFriendship(user_who_retweet.getId(), user_who_tweet.getId());
      if (r.isSourceFollowingTarget())
      {
        addNormal("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet), place, lang);
        
        return;
      }
      
      if (retweets == null) {
        if (twitter == null) {
          //System.out.println("Agregado normal 2");
          addNormal("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet),place, lang);
          return;
        }
        isAvailableTwitter("/statuses/retweets/:id");
        retweets = twitter.getRetweets(tweet_id);
      }
      
      sg.addUser(Long.valueOf(user_who_tweet.getId()), user_who_tweet.getScreenName());
      //INSERT USER
      insertUser(user_who_tweet,false);
      //FINISH INSERT
      if ((!retweets.isEmpty()) && 
        (!isLastMajor(retweets, Long.valueOf(time_retweet)))) {
        int pos = 0;
        for (Status retweet : retweets) {
          User inter_user = retweet.getUser();
          if (inter_user.getId() != user_who_retweet.getId()) {
            long time_inter_retweet = retweet.getCreatedAt().getTime();
            if (time_inter_retweet < time_retweet) {
              if (twitter == null) {
                //System.out.println("Agregado normal 3");
                addNormal("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet),place, lang);
                return;
              }
              Relationship relation = null;
              try {
            	isAvailableTwitter("/friendships/show");
                relation = twitter.showFriendship(user_who_retweet.getId(), inter_user.getId());
              } catch (TwitterException te) {
                //System.out.println("Agregado normal 4 - tras excepcion");
            	te.printStackTrace();
            	System.out.println("Twitter Exception en 1: "+te.getMessage());
                addNormal("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet),place,lang);
                return;
              }
              if (relation.isSourceFollowingTarget())
              {
                sg.addUser(Long.valueOf(inter_user.getId()), inter_user.getScreenName());
                //INSERT USER
                insertUser(inter_user,true);
                //FINISH INSERT
                sg.addEdge(user_who_retweet.getId(), inter_user.getId(), true);
                //INSERT EDGE
                DBManager.getInstance().insertEdge(user_who_retweet.getId(), inter_user.getId(), retweet.getCreatedAt());
                //FINISH INSERT
                actionLog.addInteraction("RETWEET", user_who_tweet.getId(), tweet_id, tweet_text, time_tweet, user_who_retweet.getId(), time_retweet, sg.getUser(sg.getUserNormalID(inter_user.getId()).intValue()), Long.valueOf(time_inter_retweet), place, lang);
                

                List<Status> retwts = retweets.subList(pos, retweets.size() - 1);
                lookForRetweetsChain(inter_user, user_who_tweet, time_tweet, time_inter_retweet, tweet_id, tweet_text, retwts, place, lang);
                return;
              }
            }
          }
          pos++;
        }
      }
      
      addNormal("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet),place,lang);
    }
    catch (TwitterException e) {
      //System.out.println("Agregado normal 5 - tras excepcion");
    	e.printStackTrace();
      System.out.println("Twitter Exception en 2: "+e.getMessage());
      addNormal("RETWEET", user_who_tweet, user_who_retweet, Long.valueOf(tweet_id), tweet_text, Long.valueOf(time_tweet), Long.valueOf(time_retweet),place,lang);
    }
  }
  
  private void addNormal(String interactionType, User user_who_tweet, User user_who_interact, Long tweet_id, String tweet_text, Long time_tweet, Long time_interact, String place, String lang) {
    sg.addUser(Long.valueOf(user_who_tweet.getId()), user_who_tweet.getScreenName());
    //INSERT USER
    insertUser(user_who_tweet, false);
    //FINISH INSERT
    sg.addEdge(user_who_interact.getId(), user_who_tweet.getId(), false);
    //INSERT EDGE
    DBManager.getInstance().insertEdge(user_who_interact.getId(), user_who_tweet.getId(), new Date(time_interact));
    //FINISH INSERT
    actionLog.addInteraction(interactionType, user_who_tweet.getId(), tweet_id.longValue(), tweet_text, time_tweet.longValue(), user_who_interact.getId(), time_interact.longValue(), null, null, place, lang);
  }
  
  private boolean isLastMajor(List<Status> retweets, Long time) {
    if (((Status)retweets.get(retweets.size() - 1)).getCreatedAt().getTime() > time.longValue())
      return true;
    return false;
  }

public Long getPeticiones() {
	return peticiones;
}

public void setPeticiones(Long peticiones) {
	this.peticiones = peticiones;
}

public Long getEsperas() {
	return esperas;
}

public void setEsperas(Long esperas) {
	this.esperas = esperas;
}
}

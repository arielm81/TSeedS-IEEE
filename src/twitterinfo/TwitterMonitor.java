package twitterinfo;

import filters.FiltroAbs;
import java.util.Date;
import java.util.Vector;
import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import utils.Save;

public class TwitterMonitor
{
  private static final String filePath = "./ListenedStatus/";
  private TwitterStreamFactory factory;
  private TwitterStream twitterStream;
  private Vector<Status> interactions;
  private Twitter twitter;
  private FilterQuery fq;
  private FiltroAbs filter;
  private Long timeF;
  
  public TwitterMonitor(Twitter twitter, FiltroAbs f, FilterQuery fq, Long timeF)
  {
    factory = new TwitterStreamFactory(twitterinfo.ConfigBuilder.getStreamInstance().build());
    interactions = new Vector<Status>();
    
    this.twitter = twitter;
    this.fq = fq;
    this.filter = f;
    this.timeF = timeF;
  }
  
  public void listenAndSave(){
		twitterStream = factory.getInstance();
	    StatusListener listener = new StatusListener() {
	      public void onStatus(Status status) {
	        if (((filter!=null) && (filter.cumple(status))) || (filter == null))
	          writeToFile(status);
	      }
	      
	      public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
	    	System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
	      }
	      
	      public void onTrackLimitationNotice(int numberOfLimitedStatuses)
	      {
	        System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
	      }
	      
	      public void onScrubGeo(long userId, long upToStatusId) {
	        System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
	      }
	      
	      public void onException(Exception ex) {
	        ex.printStackTrace();
	      }
	      
	      public void onStallWarning(StallWarning arg0) {}
	    };
	    
	    twitterStream.addListener(listener);
	    twitterStream.filter(fq);
  }
  
  public boolean hasInteraction() {
	  if (!interactions.isEmpty())
      return true;
    return false;
  }
  
  public Status getInteraction() {
	Status to_return = (Status)interactions.get(0);
    interactions.remove(0);
    return to_return;
  }
  
  public void startListen() {
	twitterStream = factory.getInstance();
    StatusListener listener = new StatusListener() {
      public void onStatus(Status status) {
    	if ((filter!=null) && (filter.cumple(status)))
          TwitterMonitor.this.addInteraction(status);
      }
      
      public void onDeletionNotice(StatusDeletionNotice statusDeletionNotice) {
    	System.out.println("Got a status deletion notice id:" + statusDeletionNotice.getStatusId());
      }
      
      public void onTrackLimitationNotice(int numberOfLimitedStatuses)
      {
        System.out.println("Got track limitation notice:" + numberOfLimitedStatuses);
      }
      
      public void onScrubGeo(long userId, long upToStatusId) {
        System.out.println("Got scrub_geo event userId:" + userId + " upToStatusId:" + upToStatusId);
      }
      
      public void onException(Exception ex) {
        ex.printStackTrace();
      }
      
      public void onStallWarning(StallWarning arg0) {}
    };
    
    twitterStream.addListener(listener);
    twitterStream.filter(fq);
  }
  
  
  private void addInteraction(Status interaction)
  {
    boolean timeFilter = false;
    if (interaction.isRetweet()) {
      timeFilter = checkTime(Long.valueOf(interaction.getRetweetedStatus().getCreatedAt().getTime()));
    } else if (interaction.getInReplyToStatusId() > 0L) {
      if (interaction.getInReplyToUserId() != interaction.getUser().getId())
      {
        try {
          if (twitter != null){
        	  Status reply = twitter.showStatus(interaction.getInReplyToStatusId());
        	  if (reply != null){ 
        	  	timeFilter = checkTime(Long.valueOf(reply.getCreatedAt().getTime()));
        	  }
          }
        }
        catch (TwitterException localTwitterException) {}
      }
      
    }
    else
      timeFilter = checkTime(Long.valueOf(interaction.getCreatedAt().getTime()));
    
    if (timeFilter) {
      interactions.add(interaction);
    }
  }
    

  private void writeToFile(Status s) {
	  System.out.println("Interaccion");
	  Save.getInstance().saveStatus(filePath, s);
  }
  
  private boolean checkTime(Long time) {
    if (new Date().getTime() - time.longValue() < timeF.longValue())
      return true;
    return false;
  }
}

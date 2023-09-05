package twitterinfo;

import twitter4j.conf.ConfigurationBuilder;

public class ConfigBuilder {
  public ConfigBuilder() {}
  
  public static ConfigurationBuilder getStreamInstance() {
	
	ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true);
    
    cb.setOAuthConsumerKey("*******************************");
    cb.setOAuthConsumerSecret("*******************************");
    cb.setOAuthAccessToken("*******************************");
    cb.setOAuthAccessTokenSecret("*******************************");
    cb.setJSONStoreEnabled(true);
    return cb;
  }
  
  public static ConfigurationBuilder getConsultInstance() {
    ConfigurationBuilder cb = new ConfigurationBuilder();
    cb.setDebugEnabled(true);
    cb.setOAuthConsumerKey("*******************************");
    cb.setOAuthConsumerSecret("*******************************");
    cb.setOAuthAccessToken("*******************************");
    cb.setOAuthAccessTokenSecret("*******************************");
    cb.setJSONStoreEnabled(true); 
    return cb;
  }
}

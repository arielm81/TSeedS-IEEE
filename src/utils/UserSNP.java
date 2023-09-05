package utils;

import java.util.Hashtable;
import java.util.Vector;

public class UserSNP {
		Vector<Long> influenciados;
		Hashtable<Long, Integer> tweets;
		Long id;
		Integer followers;
		
		
		public UserSNP(Long id, Integer f){
			this.id = id;
			influenciados = new Vector<Long>();
			tweets = new Hashtable<Long, Integer>();
			followers = f;
		}
		
		public void addTweet(Long idtweet){
			Integer ret = 0;
			if (tweets.containsKey(idtweet))
				ret = tweets.get(idtweet) + 1;
			tweets.put(idtweet, ret);
		}
		
		public void addInfluenciado(Long influenciado){
			if (!influenciados.contains(influenciado))
				influenciados.add(influenciado);
		}
		
		public Double getInteractorRatio(){
			if (followers != 0)
				return Double.valueOf(influenciados.size() / followers);
			else{
				System.out.println("Retorne 0 para el usuario "+id);
				return 0d;
			}
		}
		
		public Double getRMratio(){
			return Double.valueOf(getCantTweetsWithRetweet() / getCantTweets());
		}
		
		public Double getSNP(){
			return (getInteractorRatio()+getRMratio()) / 2;
		}
		
		public Integer getCantInfluenciados(){
			return influenciados.size();
		}
		
		public Integer getCantTweets(){
			return tweets.size();
		}
		
		public Integer getCantTweetsWithRetweet(){
			Integer toReturn = 0;
			for (Long tweet : tweets.keySet())
				if (tweets.get(tweet) > 0)
					toReturn++;
			return toReturn;
		}
}

package influencemetrics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import database.DBManager;

public class RetweetImpact {
	private SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Hashtable<Long, Integer> cant_tweets;
	private Hashtable<Long, Integer> cant_retweets;
	private Hashtable<Long, Double> retweet_impact;
	
	public RetweetImpact(){
		cant_tweets = new Hashtable<Long, Integer>();
		cant_retweets = new Hashtable<Long, Integer>();
		retweet_impact = new Hashtable<Long, Double>();
	}
	
	public boolean calculateSeeds(Date iniciodate, Date findate, Integer cantusers) {
	    try{
			 ResultSet actionsResult = DBManager.getInstance().executeQueryResult( "SELECT al.user_iduser, al.tweet_idtweet, al.millis, al.type, COUNT(al.tweet_idtweet) FROM actionlog al "
					 															 + "RIGHT JOIN (SELECT a.tweet_idtweet, a.created_at FROM actionlog a "
					 															 + "WHERE a.type='TWEET' AND a.created_at > '"+sdl.format(iniciodate)+"' AND a.created_at < '"+sdl.format(findate)+"') t "
					 															 + "ON al.tweet_idtweet = t.tweet_idtweet "
					 															 + "WHERE al.created_at < '"+sdl.format(findate)+"' "
					 															 + "GROUP BY al.tweet_idtweet "
					 															 + "ORDER BY COUNT(al.tweet_idtweet) DESC, al.millis ASC;");
			generateStructures(actionsResult);
			//Math.log10(1d);
			for (Long user : cant_tweets.keySet()){
				Double ri = ((double) cant_tweets.get(user)) * Math.log10((double)cant_retweets.get(user));
				retweet_impact.put(user, ri);
			}
			/*
			System.out.println("UNSORTED HASH");
			for (Long key : retweet_impact.keySet())
				System.out.println("User: "+ key + "  Retweet Impact: " + retweet_impact.get(key));
			
			*/
			LinkedList<Entry<Long,Double>> sortedByImpact = sortByComparator(retweet_impact,false);
			
			/*
			for (Entry<Long, Double> entry : sortedByImpact)
				System.out.println("User: "+ entry.getKey() + "  Retweet Impact: " + entry.getValue());
			*/
			saveResultsInDB(iniciodate, findate, sortedByImpact,cantusers);
		 }catch (Exception e){
			 return false;
		 }
		return true;
	}
	
	private void saveResultsInDB(Date iniciodate, Date findate, LinkedList<Entry<Long, Double>> list, Integer cantusers){
			  Integer idset = DBManager.getInstance().insertRetweetImpactSet(iniciodate, findate, cantusers, 0d);
		      if (cantusers > list.size())
		    	  cantusers = list.size();
		      int pos_in_seed = 1;
		      int pos_in_list = 0;
		      double totalimpact = 0d;
			  while (pos_in_seed <= cantusers){
				  Entry<Long, Double> entry = list.get(pos_in_list);
				  Long user = entry.getKey();
				  Double impact = entry.getValue();
				  totalimpact += impact;
				  DBManager.getInstance().insertRImpactSeed(idset, pos_in_seed, cant_tweets.get(user), cant_retweets.get(user), impact, user);
				  pos_in_seed++;
				  pos_in_list++;
			  }
			  DBManager.getInstance().updateRetweetImpactSet(idset, totalimpact);
	  }
	
	private LinkedList<Entry<Long, Double>> sortByComparator(Hashtable<Long, Double> unsortHash, boolean order){
		List<Entry<Long, Double>> list = new LinkedList<Entry<Long, Double>>(unsortHash.entrySet());
		Collections.sort(list, new Comparator<Entry<Long, Double>>(){
			public int compare(Entry<Long, Double> o1, Entry<Long, Double> o2) {
				if (order)
					return o1.getValue().compareTo(o2.getValue());
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		return (LinkedList<Entry<Long, Double>>) list;
	}
	
	private void generateStructures(ResultSet actionsResult){
		try {
			while (actionsResult.next()){
				Integer retweets = actionsResult.getInt("COUNT(al.tweet_idtweet)")-1;
				Integer tweets = 1;
				if ( retweets > 0){
					Long iduser = actionsResult.getLong("user_iduser");
					if (actionsResult.getString("type").equals("RETWEET")){
						iduser = getOriginalUser(actionsResult.getLong("al.tweet_idtweet"));
						if (iduser == null){
							System.out.println("User = NULL");
							return;
						}
					}
					if (cant_tweets.containsKey(iduser)){
						retweets += cant_retweets.get(iduser);
						tweets += cant_tweets.get(iduser);
					}
					cant_tweets.put(iduser, tweets);
					cant_retweets.put(iduser, retweets);
				}else return;
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

	private Long getOriginalUser(long idtweet) {
		ResultSet actionsResult = DBManager.getInstance().executeQueryResult("SELECT a.user_iduser FROM actionlog a WHERE tweet_idtweet = '"+idtweet+"' AND type = 'TWEET';");
		try {
			if (actionsResult.next())
				return actionsResult.getLong("a.user_iduser");
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
}

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
import utils.UserSNP;

public class SocialNetworkPotential {
	
	private SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Hashtable<Long, UserSNP> users;
	
	public SocialNetworkPotential(){
		users = new Hashtable<Long, UserSNP>();
	}
	
	public boolean calculateSeeds(Date iniciodate, Date findate, Integer cantusers){
		ResultSet actionsResult = DBManager.getInstance().executeQueryResult("SELECT al.user_iduser AS influenciado, al.tweet_idtweet, al.millis, al.type, t.user_iduser AS original, t.followers "
																		   + "FROM actionlog al "
																		   + "RIGHT JOIN (SELECT a.tweet_idtweet, a.created_at, a.user_iduser, u.followers "
																		               + "FROM actionlog a "
																		               + "LEFT JOIN user u ON a.user_iduser = u.iduser "
																		               + "WHERE a.type='TWEET' "
																		               + "AND a.created_at > '"+sdl.format(iniciodate)+"' "
																		               + "AND a.created_at < '"+sdl.format(findate)+"') "
																		               + "t ON al.tweet_idtweet = t.tweet_idtweet "
																		   + "WHERE al.created_at < '"+sdl.format(findate)+"' "
																		   + "ORDER BY al.type ASC;"); 

		if (actionsResult!=null){
			if (generateStructures(actionsResult)){
				//calculateSNP();
				saveResultsInDB(iniciodate, findate, sortByComparator(calculateSNP(), false), cantusers);
				return true;
			}
		}
		//Datos que necesito obtener para SNP
		//#tweets del usuario que fueron retweeteados
		//#tweets del usuario
		//#usuarios que retweetearon los tweets del usuario		
		//#seguidores (levantar los json otra vez y tomar el campo friends_count)
		
		return false;
	}
	
	public void showUsers(){
		for (Long id : users.keySet()){
			UserSNP u = users.get(id);
			System.out.println("User: "+id+" CantTweets: "+u.getCantTweets()+" CantTweetsWithRetweet: "+u.getCantTweetsWithRetweet()+" CantInlfuenciados: "+u.getCantInfluenciados());
		}
	}
	
	private Hashtable<Long, Double> calculateSNP(){
		Hashtable<Long, Double> potential = new Hashtable<Long, Double>();
		for (Long id : users.keySet())
			potential.put(id, users.get(id).getSNP());
		
		return potential;
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
	
	private void saveResultsInDB(Date iniciodate, Date findate, LinkedList<Entry<Long, Double>> list, Integer cantusers){
		  Integer idset = DBManager.getInstance().insertSNPSet(iniciodate, findate, cantusers, 0d);
	      if (cantusers > list.size())
	    	  cantusers = list.size();
	      Integer pos_in_seed = 1;
	      Integer pos_in_list = 0;
	      Double totalsnp = 0d;
		  while (pos_in_seed <= cantusers){
			  Entry<Long, Double> entry = list.get(pos_in_list);
			  Long user = entry.getKey();
			  Double snp = entry.getValue();
			  totalsnp += snp;
			  UserSNP u = users.get(user);
			  DBManager.getInstance().insertSNPSeed(idset, pos_in_seed, u.getCantInfluenciados(), u.getCantTweets(), u.getCantTweetsWithRetweet(), snp, user);
			  pos_in_seed++;
			  pos_in_list++;
		  }
		  DBManager.getInstance().updateSNPSet(idset, totalsnp);
	}
	
	private boolean generateStructures(ResultSet actionsResult) {
		try {
			while (actionsResult.next()){
				String type = actionsResult.getString("al.type");
				Long idoriginal = actionsResult.getLong("original");
				Long idtweet = actionsResult.getLong("al.tweet_idtweet");
				
				if (type.equals("TWEET")){
					if (!users.containsKey(idoriginal))
						users.put(idoriginal, new UserSNP(idoriginal, actionsResult.getInt("t.followers")));
					UserSNP user = users.get(idoriginal);
					user.addTweet(idtweet);
				}else{
					Long idinfluenciado = actionsResult.getLong("influenciado");
					UserSNP user = users.get(idoriginal);
					user.addTweet(idtweet);
					user.addInfluenciado(idinfluenciado);
				}
			}
			return true;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
	
}

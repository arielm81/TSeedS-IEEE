package influencemetrics;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.util.Map.Entry;

import database.DBManager;
import utils.Pair;

public class Closeness {
	
	private SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private Integer[][] dmatrix;
	private Integer totalusers;
	private Hashtable<Long, Pair<Integer, Vector<Long> > > users;
	
	public Closeness(){
		users = new Hashtable<Long, Pair<Integer, Vector<Long> > >();
		Integer totalusers = 1000;
		dmatrix = new Integer[totalusers][totalusers];
	}
	
	public boolean calculateSeeds(Date iniciodate, Date findate, Integer cantusers){
		if (initializeMatrix(iniciodate, findate)){
			calculateDistancesFloyd();
			saveResultsInDB(iniciodate, findate, sortByComparator(calculateShortestPaths(), true), cantusers);
			return true;
		}
		
		return false;
	}
	
	private void saveResultsInDB(Date iniciodate, Date findate, LinkedList<Entry<Long, Integer>> list, Integer cantusers){
		  Integer idset = DBManager.getInstance().insertClosenessSet(iniciodate, findate, cantusers, 0d, totalusers);
	      if (cantusers > list.size())
	    	  cantusers = list.size();
	      Integer pos_in_seed = 1;
	      Integer pos_in_list = 0;
	      Double totalcloseness = 0d;
		  while (pos_in_seed <= cantusers){
			  Entry<Long, Integer> entry = list.get(pos_in_list);
			  Long user = entry.getKey();
			  Integer distance = entry.getValue();
			  Double closeness = (totalusers-1) / Double.valueOf(distance);
			  totalcloseness += closeness;
			  DBManager.getInstance().insertClosenessSeed(idset, pos_in_seed, distance, closeness, user);
			  pos_in_seed++;
			  pos_in_list++;
		  }
		  DBManager.getInstance().updateClosenessSet(idset, totalcloseness);
	}
	
	/*	acc closeness
	private void saveResultsInDB(Date iniciodate, Date findate, LinkedList<Entry<Long, Integer>> list, Integer cantusers){
		  Integer idset = DBManager.getInstance().insertAccClosenessSet(iniciodate, findate, cantusers, 0d, totalusers);
	      if (cantusers > list.size())
	    	  cantusers = list.size();
	      Integer pos_in_seed = 1;
	      Integer pos_in_list = 0;
	      Double totalacccloseness = 0d;
		  while (pos_in_seed <= cantusers){
			  Entry<Long, Integer> entry = list.get(pos_in_list);
			  Long user = entry.getKey();
			  Integer distance = entry.getValue();
			  Double acccloseness = (totalusers-1) / Double.valueOf(distance);
			  totalacccloseness += acccloseness;
			  DBManager.getInstance().insertAccClosenessSeed(idset, pos_in_seed, distance, acccloseness, user);
			  pos_in_seed++;
			  pos_in_list++;
		  }
		  DBManager.getInstance().updateAccClosenessSet(idset, totalacccloseness);
	}
	*/
	private LinkedList<Entry<Long, Integer>> sortByComparator(Hashtable<Long, Integer> unsortHash, boolean order){
		List<Entry<Long, Integer>> list = new LinkedList<Entry<Long, Integer>>(unsortHash.entrySet());
		Collections.sort(list, new Comparator<Entry<Long, Integer>>(){
			public int compare(Entry<Long, Integer> o1, Entry<Long, Integer> o2) {
				if (order)
					return o1.getValue().compareTo(o2.getValue());
				return o2.getValue().compareTo(o1.getValue());
			}
		});
		return (LinkedList<Entry<Long, Integer>>) list;
	}
	
	public void showMatrix(int cantusers){
		System.out.println("Matrix");
		
		for (int k = 0; k<cantusers; k++)
			System.out.println(k+" = "+getUserByInt(k));
		System.out.println("\n");
		
		for (int k = 0; k < cantusers; k++){
			for (int r = 0; r < cantusers; r++)
				System.out.print("|"+dmatrix[k][r]+"|"+"\t");
			System.out.print("\n");
		}
	}
	
	private Long getUserByInt(int k) {
		for (Long key : users.keySet())
			if (users.get(key).getFirst().equals(k))
				return key;
		return null;
	}

	private boolean initializeMatrix(Date iniciodate, Date findate) {
		ResultSet actionsResult = DBManager.getInstance().executeQueryResult("SELECT al.user_iduser AS influenciado, al.tweet_idtweet, al.millis, al.type, t.user_iduser AS original "
																		   + "FROM actionlog al "
																		   + "RIGHT JOIN (SELECT a.tweet_idtweet, a.created_at, a.user_iduser "
																		               + "FROM actionlog a WHERE a.type='TWEET' "
																		               + "AND a.created_at > '"+sdl.format(iniciodate)+"' "
																		               + "AND a.created_at < '"+sdl.format(findate)+"') "
																		               + "t ON al.tweet_idtweet = t.tweet_idtweet "
																		   + "WHERE al.created_at < '"+sdl.format(findate)+"' "
																		   + "AND al.type = 'RETWEET' ;"); 
	    
		if (actionsResult!=null){
			try {
		    	while (actionsResult.next()){
		    		Long original = actionsResult.getLong("original");
		    		Long influenciado = actionsResult.getLong("influenciado");
		    		if (!users.containsKey(original)){
		    			Vector<Long> aux = new Vector<Long>();
		    			aux.add(influenciado);
		    			users.put(original, new Pair<Integer, Vector<Long>>(users.size(), aux));
		    		}else{
		    			if (users.get(original).getSecond() != null){
		    				if (!users.get(original).getSecond().contains(influenciado))
		    					users.get(original).getSecond().add(influenciado);
		    			}else{
		    				Integer id = users.get(original).getFirst();
		    				Vector<Long> aux = new Vector<Long>();
		    				aux.add(influenciado);
		    				users.put(original, new Pair<Integer, Vector<Long>>(id,aux));
		    			}
		    		}
		    		if (!users.containsKey(influenciado)){
		    			users.put(influenciado, new Pair<Integer, Vector<Long>>(users.size(), null));
		    		}/*else{ esto iria si fuese no dirigido el grafo
		    			if (!users.get(influenciado).getSecond().contains(original))
		    				users.get(influenciado).getSecond().add(original);
		    		}*/
		    	}
		    	
		    	if (users.size()>0){
		    		totalusers = users.size();
		    		dmatrix = new Integer[totalusers][totalusers];
		    		for (int k = 0; k < totalusers; k++)
		    			for (int r = 0; r < totalusers; r++)
		    				dmatrix[k][r] = totalusers;
		    		
		    		for (Long key : users.keySet()){
		    			Pair<Integer, Vector<Long>> pair = users.get(key);
		    			Integer i = pair.getFirst();
		    			if (pair.getSecond()!=null){
			    			for (Long l : pair.getSecond()){
			    				Integer j = users.get(l).getFirst();
			    				dmatrix[i][j] = 1;
			    			}
		    			}
		    			dmatrix[i][i] = 0;
		    		}
		    	}
	    	}catch(SQLException e){
	    		e.printStackTrace();
	    		return false;
	    	}
	    }
		return true;
	}

	private void calculateDistancesFloyd(){
		//FLOYD CT = O(n^3)
		for (int k = 0; k < totalusers; k++)
			for (int i = 0; i < totalusers; i++)
				for (int j = 0; j < totalusers; j++)
					if (dmatrix[i][k] + dmatrix[k][j] < dmatrix[i][j])
						dmatrix[i][j] = dmatrix[i][k] + dmatrix[k][j];
	}
	
	private Hashtable<Long, Integer> calculateShortestPaths(){
		Hashtable<Long, Integer> paths = new Hashtable<Long, Integer>();
		for (int i = 0; i < totalusers; i++){
			Integer distances = 0;
			for (int j = 0; j < totalusers; j++)
				distances += dmatrix[i][j];
			paths.put(getUserByInt(i), distances);
		}
		return paths;
	}
}

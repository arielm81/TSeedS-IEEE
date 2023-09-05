package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import javax.naming.spi.DirStateFactory.Result;

public class DBManager {

	private static final String hostDB = "localhost";
	private static final String userDB = "root";
	private static final String passDB = "";
	private static final String nameDB = "tseedsdbcomplete";
	
	private static final String name2DB = "tseedsdbbasic";
	
    private static final String database_model_script_path = "./scripts/tseeds-dbmodel.sql";
	
    private SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
	private Connection connection;
	private static DBManager instance = null;
	
	public static DBManager getInstance(){
		if (instance == null)
			instance = new DBManager();
		return instance;
	}
	
	private DBManager(){
		getConnection();
		if (connection != null){
			//initialize();
			/*
			Date creacion = new Date();
			insertTweet(1000l, "Tweet de prueba", null, "en", creacion);
			insertUser(1200l, "Virologo", "viru", 1, null, 3, 2, 1, 0, 4, 2, "es", "buenos aires", false);
			insertUser(1500l, "Diegoteee", "diego", 1, null, 3, 2, 1, 0, 4, 2, "es", null, false);
			insertEdge(1200l, 1500l, new Date());
			// viru sigue a diego
			// diego tiene que haber twitteado el 1000l
			insertAction(creacion, "TWEET", 1000l, 1500l);
			// viru tiene que haber hecho dicha accion pero mas tarde
			Date retweet_creacion = new Date(creacion.getTime() + 120000l);
			insertAction(retweet_creacion, "RETWEET", 1000l, 1200l);
			Date retweet_creacion2 = new Date(retweet_creacion.getTime() + 120000l);
			insertAction(retweet_creacion2, "RETWEET", 1000l, 1200l);
			Integer idseedset = insertSeedSet(creacion, new Date(creacion.getTime()+12000000), 10, 567.4, 1800, 1900);
			if (idseedset!=-1)
				insertSeed(idseedset, 1, 40.5d, 1200l);
			else
				System.out.println("No se pudo insertar seed porque el ID del set resulto ser -1.");
			*/
		}
	}
	
	public Connection getConnection(){
		try {
			if (connection==null || connection.isClosed()){
				// Setup the connection with the DB
			      connection = DriverManager.getConnection("jdbc:mysql://"+hostDB+"/"+nameDB+"?"+"user="+userDB+"&password="+passDB);
			}
			return connection;
		} catch (SQLException e) {
			//e.printStackTrace();
			System.out.println("Error de Conexion con la Base de Datos. ErrorCode:"+e.getErrorCode()+" Message: "+e.getMessage());
		}
		return null;
	}
	
	public void executeQuery(String query){ // Queries que no retornan valores que interesen
		if (query!=""){
			try{
				String[] queries = query.split(";");
				if (queries.length>1)
					for (int i = 0; i < queries.length-1; i++)
						getConnection().createStatement().execute(queries[i]);
				else
					getConnection().createStatement().execute(query);
			}catch(SQLIntegrityConstraintViolationException de) { // duplicate key exception
				//System.out.println("ErrorCode:" + de.getErrorCode() + "\t"+de.getMessage()+"\t QUERY:"+query);
			}catch(SQLException e){
				e.printStackTrace();			
			}
		}
	}
	
	public ResultSet executeQueryResult(String query){
		try {
			 return getConnection().createStatement().executeQuery(query);
		}catch(SQLIntegrityConstraintViolationException de) { // duplicate key exception
			//System.out.println("Duplicate Key in Query: "+query);
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void initialize(){
		String query = "";
		try{
			BufferedReader br = new BufferedReader(new FileReader(database_model_script_path));
			StringBuffer sb = new StringBuffer();
			String line="";
			while ((line = br.readLine()) != null)
				sb.append(line + "\n");
			br.close();
			query = sb.toString();
		}catch(Exception e){
			e.printStackTrace();
		}
		executeQuery(query);
	}
	
	private String formatForSQL(String text){
		try{
		text = text.replace(";", " ");
		text = text.replace("\\", "\\\\");
		text = text.replace("'", "\\'");
		
		text = "'"+text+"'";
		}catch(Exception e){
			System.out.println("Text: "+text);
			e.printStackTrace();
		}
		return text;
	}
	
	public void insertTweet(Long idtweet, String text, String place, String lang, Date createdAt){
		if (text!=null) text = formatForSQL(text);
		if (place!=null) place = formatForSQL(place);
		if (lang!=null) lang = formatForSQL(lang);
		String date = null;
		if (createdAt!=null) date = "'" + sdl.format(createdAt)+"'";
		
		String query = "INSERT INTO tweet "
					 + "VALUES (" + idtweet + "," +text + "," + place + "," + lang + "," + date + ","+createdAt.getTime()+")";
		//System.out.println("Inserting Tweet: "+idtweet);
		executeQuery(query);
	}
	
	public void insertUser(Long iduser, String screenName, String name, Integer ntweets, Integer nretweets, Integer nmentions, Integer nreplies, Integer nretweeted, Integer nmentioned, Integer nreplied, Integer nseed, String lang, String location, Boolean intermediate){
		if (screenName!=null) screenName = formatForSQL(screenName);
		if (name!=null) name = formatForSQL(name);
		if (lang!=null) lang = formatForSQL(lang);
		if (location!=null) location = formatForSQL(location);
		
		String query = "INSERT INTO user (iduser, screen_name, name, nro_tweets, nro_retweets, nro_mentions, nro_replies, nro_retweeted, nro_mentioned, nro_replied, nro_seedset, lang, location, intermediate) "
					 + "VALUES ("+iduser+","+screenName+","+name+","+ntweets+","+nretweets+","+nmentions+","+nreplies+","+nretweeted+","+nmentioned+","+nreplied+","+nseed+","+lang+","+location+","+intermediate+") "
					 + "ON DUPLICATE KEY UPDATE screen_name = "+screenName+", name = "+name+", nro_tweets = "+ntweets+", nro_retweets = "+nretweets+", nro_mentions = "+nmentions+", nro_replies = "+nreplies+", nro_retweeted = "+nretweeted+", nro_mentioned = "+nmentioned+", nro_replied = "+nreplied+", nro_seedset = "+nseed+", lang = "+lang+", location = "+location+", intermediate = "+intermediate+" ;";
		//System.out.println("Inserting User: "+iduser);
		executeQuery(query);
	}
	
	public void insertEdge(Long idfollower, Long idfollowed, Date timestamp){
		String date = null;
		if (timestamp!=null) date = "'" + sdl.format(timestamp) +"'";
		
		String query = "INSERT INTO socialgraph "
					 + "VALUES ("+idfollower+","+idfollowed+","+date+","+timestamp.getTime()+");";
		executeQuery(query);
	}
	
	public void insertAction(Date created_at, String type, Long idtweet, Long iduser){
		if (type!=null) type = "'"+type+"'";
		String date = null;
		if (created_at!=null) date = "'"+sdl.format(created_at)+"'";

		Integer id = getCountTable("actionlog")+1;
		
		if (id!=-1){
			String query = "INSERT INTO actionlog "
						 + "VALUES ("+id+","+date+","+created_at.getTime()+","+type+","+idtweet+","+iduser+");";
			executeQuery(query);
		}else System.out.println("ID del Log Entry es -1 al intentar insertar al action log lo siguiente --> Tweet: "+idtweet+" User: "+iduser+" Type: "+type);
	}
	
	public void insertSeed(Integer idset, Integer pos, Double minf, Long iduser){
		String query = "INSERT INTO marginalinfluenceseed "
					 + "VALUES ("+idset+","+pos+","+minf+","+iduser+",0);";
		executeQuery(query);
	}
	
	public void insertRImpactSeed(Integer idset, Integer pos, Integer tweets, Integer retweets, Double impact, Long user ){
		String query = "INSERT INTO retweetimpactseed "
					 + "VALUES ("+idset+","+pos+","+tweets+","+retweets+","+impact+","+user+",0);";
		executeQuery(query);
	}
	
	public Integer insertRetweetImpactSet(Date timeinit, Date timeend, Integer size, Double totalimpact){
		String dateinit = null;
		if (timeinit!=null) dateinit = "'"+sdl.format(timeinit)+"'";
		String dateend = null;
		if (timeend!=null) dateend = "'"+sdl.format(timeend)+"'";
		
		Integer id = getCountTable("retweetimpact")+1;
		if (id!=-1){
			String query = "INSERT INTO retweetimpact "
						 + " VALUES ("+id+","+dateinit+","+timeinit.getTime()+","+dateend+","+timeend.getTime()+","+size+","+totalimpact+");";
			executeQuery(query);
			return id;
		}else System.out.println("ID del Seed Set es -1 al intentar insertar el seed set siguiente --> TimeInit: "+dateinit+" TimeEnd: "+dateend+" Size: "+size+" TotalImpact: "+totalimpact);
		return -1;
	}
	
	public void updateRetweetImpactSet(Integer id, Double totalimpact){
		String query = "UPDATE retweetimpact SET totalimpact="+totalimpact+" WHERE idseedset="+id+";";
		executeQuery(query);
	}
	
	public Integer insertSeedSet(Date timeinit, Date timeend, Integer size, Double totalminf, Integer totalus, Integer totalac){
		String dateinit = null;
		if (timeinit!=null) dateinit = "'"+sdl.format(timeinit)+"'";
		String dateend = null;
		if (timeend!=null) dateend = "'"+sdl.format(timeend)+"'";
		
		Integer id = getCountTable("marginalinfluence")+1;
		if (id!=-1){
			String query = "INSERT INTO marginalinfluence "
						 + " VALUES ("+id+","+dateinit+","+timeinit.getTime()+","+dateend+","+timeend.getTime()+","+size+","+totalminf+","+totalus+","+totalac+");";
			executeQuery(query);
			return id;
		}else System.out.println("ID del Seed Set es -1 al intentar insertar el seed set siguiente --> TimeInit: "+dateinit+" TimeEnd: "+dateend+" Size: "+size+" TotalMInf: "+totalminf+" TotalUs: "+totalus+" TotalAc: "+totalac);
		return -1;
	}
	
	public void updateSeedSet(Integer id, Double totalmarginf){
		String query = "UPDATE marginalinfluence SET totalmarginf="+totalmarginf+" WHERE idseedset="+id+";";
		executeQuery(query);
	}
	
	public void updateTrueSpread(Integer id, Double trueSpread, String metric, Integer seeds){
		String query = "UPDATE "+metric+" SET truespreadx"+seeds+" = "+trueSpread+" WHERE idseedset = "+id+" ;";
		executeQuery(query);
	}
	
	public void updateTrueSpreadSeed(Integer id, Integer position, Double trueSpread, String metric){
		String query = "UPDATE "+metric+"seed SET truespread_acc = "+trueSpread+" WHERE "+metric+"_idseedset = "+id+" AND position = "+position+" ;";
		executeQuery(query);
	}
	
	public Integer getCountTable(String table){
		String countQuery = "SELECT COUNT(*) FROM "+table+";";
		ResultSet result = executeQueryResult(countQuery);
		Integer id = -1;
		try {
			if (result!=null && result.next())
				id = result.getInt(1);
		} catch (SQLException e) {
			e.printStackTrace();
		} 
		return id;
	}
	
	public Long getUserByIdNorm(Integer user_id) {
		String query = "SELECT iduser FROM user WHERE iduser_norm = "+user_id+";";
		ResultSet result = executeQueryResult(query);
		Long id = -1l;
		try{
			if (result!=null && result.next())
				id = result.getLong(1);
		}catch (SQLException e){
			e.printStackTrace();
		}
		return id;
	}
	
	public Integer insertAccClosenessSet(Date timeinit, Date timeend, Integer size, Double tacccloseness, Integer users) {
		String dateinit = null;
		if (timeinit!=null) dateinit = "'"+sdl.format(timeinit)+"'";
		String dateend = null;
		if (timeend!=null) dateend = "'"+sdl.format(timeend)+"'";
		
		Integer id = getCountTable("acccloseness")+1;
		if (id!=-1){
			String query = "INSERT INTO acccloseness "
						 + "VALUES ("+id+","+dateinit+","+timeinit.getTime()+","+dateend+","+timeend.getTime()+","+size+","+tacccloseness+","+users+");";
			executeQuery(query);
			return id;
		}else System.out.println("ID del Seed Set es -1 al intentar insertar el seed set siguiente --> TimeInit: "+dateinit+" TimeEnd: "+dateend+" Size: "+size+" TotalAccCloseness: "+tacccloseness);
		return -1;
	}
	
	public void insertAccClosenessSeed(Integer idset, Integer pos, Integer distance, Double acccloseness, Long user) {
		String query = "INSERT INTO accclosenessseed "
					 + "VALUES ("+idset+","+pos+","+distance+","+acccloseness+","+user+", 0);";
		executeQuery(query);
	}

	public void updateAccClosenessSet(Integer id, Double totalacccloseness) {
		String query = "UPDATE acccloseness SET totalacccloseness="+totalacccloseness+" WHERE idseedset="+id+";";
		executeQuery(query);
	}
	
	public Integer insertClosenessSet(Date timeinit, Date timeend, Integer size, Double tcloseness, Integer users) {
		String dateinit = null;
		if (timeinit!=null) dateinit = "'"+sdl.format(timeinit)+"'";
		String dateend = null;
		if (timeend!=null) dateend = "'"+sdl.format(timeend)+"'";
		
		Integer id = getCountTable("closeness")+1;
		if (id!=-1){
			String query = "INSERT INTO closeness "
						 + " VALUES ("+id+","+dateinit+","+timeinit.getTime()+","+dateend+","+timeend.getTime()+","+size+","+tcloseness+","+users+");";
			executeQuery(query);
			return id;
		}else System.out.println("ID del Seed Set es -1 al intentar insertar el seed set siguiente --> TimeInit: "+dateinit+" TimeEnd: "+dateend+" Size: "+size+" TotalCloseness: "+tcloseness);
		return -1;
	}
	
	public Integer insertSNPSet(Date timeinit, Date timeend, Integer size, Double tsnp) {
		String dateinit = null;
		if (timeinit!=null) dateinit = "'"+sdl.format(timeinit)+"'";
		String dateend = null;
		if (timeend!=null) dateend = "'"+sdl.format(timeend)+"'";
		
		Integer id = getCountTable("socialnetworkpotential")+1;
		if (id!=-1){
			String query = "INSERT INTO socialnetworkpotential "
						 + " VALUES ("+id+","+dateinit+","+timeinit.getTime()+","+dateend+","+timeend.getTime()+","+size+","+tsnp+");";
			executeQuery(query);
			return id;
		}else System.out.println("ID del Seed Set es -1 al intentar insertar el seed set siguiente --> TimeInit: "+dateinit+" TimeEnd: "+dateend+" Size: "+size+" TotalSNP: "+tsnp);
		return -1;
	}
	
	public void insertSNPSeed(Integer idset, Integer pos, Integer cantInfluenciados, Integer cantTweets, Integer cantTweetsWithRetweet, Double snp, Long user){
		String query = "INSERT INTO socialnetworkpotentialseed "
					 + "VALUES ("+idset+","+pos+","+cantInfluenciados+","+cantTweets+","+cantTweetsWithRetweet+","+snp+","+user+",0);";
		executeQuery(query);
	}
	
	public void updateSNPSet(Integer id, Double totalsnp){
		String query = "UPDATE socialnetworkpotential SET totalsnp="+totalsnp+" WHERE idseedset="+id+";";
		executeQuery(query);
	}
	
	public void insertClosenessSeed(Integer idset, Integer pos, Integer distance, Double closeness, Long user) {
		String query = "INSERT INTO closenessseed "
					 + "VALUES ("+idset+","+pos+","+distance+","+closeness+","+user+",0);";
		executeQuery(query);
	}

	public void updateClosenessSet(Integer id, Double totalcloseness) {
		String query = "UPDATE closeness SET totalcloseness="+totalcloseness+" WHERE idseedset="+id+";";
		executeQuery(query);
	}
	
	public void updateUserFollowers(Long user, Integer followers){
		String query = "UPDATE user SET followers="+followers+" WHERE iduser="+user+";";
		executeQuery(query);
	}
	
	public Integer getFollowers(Long id) {
		String query = "SELECT followers FROM user WHERE iduser="+id+";";
		ResultSet result = executeQueryResult(query);
		try {
			if (result!=null && result.next())
				return result.getInt("followers");
		} catch (SQLException e) {
			//e.printStackTrace();
		}
		return 0;
	}
	
	public ArrayList<Long> getOtherDBIDS(){
		ArrayList<Long> toReturn = new ArrayList<Long>();
		try {
			Connection c = DriverManager.getConnection("jdbc:mysql://"+hostDB+"/"+name2DB+"?"+"user="+userDB+"&password="+passDB);
			ResultSet result = c.createStatement().executeQuery("SELECT iduser FROM user WHERE followers = 0;");
			if (result!=null){
				while (result.next()){
					toReturn.add(result.getLong("iduser"));
				}
			}
				
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return toReturn;
	}
}
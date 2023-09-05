package application;
import influencemodels.FilesBuilder;
import influencemodels.InfluenceModelsExecutor;
import influencemodels.ResultsInterpreter;
import infoprocess.InfoLoaderProcessor;
import infoprocess.InfoProcessor;
import infoprocess.User;
//import net.sf.jxls.exception.ParsePropertyException;
//import net.sf.jxls.transformer.XLSTransformer;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.DefaultListModel;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JScrollPane;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.ScrollPaneConstants;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import org.apache.log4j.BasicConfigurator;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.jxls.area.Area;
import org.jxls.builder.AreaBuilder;
import org.jxls.builder.xls.XlsCommentAreaBuilder;
import org.jxls.common.Context;
import org.jxls.transform.Transformer;
import org.jxls.transform.poi.PoiTransformer;
import org.jxls.util.JxlsHelper;

import database.DBManager;
import influencemetrics.Closeness;
import influencemetrics.Metric;
import influencemetrics.RetweetImpact;
import influencemetrics.SocialNetworkPotential;

import java.awt.Font;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JList;


import seed.HistoricSeedsManager;
import seed.SeedProfile;
import socialgraph.GraphVisualization;
import twitter4j.RateLimitStatus;
import twitter4j.RateLimitStatusEvent;
import twitter4j.RateLimitStatusListener;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitterinfo.ConfigBuilder;
import twitterinfo.TwitterMonitor;
import utils.ExcelGenerator;
import utils.IO;
import utils.Save;

public class MainWindow {

	private JFrame frame;
	private TwitterMonitor twitterMonitor;
	private InfoProcessor infoProcessor;
	private FilesBuilder fbuilder;
	private JButton btnShowGraph;
	private ResultsInterpreter resultsInterpreter;
	private InfluenceModelsExecutor influenceModels;
	private HistoricSeedsManager historicSeedsManager;
	private DefaultTableModel table_model;
	private Twitter twitter;
	private static SimpleDateFormat sdf_conPuntos = new SimpleDateFormat("HH:mm:ss  yyyy-MM-dd");
	
	public static final String FONT_NAME = "Roboto";
	
	/**
	 * @wbp.nonvisual location=61,529
	 */
	
	private Timer timer = null;
	private JTable table_seedset;
	private JScrollPane scrollPane;
	private JScrollPane scrollPaneHistoricSeeds;
	private JList<String> list_historicSeeds;
	private DefaultListModel<String> list_model;
	private JLabel lblDate;
	
	
	private ConfigurationManager cm;
	
	public static void main(final String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					System.out.println("Started running at "+sdf_conPuntos.format(new Date()));
					MainWindow window = new MainWindow(args);
					//window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	public MainWindow(String[] args) { 	
		//MODIFICADO 2017
		/*
		initialize();
		cm = new ConfigurationManager();
		cm.configure();
		twitter = new TwitterFactory(ConfigBuilder.getConsultInstance().build()).getInstance();
		
		if (args.length > 0){
			if (args[0].toLowerCase().equals("listenmode")){
				System.out.println("LISTEN MODE");
				listenMode();
			}
			else{
				System.out.println("LOAD JSON MODE");
				for (int i = 0; i < args.length ; i++)
					loadJSONMode(args[i]);
				System.out.println("termino LOAD JSON MODE");
			}
		}
		else{
			System.out.println("REAL TIME MODE");
			realTimeMode();
		}
		*/
		
		//twitter = new TwitterFactory(ConfigBuilder.getConsultInstance().build()).getInstance();
		
		/* twitter.addRateLimitStatusListener(new RateLimitStatusListener() {
			
			@Override
			public void onRateLimitStatus(RateLimitStatusEvent arg0) {
				
			}
			
			@Override
			public void onRateLimitReached(RateLimitStatusEvent arg0) {
				//Thread.sleep(15*60*1000); //sleep de 15 minutos
			}
		});*/
		
		//DBManager.getInstance();
		//loadJSONMode("./CleanedStatus/");
		//calculateSeeds();
		//calculateRetweetImpact();
		//calculateCloseness();
		//calculateSNP();
		//calculateTrueSpread();
		/*
		ExcelGenerator eg = new ExcelGenerator();
		eg.generateMetricsPerSeeds("Templates/template_MetricsPerSeeds.xls", "2017-09-28 18:00:00", "2017-10-01 00:00:01");
		for (int seeds = 10; seeds < 51; seeds = seeds + 10)
			eg.generateMetricsPerHours("Templates/template_MetricsPerHours.xls", seeds);
		*/	
		//runShuffleTest("shuffle_test_18hs/");
		
		System.out.println("Finished running at "+sdf_conPuntos.format(new Date()));
		
	}
	
	private void runShuffleTest(String folder){
		String fileName = folder+"actionslog.txt";
		/*
		try{
			  @SuppressWarnings("resource")
			  BufferedReader br = new BufferedReader(new FileReader(fileName));
			  String line = null;
			  String old_action = "0";
			  Vector<String> users = new Vector<String>();
			  Vector<String> actions  = new Vector<String>();
			  Vector<String> times = new Vector<String>();
			  String actionslog_shuffle_text  = ""; 
			  while (((line = br.readLine()) != null) && line != ""){
				  String[] linea = line.split(" ");
				  String user = linea[0];
				  String action = linea[1];
				  String time = linea[2];
				  if (old_action.equals(action)){
					 users.add(user);
					 actions.add(action);
					 times.add(time);
				  }else{
					 Collections.shuffle(users);
					 for (int i = 0; i < users.size(); i++)
						actionslog_shuffle_text += users.get(i) + " " + actions.get(i) + " " + times.get(i) + "\n";
					 users.clear();
					 actions.clear();
					 times.clear();
					 old_action = action;
					 users.add(user);
					 actions.add(action);
					 times.add(time);
				  }
			  }
			  if (!users.isEmpty()){
			     Collections.shuffle(users);
				 for (int i = 0; i < users.size(); i++)
					actionslog_shuffle_text += users.get(i) + " " + actions.get(i) + " " + times.get(i) + "\n";
				 users.clear();
				 actions.clear();
				 times.clear();
			  }
			  IO io = new IO();
			  io.writeToFileNew(folder+"actionslog-shuffle.txt", actionslog_shuffle_text, false);
		 }catch(FileNotFoundException e){
		  e.printStackTrace();
		  return;
		 }catch(IOException e){
		  e.printStackTrace();
		  return;
		 }
		 */
		/*
		InfluenceModelsExecutor ime = new InfluenceModelsExecutor();
		ime.runFirstScanDir(folder+"config_training_scan1.txt");
		ime.runSecondScanDir(folder+"config_training_scan2.txt");
		ime.executeDir(folder+"config_maxinf_CD.txt");
		ime.runTrueSpreadDir(folder+"config_true_spreadx50.txt");
		*/
	}
	
	private void calculateSeeds(){
		FilesBuilder fbuilder = new FilesBuilder();
		InfluenceModelsExecutor ime = new InfluenceModelsExecutor();
		ResultsInterpreter.getInstance().saveExecConfig();
		SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String inicio = "2017-09-28 15:59:59";
		String fin = "2017-09-28 18:00:00"; 
		Long hora_en_millis = 3600000l;
		try {
			Date iniciodate = sdl.parse(inicio);
			Date findate = sdl.parse(fin);
			while (findate.getTime() < sdl.parse("2017-10-01 00:00:01").getTime()){
				System.out.println("Entro a while con inicio: "+sdl.format(iniciodate)+"  fin: "+sdl.format(findate));
				if (fbuilder.buildFirstScan(iniciodate, findate)){
					ime.runFirstScan();
					ime.runSecondScan();
					ime.execute();
					ResultsInterpreter.getInstance().saveResultsInDB();
					iniciodate = new Date(iniciodate.getTime()+hora_en_millis);
					findate = new Date(findate.getTime()+hora_en_millis);
				}else{
					System.out.println("Fallo First Scan");
					return;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateRetweetImpact(){
		SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String inicio = "2017-09-28 15:59:59";
		String fin = "2017-09-28 18:00:00"; 
		Long hora_en_millis = 3600000l;
		try {
			Date iniciodate = sdl.parse(inicio);
			Date findate = sdl.parse(fin);
			while (findate.getTime() < sdl.parse("2017-10-01 00:00:01").getTime()){
				System.out.println("Entro a while con inicio: "+sdl.format(iniciodate)+"  fin: "+sdl.format(findate));
				RetweetImpact rimpact = new RetweetImpact();
				if (rimpact.calculateSeeds(iniciodate, findate, 50)){
					iniciodate = new Date(iniciodate.getTime()+hora_en_millis);
					findate = new Date(findate.getTime()+hora_en_millis);
				}else{
					System.out.println("Fallo Retweet Impact");
					return;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateDegree(){
		//TODO
		//Considers for each node the number of its adjacent edges.
	}
	
	private void calculateCloseness(){
		//Is based on the length of the shortest paths from a node i to everyone else.
		//It measures the visibility or accessibility of each node with respect to the entire network.
		SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String inicio = "2017-09-28 15:59:59";
		String fin = "2017-09-28 18:00:00";
		Long hora_en_millis = 3600000l;
		try {
			Date iniciodate = sdl.parse(inicio);
			Date findate = sdl.parse(fin);
			while (findate.getTime() < sdl.parse("2017-10-01 00:00:01").getTime()){
				System.out.println("Entro a while con inicio: "+sdl.format(iniciodate)+"  fin: "+sdl.format(findate));
				Closeness closeness = new Closeness();
				if (closeness.calculateSeeds(iniciodate, findate, 50)){
					iniciodate = new Date(iniciodate.getTime()+hora_en_millis);
					findate = new Date(findate.getTime()+hora_en_millis);
				}else{
					System.out.println("Fallo Closeness");
					return;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateBetweenness(){
		//TODO
	}
	
	private void calculateSNP(){
		SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String inicio = "2017-09-28 15:59:59";
		String fin = "2017-09-28 18:00:00"; 
		Long hora_en_millis = 3600000l;
		try {
			Date iniciodate = sdl.parse(inicio);
			Date findate = sdl.parse(fin);
			while (findate.getTime() < sdl.parse("2017-10-01 00:00:01").getTime()){
				System.out.println("Entro a while con inicio: "+sdl.format(iniciodate)+"  fin: "+sdl.format(findate));
				SocialNetworkPotential snp = new SocialNetworkPotential();
				if (snp.calculateSeeds(iniciodate, findate, 50)){
					iniciodate = new Date(iniciodate.getTime()+hora_en_millis);
					findate = new Date(findate.getTime()+hora_en_millis);
				}else{
					System.out.println("Fallo Social Potential Network");
					return;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void generateFilesForSpread(){
		FilesBuilder fbuilder = new FilesBuilder();
		InfluenceModelsExecutor ime = new InfluenceModelsExecutor();
		ResultsInterpreter.getInstance().saveExecConfig();
		SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String inicio = "2017-09-28 15:59:59";
		String fin = "2017-09-28 18:00:00"; 
		Long hora_en_millis = 3600000l;
		try {
			Date iniciodate = sdl.parse(inicio);
			Date findate = sdl.parse(fin);
			while (findate.getTime() < sdl.parse("2017-10-01 00:00:01").getTime()){
				System.out.println("Entro a while con inicio: "+sdl.format(iniciodate)+"  fin: "+sdl.format(findate));
				if (fbuilder.buildFirstScan(iniciodate, findate)){
					ime.runFirstScan();
					ime.runSecondScan();
					ime.execute();
					ResultsInterpreter.getInstance().saveResultsInDB();
					iniciodate = new Date(iniciodate.getTime()+hora_en_millis);
					findate = new Date(findate.getTime()+hora_en_millis);
				}else{
					System.out.println("Fallo First Scan");
					return;
				}
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void calculateTrueSpreadMetric(String dir){
		InfluenceModelsExecutor ime = new InfluenceModelsExecutor();
		ime.runTrueSpreadDir(dir+"config_true_spreadx50.txt");
	}
	
	private void calculateTrueSpread(){
		SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		SimpleDateFormat sdfolder = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
		FilesBuilder fb = new FilesBuilder();
		String inicio = "2017-09-28 15:59:59";
		String fin = "2017-09-30 06:00:00"; // poner en 2017-09-28 18:00:00
		Long hora_en_millis = 3600000l;
		try {
			Date iniciodate = sdl.parse(inicio);
			Date findate = sdl.parse(fin);
			int i = 37; // poner en 1 --> esta en 37 porque es el seed set del 30.09.2017 a las 6.00.00
			while (findate.getTime() < sdl.parse("2017-10-01 00:00:01").getTime()){
				System.out.println("Entro a while con inicio: "+sdl.format(iniciodate)+"  fin: "+sdl.format(findate));
				String dir = FilesBuilder.dir_root+sdfolder.format(findate)+"/";
				fb.buildFirstScanForTrueSpread(iniciodate, findate);
				fb.dumpRankingFiles(findate);
				fb.createTrueSpreadConfigFiles(findate);
				calculateTrueSpreadMetric(dir+"closeness"+"/");
				calculateTrueSpreadMetric(dir+"marginalinfluence"+"/");
				calculateTrueSpreadMetric(dir+"retweetimpact"+"/");
				calculateTrueSpreadMetric(dir+"socialnetworkpotential"+"/");
				ResultsInterpreter.getInstance().saveTrueSpreadInDB(i, dir, "closeness");
				ResultsInterpreter.getInstance().saveTrueSpreadInDB(i, dir, "marginalinfluence");
				ResultsInterpreter.getInstance().saveTrueSpreadInDB(i, dir, "retweetimpact");
				ResultsInterpreter.getInstance().saveTrueSpreadInDB(i, dir, "socialnetworkpotential");
				i++;
				findate = new Date(findate.getTime()+hora_en_millis);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		}
	}
	
	private void loadJSONMode(String fileName){			//MODIFICADO 2017
		System.out.println(fileName);
		//System.out.println("Started at: "+new Date());
		try {
			File file = new File(fileName);
			if (file.exists()){
				InfoLoaderProcessor ilprocessor = new InfoLoaderProcessor(twitter);
				Vector<File> listFiles = listAllFiles(file);
				/* TODO SACAR COMENTARIO
				fbuilder = new FilesBuilder();
				resultsInterpreter = new ResultsInterpreter();
				influenceModels = new InfluenceModelsExecutor();
				String filePath = "./Results/";
				FIN TODO */
				for (int i = 0; i < listFiles.size(); i++) {
					File f = listFiles.elementAt(i);
					System.out.println("Starting File: "+f.getName()+" at "+sdf_conPuntos.format(new Date()));
					ilprocessor.load(f);
					System.out.println("Finishing File at "+sdf_conPuntos.format(new Date()));
					//AL HACERLO ADENTRO DEL FOR SE CALCULA EL SEED SET PARA CADA ARCHIVO
					//QUE EN EL CASO DE ESTUDIO SERIA CADA UNA HORA, LO QUE DEBERIA HACER ES QUE
					//LOS SEED SET SE ALMACENEN EN UN ARCHIVO CON EL MISMO NOMBRE A LA PAR
					//LA COSA ES QUE AL FINALIZAR DEBERIA BORRAR TODOS LOS DATOS QUE TENGO
					//SOBRE EL SOCIAL GRAPH Y EL ACTION LOG
						/*
					fbuilder.makeFiles();
					influenceModels.execute();
					System.out.println("Finished execution");
					//String name = f.getName().split(".")[0];
					String name = f.getName();
					System.out.println("ResultsFileName: " + name);
					System.out.println("ResultsFilePath: " + filePath + name + ".txt");
					Save.getInstance().saveResults(filePath + name + ".txt", resultsInterpreter.getResultsAsText());
					//UNA VEZ OBTENIDOS LOS RESULTADOS RESETEO EL SOCIAL GRAPH Y EL ACTION LOG
					ActionLog.getInstance().removeAll();
					SocialGraph.getInstance().removeAll();				
					*/
				}
				
				System.out.println("Total requests: "+ ilprocessor.getPeticiones());
				System.out.println("Total waits: "+ilprocessor.getEsperas());
				/* TODO SACAR COMENTARIO 
				fbuilder.makeFiles();
				influenceModels.execute();
				System.out.println("Finished execution");
				//String name = f.getName().split(".")[0];
				Save.getInstance().saveResults(filePath + "AllHoursResults.txt", resultsInterpreter.getResultsAsText());
				FIN TODO */
				//historicSeedsManager = HistoricSeedsManager.getInstance();
			}
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
		System.out.println("Finished at: "+new Date());
	}
	
	private Vector<File> listAllFiles(File f){				//MODIFICADO 2017
		Vector<File> files = new Vector<File>();
		if (f.isDirectory()){ //es directorio
			File[] vfiles = f.listFiles(new FilenameFilter() {
			    public boolean accept(File dir, String name) {
			        return name.toLowerCase().endsWith(".json");
			    }
			});
			for (File file : vfiles)
				files.addAll(listAllFiles(file));
		}
		else if (f.getName().toLowerCase().endsWith(".json"))	//es archivo
			files.add(f);
		return files;
	}
	
	
	private void listenMode(){		
		twitterMonitor = new TwitterMonitor(twitter, cm.getFilter(),cm.getFilterQuery() , cm.getFilterTime());
		twitterMonitor.listenAndSave();
	}
	
	private void realTimeMode(){				//MODIFICADO 2017
		twitterMonitor = new TwitterMonitor(twitter, cm.getFilter(),cm.getFilterQuery() , cm.getFilterTime());
		twitterMonitor.startListen();
		
		infoProcessor = new InfoProcessor(twitterMonitor, twitter);
		infoProcessor.start();
		fbuilder = new FilesBuilder();
		resultsInterpreter = new ResultsInterpreter();
		influenceModels = new InfluenceModelsExecutor();
		historicSeedsManager = HistoricSeedsManager.getInstance();
		
		ActionListener timerListener = new ActionListener()
		{
		     @Override
		     public void actionPerformed(ActionEvent e)
		     {
		    	fbuilder.makeFiles();
		        influenceModels.execute();
		        table_model.getDataVector().removeAllElements();
		        for (String[] cell : resultsInterpreter.getResultsAsCells()){
		        	table_model.addRow(cell);
		        }
		        table_seedset.repaint();
		        for (String historicSeedName : historicSeedsManager.getHistoricSeeds()){
		        	if (!list_model.contains(historicSeedName))
		        		list_model.addElement(historicSeedName);
		        }
		        list_historicSeeds.repaint();
		        lblDate.setText(sdf_conPuntos.format(new Date()));
		        lblDate.repaint();
		     }
		};
		
		timer = new Timer(0, timerListener);
		timer.setInitialDelay(cm.getTraining_window());
		timer.setDelay(cm.getUpdate_window());
		timer.setRepeats(true);
		timer.start();		
	}
	
	
	@SuppressWarnings("serial")
	private void initialize() {				//MODIFICADO 2017
		frame = new JFrame();
		frame.setTitle("Twitter Influenciability Tool");
		frame.setBounds(100, 100, 785, 342);
		frame.setResizable(false);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		ImageIcon img = new ImageIcon("resources/twitterlogo.png");
		frame.setIconImage(img.getImage());
		
		String column_names[]= {"User","Marginal Inf","Tweets","Retweets","Replies","Retweeted","Replied"};
		table_model=new DefaultTableModel(column_names,0){
			 @Override
		     public boolean isCellEditable(int row, int column) {
				 return false;
		     }
		};
		
		list_historicSeeds = new JList<String>();
		list_model = new DefaultListModel<String>();
		list_historicSeeds.setModel(list_model);
		
		MouseListener mouseListener = new MouseAdapter() {
		     public void mouseClicked(MouseEvent e) {
		         if (e.getClickCount() == 2) {
		             int index = list_historicSeeds.locationToIndex(e.getPoint());
		             String screenName = list_model.get(index);
		             if (screenName != null){
		            	//ABRIR NUEVO FRAME CON EL USER SCREENNAME DEL INDEX dobleclickeado
		            	User seed = historicSeedsManager.getSeed(screenName);
		            	if (seed != null){
		            		SeedProfile sp = new SeedProfile(seed);
		            		sp.show();
		            	}
		             }
		          }
		     }
		};
		list_historicSeeds.addMouseListener(mouseListener);
		
		table_seedset = new JTable(table_model){
		    @Override
		       public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
		           Component component = super.prepareRenderer(renderer, row, column);
		           int rendererWidth = component.getPreferredSize().width;
		           TableColumn tableColumn = getColumnModel().getColumn(column);
		           tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
		           return component;
		        }
		    };
		table_seedset.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	
		scrollPane = new JScrollPane();
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
		
		
		
		btnShowGraph = new JButton("SHOW SOCIAL GRAPH");
		btnShowGraph.setFont(new Font(FONT_NAME, Font.BOLD, 14));
		btnShowGraph.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				GraphVisualization.getInstance().showGraph();
			}
		});
		
		JLabel lblResults = new JLabel("SEED SET at");
		lblResults.setFont(new Font(FONT_NAME, Font.BOLD, 22));
		
		scrollPaneHistoricSeeds = new JScrollPane();
		
		lblDate = new JLabel("hh:mm:ss   dd-mm-aaaa");
		lblDate.setFont(new Font(FONT_NAME, Font.BOLD, 22));
		
		JLabel lblNewLabel = new JLabel("HISTORIC SEEDS");
		lblNewLabel.setFont(new Font(FONT_NAME, Font.BOLD, 22));

		
		GroupLayout groupLayout = new GroupLayout(frame.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
						.addGroup(groupLayout.createSequentialGroup()
							.addGroup(groupLayout.createParallelGroup(Alignment.LEADING)
								.addComponent(lblNewLabel, GroupLayout.DEFAULT_SIZE, 204, Short.MAX_VALUE)
								.addComponent(scrollPaneHistoricSeeds, 0, 0, Short.MAX_VALUE))
							.addPreferredGap(ComponentPlacement.RELATED)
							.addGroup(groupLayout.createParallelGroup(Alignment.TRAILING)
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 537, GroupLayout.PREFERRED_SIZE)
									.addContainerGap(22, Short.MAX_VALUE))
								.addGroup(groupLayout.createSequentialGroup()
									.addComponent(lblResults)
									.addGap(18)
									.addComponent(lblDate, GroupLayout.PREFERRED_SIZE, 270, GroupLayout.PREFERRED_SIZE)
									.addGap(47))))
						.addGroup(groupLayout.createSequentialGroup()
							.addComponent(btnShowGraph)
							.addGap(200))))
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblNewLabel)
						.addComponent(lblResults)
						.addComponent(lblDate, GroupLayout.PREFERRED_SIZE, 27, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(groupLayout.createParallelGroup(Alignment.BASELINE)
						.addComponent(scrollPane, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE)
						.addComponent(scrollPaneHistoricSeeds, GroupLayout.PREFERRED_SIZE, 201, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnShowGraph, GroupLayout.PREFERRED_SIZE, 43, GroupLayout.PREFERRED_SIZE)
					.addContainerGap(14, Short.MAX_VALUE))
		);
		
		scrollPaneHistoricSeeds.setViewportView(list_historicSeeds);
		scrollPane.setViewportView(table_seedset);
		frame.getContentPane().setLayout(groupLayout);
	}
}
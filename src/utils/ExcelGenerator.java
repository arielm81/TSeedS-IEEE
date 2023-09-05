package utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.BasicConfigurator;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import database.DBManager;
import influencemetrics.Metric;

public class ExcelGenerator {
	
	private SimpleDateFormat sdl = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	private SimpleDateFormat sdfile = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
	private static final String root = "Analisis_TSeedSComplete/";
	
	public void generateMetricsPerHours(String template, Integer seeds){
		String folder = root+"MetricsPerHours/";
		File dir = new File(folder);
		dir.mkdirs();
		
		BasicConfigurator.configure();
		ResultSet r = DBManager.getInstance().executeQueryResult("SELECT m.truespread_acc AS mi, c.truespread_acc AS cl, r.truespread_acc AS ri, s.truespread_acc AS snp "
															   + "FROM marginalinfluenceseed m "
															   + "LEFT JOIN closenessseed c ON c.closeness_idseedset = m.marginalinfluence_idseedset AND c.position = m.position "
															   + "LEFT JOIN retweetimpactseed r ON r.retweetimpact_idseedset = m.marginalinfluence_idseedset AND r.position = m.position "
															   + "LEFT JOIN socialnetworkpotentialseed s ON s.socialnetworkpotential_idseedset = m.marginalinfluence_idseedset AND s.position = m.position "
															   + "WHERE m.position = "+seeds+";");
		List<Metric> metrics = new ArrayList<Metric>();
		try {
			InputStream is = new FileInputStream(new File(template));
			
			while (r.next())
				metrics.add(new Metric(r.getDouble("mi"), r.getDouble("cl"), r.getDouble("ri"), r.getDouble("snp")));
			
			Context context = new Context();
			context.putVar("metrics", metrics);
			context.putVar("seeds", seeds);
			
			OutputStream os = new FileOutputStream(folder+seeds+"seeds.xls");
			
			JxlsHelper.getInstance().processTemplate(is, os, context);
			
			is.close();
			os.close();
		} catch (SQLException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	public void generateMetricsPerSeeds(String template, String inicio, String fin){
		String folder = root+"MetricsPerSeeds/";
		File dir = new File(folder);
		dir.mkdirs();
		
		Long hora_en_millis = 3600000l;
		
		try {
			BasicConfigurator.configure();
			Date iniciodate = sdl.parse(inicio);			
			while (iniciodate.getTime() < sdl.parse(fin).getTime()){
				System.out.println("Entro a while con: "+sdl.format(iniciodate));
				InputStream is = new FileInputStream(new File(template));
				ResultSet r = DBManager.getInstance().executeQueryResult("SELECT m1.idseedset AS id, m2.position, m2.truespread_acc AS mi, c.truespread_acc AS cl, r.truespread_acc AS ri, s.truespread_acc AS snp "
														 			   + "FROM marginalinfluence m1 LEFT JOIN marginalinfluenceseed m2 ON m2.marginalinfluence_idseedset = m1.idseedset "
														 			   + "LEFT JOIN closenessseed c ON c.closeness_idseedset = m1.idseedset AND c.position = m2.position "
														 			   + "LEFT JOIN retweetimpactseed r ON r.retweetimpact_idseedset = m1.idseedset AND r.position = m2.position "
														 			   + "LEFT JOIN socialnetworkpotentialseed s ON s.socialnetworkpotential_idseedset = m1.idseedset AND s.position = m2.position "
														 			   + "WHERE timeend = '"+sdl.format(iniciodate)+"' ORDER BY m2.position ;");
				//generacion de los datos
				List<Metric> metrics = new ArrayList<Metric>();
				while (r.next())
					metrics.add(new Metric(r.getDouble("mi"), r.getDouble("cl"), r.getDouble("ri"), r.getDouble("snp")));
				
				Context context = new Context();
				context.putVar("metrics", metrics);
				
				OutputStream os = new FileOutputStream(folder+sdfile.format(iniciodate)+".xls");
				
				JxlsHelper.getInstance().processTemplate(is,  os, context);
				
				is.close();
				os.close();

				iniciodate = new Date(iniciodate.getTime()+hora_en_millis);
			}
		} catch (ParseException e) {
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
	
}

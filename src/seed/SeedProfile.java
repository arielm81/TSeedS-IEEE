package seed;

import actionlog.Action;
import application.MainWindow;
import infoprocess.User;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;
import javax.swing.GroupLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.DateTickUnit;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.NumberTickUnit;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.time.Minute;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import org.jfree.data.xy.XYDataset;
import utils.Pair;



public class SeedProfile
{
  JFrame profileWindow;
  private static SimpleDateFormat sdf_conPuntos = new SimpleDateFormat("HH:mm:ss  yyyy-MM-dd");
  protected JTable table_UserActions;
  
  @SuppressWarnings("serial")
public SeedProfile(User u) {
    profileWindow = new JFrame();
    profileWindow.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    Date actualDate = new Date();
    profileWindow.setTitle("Seed User Profile at " + sdf_conPuntos.format(actualDate));
    ImageIcon img = new ImageIcon("resources/twitterlogo.png");
    profileWindow.setIconImage(img.getImage());
    profileWindow.setBounds(100, 100, 820, 654);
    profileWindow.setResizable(false);
    
    Vector<Pair<Action, Long>> actions = u.getActionsPerformed();
    Vector<SeedInfo> seed_occurs = u.getSeedInfo();
    
    JScrollPane panel_UserInfo = new JScrollPane();
    
    String[][] userInfo = { { "ScreenName", u.getScreenName() }, 
      { "Twitter ID", u.getId().toString() }, 
      { "# Tweets", u.getNumberOfTweets().toString() }, 
      { "# Retweets", u.getNumberOfRetweets().toString() }, 
      { "# Replies", u.getNumberOfReplies().toString() }, 
      { "# Retweeted", u.getRetweeted().toString() }, 
      { "# Replied", u.getReplied().toString() }, 
      { "# in Seed Set", Integer.toString(seed_occurs.size()) } };
    
    String[] columnIdentifiers = { "Field", "Value" };
    
    JTable table_UserInfo = new JTable();
    table_UserInfo.setFont(new Font(MainWindow.FONT_NAME, 0, 12));
    DefaultTableModel table_model_UserInfo = new DefaultTableModel(userInfo, columnIdentifiers){
      @Override
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    table_UserInfo.setModel(table_model_UserInfo);
    table_UserInfo.setFillsViewportHeight(true);
    panel_UserInfo.setViewportView(table_UserInfo);
    panel_UserInfo.setVerticalScrollBarPolicy(20);
    panel_UserInfo.setHorizontalScrollBarPolicy(30);
    

    JScrollPane panel_UserActions = new JScrollPane();
    table_UserActions = new JTable(){
      @Override
      public Component prepareRenderer(TableCellRenderer renderer, int row, int column) {
        Component component = super.prepareRenderer(renderer, row, column);
        int rendererWidth = component.getPreferredSize().width;
        TableColumn tableColumn = getColumnModel().getColumn(column);
        tableColumn.setPreferredWidth(Math.max(rendererWidth + getIntercellSpacing().width, tableColumn.getPreferredWidth()));
        return component;
      }
      
    };
    table_UserActions.setFont(new Font(MainWindow.FONT_NAME, 0, 12));
    String[] columnNames = { "Type", "Time", "Text" };
    DefaultTableModel table_model_UserActions = new DefaultTableModel(columnNames, 0)
    {
      public boolean isCellEditable(int row, int column) {
        return false;
      }
    };
    for (Pair<Action, Long> p : actions) {
      Action a = (Action)p.getFirst();
      Long time = (Long)p.getSecond();
      Date d = new Date(time.longValue());
      String[] row = { a.getType(), sdf_conPuntos.format(d), a.getTweet() };
      table_model_UserActions.addRow(row);
    }
    table_UserActions.setModel(table_model_UserActions);
    panel_UserActions.setViewportView(table_UserActions);
    table_UserActions.getParent().addComponentListener(new ComponentAdapter()
    {
      public void componentResized(ComponentEvent e) {
        if (table_UserActions.getPreferredSize().width < table_UserActions.getParent().getWidth()) {
          table_UserActions.setAutoResizeMode(4);
        } else {
          table_UserActions.setAutoResizeMode(0);
        }
      }
    });
    panel_UserActions.setVerticalScrollBarPolicy(20);
    panel_UserActions.setHorizontalScrollBarPolicy(30);
    

    Long firstActionTime = actions.get(0).getSecond();
    Date initialDate = new Date(firstActionTime.longValue());
    JFreeChart chart = getChart(initialDate, actualDate, seed_occurs);
    
    JScrollPane panel_UserGrafico = new JScrollPane();
    panel_UserGrafico.setViewportView(new ChartPanel(chart));
    
    GroupLayout groupLayout = new GroupLayout(profileWindow.getContentPane());
    groupLayout.setHorizontalGroup(
      groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(GroupLayout.Alignment.TRAILING, groupLayout.createSequentialGroup()
      .addContainerGap()
      .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.TRAILING)
      .addComponent(panel_UserGrafico, GroupLayout.Alignment.LEADING, -1, 794, 32767)
      .addGroup(groupLayout.createSequentialGroup()
      .addComponent(panel_UserInfo, -2, 180, -2)
      .addGap(18)
      .addComponent(panel_UserActions, -1, 596, 32767)))
      .addContainerGap()));
    
    groupLayout.setVerticalGroup(
      groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING)
      .addGroup(groupLayout.createSequentialGroup()
      .addContainerGap()
      .addGroup(groupLayout.createParallelGroup(GroupLayout.Alignment.LEADING, false)
      .addComponent(panel_UserActions, 0, 0, Short.MAX_VALUE)
      .addComponent(panel_UserInfo, GroupLayout.DEFAULT_SIZE, 155, Short.MAX_VALUE))
      .addPreferredGap(ComponentPlacement.RELATED)
      .addComponent(panel_UserGrafico, GroupLayout.DEFAULT_SIZE, 328, Short.MAX_VALUE)
      .addContainerGap()));
    

    profileWindow.getContentPane().setLayout(groupLayout);
  }
  
@SuppressWarnings("deprecation")
private JFreeChart getChart(Date initialDate, Date lastDate, Vector<SeedInfo> seed_occurs) {
    TimeSeries series = new TimeSeries("Date Series", Minute.class);
    for (SeedInfo si : seed_occurs) {
      int pos = (si.getPosInSeed().intValue() - 11) * -1;
      series.addOrUpdate(new Minute(si.getTime()), pos);
    }
    series.add(new Minute(initialDate), 0.0D);
    XYDataset dataset = new TimeSeriesCollection(series);
    JFreeChart chart = ChartFactory.createTimeSeriesChart("User Influenciability", "Date", "Seed Set Position", null, false, false, false);
    XYPlot plot = chart.getXYPlot();
    DateAxis domainAxis = new DateAxis();
    domainAxis.setRange(initialDate, lastDate);
    domainAxis.setTickUnit(new DateTickUnit(4, 1));
    domainAxis.setDateFormatOverride(new SimpleDateFormat("hh:mm"));
    NumberAxis rangeAxis = new NumberAxis();
    rangeAxis.setRange(0.0D, 10.0D);
    rangeAxis.setTickUnit(new NumberTickUnit(1.0D));
    plot.setDomainAxis(domainAxis);
    plot.setRangeAxis(rangeAxis);
    plot.setDataset(dataset);
    
    return chart;
  }
  
  public void show() {
    profileWindow.setVisible(true);
  }
}

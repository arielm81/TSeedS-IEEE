package socialgraph;

import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Vector;
import javax.swing.ImageIcon;
import javax.swing.JFrame;

public class GraphVisualization
{
  private static GraphVisualization instance = null;
  
  private DirectedSparseGraph<String, Integer> graph;
  private VertexColorTransformer vertexColor;
  private int edgeCounts;
  private Vector<String> users_who_tweets;
  private Vector<String> users_who_retweets;
  private Vector<String> inter_users;
  private Vector<String> maxinf_users;
  private JFrame frame = null;
  
  private GraphVisualization() {
    graph = new DirectedSparseGraph<String, Integer>();
    edgeCounts = 1;
    users_who_tweets = new Vector<String>();
    users_who_retweets = new Vector<String>();
    maxinf_users = new Vector<String>();
    inter_users = new Vector<String>();
  }
  
  public static GraphVisualization getInstance() {
    if (instance == null)
      instance = new GraphVisualization();
    return instance;
  }
  
  public void addEdge(String vertex1, String vertex2) {
    if (!graph.containsVertex(vertex1))
      graph.addVertex(vertex1);
    if (!graph.containsVertex(vertex2))
      graph.addVertex(vertex2);
    if (!users_who_tweets.contains(vertex2))
      users_who_tweets.add(vertex2);
    if (!users_who_retweets.contains(vertex1))
      users_who_retweets.add(vertex1);
    graph.addEdge(Integer.valueOf(edgeCounts), vertex1, vertex2);
    edgeCounts += 1;
    if (frame != null)
      frame.repaint();
  }
  
  public void addVertex(String vertex) {
    if (!graph.containsVertex(vertex))
      graph.addVertex(vertex);
    if (frame != null)
      frame.repaint();
  }
  
  public void showGraph() {
    if (frame == null) {
      try {
        Layout<String, Integer> layout = new ISOMLayout<String, Integer>(graph);
        layout.setSize(new Dimension(1400, 690));
        VisualizationViewer<String, Integer> vv = new VisualizationViewer<String, Integer>(layout);
        vv.setPreferredSize(new Dimension(1410, 700));
        vv.getRenderContext().setVertexLabelTransformer(new ToStringLabeller<String>());
        DefaultModalGraphMouse<String, Integer> gm = new DefaultModalGraphMouse<String, Integer>();
        gm.setMode(DefaultModalGraphMouse.Mode.TRANSFORMING);
        vv.setGraphMouse(gm);
        vertexColor = new VertexColorTransformer(users_who_tweets, users_who_retweets, maxinf_users, inter_users);
        vv.getRenderContext().setVertexFillPaintTransformer(vertexColor);
        
        frame = new JFrame("Social Graph View");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        ImageIcon img = new ImageIcon("resources/twitterlogo.png");
        frame.setIconImage(img.getImage());
        frame.addWindowListener(new WindowAdapter()
        {
          public void windowClosed(WindowEvent e) {
            frame = null;
          }
        });
        frame.add(vv);
        frame.pack();
        frame.setVisible(true);
      }
      catch (Exception localException) {}
    }
  }
  
  public void addInterUser(String id)
  {
    inter_users.add(id);
  }
  
  public void setMaxInfUsers(Vector<String> maxInfUsers) {
    maxinf_users.clear();
    maxinf_users.addAll(maxInfUsers);
    if (frame != null) {
      frame.repaint();
    }
  }
}

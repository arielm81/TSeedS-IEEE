package actionlog;

import infoprocess.User;

public class LogEntry {
  private User user;
  private Integer userID_normal;
  private Long time;
  private Action action;
  private User inter_user;
  private Long time_inter;
  
  public LogEntry(User u, Integer id, Long time, Action a) { user = u;
    userID_normal = id;
    this.time = time;
    action = a;
    inter_user = null;
  }
  
  public LogEntry(User u, Integer id, Long time, Action a, User inter_user, Long time_inter) {
    user = u;
    userID_normal = id;
    this.time = time;
    action = a;
    this.inter_user = inter_user;
    this.time_inter = time_inter;
  }
  
  public Action getAction() {
    return action;
  }
  
  public void setAction(Action action) {
    this.action = action;
  }
  
  public Long getTime() {
    return time;
  }
  
  public void setTime(Long time) {
    this.time = time;
  }
  
  public User getUser() {
    return user;
  }
  
  public void setUser(User user) {
    this.user = user;
  }
  
  public Integer getUserID_normal() {
    return userID_normal;
  }
  
  public void setUserID_normal(Integer userID_normal) {
    this.userID_normal = userID_normal;
  }
  
  public Long getTime_normal(long olderTime) {
    long time_clone = time.longValue();
    time_clone -= olderTime;
    
    time_clone /= 1000L;
    
    time_clone += 1L;
    if (time_clone > 0L) {
      return Long.valueOf(time_clone);
    }
    return Long.valueOf(-1L);
  }
  
  public boolean isInterEntry() {
    if (inter_user != null)
      return true;
    return false;
  }
  
  public User getInter_user() {
    return inter_user;
  }
  
  public void setInter_user(User inter_user) {
    this.inter_user = inter_user;
  }
  
  public Long getTimeInter_normal(long olderTime) {
    long time_clone = time_inter.longValue();
    time_clone -= olderTime;
    time_clone /= 60000L;
    
    time_clone += 1L;
    if (time_clone > 0L) {
      return Long.valueOf(time_clone);
    }
    return Long.valueOf(-1L);
  }
  
  public void setTime_inter(Long time_inter) {
    this.time_inter = time_inter;
  }
}

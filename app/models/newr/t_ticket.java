package models.newr;

import java.util.Date;

import javax.persistence.Entity;

import play.db.jpa.Model;
@Entity
public class t_ticket extends Model {
   public String holder;
   public String creater;
   public String type;
   public String ticket_no;
   public Date  end_time;
   public Date start_time;
   public Long ticket_amout;
}

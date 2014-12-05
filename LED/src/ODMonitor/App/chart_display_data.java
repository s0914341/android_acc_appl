package ODMonitor.App;

import java.io.Serializable;
import java.util.Date;

public class chart_display_data implements Serializable{
	private long index;
	private Date date;
	private double concentration;
	
	public chart_display_data(long local_index, double local_concentration) {
		date = new Date();
		concentration = local_concentration;
		index = local_index;
	}
	
	public long get_index_value() {
		return index;
	}
	
	public Date get_date_value() {
		return date;
	}
	
	public double get_concentration_value() {
		return concentration;
	}
}

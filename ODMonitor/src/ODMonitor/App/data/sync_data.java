package ODMonitor.App.data;

import java.io.Serializable;

public class sync_data{
	public static final int STATUS_END = 1;
	public static final int STATUS_CONTINUE = 2;
	
	private int status;
	private int meta_data;
	
	public sync_data() {
		status = 0;
		meta_data = 0;
	}
	
	public void set_status(int data) {
		status = data;
	}
	
	public int get_status() {
	    return status;
	}
	
	public void set_meta_data(int data) {
		meta_data = data;
	}
	
	public int get_meta_data() {
	    return meta_data;
	}

}

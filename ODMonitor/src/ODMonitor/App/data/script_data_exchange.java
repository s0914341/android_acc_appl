package ODMonitor.App.data;

import java.io.Serializable;
import java.util.HashMap;

import android.widget.ListView;
import android.widget.SimpleAdapter;

public class script_data_exchange implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3086190372173945249L;
	public HashMap<Object, Object> item;
	public SimpleAdapter adapter;
	public ListView list_view;
}

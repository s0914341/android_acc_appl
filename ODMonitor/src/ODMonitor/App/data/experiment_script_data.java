package ODMonitor.App.data;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class experiment_script_data implements Serializable{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7763243430535626819L;
	public static final Map<Integer, String> SCRIPT_INSTRUCT;
    static {
        Map<Integer, String> aMap = new HashMap<Integer, String>();
        aMap.put(0, "Read Sensor");
        aMap.put(1, "Shaker ON");
        aMap.put(2, "Shaker OFF");
        aMap.put(3, "Shaker Set Temperature");
        aMap.put(4, "Shaker Set Speed");
        aMap.put(5, "Repeat Count");
        aMap.put(6, "Repeat Time");
        SCRIPT_INSTRUCT = Collections.unmodifiableMap(aMap);
    }
    
    private byte instruct = 0;
    private byte repeat_from = 0;
    private byte repeat_count = 0;
    private byte repeat_time = 0;
    private int shaker_arg = 0;
    
    public int get_instruct_value() {
		return (int)instruct;
	}
    
    public String get_instruct_string() {
		return SCRIPT_INSTRUCT.get(get_instruct_value());
	}
    
    public int get_repeat_from_value() {
		return (int)repeat_from;
	}
    
    public String get_repeat_from_string() {
    	String str;
    	str = String.format("%d", get_repeat_from_value());
		return str;
	}
    
    public int get_repeat_count_value() {
		return (int)repeat_count;
	}
    
    public String get_repeat_count_string() {
    	String str;
    	str = String.format("%d", get_repeat_count_value());
		return str;
	}
    
    public int get_repeat_time_value() {
		return (int)repeat_time;
	}
    
    public String get_repeat_time_string() {
    	String str;
    	str = String.format("%d", get_repeat_time_value());
		return str;
	}
}

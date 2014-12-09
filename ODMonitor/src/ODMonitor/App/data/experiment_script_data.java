package ODMonitor.App.data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


public class experiment_script_data {
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
}

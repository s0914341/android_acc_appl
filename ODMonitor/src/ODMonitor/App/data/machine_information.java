package ODMonitor.App.data;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;


public class machine_information {
	public static final int SHAKER_STATUS_INDEX = 0;
	public static final int SENSOR_STATUS_INDEX = 1; 
	public static final int MASS_STORAGE_STATUS_INDEX = 2; 
	public static final int EXPERIMENT_STATUS_INDEX = 3; 
	public static final int SYNCHRONOUS_DATA_INDEX = 4;
	public static final int CURRENT_INSTRUCTION_INDEX = 5; 
	public static final int EXPERIMENT_TIMER_INDEX = 9;
	public static final int VERSION1_INDEX = 13;
	public static final int VERSION2_INDEX = 14;
	public static final int VERSION3_INDEX = 15;
	public static final int VERSION4_INDEX = 16;
	public static final int TOTAL_SIZE = 17; 

	public byte[] buffer = new byte[TOTAL_SIZE]; /* data content */
	
	public machine_information () {

	}
	
	public byte get_shaker_status() {
	    return buffer[SHAKER_STATUS_INDEX];
	}
	
	public byte get_sensor_status() {
	    return buffer[SENSOR_STATUS_INDEX];
	}
	
	public byte get_mass_storage_status() {
	    return buffer[MASS_STORAGE_STATUS_INDEX];
	}
	
	public byte get_experiment_status() {
	    return buffer[EXPERIMENT_STATUS_INDEX];
	}
	
	public byte get_synchronous_data() {
	    return buffer[SYNCHRONOUS_DATA_INDEX];
	}
	
	public int get_current_instruction() {
		ByteBuffer byte_buffer = ByteBuffer.wrap(buffer, CURRENT_INSTRUCTION_INDEX, 4);
		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
        int ret = byte_buffer.getInt();
        
	    return ret;
	}
	
    public int get_experiment_time() {
		ByteBuffer byte_buffer = ByteBuffer.wrap(buffer, EXPERIMENT_TIMER_INDEX, 4);
		byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
        int ret = byte_buffer.getInt();
        
	    return ret;
	}
    
    public byte get_version1() {
	    return buffer[VERSION1_INDEX];
	}
    
    public byte get_version2() {
	    return buffer[VERSION2_INDEX];
	}
    
    public byte get_version3() {
	    return buffer[VERSION3_INDEX];
	}
    
    public byte get_version4() {
	    return buffer[VERSION4_INDEX];
	}
    
    public int copy_to_buffer(byte[] indata, int len) {
		int retval = -1;
		if (len <= (TOTAL_SIZE)) {
		    System.arraycopy(indata, 0, buffer, 0, len);
		    retval = 0;
		}
		
		return retval;
	}
}

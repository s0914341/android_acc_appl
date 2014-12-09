package ODMonitor.App.data;

public class android_accessory_packet {
	public static final byte DATA_TYPE_KEYPAD = 0;
	public static final byte DATA_TYPE_GET_MACHINE_STATUS = 1;
	public static final byte DATA_TYPE_SEND_SHAKER_COMMAND = 2;
	public static final byte DATA_TYPE_GET_SHAKER_RETURN = 3;
	public static final byte DATA_TYPE_GET_EXPERIMENT_DATA = 4;
	public static final byte DATA_TYPE_SET_EXPERIMENT_SCRIPT = 5;
	public static final byte DATA_TYPE_SET_EXPERIMENT_STATUS = 6;
	public static final byte DATA_TYPE_NOTIFY_EXPERIMENT_DATA = 7;
	
	public static final byte  STATUS_EXPERIMENT_IDLE = 0;
	public static final byte  STATUS_EXPERIMENT_START = 1; 
	public static final byte  STATUS_EXPERIMENT_RUNNING = 2;
	public static final byte  STATUS_EXPERIMENT_STOP = 3;
	public static final byte  STATUS_EXPERIMENT_STORAGE_FULL = 4;
	public static final byte  STATUS_EXPERIMENT_FINISH = 5;
	
	public static final byte  STATUS_OK = 0;
	public static final byte  STATUS_FAIL = 1;
	public static final byte  STATUS_HAVE_DATA = 2; 
	public static final byte  STATUS_START = 3; 
	
	
	public static final int PREFIX = 0;
	public static final int TYPE = 1; 
	public static final int STATUS = 2; 
	public static final int RESERVED = 3; 
	public static final int LEN = 4;
	public static final int HEADER_SIZE = 5; 
	public static final int DATA_START = HEADER_SIZE;
	public static final int DATA_SIZE = 128;
	public static final int TOTAL_SIZE = DATA_SIZE+HEADER_SIZE; 
	public static final byte PREFIX_VALUE = 0x0D;
	public static final byte INIT_PREFIX_VALUE = 1;
	public static final byte NO_INIT_PREFIX_VALUE = 0;
	public static final String key_receive = "ODMonitorReceive";

	public byte[] buffer = new byte[TOTAL_SIZE]; /* data content */
	
	public android_accessory_packet(int init) {
		if (INIT_PREFIX_VALUE == init)
		    buffer[PREFIX] = PREFIX_VALUE;
	}
	
	public void set_Type(byte indata) {
		buffer[TYPE] = indata;
	}
	
	public void set_Status(byte indata) {
		buffer[STATUS] = indata;
	}
	
	public void set_Reserved(byte indata) {
		buffer[RESERVED] = indata;
	}
	
	public void set_Len(byte indata) {
		buffer[LEN] = indata;
	}

	public int copy_to_data(byte[] indata, int len) {
		int retval = -1;
		if (len <= (DATA_SIZE)) {
		    System.arraycopy(indata, 0, buffer, DATA_START, len);
		    retval = 0;
		}
		
		return retval;
	}
	
	public int copy_to_buffer(byte[] indata, int len) {
		int retval = -1;
		if (len <= (TOTAL_SIZE)) {
		    System.arraycopy(indata, 0, buffer, 0, len);
		    retval = 0;
		}
		
		return retval;
	}
	
	public byte get_Prefix_value() {
	    return buffer[PREFIX];
	}
	
	public byte get_Type_value() {
	    return buffer[TYPE];
	}
	
	public byte get_Status_value() {
	    return buffer[STATUS];
	}
	
	public int get_Len_value() {
	    return (int)buffer[LEN]&0xff;
	}
	
	public static int get_size() {
	    return TOTAL_SIZE;
	}
	
	public static int get_header_size() {
	    return HEADER_SIZE;
	}
	
	public static int get_data_size() {
	    return DATA_SIZE;
	}
	
}

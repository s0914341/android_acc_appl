package FTDI.LED;

public class android_accessory_packet {
	public static final byte DATA_TYPE_KEYPAD = 0;
	public static final byte DATA_TYPE_SLIDER = 1;
	public static final byte DATA_TYPE_SHAKER_COMMAND = 2;
	public static final byte DATA_TYPE_SHAKER_RETURN = 3;
	public static final byte DATA_TYPE_SENSOR_DATA = 4;
	
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
	
	android_accessory_packet(int init) {
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
	
	public int get_Len_value() {
	    return (int)buffer[LEN];
	}
	
	public int get_size() {
	    return TOTAL_SIZE;
	}
	
	public int get_header_size() {
	    return HEADER_SIZE;
	}
	
	public int get_data_size() {
	    return DATA_SIZE;
	}
	
}

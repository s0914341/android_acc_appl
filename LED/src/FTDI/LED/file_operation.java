package FTDI.LED;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

public class file_operation {
	public final String Tag = "ODMonitor_Sensor";
	File sdcard = Environment.getExternalStorageDirectory();
	private String file_Dir = sdcard.getPath() + "/"; 
	private String CreateFileName = "Default";
	File file_MetaData;
	File file;
	static BufferedWriter file_buf;
	static FileOutputStream fos;
	static FileInputStream fis;
	//SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd-HHmmss");
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");
	static SimpleDateFormat df1 = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
	public static String Flush_File = ""; 
	private boolean file_append = false;
	
	file_operation(String dir_name, String file_name, boolean append) {
	    file_Dir = file_Dir + dir_name;
	    file_MetaData =  new File(file_Dir);
	    CreateFileName = file_name;
	    file_append = append;
	}
	
/*	public void Show_Toast_Msg(String msg ) {
		Toast mToastMsg;
		
		if (msg.contains("flush log file: "))
			mToastMsg = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG);
		else
			mToastMsg = Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT);
    	TextView v = (TextView) mToastMsg.getView().findViewById(android.R.id.message);
    	v.setTextColor(Color.YELLOW);
    	v.setTextSize(TypedValue.COMPLEX_UNIT_SP, 20);
    	//LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) v.getLayoutParams();
    	//v.setGravity(Gravity.CENTER);
		//mToastMsg.setGravity(Gravity.LEFT | Gravity.TOP, 300,100);
    	if (msg.equals(I_Tracker_Device_Conn) || msg.equals(I_Tracker_Device_DisCon))
    	  mToastMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 250);
    	else
    		mToastMsg.setGravity(Gravity.CENTER_HORIZONTAL | Gravity.TOP, 0, 125);
		mToastMsg.show();
    }*/
	
	public void flush_close_file() {
		try {
			if (file_buf != null) {
				file_buf.flush();
				file_buf.close();
				Flush_File = "log file saved: " + file.getPath(); 
			//	Show_Toast_Msg(Flush_File);
				file_buf = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void flush_close_file_byte_array() {
		try {
			if (fos != null) {
				fos.flush();
				fos.close();
				Flush_File = "log file saved: " + file.getPath(); 
			//	Show_Toast_Msg(Flush_File);
				fos = null;
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void write_file_msg(String line) {
		if (file_buf != null) {
			String line_text = df1.format(new Date()) + "  " + line;
			try {
				file_buf.write(line_text, 0, line_text.length());
				file_buf.newLine();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void write_file_byte_array(byte[] data) {
		if (fos != null) {
			try {
				fos.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	 /* file naming format logyyyymmdd-hhmmss.txt*/
	public String generate_filename() {
		String filename;
		filename = CreateFileName + df.format(new Date()) + ".txt";
		Log.d(Tag, filename);
		return filename;
	}
	
	public String generate_filename_no_date() {
		String filename;
		filename = CreateFileName + ".txt";
		Log.d(Tag, filename);
		return filename;
	}
	
	public void create_file(String filename) throws IOException {
		if (sdcard.exists()) {
			if (!file_MetaData.exists()) {
		        file_MetaData.mkdirs();
				Log.d(Tag, "sdcard directory exist:" + Boolean.toString(file_MetaData.exists()));
			}
			
			if (file_MetaData.exists()) {
			    file = new File(file_MetaData, filename);
			    file_buf = new BufferedWriter(new FileWriter(file, file_append));
		    } else {
				file_buf = null;
				Log.d(Tag, "Can't create the log file");
			}
		} else {
			file_buf = null;
			Log.d(Tag, "Can't found external sdcard ");
		}
	}
	
	public void delete_file(String filename) throws IOException {
		if (sdcard.exists()) {
			if (!file_MetaData.exists()) {
				Log.d(Tag, "sdcard directory exist:" + Boolean.toString(file_MetaData.exists()));
			} else {
			    file = new File(file_MetaData, filename);
			    Boolean deleted = file.delete();
			    
			    Log.d(Tag, "delete file:" + deleted.toString());
		    }
		} else {
			Log.d(Tag, "Can't found external sdcard ");
		}
	}
	
	public void create_write_file_byte_array(String filename) throws IOException {
		if (sdcard.exists()) {
			if (!file_MetaData.exists()) {
		        file_MetaData.mkdirs();
				Log.d(Tag, "sdcard directory exist:" + Boolean.toString(file_MetaData.exists()));
			}
			
			if (file_MetaData.exists()) {
			    file = new File(file_MetaData, filename);
			    fos = new FileOutputStream(file.getAbsolutePath(), file_append);   
		    } else {
		    	fos = null;
				Log.d(Tag, "Can't create the byte file");
			}
		} else {
			fos = null;
			Log.d(Tag, "Can't found external sdcard ");
		}
	}
	
	public int open_read_file_byte_array(String filename) throws IOException {
		int ret = 0;
		
		if (sdcard.exists()) {
			if (!file_MetaData.exists()) {
				ret = -1;
				fis = null;
				Log.d(Tag, "sdcard directory exist:" + Boolean.toString(file_MetaData.exists()));
			} else {
			    file = new File(file_MetaData, filename);
			    fis = new FileInputStream(file.getAbsolutePath());  
			    ret = (int)file.length();
		    }
		} else {
			ret = -2;
			fis = null;
			Log.d(Tag, "Can't found external sdcard ");
		}
		
		return ret;
	}
	
	public void read_file_byte_array(byte[] data) {
		if (fis != null) {
			try {
				fis.read(data);
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
}

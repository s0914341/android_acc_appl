package ODMonitor.App.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.util.Log;

public class file_operate_byte_array extends file_operation {
	private String Tag = "file_operate_byte_array";
	protected FileOutputStream fos;
	protected FileInputStream fis;
	
	public file_operate_byte_array(String dir_name, String file_name,
			boolean append) {
		super(dir_name, file_name, append);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public void create_file(String filename) throws IOException {
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
	 
	@Override
	public int open_read_file(String filename) throws IOException {
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
	
	public void write_file(byte[] data) {
		if (fos != null) {
			try {
				fos.write(data);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public void read_file(byte[] data) {
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

	@Override
	public void flush_close_file() {
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
}

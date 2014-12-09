package ODMonitor.App.file;

import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

import ODMonitor.App.data.chart_display_data;
import android.util.Log;

public class file_operate_chart extends file_operate_byte_array {

	public file_operate_chart(String dir_name, String file_name, boolean append) {
		super(dir_name, file_name, append);
		// TODO Auto-generated constructor stub
	}
	
/*	public void writeObject(ArrayList<Object> listChartData) throws IOException {
         Create ObjectOutputStream to write object 
        ObjectOutputStream objOutputStream = new ObjectOutputStream(fos);
         Write object to file 
        for (Object obj : listChartData) {
            objOutputStream.writeObject(obj);
            objOutputStream.reset();
        }
        
        objOutputStream.close();
    }

    public ArrayList<chart_display_data> readObject() throws ClassNotFoundException, IOException {
        ArrayList<chart_display_data> listChartData = new ArrayList();
         Create new ObjectInputStream object to read object from file 
        ObjectInputStream obj = new ObjectInputStream(fis);
        try {
            while (fis.available() != -1) {
                 Read object from file 
            	chart_display_data chart_data = (chart_display_data) obj.readObject();
            	listChartData.add(chart_data);
            }
        } catch (EOFException ex) {
            //ex.printStackTrace();
        }
        
        return listChartData;
    }*/
    
    public void writeObject(chart_display_data listChartData) throws IOException {
        /* Create ObjectOutputStream to write object */
        ObjectOutputStream objOutputStream = new ObjectOutputStream(fos);
        /* Write object to file */
      
        objOutputStream.writeObject(listChartData);
        objOutputStream.reset();
        objOutputStream.close();
    }

    public ArrayList<chart_display_data> readObject() throws ClassNotFoundException, IOException {
        ArrayList<chart_display_data> listChartData = new ArrayList();
        /* Create new ObjectInputStream object to read object from file */
        ObjectInputStream obj = new ObjectInputStream(fis);
        try {
            while (fis.available() != -1) {
                /* Read object from file */
            	chart_display_data chart_data = (chart_display_data) obj.readObject();
            	listChartData.add(chart_data);
            }
        } catch (EOFException ex) {
            //ex.printStackTrace();
        }
        
        return listChartData;
    }
}

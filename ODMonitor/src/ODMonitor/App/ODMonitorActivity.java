package ODMonitor.App;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chartdemo.demo.chart.AverageTemperatureChart;
import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.chartdemo.demo.chart.XYChartBuilder;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import ODMonitor.App.R.drawable;
import ODMonitor.App.data.android_accessory_packet;
import ODMonitor.App.data.chart_display_data;
import ODMonitor.App.file.file_operate_byte_array;
import ODMonitor.App.file.file_operation;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnKeyListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class ODMonitorActivity extends Activity{
	public String Tag = "ODMonitorActivity";
	private static final String ACTION_USB_PERMISSION = "FTDI.LED.USB_PERMISSION";
	public UsbManager usbmanager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor = null;
	public FileInputStream inputstream = null;
	public FileOutputStream outputstream = null;
	public boolean mPermissionRequestPending = true;
	
	public byte  ledPrevMap = 0x00;
	//public byte[] usbdataIN;
	public android_accessory_packet acc_pkg_transfer = new android_accessory_packet(android_accessory_packet.INIT_PREFIX_VALUE);
	public android_accessory_packet acc_pkg_receive = new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE);
	
	public SeekBar volumecontrol;
    public ProgressBar slider;
    
    public ImageButton button1; //Button led1;
    public ImageButton button2; //Button led2;
    public ImageButton button3; //Button led3;
    public ImageButton button4; //Button led4;
    public ImageButton button5; //Button led4;
    
    public ImageView led1;
    public ImageView led2;
    public ImageView led3;
    public ImageView led4;
    public ImageView ledvolume;
    
    public ImageView led_connect_status;
    
    public EditText etInput; //shaker command input
    public TextView shaker_return;
    public TextView debug_view;
    public int get_experiment_data_start = 0;
    public int script_length = 0;
    public int script_offset = 0;
    public byte[] script = null;
    private IDemoChart[] mCharts = new IDemoChart[] { new AverageTemperatureChart() };
    final Context context = this;
    
    /*thread to listen USB data*/
    public handler_thread handlerThread;
    public data_write_thread data_write_thread;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {     
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Thread.currentThread().setName("Thread_ODMonitorActivity");
        
        usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.d(Tag, "usbmanager" +usbmanager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        Log.d(Tag, "filter" +filter);
        registerReceiver(mUsbReceiver, filter);
        
        led1 = (ImageView) findViewById(R.id.LED1);
		led2 = (ImageView) findViewById(R.id.LED2);
		led3 = (ImageView) findViewById(R.id.LED3);
		led4 = (ImageView) findViewById(R.id.LED4);
		
		shaker_return = (TextView)findViewById(R.id.ShakerReturn);
		debug_view = (TextView)findViewById(R.id.DebugView);
		led_connect_status = (ImageView)findViewById(R.id.ConnectStatus);
		
		data_write_thread = new data_write_thread(handler);
		data_write_thread.start();
               
        button1 = (ImageButton) findViewById(R.id.Button1);
        button1.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v) 
        	{
        		byte ibutton = 0x01;
        		Log.d("EXPERIMENT", "START EXPERIMENT");
        		
        		ledPrevMap ^= 0x01;
        		
        		if((ledPrevMap & 0x01) == 0x01){
        				led1.setImageResource(drawable.image100);
        			}
        			else{
        				led1.setImageResource(drawable.image0);		
        		}
        		
        		file_operation write_file = new file_operation("Sensor", "sensor", true);
        		try {
        			write_file.delete_file(write_file.generate_filename());
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		byte[] data = new byte[1];
        		data[0] = android_accessory_packet.STATUS_EXPERIMENT_START;
        		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_STATUS, android_accessory_packet.STATUS_OK, data, 1);
        	}
		});
        
        button2 = (ImageButton) findViewById(R.id.Button2);
        button2.setOnClickListener(new View.OnClickListener()
        {		
			public void onClick(View v)
			{
        		byte ibutton = 0x02;
        		//v.bringToFront();
        		
        		ledPrevMap ^= 0x02;
        		
        		if((ledPrevMap & 0x02) == 0x02){
        				led2.setImageResource(drawable.image100);
        			}
        			else{
        				led2.setImageResource(drawable.image0);		
        		}
        		
        		file_operate_byte_array write_file = new file_operate_byte_array("GetExperimentData", "ExperimentData", true);
        		try {
        			write_file.delete_file(write_file.generate_filename());
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
        		byte[] data = new byte[1];
        		data[0] = 0;
        		get_experiment_data_start = 1;
        		WriteUsbCommand(android_accessory_packet.DATA_TYPE_GET_EXPERIMENT_DATA, android_accessory_packet.STATUS_OK, data, 0);
			}
		});
        
        button3 = (ImageButton) findViewById(R.id.Button3);
        button3.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
        		byte ibutton = 0x04;
        		//v.bringToFront();
        		
        		ledPrevMap ^= 0x04;
        		
        		if((ledPrevMap & 0x04) == 0x04){
        				led3.setImageResource(drawable.image100);
        			}
        			else{
        				led3.setImageResource(drawable.image0);		
        		}
        		
        		file_operate_byte_array read_file = new file_operate_byte_array("Script", "Script", true);
		    	try {
		    		script_length = read_file.open_read_file(read_file.generate_filename_no_date());
		    		
		    		if (script_length > 0) {
		    		    script = new byte[script_length];
		    		    read_file.read_file(script);
		    		    byte[] data = new byte[1];
		        		data[0] = ibutton;
		        		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_SCRIPT, android_accessory_packet.STATUS_START, data, 0);	
		        		Toast.makeText(ODMonitorActivity.this, "Set experiment script start", Toast.LENGTH_SHORT).show(); 
		    		} else {
		    			Toast.makeText(ODMonitorActivity.this, "Get script lenght < 0", Toast.LENGTH_SHORT).show(); 
		    			Log.d(Tag, "open script fail");
		    		}
				} catch (IOException e) {
					Toast.makeText(ODMonitorActivity.this, "read_file constructor fail", Toast.LENGTH_SHORT).show(); 
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
        button4 = (ImageButton) findViewById(R.id.Button4);
        button4.setOnClickListener(new View.OnClickListener()
        {
			public void onClick(View v)
			{
        		byte ibutton = 0x08;
        		//v.bringToFront();
        		ledPrevMap ^= 0x08;
        		
        		if((ledPrevMap & 0x08) == 0x08){
        				led4.setImageResource(drawable.image100);
        			}
        			else{
        				led4.setImageResource(drawable.image0);		
        		}
        		
        		//byte[] data = new byte[1];
        		//data[0] = ibutton;
        		//WriteUsbCommand(android_accessory_packet.DATA_TYPE_KEYPAD, android_accessory_packet.STATUS_OK, data, 1);
        		//reconnect_to_accessory();
        		show_chart_activity();
			}
		});  
        
        button5 = (ImageButton) findViewById(R.id.Button5);
        button5.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
        		show_script_activity();
			}
		});  
        
        etInput = (EditText)findViewById(R.id.etInput); 
        etInput.setOnKeyListener(new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) { 
            if(event.getAction() == KeyEvent.ACTION_DOWN && 
               keyCode == KeyEvent.KEYCODE_ENTER){ 
               Toast.makeText(ODMonitorActivity.this, etInput.getText(), Toast.LENGTH_SHORT).show(); 
               String cmd = etInput.getText().toString();  
               byte[] usb_cmd = cmd.getBytes();
               WriteUsbCommand(android_accessory_packet.DATA_TYPE_SEND_SHAKER_COMMAND, android_accessory_packet.STATUS_OK, usb_cmd, cmd.length());
               return true; 
            } 
           return false; 
         } 
         }); 
       
 /*
        volumecontrol = (SeekBar)findViewById(R.id.seekBar1);
  
        //set the max value to 50
        volumecontrol.setMax(50);
        
        volumecontrol.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() 
        {		
			public void onStopTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
				
			}
			
			public void onStartTrackingTouch(SeekBar seekBar)
			{
				// TODO Auto-generated method stub
				
			}
			
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) 
			{
				int len = 1;
				byte[] data = new byte[1];
				data[0] = (byte)progress;
				
				acc_pkg_transfer.copy_to_data(data, len);
				acc_pkg_transfer.set_Type((byte)1);
				acc_pkg_transfer.set_Len((byte)1);
				
				try{
					if(outputstream != null){
						outputstream.write(acc_pkg_transfer.buffer, 0,  len + acc_pkg_transfer.get_header_size());
					}
				}
				catch (IOException e) {}		
				
				ledvolume = (ImageView) findViewById(R.id.LEDvolume);
				if(progress == 0)
				{
					ledvolume.setImageResource(drawable.image0);
				}
				else if(progress > 0 && (int)progress < 11)
				{
					ledvolume.setImageResource(drawable.image10);
				}
				else if (progress > 10 && progress < 21)
				{
					ledvolume.setImageResource(drawable.image20);
				}
				else if (progress > 20 && progress < 36)
				{
					ledvolume.setImageResource(drawable.image35);
				}
				else if (progress > 35 && progress < 51)
				{
					ledvolume.setImageResource(drawable.image50);
				}
				else if (progress > 50 && progress < 66)
				{
					ledvolume.setImageResource(drawable.image65);
				}
				else if (progress > 65 && progress < 76)
				{
					ledvolume.setImageResource(drawable.image75);
				}
				else if (progress > 75 && progress < 91)
				{
					ledvolume.setImageResource(drawable.image90);
				}
				else
				{
					ledvolume.setImageResource(drawable.image100);
				}
			}
        });
        */
    }
    
    public void show_chart_activity() {
    /*  final Dialog dialog = new Dialog(context);
 	    dialog.setContentView(R.layout.xy_layout);
 	    dialog.setTitle("Title...");
 	   
 	    Button dialogButton = (Button) dialog.findViewById(R.id.toggleButton1);
 		// if button is clicked, close the custom dialog
 	    dialogButton.setOnClickListener(new OnClickListener() {
 	        public void onClick(View v) {
 				dialog.dismiss();
 			}
 		});
 	    

 		dialog.show();*/
    	Intent intent = null;
    	//intent = mCharts[0].execute(this);
    	intent = new Intent(this, XYChartBuilder.class);
    	
    	startActivity(intent);
    }
    
    public void show_script_activity() {
        	Intent intent = null;
        	intent = new Intent(this, script_activity_list.class);
        	startActivity(intent);
    }
    
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)) {   //確定按下退出鍵
            ConfirmExit();
            return true;  
        }  
        
        return super.onKeyDown(keyCode, event);  
    }

  

    public void ConfirmExit(){
        AlertDialog.Builder ad=new AlertDialog.Builder(ODMonitorActivity.this); //創建訊息方塊
        ad.setTitle("EXIT");
        ad.setMessage("Are you sure want to exit?");
        ad.setPositiveButton("Yes", new DialogInterface.OnClickListener() { //按"是",則退出應用程式
            public void onClick(DialogInterface dialog, int i) {
            	CloseAccessory();
            	//LEDActivity.this.finish();//關閉activity
            }
        });
        
        ad.setNegativeButton("No",new DialogInterface.OnClickListener() { //按"否",則不執行任何操作
            public void onClick(DialogInterface dialog, int i) {

            }
        });

        ad.show();//顯示訊息視窗
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.setting_menu, menu);
		return true;
	}
    
    public void reconnect_to_accessory() {
    	Intent intent = getIntent();
    	
		if (inputstream != null && outputstream != null) {
			return;
		}
		
		UsbAccessory[] accessories = usbmanager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (usbmanager.hasPermission(accessory)) {
				OpenAccessory(accessory);
			} 
			else
			{
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						usbmanager.requestPermission(accessory,
								mPermissionIntent);
						mPermissionRequestPending = true;
					}
			}
		}
		} else {}
    }
        
        
    @Override
    public void onResume() {
    		super.onResume();
    		Log.d(Tag, "on Resume");
    		reconnect_to_accessory();
    }
    
    @Override
    public void onPause() {
    	Log.d(Tag, "on Pause");
    	super.onPause();
    }
    
	@Override
	public void onDestroy() {
		//unregisterReceiver(mUsbReceiver);
		//CloseAccessory();
		Log.d(Tag, "on Destory");
		super.onDestroy();
	}
	
	
	/*open the accessory*/
	private void OpenAccessory(UsbAccessory accessory) {
		filedescriptor = usbmanager.openAccessory(accessory);
		if(filedescriptor != null){
			usbaccessory = accessory;
			FileDescriptor fd = filedescriptor.getFileDescriptor();
			inputstream = new FileInputStream(fd);
			outputstream = new FileOutputStream(fd);
			/*check if any of them are null*/
			if(inputstream == null || outputstream==null){
				return;
			}
		}
		
		handlerThread = new handler_thread(handler, inputstream);
		handlerThread.start();
		led_connect_status.setImageResource(drawable.image100);
		
	} /*end OpenAccessory*/
	
	public void KeypadLed_Receive(byte[] data)
	{
		/*
		led1 = (ImageView) findViewById(R.id.LED1);
		led2 = (ImageView) findViewById(R.id.LED2);
		led3 = (ImageView) findViewById(R.id.LED3);
		led4 = (ImageView) findViewById(R.id.LED4);
		*/
		ledPrevMap ^= data[3];
		data[3] = ledPrevMap;
		
		if((data[3]& 0x01) == 0x01)
		{
			led1.setImageResource(drawable.image100);
		}
		else{
			led1.setImageResource(drawable.image0);		
		}
		
		if((data[3]& 0x02) == 0x02){
			led2.setImageResource(drawable.image100);
		}else{
			led2.setImageResource(drawable.image0);
		}
		
		if((data[3]& 0x04) == 0x04){
			led3.setImageResource(drawable.image100);
		}else{
			led3.setImageResource(drawable.image0);
		}
		
		if((data[3]& 0x08) == 0x08){
			led4.setImageResource(drawable.image100);
		}else{
			led4.setImageResource(drawable.image0);
		}
	}
	
	/*public void Slider_Receive(byte[] data) {
		ledvolume = (ImageView) findViewById(R.id.LEDvolume);
		if((int)data[3] == 0)
		{
			ledvolume.setImageResource(drawable.image0);
		}
		else if((int)data[3] > 0 && (int)data[3] < 11)
		{
			ledvolume.setImageResource(drawable.image10);
		}
		else if ((int)data[3] > 10 && (int)data[3] < 21)
		{
			ledvolume.setImageResource(drawable.image20);
		}
		else if ((int)data[3] > 20 && (int)data[3] < 36)
		{
			ledvolume.setImageResource(drawable.image35);
		}
		else if ((int)data[3] > 35 && (int)data[3] < 51)
		{
			ledvolume.setImageResource(drawable.image50);
		}
		else if ((int)data[3] > 50 && (int)data[3] < 66)
		{
			ledvolume.setImageResource(drawable.image65);
		}
		else if ((int)data[3] > 65 && (int)data[3] < 76)
		{
			ledvolume.setImageResource(drawable.image75);
		}
		else if ((int)data[3] > 75 && (int)data[3] < 91)
		{
			ledvolume.setImageResource(drawable.image90);
		}
		else
		{
			ledvolume.setImageResource(drawable.image100);
		}
	}*/
	
	public void SensorData_Receive(android_accessory_packet rec) {
		file_operation write_file = new file_operation("Sensor", "sensor", true);
		byte[] byte_str = new byte[rec.get_Len_value()];
    	System.arraycopy(rec.buffer, rec.DATA_START, byte_str, 0, rec.get_Len_value());
    	String str = new String(byte_str);
    	shaker_return.setText(str);
    	try {
    		write_file.create_file(write_file.generate_filename());
    		write_file.write_file(str);
    		write_file.flush_close_file();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void CloseAccessory() {
		try{
			if (filedescriptor != null)
			    filedescriptor.close();
		}catch (IOException e){}
		
		try {
			if (inputstream != null)
			    inputstream.close();
		} catch(IOException e){}
		
		try {
			if (outputstream != null)
			    outputstream.close();
		} catch(IOException e){}
		
		/*FIXME, add the notfication also to close the application*/
		unregisterReceiver(mUsbReceiver);
		//CloseAccessory();
		//super.onDestroy();
		filedescriptor = null;
		inputstream = null;
		outputstream = null;
		led_connect_status.setImageResource(drawable.image0);
		
		System.exit(0);
	}
		
	
	final Handler handler =  new Handler() {
    	@Override 
    	public void handleMessage(Message msg) {	
    		Bundle b = msg.getData();
    		android_accessory_packet handle_receive_data = new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE);
    		byte[] recv = b.getByteArray(android_accessory_packet.key_receive);
    		
    		handle_receive_data.copy_to_buffer(recv, handle_receive_data.get_size());
    		
    		switch (handle_receive_data.get_Type_value()) {
    		    case android_accessory_packet.DATA_TYPE_KEYPAD:
		    	    KeypadLed_Receive(handle_receive_data.buffer);
    		    break;
    		    
    		    case android_accessory_packet.DATA_TYPE_GET_MACHINE_STATUS:
    		    	//Slider_Receive(handle_receive_data.buffer);
        		break;
        		
    		    case android_accessory_packet.DATA_TYPE_SEND_SHAKER_COMMAND:
    		    	
        		break;
    		
    		    case android_accessory_packet.DATA_TYPE_GET_SHAKER_RETURN:
    		    	byte[] byte_str = new byte[handle_receive_data.get_data_size()];
    		    	System.arraycopy(handle_receive_data.buffer, handle_receive_data.DATA_START, byte_str, 0, handle_receive_data.get_data_size());
    		    	String str = new String(byte_str);
    		    	
    		    	shaker_return.setText(str);
    			break;
    			
    		    case android_accessory_packet.DATA_TYPE_GET_EXPERIMENT_DATA:
    		    	if (1 == get_experiment_data_start) {
    		    	    int len = handle_receive_data.get_Len_value();
    		    		byte[] experiment_data = new byte[len];
        		    	System.arraycopy(handle_receive_data.buffer, handle_receive_data.DATA_START, experiment_data, 0, len); 
        		    	//String debug_str;
        		    	//debug_str = String.format("prefix:%d, type:%d, status:%d, len:%d", handle_receive_data.get_Prefix_value(),
        		    	//   		handle_receive_data.get_Type_value(), handle_receive_data.get_Status_value(), handle_receive_data.get_Len_value());
        		    	//debug_view.setText(debug_str);
        		    	//String experiment_str = new String(experiment_data);
        		    	file_operate_byte_array write_file = new file_operate_byte_array("GetExperimentData", "ExperimentData", true);
        		    	try {
        		            write_file.create_file(write_file.generate_filename());
        		    		write_file.write_file(experiment_data);
        		    		write_file.flush_close_file();
        				} catch (IOException e) {
        					// TODO Auto-generated catch block
        					e.printStackTrace();
        				}
        		    	
        		    	if (android_accessory_packet.STATUS_OK == handle_receive_data.get_Status_value()) {
        		    		get_experiment_data_start = 0;
        		    		Toast.makeText(ODMonitorActivity.this, "Get experiment data complete", Toast.LENGTH_SHORT).show(); 
        		    	}
    		    	
    		    	}		    	
        		break;
        		
    		    case android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_SCRIPT:
    		    	if (script_offset < script_length) {
    		    		int len = 0;
    		    		byte status = android_accessory_packet.STATUS_HAVE_DATA;
    		    		byte[] script_buffer;
    		    		if ((script_length-script_offset) > android_accessory_packet.DATA_SIZE)
    		    			len = android_accessory_packet.DATA_SIZE;
    		    		else
    		    			len = script_length-script_offset;
    		    		
    		    		script_buffer = new byte[len];
    		    		System.arraycopy(script, script_offset, script_buffer, 0, len);
    		    		if ((script_offset+len) == script_length)
    		    			status = android_accessory_packet.STATUS_OK;
    		    		else
    		    			status = android_accessory_packet.STATUS_HAVE_DATA;
    		    		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_SCRIPT, android_accessory_packet.STATUS_HAVE_DATA, script_buffer, len);
    		    		script_offset += len;
    		    	} else {
    		    		script_offset = 0;
    		    		script_length = 0;
    		    		script = null;
    		    		Toast.makeText(ODMonitorActivity.this, "Set experiment script end", Toast.LENGTH_SHORT).show(); 
    		    	}
            	break;
    			
    		    case android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_STATUS:
    		    	Log.d("EXPERIMENT", "RECEIVE SET EXPERIMENT STATUS");
                break;
                
    		    case android_accessory_packet.DATA_TYPE_NOTIFY_EXPERIMENT_DATA:
    		    	SensorData_Receive(handle_receive_data);
        		break;			
    		}
    	}
    };
  
	
	private class handler_thread  extends Thread {
		Handler mHandler;
		FileInputStream instream;
		
		handler_thread(Handler h,FileInputStream stream ){
			mHandler = h;
			instream = stream;
		}
		
		public void run() {
			Thread.currentThread().setName("Thread_AOA");
			int readcount;
		    android_accessory_packet[] receive_data = new android_accessory_packet[]{
		    		new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE),
		    		new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE),
		    		new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE),
		    };
			Bundle b = new Bundle(3);
			int read_offset = 0;
			int receive_data_index = 0;
			
			while(true) {
				Message msg = mHandler.obtainMessage();
				try {
					if (instream != null) {	
					    readcount = instream.read(receive_data[receive_data_index].buffer, read_offset, receive_data[receive_data_index].get_size());
					    if (readcount > 0) {
					        read_offset += readcount;
					        if (android_accessory_packet.PREFIX_VALUE == receive_data[receive_data_index].get_Prefix_value()) {
					        	if (read_offset >= android_accessory_packet.HEADER_SIZE) {
					        		/* prefix match , set read data length */
					        	    if ((android_accessory_packet.HEADER_SIZE+receive_data[receive_data_index].get_Len_value()) <= read_offset) {
									    read_offset = 0;
									    b.putByteArray(android_accessory_packet.key_receive, receive_data[receive_data_index].buffer);
									    msg.setData(b);
									    mHandler.sendMessage(msg);
									    if (receive_data_index < 2)
									        receive_data_index++;
									    else
									        receive_data_index = 0;
					        	    }
					            }
					        } else {
					        	/* prefix value is not correct, throw this data */
					            read_offset = 0;
					        }
					    }
					}
				} catch (IOException e){}
			}
		}
	}
	
	
	private class data_write_thread  extends Thread {
		Handler mHandler;
		
		data_write_thread(Handler h){
			mHandler = h;
		}
		
		public void run() {
			long index = 0;
			double concentration = 0;
	
			file_operate_byte_array write_file = new file_operate_byte_array("testExperimentData", "testExperimentData", true);
    		try {
    			write_file.delete_file(write_file.generate_filename_no_date());
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			e.printStackTrace();
    		}
			
			while(true) {
				try {
					chart_display_data chart_data = new chart_display_data(index, concentration);
		            write_file.create_file(write_file.generate_filename_no_date());
		            write_file.write_file(chart_data.get_object_buffer());
		            write_file.flush_close_file();
		        } catch (IOException ex) {
		     
		        }
				
				index++;
				concentration = (double)(Math.random()*50);
	    	    Log.d(Tag, "data_write_thread");
	    	    
	    	    try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
	
	public void WriteUsbCommand(byte type, byte status, byte[] data, int len){	
		
		acc_pkg_transfer.set_Type(type);
		acc_pkg_transfer.set_Status(status);
		acc_pkg_transfer.copy_to_data(data, len);
		acc_pkg_transfer.set_Len((byte)len);
		
		//Log.d("LED", "pressed " +iButton);
		/*switch(cmd) {
		    case  android_accessory_packet.DATA_TYPE_KEYPAD:
		    	acc_pkg_transfer.copy_to_data(data, len);
		    	break;
		    	
		    case android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_STATUS:
		    	break;
		    	
		    case android_accessory_packet.DATA_TYPE_GET_EXPERIMENT_DATA:
		    	break;
		    	
		    case android_accessory_packet.DATA_TYPE_SEND_SHAKER_COMMAND:
		    	break;
		}*/
		
		try{
			if(outputstream != null){
				outputstream.write(acc_pkg_transfer.buffer, 0,  len + acc_pkg_transfer.get_header_size());
			}
		}
		catch (IOException e) {}		
	}
   
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) {
				synchronized (this) {
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
						OpenAccessory(accessory);
					} else {
						Log.d(Tag, "permission denied for accessory "+ accessory);
						
					}
					mPermissionRequestPending = false;
				}
			} else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) {
				UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null )//&& accessory.equals(usbaccessory))
				{
					CloseAccessory();
				}
			} else {
				Log.d(Tag, "....");
			}
		}	
	};
};




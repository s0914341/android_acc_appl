package ODMonitor.App;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.chartdemo.demo.chart.AverageTemperatureChart;
import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.chartdemo.demo.chart.ODChartBuilder;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import ODMonitor.App.R.drawable;
import ODMonitor.App.data.android_accessory_packet;
import ODMonitor.App.data.chart_display_data;
import ODMonitor.App.data.experiment_script_data;
import ODMonitor.App.data.machine_information;
import ODMonitor.App.data.sync_data;
import ODMonitor.App.file.file_operate_byte_array;
import ODMonitor.App.file.file_operation;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
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
import android.text.Html;
import android.text.method.LinkMovementMethod;
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
    public ImageButton button5;
    public ImageButton button6;
    
    public ImageView ledvolume;
    
    public ImageView connect_status;
    public ImageView mass_storage_status;
    public ImageView sensor_status;
    public ImageView shaker_status;
    
    public EditText etInput; //shaker command input
    public TextView shaker_return;
    public TextView debug_view;
    public int get_experiment_data_start = 0;
    public long script_length = 0;
    public int script_offset = 0;
    public byte[] script = null;
    private IDemoChart[] mCharts = new IDemoChart[] { new AverageTemperatureChart() };
    final Context context = this;
    
    /*thread to listen USB data*/
    public aoa_thread handlerThread;
    public data_write_thread data_write_thread;
    public Object sync_object;
    
    public TextView textView2;
    public ProgressDialog mypDialog;
    public sync_data sync_get_experiment;
    public sync_data sync_chart_notify;
    private boolean aoa_thread_run = false;
    
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {     
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.od_monitor);
        Thread.currentThread().setName("Thread_ODMonitorActivity");
        
        usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.d(Tag, "usbmanager" +usbmanager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
      //  filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        Log.d(Tag, "filter" +filter);
        registerReceiver(mUsbReceiver, filter);
		
		shaker_return = (TextView)findViewById(R.id.ShakerReturn);
		debug_view = (TextView)findViewById(R.id.DebugView);
		connect_status = (ImageView)findViewById(R.id.ConnectStatus);
		connect_status.setEnabled(false);
		mass_storage_status = (ImageView)findViewById(R.id.MassStorageStatus);
		mass_storage_status.setEnabled(false);
		sensor_status = (ImageView)findViewById(R.id.SensorStatus);
		sensor_status.setEnabled(false);
		shaker_status = (ImageView)findViewById(R.id.ShakerStatus);
		shaker_status.setEnabled(false);
		//data_write_thread = new data_write_thread(handler);
		//data_write_thread.start();
	//	textView2 = (TextView) findViewById(R.id.test);
	//	textView2.setText( Html.fromHtml("<a href=\"http://www.maestrogen.com/ftp/i-track/user_manual.html\">iTrack User Manual</a>") );
	//	textView2.setMovementMethod(LinkMovementMethod.getInstance());
		
		mypDialog = new ProgressDialog(this);
		mypDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
		mypDialog.setTitle("OD Monitor");
		mypDialog.setMessage("Get device information!");
		//mypDialog.setIcon(R.drawable.android);
		//mypDialog.setButton("Google",this);
		mypDialog.setIndeterminate(true);
		mypDialog.setCancelable(false);
		sync_object = new Object();
		sync_chart_notify = new sync_data();
		ODMonitor_Application app_data = ((ODMonitor_Application)this.getApplication());
		app_data.set_sync_chart_notify(sync_chart_notify);
               
        button1 = (ImageButton) findViewById(R.id.Button1);
        button1.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		start_experiment();
        	}
		});
        
        button2 = (ImageButton) findViewById(R.id.Button2);
        button2.setOnClickListener(new View.OnClickListener() {		
			public void onClick(View v) {
				new Thread(new get_experiment_task()).start();
			}
		});
        
        button3 = (ImageButton) findViewById(R.id.Button3);
        button3.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
        		file_operate_byte_array read_file = new file_operate_byte_array("ExperimentScript", "ExperimentScript", true);
		    	try {
		    		script_length = read_file.open_read_file(read_file.generate_filename_no_date());
		    		
		    		if (script_length > 0) {
		    		    script = new byte[(int)script_length];
		    		    read_file.read_file(script);
		    		    byte[] data = new byte[1];
		        		data[0] = 0;
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
			public void onClick(View v) {
        		show_chart_activity();
			}
		});  
        
        button5 = (ImageButton) findViewById(R.id.Button5);
        button5.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
        		show_script_activity();
			}
		});  
        
        button6 = (ImageButton) findViewById(R.id.Button6);
        button6.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				set_tablet_on_off_line((byte)0, false);
				//SensorDataReceive();
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
        
        Log.d ( Tag, "intent get action: " +this.getIntent().getAction());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {

        super.onNewIntent(intent);

    	setIntent(intent);//must store the new intent unless getIntent() will return the old one

    	  //processExtraData();
    	
    /*	usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.d(Tag, "usbmanager" +usbmanager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        //filter.addAction(UsbManager.ACTION_USB_ACCESSORY_ATTACHED);
        Log.d(Tag, "filter" +filter);
        registerReceiver(mUsbReceiver, filter);*/
    }
    
    public void start_experiment() {
    	file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", true);
		try {
			write_file.delete_file(write_file.generate_filename_no_date());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		byte[] data = new byte[9];
		data[0] = android_accessory_packet.STATUS_EXPERIMENT_START;
		// get current time data and write to file
		byte[] start_time_bytes = ByteBuffer.allocate(8).putLong(new Date().getTime()).array();
		try {
			write_file.create_file(write_file.generate_filename_no_date());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		write_file.write_file(start_time_bytes);
		write_file.flush_close_file();
		System.arraycopy(start_time_bytes, 0, data, 1, 8);
		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_STATUS, android_accessory_packet.STATUS_OK, data, 9);
    }
    
    public void get_experiment_data(boolean delete_file, boolean block) {
    	if (true == delete_file) {
    	    file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline", true);
		    try {
			    write_file.delete_file(write_file.generate_filename_no_date());
		    } catch (IOException e) {
			    // TODO Auto-generated catch block
			    e.printStackTrace();
		    }
    	}
    	
    	int address = 0;
    	do {
    		address += sync_get_experiment.get_meta_data();
    	    byte[] data = android_accessory_packet.set_file_struct(address);
		  //  get_experiment_data_start = 1;
		    WriteUsbCommand(android_accessory_packet.DATA_TYPE_GET_EXPERIMENT_DATA, android_accessory_packet.STATUS_START, data, android_accessory_packet.GET_FILE_STRUCT_SIZE);
		
		    if (false == block)
			    return;
		
		    synchronized (sync_get_experiment) {
		        try {
		    	    sync_get_experiment.wait();
			    } catch (InterruptedException e) {
				    // TODO Auto-generated catch block
				    e.printStackTrace();
			    }
		    }
    	} while (sync_data.STATUS_END != sync_get_experiment.get_status());
    }
    
    public void get_machine_information(boolean block) {
		byte[] data = new byte[1];
		data[0] = 0;
		WriteUsbCommand(android_accessory_packet.DATA_TYPE_GET_MACHINE_STATUS, android_accessory_packet.STATUS_OK, data, 0);
		
		if (false == block)
			return;
		
		synchronized (sync_object) {
		    try {
		    	sync_object.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    public void set_tablet_on_off_line(byte status, boolean block) {
		byte[] data = new byte[1];
		data[0] = status;
		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_TABLET_ON_OFF_LINE, android_accessory_packet.STATUS_OK, data, 1);
		
		if (false == block)
			return;
		
		synchronized (sync_object) {
		    try {
		    	sync_object.wait();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
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
    	intent = new Intent(this, ODChartBuilder.class);
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
            	unregisterReceiver(mUsbReceiver);
            	System.exit(0);
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
		if (inputstream != null && outputstream != null) {
			return;
		}
		
		UsbAccessory[] accessories = usbmanager.getAccessoryList();
		UsbAccessory accessory = (accessories == null ? null : accessories[0]);
		if (accessory != null) {
			if (usbmanager.hasPermission(accessory)) {
				Log.d ( Tag, "hasPermission");
				OpenAccessory(accessory);
			} else {
				synchronized (mUsbReceiver) {
					if (!mPermissionRequestPending) {
						usbmanager.requestPermission(accessory, mPermissionIntent);
						mPermissionRequestPending = true;
						Log.d ( Tag, "requestPermission");
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
    	//CloseAccessory();
    }
    
	@Override
	public void onDestroy() {
		/* when tablet rotate , it will exec onCreate again */
		CloseAccessory();
		unregisterReceiver(mUsbReceiver);
		Log.d(Tag, "on Destory");
		super.onDestroy();
	}
	
	class initial_task implements Runnable {
		public void run() {	
			get_experiment_data(true, true);
			get_machine_information(true);
			set_tablet_on_off_line((byte)1, true);
	    } 	
	}
	
	class get_experiment_task implements Runnable {
		public void run() {	
			get_experiment_data(true, true);
	    } 	
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
				Log.d(Tag, "inputstream null");
				return;
			}
		}
		
		handlerThread = new aoa_thread(handler, inputstream);
		aoa_thread_run = true;
		handlerThread.start();
		connect_status.setEnabled(true);
		mypDialog.show();
		sync_get_experiment = new sync_data();
		new Thread(new initial_task()).start();
		
	} /*end OpenAccessory*/
	
	public void SensorDataReceive(android_accessory_packet rec) {
		file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", true);
		file_operate_byte_array read_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", true);
		byte[] byte_str = new byte[rec.get_Len_value()];
    	System.arraycopy(rec.buffer, android_accessory_packet.DATA_START, byte_str, 0, rec.get_Len_value());
    	String str = new String(byte_str);
    	
    	try {
    		int[] data = null;
            data = OD_calculate.parse_raw_data(str);
            if (data != null) {
            	long size = read_file.open_read_file(read_file.generate_filename_no_date());
         	    if (size <= 0)
         	        return;
         	    
         	    if (size >= (4*OD_calculate.experiment_data_size)+8) {
         	        read_file.seek_read_file(size-(long)(4*OD_calculate.experiment_data_size)+4);
        	        byte[] final_current_raw_index_bytes = new byte[4];
        	        read_file.read_file(final_current_raw_index_bytes);
        	        ByteBuffer byte_buffer = ByteBuffer.wrap(final_current_raw_index_bytes, 0, 4);
                    byte_buffer = ByteBuffer.wrap(final_current_raw_index_bytes, 0, 4);
                    int final_current_raw_index = byte_buffer.getInt();
                    
                    if (final_current_raw_index != data[OD_calculate.pre_raw_index_index]) {
                        return;
    	            }
         	    } else {
         	    	if ((data[OD_calculate.current_raw_index_index] != 0) || (data[OD_calculate.pre_raw_index_index] != 0))
         	    		return;
         	    }
            	
            	try {
            		write_file.create_file(write_file.generate_filename_no_date());
            		ByteBuffer byteBuffer = ByteBuffer.allocate(data.length * 4);        
                    IntBuffer intBuffer = byteBuffer.asIntBuffer();
                    intBuffer.put(data);
                    byte[] data_bytes = byteBuffer.array();
            		write_file.write_file(data_bytes);
            		write_file.flush_close_file();
            		shaker_return.setText(str);
            		// notify ODChartBuilder object has new sensor to display
            		if (sync_chart_notify != null) {
                	    synchronized (sync_chart_notify) {
                	    	sync_chart_notify.notify();
                	    }
                	}
        		} catch (IOException e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        			return;
        		}
            }
         } catch (IOException e) {
 	        // TODO Auto-generated catch block
 	        e.printStackTrace();
         }
	}
	
	private void CloseAccessory() {
		try {
			if (filedescriptor != null)
			    filedescriptor.close();
		} catch (IOException e){}
		
		try {
			if (inputstream != null)
			    inputstream.close();
		} catch(IOException e){}
		
		try {
			if (outputstream != null)
			    outputstream.close();
		} catch(IOException e){}
		
		/*FIXME, add the notfication also to close the application*/
		//unregisterReceiver(mUsbReceiver);
		//CloseAccessory();
		//super.onDestroy();
		filedescriptor = null;
		inputstream = null;
		outputstream = null;
		connect_status.setEnabled(false);
		mass_storage_status.setEnabled(false);
		sensor_status.setEnabled(false);
		shaker_status.setEnabled(false);
		
		//System.exit(0);
	}
	
	public void refresh_machine_information_view(machine_information info) {
	    String str = String.format("time: %d\nversion: %c.%c%c%c\ncurrent instruction index: %d\nexperiment status: %s",
	    		info.get_experiment_time(), 
	    		info.get_version1(), info.get_version2(), info.get_version3(), info.get_version4(),
	    		info.get_current_instruction_index(),
	    		machine_information.EXPERIMENT_STATUS.get(info.get_experiment_status()));
    	debug_view.setText(str);
    	
    	if (info.get_sensor_status() == machine_information.STATUS_SENSOR_READY)
    		sensor_status.setEnabled(true);
    	else
    		sensor_status.setEnabled(false);
    	
    	if (info.get_mass_storage_status() == machine_information.STATUS_MASS_STORAGE_READY)
    		mass_storage_status.setEnabled(true);
    	else
    		mass_storage_status.setEnabled(false);
    	
    	if (info.get_shaker_status() == machine_information.STATUS_SHAKER_READY)
    		shaker_status.setEnabled(true);
    	else
    		shaker_status.setEnabled(false);
    	
    	if (sync_object != null) {
    	    synchronized (sync_object) {
    		    sync_object.notify();
    	    }
    	}
	}
	
	public void convert_string_to_byte_file() {
		file_operation read_file = new file_operation("od_sensor", "sensor_offline", true);
        try {
	        read_file.open_read_file(read_file.generate_filename_no_date());
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
        
        file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline_byte", false);
    	try {
    	//	write_file.delete_file(write_file.generate_filename_no_date());
            write_file.create_file(write_file.generate_filename_no_date());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        try {    	
	        String sensor_str = new String();
	        sensor_str = read_file.read_file();
	        if(sensor_str != null) {
	        	byte[] temp = OD_calculate.parse_date(sensor_str);
	        	write_file.write_file(temp);
	        }
	        
	        sensor_str = read_file.read_file();
	        while (sensor_str != null) {
				int[] data = null;
		        data = OD_calculate.parse_raw_data(sensor_str);
		        if (data != null) {
		        	for (int i = 0; i < data.length; i++) {
		        		ByteBuffer byteBuffer = ByteBuffer.allocate(4);
		        		byte[] bytes = byteBuffer.putInt(data[i]).array();
		        		write_file.write_file(bytes);
		        	}
		        } else {
		        	Log.e(Tag, "parse raw data fail");
		        }
		        
		        sensor_str = read_file.read_file();
			} 
	        
	        write_file.flush_close_file();
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
	}
		
	
	final Handler handler =  new Handler() {
    	@SuppressLint("DefaultLocale") @Override 
    	public void handleMessage(Message msg) {	
    		String view_str = msg.getData().getString("aoa thread exception", "no exception");
    		if (true == view_str.equals("aoa thread exception"))
			    debug_view.setText(view_str);
    		
    		Bundle b = msg.getData();
    		android_accessory_packet handle_receive_data = new android_accessory_packet(android_accessory_packet.NO_INIT_PREFIX_VALUE);
    		byte[] recv = b.getByteArray(android_accessory_packet.key_receive);
    		
    		Log.d(Tag, "Handler  id:"+Thread.currentThread().getId() + "process:" + android.os.Process.myTid());
    			
    		handle_receive_data.copy_to_buffer(recv, android_accessory_packet.get_size());
    		switch (handle_receive_data.get_Type_value()) {
    		    case android_accessory_packet.DATA_TYPE_GET_MACHINE_STATUS: {
    		    	machine_information info = new machine_information();
    		    	info.copy_to_buffer(handle_receive_data.get_data(0, machine_information.TOTAL_SIZE), machine_information.TOTAL_SIZE);
    		    	refresh_machine_information_view(info);
    		    } break;
        		
    		    case android_accessory_packet.DATA_TYPE_SEND_SHAKER_COMMAND:
    		    	
        		break;
    		
    		    case android_accessory_packet.DATA_TYPE_GET_SHAKER_RETURN: {
    		    	byte[] byte_str = new byte[android_accessory_packet.get_data_size()];
    		    	System.arraycopy(handle_receive_data.buffer, android_accessory_packet.DATA_START, byte_str, 0, android_accessory_packet.get_data_size());
    		    	String str = new String(byte_str);
    		    	
    		    	shaker_return.setText(str);
    		    } break;
    			
    		    case android_accessory_packet.DATA_TYPE_GET_EXPERIMENT_DATA:
    		    	//if (1 == get_experiment_data_start) {
    		    		if (android_accessory_packet.STATUS_FAIL == handle_receive_data.get_Status_value()) {
    		    		    get_experiment_data_start = 0;
        		    		Toast.makeText(ODMonitorActivity.this, "Get experiment data fail", Toast.LENGTH_SHORT).show(); 
        		    		if (sync_get_experiment != null) {
    		        	        synchronized (sync_get_experiment) {
    		        	        	sync_get_experiment.set_status(sync_data.STATUS_END);
    		        	        	sync_get_experiment.notify();
    		        	        }
    		        	    }
    		    		} else {
    		    	        int len = handle_receive_data.get_Len_value();
    		    		    byte[] experiment_data = new byte[len];
        		    	    System.arraycopy(handle_receive_data.buffer, handle_receive_data.DATA_START, experiment_data, 0, len); 
        		            //String debug_str;
        		    	    //debug_str = String.format("prefix:%d, type:%d, status:%d, len:%d", handle_receive_data.get_Prefix_value(),
        		    	    //   		handle_receive_data.get_Type_value(), handle_receive_data.get_Status_value(), handle_receive_data.get_Len_value());
        		    	    //debug_view.setText(debug_str);
        		    	    //String experiment_str = new String(experiment_data);
        		    	    file_operate_byte_array write_file = new file_operate_byte_array("od_sensor", "sensor_offline", true);
        		     	    try {
        		                write_file.create_file(write_file.generate_filename_no_date());
        		    		    write_file.write_file(experiment_data);
        		    		    write_file.flush_close_file();
        				    } catch (IOException e) {
        					    // TODO Auto-generated catch block
        					    e.printStackTrace();
        				    }
        		    	
        		    	    if (android_accessory_packet.STATUS_OK == handle_receive_data.get_Status_value()) {
        		    		    get_experiment_data_start = 0;
        		    		    convert_string_to_byte_file();
        		    		    Toast.makeText(ODMonitorActivity.this, "Get experiment data complete", Toast.LENGTH_SHORT).show(); 
        		    		    if (sync_get_experiment != null) {
        		        	        synchronized (sync_get_experiment) {
        		        	        	sync_get_experiment.set_status(sync_data.STATUS_END);
        		        	        	sync_get_experiment.notify();
        		        	        }
        		        	    }
        		    	    } else if (android_accessory_packet.STATUS_HAVE_DATA == handle_receive_data.get_Status_value()) {
        		    	    	if (sync_get_experiment != null) {
        		        	        synchronized (sync_get_experiment) {
        		        	        	sync_get_experiment.set_meta_data(len);
        		        	        	sync_get_experiment.set_status(sync_data.STATUS_CONTINUE);
        		        	        	sync_get_experiment.notify();
        		        	        }
        		        	    }	
        		    	    }
    		    	    }	
    		    //	}
        		break;
        		
    		    case android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_SCRIPT:
    		    	if (script_offset < script_length) {
    		    		long len = 0;
    		    		byte status = android_accessory_packet.STATUS_HAVE_DATA;
    		    		byte[] script_buffer;
    		    		if ((script_length-script_offset) > android_accessory_packet.DATA_SIZE)
    		    			len = android_accessory_packet.DATA_SIZE;
    		    		else
    		    			len = script_length-script_offset;
    		    		
    		    		script_buffer = new byte[(int)len];
    		    		System.arraycopy(script, script_offset, script_buffer, 0, (int)len);
    		    		if ((script_offset+len) == script_length)
    		    			status = android_accessory_packet.STATUS_OK;
    		    		else
    		    			status = android_accessory_packet.STATUS_HAVE_DATA;
    		    		WriteUsbCommand(android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_SCRIPT, status, script_buffer, (int)len);
    		    		script_offset += len;
    		    	} else {
    		    		script_offset = 0;
    		    		script_length = 0;
    		    		script = null;
    		    		Toast.makeText(ODMonitorActivity.this, "Set experiment script end", Toast.LENGTH_SHORT).show(); 
    		    	}
            	break;
    			
    		    case android_accessory_packet.DATA_TYPE_SET_EXPERIMENT_STATUS:
    		    	Log.d(Tag, "RECEIVE SET EXPERIMENT STATUS");
                break;
                
    		    case android_accessory_packet.DATA_TYPE_NOTIFY_EXPERIMENT_DATA:
    		    	SensorDataReceive(handle_receive_data);
        		break;	
        		
    		    case android_accessory_packet.DATA_TYPE_SET_TABLET_ON_OFF_LINE:
    		    	Log.d(Tag, "Set tablet on off line");
    		    	
    		    	if (sync_object != null) {
    		    	    synchronized (sync_object) {
    		    		    sync_object.notify();
    		    	    }
    		    	}
    		    	mypDialog.cancel();
        		break;	
    		}
    	}
    };
	
	private class aoa_thread  extends Thread {
		Handler mHandler;
		FileInputStream instream;
		
		aoa_thread(Handler h, FileInputStream stream ) {
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
			
			while(aoa_thread_run) {
				try {
					if (instream != null) {	
					    readcount = instream.read(receive_data[receive_data_index].buffer, read_offset, android_accessory_packet.get_size());
					    if (readcount > 0) {
					        read_offset += readcount;
					        if (android_accessory_packet.PREFIX_VALUE == receive_data[receive_data_index].get_Prefix_value()) {
					        	if (read_offset >= android_accessory_packet.HEADER_SIZE) {
					        		// prefix match , set read data length
					        	    if ((android_accessory_packet.HEADER_SIZE+receive_data[receive_data_index].get_Len_value()) <= read_offset) {
									    read_offset = 0;
									    Message msg = mHandler.obtainMessage();
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
					        	// prefix value is not correct, throw this data
					            read_offset = 0;
					        }
					    }
					}
				} catch (IOException e){
					aoa_thread_run = false;
					Message msg = mHandler.obtainMessage();
				    b.putString("aoa thread exception", "aoa thread exception");
				    msg.setData(b);
				    mHandler.sendMessage(msg);
				}
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
				outputstream.write(acc_pkg_transfer.buffer, 0,  len + android_accessory_packet.get_header_size());
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
						Log.d(Tag, "BroadcastReceiver open accessory "+ accessory);
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




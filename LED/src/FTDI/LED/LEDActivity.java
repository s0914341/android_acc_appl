package FTDI.LED;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import FTDI.LED.R.drawable;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbAccessory;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnKeyListener;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

public class LEDActivity extends Activity{
	
	private static final String ACTION_USB_PERMISSION =    "FTDI.LED.USB_PERMISSION";
	public UsbManager usbmanager;
	public UsbAccessory usbaccessory;
	public PendingIntent mPermissionIntent;
	public ParcelFileDescriptor filedescriptor;
	public FileInputStream inputstream;
	public FileOutputStream outputstream;
	public boolean mPermissionRequestPending = true;
	
	//public Handler usbhandler;
	//public byte[] usbdata;
	//public byte[] writeusbdata;
	//public byte[] writeusbcommand;
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
    
    public ImageView led1;
    public ImageView led2;
    public ImageView led3;
    public ImageView led4;
    public ImageView ledvolume;
    
    public EditText etInput; //shaker command input
    public TextView shaker_return;
    
    /*thread to listen USB data*/
    public handler_thread handlerThread;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {     
    	super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
       // usbdata = new byte[4]; 
       // writeusbdata = new byte[4];
      //  writeusbcommand = new byte[24];
        
        usbmanager = (UsbManager) getSystemService(Context.USB_SERVICE);
        Log.d("LED", "usbmanager" +usbmanager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_ACCESSORY_DETACHED);
        Log.d("LED", "filter" +filter);
        registerReceiver(mUsbReceiver, filter);
        
        led1 = (ImageView) findViewById(R.id.LED1);
		led2 = (ImageView) findViewById(R.id.LED2);
		led3 = (ImageView) findViewById(R.id.LED3);
		led4 = (ImageView) findViewById(R.id.LED4);
		
		shaker_return = (TextView)findViewById(R.id.ShakerReturn);
	//	str = new String("testtest");
               
        button1 = (ImageButton) findViewById(R.id.Button1);
        button1.setOnClickListener(new View.OnClickListener()
        {
        	public void onClick(View v) 
        	{
        		byte ibutton = 0x01;
        		Log.d("LED", "Button 1 pressed");
        		
        		ledPrevMap ^= 0x01;
        		
        		if((ledPrevMap & 0x01) == 0x01){
        				led1.setImageResource(drawable.image100);
        			}
        			else{
        				led1.setImageResource(drawable.image0);		
        		}
        			
        		
        		
        		//v.bringToFront();
        		WriteUsbData(ibutton);
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
        		
        		WriteUsbData(ibutton);	
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
        		WriteUsbData(ibutton);	
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
        		WriteUsbData(ibutton);	
			}
		});  
        
        etInput = (EditText)findViewById(R.id.etInput); 
        etInput.setOnKeyListener(new OnKeyListener() {
        public boolean onKey(View v, int keyCode, KeyEvent event) { 
            if(event.getAction() == KeyEvent.ACTION_DOWN && 
               keyCode == KeyEvent.KEYCODE_ENTER){ 
               Toast.makeText(LEDActivity.this, etInput.getText(), Toast.LENGTH_SHORT).show(); 
               String cmd = etInput.getText().toString();  
               byte[] usb_cmd = cmd.getBytes();
               //byte ibutton = 0x08;
               WriteUsbCommandShaker(usb_cmd, cmd.length());
               return true; 
            } 
           return false; 
         } 
         }); 
        
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
    }
        
        
    @Override
    public void onResume()
    {
    		super.onResume();
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
	public void onDestroy()
	{
		unregisterReceiver(mUsbReceiver);
		//CloseAccessory();
		super.onDestroy();
	}
	
	
	/*open the accessory*/
	private void OpenAccessory(UsbAccessory accessory)
	{
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
	
	public void Slider_Receive(byte[] data) {
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
	}
	
	public void SensorData_Receive(android_accessory_packet rec) {
		file_operation write_file = new file_operation("Sensor", "sensor", true);
		byte[] byte_str = new byte[rec.get_Len_value()];
    	System.arraycopy(rec.buffer, rec.DATA_START, byte_str, 0, rec.get_Len_value());
    	String str = new String(byte_str);
    	shaker_return.setText(str);
    	try {
    		write_file.create_file(write_file.generate_filename());
    		write_file.write_file_msg(str);
    		write_file.flush_close_file();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}
	
	private void CloseAccessory() {
		try{
			filedescriptor.close();
		}catch (IOException e){}
		
		try {
			inputstream.close();
		} catch(IOException e){}
		
		try {
			outputstream.close();
			
		}catch(IOException e){}
		/*FIXME, add the notfication also to close the application*/
		//unregisterReceiver(mUsbReceiver);
		//CloseAccessory();
		//super.onDestroy();
		filedescriptor = null;
		inputstream = null;
		outputstream = null;
	
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
    		    case android_accessory_packet.DATA_TYPE_SHAKER_RETURN:
    		    	byte[] byte_str = new byte[handle_receive_data.get_data_size()];
    		    	System.arraycopy(handle_receive_data.buffer, handle_receive_data.DATA_START, byte_str, 0, handle_receive_data.get_data_size());
    		    	String str = new String(byte_str);
    		   // 	String str = new String();
    		   // 	str = String.format("[0]=%d, [1]=%d, [2]=%d, [3]=%d, [4]=%d\n[5]=%d, [6]=%d, [7]=%d, [8]=%d, [9]=%d", recv[0], recv[1],
    		   // 			recv[2],recv[3],recv[4],recv[5],recv[6],recv[7],recv[8],recv[9]);
    		   // 	str.format("test123\r\n");
    		    	shaker_return.setText(str);
    			break;
    			
    		    case android_accessory_packet.DATA_TYPE_SENSOR_DATA:
    		    	SensorData_Receive(handle_receive_data);
        		break;
    			
    		    case android_accessory_packet.DATA_TYPE_SLIDER:
    		    	Slider_Receive(handle_receive_data.buffer);
        		break;
        		
    		    case android_accessory_packet.DATA_TYPE_KEYPAD:
    		    	KeypadLed_Receive(handle_receive_data.buffer);
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
	
	public void WriteUsbData(byte iButton){	
		int len = 1;
		byte[] data = new byte[1];
		data[0] = iButton;
		
		acc_pkg_transfer.copy_to_data(data, len);
		acc_pkg_transfer.set_Type((byte)0);
		acc_pkg_transfer.set_Len((byte)1);
		
		Log.d("LED", "pressed " +iButton);
		
		try{
			if(outputstream != null){
				outputstream.write(acc_pkg_transfer.buffer, 0,  len + acc_pkg_transfer.get_header_size());
			}
		}
		catch (IOException e) {}		
	}
	
	public void WriteUsbCommandShaker(byte[] cmd, int len){	
		acc_pkg_transfer.copy_to_data(cmd, len);
		acc_pkg_transfer.set_Type((byte)2);
		acc_pkg_transfer.set_Len((byte)len);
		
		//Log.d("Shaker", "pressed " +iButton);

		try{
			if(outputstream != null){
				outputstream.write(acc_pkg_transfer.buffer, 0, len + acc_pkg_transfer.get_header_size());
			}
		}
		catch (IOException e) {}		
	}
   
    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() 
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			String action = intent.getAction();
			if (ACTION_USB_PERMISSION.equals(action)) 
			{
				synchronized (this)
				{
					UsbAccessory accessory = (UsbAccessory) intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
					if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false))
					{
						OpenAccessory(accessory);
					} 
					else 
					{
						Log.d("LED", "permission denied for accessory "+ accessory);
						
					}
					mPermissionRequestPending = false;
				}
			} 
			else if (UsbManager.ACTION_USB_ACCESSORY_DETACHED.equals(action)) 
			{
				UsbAccessory accessory = (UsbAccessory)intent.getParcelableExtra(UsbManager.EXTRA_ACCESSORY);
				if (accessory != null )//&& accessory.equals(usbaccessory))
				{
					CloseAccessory();
				}
			}else
			{
				Log.d("LED", "....");
			}
		}	
	};
};




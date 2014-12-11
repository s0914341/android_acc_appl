package ODMonitor.App;

import java.nio.ByteBuffer;

import ODMonitor.App.data.experiment_script_data;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

public class script_setting_activity extends Activity {
	public String Tag = "script_setting_activity";
	public Button button_ok;
	public EditText editText_repeat_count;
	public EditText editText_repeat_time;
	public EditText editText_shaker_temperature;
	public EditText editText_shaker_speed;
	public Spinner spinner_instruct;
	public Spinner spinner_repeat_from;
	experiment_script_data item_data;
	public int total_item = 0;
	public long item_id = 0;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.script_setting);
	    
	    Intent intent = getIntent(); 
	    item_data = (experiment_script_data)intent.getSerializableExtra("send_experiment_script_data");
	    total_item = intent.getIntExtra("send_total_item", 0);
	    item_id = intent.getLongExtra("send_item_id", 0);
	    
	    editText_repeat_count = (EditText) findViewById(R.id.editText_repeat_count);
	    editText_repeat_time = (EditText) findViewById(R.id.editText_repeat_time);
	    editText_shaker_temperature = (EditText) findViewById(R.id.editText_shaker_temperature);
	    editText_shaker_speed = (EditText) findViewById(R.id.editText_shaker_speed);
	    
	    spinner_repeat_from = (Spinner)findViewById(R.id.spinner_repeat_from);
	    ArrayAdapter<String> spinner_repeat_from_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
	    spinner_repeat_from_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner_repeat_from.setAdapter(spinner_repeat_from_Adapter);
	    
	    for (int i = 0; i < total_item; i++) {
	    	String str;
	    	str = String.format("%d", i);
	    	spinner_repeat_from_Adapter.add(str);
	    	spinner_repeat_from_Adapter.notifyDataSetChanged();
	    }
	    spinner_repeat_from.setSelection(item_data.get_repeat_from_value());
	    
	    button_ok = (Button) findViewById(R.id.button_ok);
	    button_ok.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			save_experiment_script();
        			finish();
                } catch (NullPointerException e) {
                    Log.i(Tag, "script setting ok button exception");
                }
        	}
		});
	    
	    spinner_instruct = (Spinner)findViewById(R.id.spinner_instruct);
	    ArrayAdapter<String> spinner_instruct_Adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
	    spinner_instruct_Adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner_instruct.setAdapter(spinner_instruct_Adapter);
	    
	    int size = experiment_script_data.SCRIPT_INSTRUCT.size();
	    for (int i = 0; i < size; i++) {
	    	spinner_instruct_Adapter.add(experiment_script_data.SCRIPT_INSTRUCT.get(i));
	    	spinner_instruct_Adapter.notifyDataSetChanged();
	    }
	    spinner_instruct.setSelection(item_data.get_instruct_value());
	    
	    spinner_instruct.setOnItemSelectedListener(new OnItemSelectedListener() { 
	        public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
	        	Log.i(Tag, "select item: "+ id);
	        
	        	spinner_repeat_from.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get((int)id).get(0));
	        	editText_repeat_count.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get((int)id).get(1));
	        	editText_repeat_time.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get((int)id).get(2));
	        	editText_shaker_temperature.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get((int)id).get(3));
	        	editText_shaker_speed.setEnabled(experiment_script_data.SCRIPT_SETTING_ENABLE_LIST.get((int)id).get(4));
	        }

	        public void onNothingSelected(AdapterView<?> parentView) {
	            // your code here
	        }
	    });
	}
	
	public void save_experiment_script() {	
		item_data.set_instruct_value((byte)(0xff&spinner_instruct.getSelectedItemPosition()));
		item_data.set_repeat_from_value((byte)(0xff&spinner_repeat_from.getSelectedItemPosition()));
		item_data.set_repeat_count_value(Byte.parseByte(editText_repeat_count.getText().toString()));
		item_data.set_repeat_time_value(Byte.parseByte(editText_repeat_time.getText().toString()));
		item_data.set_shaker_temperature_value(Integer.parseInt(editText_shaker_temperature.getText().toString()));
		item_data.set_shaker_speed_value(Integer.parseInt(editText_shaker_temperature.getText().toString()));
		
		Intent intent = new Intent();
		intent.putExtra("return_experiment_script_data", item_data); //value should be your string from the edittext
		intent.putExtra("return_item_id", item_id); //value should be your string from the edittext
		setResult(RESULT_OK, intent); //The data you want to send back
		Log.d(Tag, "save_experiment_script = " + item_id);
	}
}

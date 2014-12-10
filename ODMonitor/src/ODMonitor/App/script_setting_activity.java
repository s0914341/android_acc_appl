package ODMonitor.App;

import ODMonitor.App.data.experiment_script_data;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

public class script_setting_activity extends Activity {
	public String Tag = "script_setting_activity";
	public Button button_ok;
	public Spinner spinner_instruct;
	experiment_script_data item_data;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.script_setting);
	    
	    spinner_instruct = (Spinner)findViewById(R.id.spinner_instruct);
	    ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, android.R.id.text1);
	    spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
	    spinner_instruct.setAdapter(spinnerAdapter);
	    
	    int size = experiment_script_data.SCRIPT_INSTRUCT.size();
	    for (int i = 0; i < size; i++) {
	        spinnerAdapter.add(experiment_script_data.SCRIPT_INSTRUCT.get(i));
	        spinnerAdapter.notifyDataSetChanged();
	    }
	    
	    button_ok = (Button) findViewById(R.id.button_ok);
	    button_ok.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			finish();
                } catch (NullPointerException e) {
                    Log.i(Tag, "script setting ok button exception");
                }
        	}
		});
	    
	    Intent intent = getIntent(); 
	    item_data = (experiment_script_data)intent.getSerializableExtra("experiment_script_data");
	}
}

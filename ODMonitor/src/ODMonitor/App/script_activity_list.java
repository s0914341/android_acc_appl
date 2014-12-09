package ODMonitor.App;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.chartdemo.demo.chart.XYChartBuilder;

import ODMonitor.App.R.drawable;
import ODMonitor.App.data.experiment_script_data;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;

public class script_activity_list extends Activity {
	public String Tag = "script_activity_list";
	private final static String key_picture = "picture";
	private final static String key_index = "index"; 
	private final static String key_instruction = "instruction"; 
	private final static String key_repeat_from = "repeat_from"; 
	private final static String key_repeat_count = "repeat_count"; 
	private final static String key_repeat_time = "repeat_time";
	private final static String key_shaker_argument = "shaker_argument";
    private String[] mMenuText;
	private String[] mMenuSummary;
	ArrayList<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
	private SimpleAdapter adapter;
	ListView list_view;
	Button button_add_item;
	
	private static final int[] mPics=new int[]{
        R.drawable.image50,R.drawable.image35,R.drawable.image20, R.drawable.image100,R.drawable.image50,
        R.drawable.image50,R.drawable.image50,R.drawable.image50, R.drawable.image50,R.drawable.image50,
        R.drawable.image50,R.drawable.image50,R.drawable.image50, R.drawable.image50,R.drawable.image50,
        R.drawable.image50,R.drawable.image50,R.drawable.image50, R.drawable.image50,R.drawable.image50,
        R.drawable.image50,R.drawable.image50,R.drawable.image50, R.drawable.image50,R.drawable.image50,
        R.drawable.image50,R.drawable.image50,R.drawable.image50, R.drawable.image50,R.drawable.image50
    };
	
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	  //把資料加入ArrayList中
	    setContentView(R.layout.script_layout);
	    
	    list_view = (ListView) findViewById(R.id.listView1);
	    
	    for (int i = 0; i < 5; i++) {
	        HashMap<String, Object> item = new HashMap<String,Object>();
	        String str_index;
            str_index = String.format("%d", list.size());
	        item.put(key_picture, mPics[i]);
	        item.put(key_index, str_index);
	        item.put(key_instruction, experiment_script_data.SCRIPT_INSTRUCT.get(0));
	        item.put(key_repeat_from,"from："+"3" );
	        item.put(key_repeat_count, "count："+"5");
	        item.put(key_repeat_time, "time："+"30"+"min");
	        item.put(key_shaker_argument, "shaker command："+"set speed");
	        list.add(item);
	    }
	    
	  //新增SimpleAdapter
	    adapter = new SimpleAdapter( 
	    this, 
	    list,
	    R.layout.script_list,
	    new String[] {key_picture, key_index, key_instruction, key_repeat_from, key_repeat_count, key_repeat_time, key_shaker_argument},
	    new int[] { R.id.imageView1, R.id.textViewIndex, R.id.textViewInstruction, R.id.textViewFrom, R.id.textViewCount, R.id.textViewTime, R.id.textViewShakerArgument } );
	    
	    //listview物件使用setAdapter方法（比對ListActivity是用setListAdapter）
	    list_view.setAdapter(adapter);
	    
	    //ListActivity設定adapter
	   // setListAdapter( adapter );
	    
	    //啟用按鍵過濾功能，這兩行都會進行過濾
	    list_view.setTextFilterEnabled(true);
	   // getListView().setTextFilterEnabled(true);
	    list_view.setOnItemClickListener(new OnItemClickListener() {
	    	 
	        public void onItemClick(AdapterView<?> arg0, View view,
	                int position, long id) {
	            Log.d(Tag, "ListViewItem = " + id);
	            show_script_setting_dialog();
	        }
	    });
	    
	    button_add_item = (Button) findViewById(R.id.button_add);
	    button_add_item.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
                    HashMap<String, Object> item = new HashMap<String, Object>();
                    String str_index;
                    str_index = String.format("%d", list.size());
                    item.put(key_picture, mPics[3]);
                    item.put(key_index, str_index);
        	        item.put(key_instruction, experiment_script_data.SCRIPT_INSTRUCT.get(1));
        	        item.put(key_repeat_from,"from："+"3" );
        	        item.put(key_repeat_count, "count："+"5");
        	        item.put(key_repeat_time, "time："+"30"+"min");
        	        item.put(key_shaker_argument, "shaker command："+"set speed");
            	    list.add(item);
                    adapter.notifyDataSetChanged();
                } catch (NullPointerException e) {
                    Log.i(Tag, "Tried to add null value");
                }
        	}
		});
    }
	
	private List<Map<String, String>> getListValues() {
	    List<Map<String, String>> values = new ArrayList<Map<String, String>>();
	    int length = mMenuText.length;
	    for (int i = 0; i < length; i++) {
	      Map<String, String> v = new HashMap<String, String>();
	      v.put(IDemoChart.NAME, mMenuText[i]);
	      v.put(IDemoChart.DESC, mMenuSummary[i]);
	      values.add(v);
	    }
	    return values;
	}
	
	public void show_script_setting_dialog() {
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
	    	intent = new Intent(this, script_setting_activity.class);
	    	startActivity(intent);
    }

}

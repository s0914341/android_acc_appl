package ODMonitor.App;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.achartengine.chartdemo.demo.chart.IDemoChart;
import org.achartengine.chartdemo.demo.chart.XYChartBuilder;

import ODMonitor.App.R.drawable;
import ODMonitor.App.data.experiment_script_data;
import ODMonitor.App.data.script_data_exchange;
import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

public class script_activity_list extends Activity {
	public String Tag = "script_activity_list";
	private final static String key_picture = "picture";
	private final static String key_index = "index"; 
	private final static String key_instruction = "instruction"; 
	private final static String key_repeat_from = "repeat_from"; 
	private final static String key_repeat_count = "repeat_count"; 
	private final static String key_repeat_time = "repeat_time";
	private final static String key_shaker_argument = "shaker_argument";
	
	private final static int PICK_CONTACT_REQUEST = 0;
    private String[] mMenuText;
	private String[] mMenuSummary;
	List<HashMap<String,Object>> list = new ArrayList<HashMap<String,Object>>();
	public HashMap<Object, Object> experiment_item = new HashMap<Object, Object>();
	public SimpleAdapter adapter;
	public ListView list_view;
	Button button_add_item;
	Button button_clear_all;
	
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
	        HashMap<String, Object> item_string_view = new HashMap<String, Object>();
	        experiment_script_data item_data = new experiment_script_data();
	        refresh_script_list_view(list.size(), item_data, item_string_view);
	        list.add(item_string_view);
	        
	        experiment_item.put(item_string_view, item_data);
	    }
	    
	  //新增SimpleAdapter
	    adapter = new SimpleAdapter(this, list, R.layout.script_list,
	                                new String[] {key_picture, key_index, key_instruction, key_repeat_from, key_repeat_count, key_repeat_time, key_shaker_argument},
	                                new int[] { R.id.imageView1, R.id.textViewIndex, R.id.textViewInstruction, R.id.textViewFrom, R.id.textViewCount, R.id.textViewTime, R.id.textViewShakerArgument } );
	    
	    //listview物件使用setAdapter方法（比對ListActivity是用setListAdapter）
	    list_view.setAdapter(adapter);
	    
	    //ListActivity設定adapter
	   // setListAdapter( adapter );
	    
	    //啟用按鍵過濾功能，這兩行都會進行過濾
	    list_view.setTextFilterEnabled(true);
	   // getListView().setTextFilterEnabled(true);
	    registerForContextMenu(list_view);
	    
	    list_view.setOnItemClickListener(new OnItemClickListener() {
	    	 
	        public void onItemClick(AdapterView<?> arg0, View view,
	                int position, long id) {
	            Log.d(Tag, "ListViewItem id = " + id);
	            Log.d(Tag, "ListViewItem position= " + position);
	            show_script_setting_dialog(id, position);
	        }
	    });
	    
	    
	    button_add_item = (Button) findViewById(R.id.button_add);
	    button_add_item.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
                    HashMap<String, Object> item_string_view = new HashMap<String, Object>();
                    experiment_script_data item_data = new experiment_script_data();
                    refresh_script_list_view(list.size(), item_data, item_string_view);
            	    list.add(item_string_view);
            	    experiment_item.put(item_string_view, item_data);
                    adapter.notifyDataSetChanged();
                } catch (NullPointerException e) {
                    Log.i(Tag, "Tried to add null value");
                }
        	}
		});
	    
	    button_clear_all = (Button) findViewById(R.id.button_clear_all);
	    button_clear_all.setOnClickListener(new View.OnClickListener() {
        	public void onClick(View v) {
        		try {
        			list.clear();
        			experiment_item.clear();
        		    adapter.notifyDataSetChanged();
                } catch (NullPointerException e) {
                    Log.i(Tag, "Tried to clear all exception");
                }
        	}
		});
    }
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    if (v.getId()==R.id.listView1) {
	        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	        menu.setHeaderTitle("Edit");
	        String[] menuItems = getResources().getStringArray(R.array.list_menu);
	        for (int i = 0; i < menuItems.length; i++) {
	            menu.add(Menu.NONE, i, i, menuItems[i]);
	        }
	    }
	}
	
	@Override
	public boolean onContextItemSelected(MenuItem item) {
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
	    int menuItemIndex = item.getItemId();
	    String[] menuItems = getResources().getStringArray(R.array.list_menu);
	    String menuItemName = menuItems[menuItemIndex];
	    Log.d(Tag, "onContextItemSelected = " + info.id);
	    
	    experiment_item.remove(list.get((int)info.id));
	    list.remove((int)info.id);
	    
	    for(int i = (int)info.id; i < list.size(); i++) {
	    	refresh_script_list_view(i, (experiment_script_data)experiment_item.get(list.get(i)), list.get(i));
	    }
        adapter.notifyDataSetChanged();
        
	   // String listItemName = Countries[info.position];

	  //  TextView text = (TextView)findViewById(R.id.footer);
	  //  text.setText(String.format("Selected %s for item %s", menuItemName, listItemName));
	    return true;
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
	
	public void show_script_setting_dialog(long id, int position) {
		    //In the method that is called when click on "update"
	    	Intent intent = new Intent(this, script_setting_activity.class);
	    	intent.setClass(script_activity_list.this, script_setting_activity.class); 
	    	intent.putExtra("send_experiment_script_data", (experiment_script_data)experiment_item.get(list.get((int)id))); 
	    	intent.putExtra("send_total_item", list.size()); 
	    	intent.putExtra("send_item_id", id); 
	    	intent.putExtra("send_item_position", position); 
	    	
	    	//startActivity(intent);
	    	
	    	startActivityForResult(intent, PICK_CONTACT_REQUEST); //I always put 0 for someIntValue
    }
	
	//In your class
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    //Retrieve data in the intent
		if (requestCode == PICK_CONTACT_REQUEST) {
	        // Make sure the request was successful
	        if (resultCode == RESULT_OK) {
	        	long id = data.getLongExtra("return_item_id", -1);
	        	int position = data.getIntExtra("return_item_position", -1);
	     	    if (id >= 0 && position >= 0) {
	     	        experiment_script_data item_data = (experiment_script_data)data.getSerializableExtra("return_experiment_script_data");  
	     	        experiment_item.remove(list.get((int)id));
	     	        
	     	      //  HashMap<String, Object> item_string_view = new HashMap<String, Object>();
	     	     //   refresh_script_list_view((int)id, item_data, item_string_view);
	   	          //  list.set(position, item_string_view);
	     	     //   experiment_item.put(list.get((int)id), item_data);
	     	        refresh_script_list_view((int)id, item_data, list.get(position));
	   	            adapter.notifyDataSetChanged();  
	     	    }
	     	    
	     	    Log.d(Tag, "onActivityResult = " + id);
	        }
	    }
	}
	
	public void refresh_script_list_view(int id,  experiment_script_data item_data, HashMap<String, Object> item_string_view) {
		 String str_index;
        str_index = String.format("%d", id);
        int instruct = item_data.get_instruct_value();
        
        item_string_view.put(key_picture, mPics[instruct]);
        item_string_view.put(key_index, str_index);
        item_string_view.put(key_instruction, experiment_script_data.SCRIPT_INSTRUCT.get(instruct));
        switch(instruct) {
            case  experiment_script_data.INSTRUCT_READ_SENSOR:
            case  experiment_script_data.INSTRUCT_SHAKER_ON:
            case  experiment_script_data.INSTRUCT_SHAKER_OFF:
	                item_string_view.put(key_repeat_from,"");
	                item_string_view.put(key_repeat_count, "");
	                item_string_view.put(key_repeat_time, "");
	   	            item_string_view.put(key_shaker_argument, "");
            break;
            
            case  experiment_script_data.INSTRUCT_REPEAT_COUNT:
	                item_string_view.put(key_repeat_from, "from："+item_data.get_repeat_from_string()+"   ");
	                item_string_view.put(key_repeat_count, "count："+item_data.get_repeat_count_string());
	                item_string_view.put(key_repeat_time, "");
	   	            item_string_view.put(key_shaker_argument, "");
            break;
            
            case  experiment_script_data.INSTRUCT_REPEAT_TIME:
	                item_string_view.put(key_repeat_from, "from："+item_data.get_repeat_from_string()+"   ");
	                item_string_view.put(key_repeat_count, "");
	                item_string_view.put(key_repeat_time, "time："+item_data.get_repeat_time_string()+"min");
	   	            item_string_view.put(key_shaker_argument, "");
            break;
            
            case  experiment_script_data.INSTRUCT_SHAKER_SET_SPEED:
	                item_string_view.put(key_repeat_from, "");
	                item_string_view.put(key_repeat_count, "");
	                item_string_view.put(key_repeat_time, "");
	   	            item_string_view.put(key_shaker_argument, "shaker speed：" + item_data.get_shaker_speed_string());
            break;
            
            case  experiment_script_data.INSTRUCT_SHAKER_SET_TEMPERATURE:
	                item_string_view.put(key_repeat_from, "");
	                item_string_view.put(key_repeat_count, "");
	                item_string_view.put(key_repeat_time, "");
	   	            item_string_view.put(key_shaker_argument, "shaker temperature：" + item_data.get_shaker_temperature_string());
            break;
        }
	}
	
	@Override
    public void onResume() {
    		super.onResume();
    		Log.d(Tag, "on Resume");
    }
    
    @Override
    public void onPause() {
    	Log.d(Tag, "on Pause");
    	super.onPause();
    }
    
	@Override
	public void onDestroy() {
		Log.d(Tag, "on Destory");
		super.onDestroy();
	}
}

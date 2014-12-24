/**
 * Copyright (C) 2009 - 2013 SC 4ViewSoft SRL
 *  
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *  
 *      http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.achartengine.chartdemo.demo.chart;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Date;

import org.achartengine.ChartFactory;
import org.achartengine.GraphicalView;
import org.achartengine.chart.PointStyle;
import org.achartengine.model.SeriesSelection;
import org.achartengine.model.TimeSeries;
import org.achartengine.model.XYMultipleSeriesDataset;
import org.achartengine.model.XYSeries;
import org.achartengine.renderer.XYMultipleSeriesRenderer;
import org.achartengine.renderer.XYSeriesRenderer;

import ODMonitor.App.ODMonitorActivity;
import ODMonitor.App.OD_calculate;
import ODMonitor.App.R;
import ODMonitor.App.data.android_accessory_packet;
import ODMonitor.App.data.chart_display_data;
import ODMonitor.App.file.file_operate_byte_array;
import ODMonitor.App.file.file_operate_chart;
import ODMonitor.App.file.file_operation;
import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.LinearLayout;
import android.widget.Toast;

public class ODChartBuilder extends Activity {
  public String Tag = "XYChartBuilder";
  /** The main dataset that includes all the series that go into a chart. */
  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
  /** The main renderer that includes all the renderers customizing a chart. */
  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
  /** The most recently added series. */
  private TimeSeries mCurrentSeries = null;
  /** The most recently created renderer, customizing the current series. */
  private XYSeriesRenderer mCurrentRenderer;
  /** The chart view that displays the data. */
  private GraphicalView mChartView;
  public data_read_thread data_read_thread;
  public long current_index = -1;
  
  private static final long SECOND = 1000;
  private static final long MINUTE = 60*SECOND;
  private static final long HOUR = 60*MINUTE;
  private static final long DAY = 24*HOUR;
  private static final int HOURS = 24;


  @Override
  protected void onSaveInstanceState(Bundle outState) {
      super.onSaveInstanceState(outState);
      // save the current data, for instance when changing screen orientation
      outState.putSerializable("dataset", mDataset);
      outState.putSerializable("renderer", mRenderer);
      outState.putSerializable("current_series", mCurrentSeries);
      outState.putSerializable("current_renderer", mCurrentRenderer);
  }

  @Override
  protected void onRestoreInstanceState(Bundle savedState) {
      super.onRestoreInstanceState(savedState);
      // restore the current data, for instance when changing the screen
      // orientation
      mDataset = (XYMultipleSeriesDataset) savedState.getSerializable("dataset");
      mRenderer = (XYMultipleSeriesRenderer) savedState.getSerializable("renderer");
      mCurrentSeries = (TimeSeries) savedState.getSerializable("current_series");
      mCurrentRenderer = (XYSeriesRenderer) savedState.getSerializable("current_renderer");
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
	  Log.d(Tag, "on Create");
      super.onCreate(savedInstanceState);
      setContentView(R.layout.xy_layout);
      getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
      Thread.currentThread().setName("Thread_XYChartBuilder");
      // set some properties on the main renderer
      mRenderer.setApplyBackgroundColor(true);
      mRenderer.setBackgroundColor(Color.argb(100, 50, 50, 50));
      mRenderer.setAxisTitleTextSize(16);
      mRenderer.setChartTitleTextSize(20);
      mRenderer.setLabelsTextSize(15);
      mRenderer.setLegendTextSize(15);
      mRenderer.setMargins(new int[] { 20, 30, 15, 0 });
      // long now = Math.round(new Date().getTime() / DAY) * DAY;
      long now = new Date().getTime();
    
      mRenderer.setRange(new double[] {now, now+60000, 0, 50});
      mRenderer.setZoomButtonsVisible(true);
      mRenderer.setShowGrid(true);
      mRenderer.setPointSize(5);
    
      init_time_series();
    //  data_read_thread = new data_read_thread(handler);
    //  data_read_thread.start();
  }
  
  public void refresh_current_view_range(Date x, double y) {
	  double XMax = mRenderer.getXAxisMax();
	  double XMin = mRenderer.getXAxisMin();
	  double thread = XMin+(XMax-XMin)*8.0/10.0;
	  if (thread < x.getTime()) {
		  //double refresh_XMin = XMin+(XMax-XMin)*7.0/10.0;
		  
		  double refresh_XMin = x.getTime()-((XMax-XMin)*3.0/10.0);
		  mRenderer.setXAxisMin(refresh_XMin);
		  mRenderer.setXAxisMax(refresh_XMin+(XMax-XMin));
	  }
  }

  public void SerialAdd(Date x, double y) {
      // add a new data point to the current series
	  refresh_current_view_range(x, y);
      mCurrentSeries.add(x, y);
      // repaint the chart such as the newly added point to be visible
      mChartView.repaint();
  }
  
  final Handler handler =  new Handler() {
  	@Override 
  	public void handleMessage(Message msg) {
  		chart_display_data objectRcvd = (chart_display_data)msg.getData().getSerializable("chart");
  		
  		if (objectRcvd.get_index_value() == 0) {
  			mRenderer.setRange(new double[] {objectRcvd.get_date_value(), objectRcvd.get_date_value()+20000, 0, 50});
  			CreateNewSeries();
  		    mCurrentSeries.add(new Date(objectRcvd.get_date_value()), objectRcvd.get_concentration_value());
  		} else {
  		    SerialAdd(new Date(objectRcvd.get_date_value()), objectRcvd.get_concentration_value());
  		}
  	
		Log.d("EXPERIMENT", "data_read_thread handler");
  	}
  };
  
  private class data_read_thread  extends Thread {
		Handler mHandler;
		
		data_read_thread(Handler h){
			mHandler = h;
		}
		
		public void run() {
			int file_len = 0;
			Bundle b = new Bundle(1);
			
			file_operate_byte_array read_file = new file_operate_byte_array("testExperimentData", "testExperimentData", true);
				
			while(true) {
				try {
					file_len = read_file.open_read_file(read_file.generate_filename_no_date());
					if (file_len > 0) {
						byte[] read_buf = new byte[file_len];
						read_file.read_file(read_buf);
					    chart_display_data chart_data = new chart_display_data();
					    byte[] chart_temp = new byte[chart_display_data.get_total_length()];
					    int offset = 0;
					    
					    for (offset = 0; offset < file_len; offset += chart_temp.length) {
					    	if ((offset + chart_display_data.get_total_length()) <= file_len) {
					    	    System.arraycopy(read_buf, offset, chart_temp, 0, chart_temp.length);
					    	    if (0 == chart_data.set_object_buffer(chart_temp)) {
					    	        if (chart_data.get_index_value() > current_index) {
					    	    	    Message msg = mHandler.obtainMessage();
					    	    	    current_index = chart_data.get_index_value();
					    	            b.putSerializable("chart", chart_data);
							            msg.setData(b);
					    	            mHandler.sendMessage(msg);
					    	            break;
					    	        }
					    	    }
					    	} else
					    		break;
					    }
					} else {
		    			Log.d("data_read_thread", "open testExperimentData fail");
		    		}
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	    	    Log.d(Tag, "data_read_thread");
	    	    
	    	    try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
  
  /*public void init_time_series() {
	  int file_len = 0;
	  Date date = null;
	  long ms = 0;
	  double concentration = 0;
	  
	  file_operate_byte_array read_file = new file_operate_byte_array("testExperimentData", "testExperimentData", true);
	  try {
		file_len = read_file.open_read_file(read_file.generate_filename_no_date());
		if (file_len > 0) {
			byte[] read_buf = new byte[file_len];
			read_file.read_file(read_buf);
		    chart_display_data chart_data = new chart_display_data();
		    byte[] chart_temp = new byte[chart_data.get_total_length()];
		    int offset = 0;
		    
		    for (offset = 0; offset < file_len; offset += chart_temp.length) {
		    	if ((offset + chart_data.get_total_length()) <= file_len) {
		    	    System.arraycopy(read_buf, offset, chart_temp, 0, chart_temp.length);
		    	    if (0 == chart_data.set_object_buffer(chart_temp)) {
		    	    	ms = chart_data.get_date_value();
		    	    	concentration = chart_data.get_concentration_value();
		    	    	date = new Date(ms);
		    	    	if (chart_data.get_index_value() == 0) {
		    	  			mRenderer.setRange(new double[] {ms, ms+20000, 0, 50});
		    	  			CreateNewSeries();
		    	  		}
		    	    	
		    	    	current_index = chart_data.get_index_value();
		    	    	mCurrentSeries.add(date, concentration);
		    	    } else {
		    	    	Log.e("Chart", "chart_data.set_object_buffer fail");
		    	    }
		    	} else
		    		break;
		    }
		    refresh_current_view_range(date, concentration);
		}
	} catch (IOException e) {
		// TODO Auto-generated catch block
		e.printStackTrace();
	}
  }*/
  
    public void init_time_series() {
        Date date = null;
        double od_value = 0;
        long experiment_start_ms = 0;
        
   
		file_operation read_file = new file_operation("od_sensor", "sensor_online", true);
        try {
	        read_file.open_read_file(read_file.generate_filename_no_date());
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
	        read_file = new file_operation("od_sensor", "sensor_offline", true);
	        try {
				read_file.open_read_file(read_file.generate_filename_no_date());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
        }
        
        try {    	
	        String sensor_str = new String();
	        sensor_str = read_file.read_file();
	        if(sensor_str != null) {
	        	byte[] temp = OD_calculate.parse_date(sensor_str);
	        	ByteBuffer byte_buffer = ByteBuffer.wrap(temp, 0, 8);
	    		//byte_buffer.order(ByteOrder.LITTLE_ENDIAN);
	        	experiment_start_ms = byte_buffer.getLong();
	        }
	        sensor_str = read_file.read_file();
	        while (sensor_str != null) {
				int[] data = null;
		        data = OD_calculate.parse_raw_data(sensor_str);
		        if (data != null) {
		            int[] channel_data = new int[OD_calculate.total_sensor_channel];
		            System.arraycopy(data, OD_calculate.sensor_ch1_index, channel_data, 0, OD_calculate.total_sensor_channel);
		            od_value = OD_calculate.calculate_od(channel_data);
		            date = new Date(experiment_start_ms + (long)(data[OD_calculate.experiment_seconds_index]*1000));
		            
		            if (mCurrentSeries == null) {
		            	mRenderer.setRange(new double[] {date.getTime(), date.getTime()+20000, 0, 50});
	  			        CreateNewSeries();
		            }
		            
		            current_index = (long)data[OD_calculate.index_index];
	    	        mCurrentSeries.add(date, od_value);
		        } else {
		        	Log.e(Tag, "parse raw data fail");
		        }
		        
		        sensor_str = read_file.read_file();
			} 
	        
	        refresh_current_view_range(date, od_value);     
        } catch (IOException e) {
	        // TODO Auto-generated catch block
	        e.printStackTrace();
        }
    }
  
  public void CreateNewSeries() {
      String seriesTitle = "Series " + (mDataset.getSeriesCount() + 1);
      // create a new series of data
      TimeSeries series = new TimeSeries(seriesTitle);
     // XYSeries series = new XYSeries(seriesTitle);
      mDataset.addSeries(series);
      mCurrentSeries = series;
      // create a new renderer for the new series
      XYSeriesRenderer renderer = new XYSeriesRenderer();
      mRenderer.addSeriesRenderer(renderer);
      // set some renderer properties
      renderer.setColor(Color.argb(255, 0, 255, 0));
      renderer.setPointStyle(PointStyle.CIRCLE);
      renderer.setFillPoints(true);
      renderer.setDisplayChartValues(true);
     // renderer.setDisplayChartValuesDistance(10);
      mCurrentRenderer = renderer;
    //  mChartView.repaint();
  }

  @Override
  protected void onResume() {
	  Log.d(Tag, "on Resume");
      super.onResume();
      if (mChartView == null) {
          LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
          mChartView = ChartFactory.getTimeChartView(this, mDataset, mRenderer, "h:mm:ss a");
          //mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
          // enable the chart click events
          mRenderer.setClickEnabled(true);
          mRenderer.setSelectableBuffer(10);
          mChartView.setOnClickListener(new View.OnClickListener() {
          public void onClick(View v) {
          // handle the click event on the chart
              SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
              if (seriesSelection == null) {
                  Toast.makeText(ODChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
              } else {
                  // display information of the clicked point
                  Toast.makeText(
                      ODChartBuilder.this,
                      "Chart element in series index " + seriesSelection.getSeriesIndex()
                      + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                      + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                      + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
              }
          }
          });
        layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
        boolean enabled = mDataset.getSeriesCount() > 0;
      } else {
        mChartView.repaint();
    }
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
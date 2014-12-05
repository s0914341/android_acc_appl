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

import ODMonitor.App.LEDActivity;
import ODMonitor.App.R;
import ODMonitor.App.android_accessory_packet;
import ODMonitor.App.chart_display_data;
import ODMonitor.App.file_operate_byte_array;
import ODMonitor.App.file_operate_chart;
import ODMonitor.App.file_operation;
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

public class XYChartBuilder extends Activity {
  /** The main dataset that includes all the series that go into a chart. */
  private XYMultipleSeriesDataset mDataset = new XYMultipleSeriesDataset();
  /** The main renderer that includes all the renderers customizing a chart. */
  private XYMultipleSeriesRenderer mRenderer = new XYMultipleSeriesRenderer();
  /** The most recently added series. */
  private TimeSeries mCurrentSeries;
  /** The most recently created renderer, customizing the current series. */
  private XYSeriesRenderer mCurrentRenderer;
  /** The chart view that displays the data. */
  private GraphicalView mChartView;
  public data_read_thread data_read_thread;
  
  private static final long HOUR = 3600 * 1000;
  private static final long DAY = HOUR * 24;
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
    long now = Math.round(new Date().getTime() / DAY) * DAY;
    
    mRenderer.setRange(new double[] {now, now+2*HOUR, 0, 50});
    mRenderer.setZoomButtonsVisible(true);
    mRenderer.setShowGrid(true);
    mRenderer.setPointSize(5);
    
    CreateNewSeries();
    
    mCurrentSeries.add(new Date(now), 0);
    
    data_read_thread = new data_read_thread(handler);
    data_read_thread.start();
  }

  public void SerialAdd(Date x, double y) {
      // add a new data point to the current series
	 /* double XMax = mRenderer.getXAxisMax();
	  double XMin = mRenderer.getXAxisMin();
	  Date thread = XMin+(XMax-XMin)*8.0/10.0;
	  if (thread < x) {
		  double refresh_XMin = XMin+(XMax-XMin)*7.0/10.0;
		  mRenderer.setXAxisMin(refresh_XMin);
		  mRenderer.setXAxisMax(refresh_XMin+(XMax-XMin));
	  }*/
      mCurrentSeries.add(x, y);
      // repaint the chart such as the newly added point to be visible
      mChartView.repaint();
  }
  
  final Handler handler =  new Handler() {
  	@Override 
  	public void handleMessage(Message msg) {
  		chart_display_data objectRcvd = (chart_display_data)msg.getData().getSerializable("chart");
  		SerialAdd(objectRcvd.get_date_value(), objectRcvd.get_concentration_value());
  	}
  };
  
  private class data_read_thread  extends Thread {
		Handler mHandler;
		
		data_read_thread(Handler h){
			mHandler = h;
		}
		
		public void run() {
			byte[] experiment_data;
			long last_index = 0;
			byte i;
			int len = 0;
			Bundle b = new Bundle(1);
			file_operate_chart read_file = new file_operate_chart("testExperimentData", "testExperimentData", true);
			
			while(true) {
				Message msg = mHandler.obtainMessage();
				try {
					read_file.open_read_file(read_file.generate_filename_no_date());
					ArrayList<chart_display_data> listChartData = read_file.readObject();
					if (listChartData.size() > 0) { 
						for (chart_display_data iterator : listChartData) {
							chart_display_data object = (chart_display_data)iterator;
							if (object.get_index_value() > last_index) {
								b.putSerializable("chart", object);
							    msg.setData(b);
					    	    mHandler.sendMessage(msg);
					    	    last_index = object.get_index_value();
					    	    break;
							}
		                }
					} else {
		    			Log.d("data_read_thread", "open testExperimentData fail");
		    		}
			            
					/*len = read_file.open_read_file(read_file.generate_filename_no_date());
		    		
		    		if (len > 0) {
		    			experiment_data = new byte[len];
		    		    read_file.read_file(experiment_data);
		    		    
		    		    for(i = 0; i < 250; i++) {
							if (i*3 < len) {
							    if (experiment_data[i*3] > last_index) {
							    	msg.arg1 = experiment_data[i*3+1];
							    	msg.arg2 = experiment_data[i*3+2];
							    	mHandler.sendMessage(msg);
							    	last_index = experiment_data[i*3];
							        break;
							    }
							} else {
							    break;
							}
						}
		    		} else {
		    			Log.d("data_read_thread", "open testExperimentData fail");
		    		}*/
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ClassNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
	    	    Log.d("EXPERIMENT", "data_read_thread");
	    	    
	    	    try {
					Thread.sleep(3000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
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
    super.onResume();
    if (mChartView == null) {
      LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
      mChartView = ChartFactory.getTimeChartView(this, mDataset, mRenderer, "h:mm a");
      //mChartView = ChartFactory.getLineChartView(this, mDataset, mRenderer);
      // enable the chart click events
      mRenderer.setClickEnabled(true);
      mRenderer.setSelectableBuffer(10);
      mChartView.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          // handle the click event on the chart
          SeriesSelection seriesSelection = mChartView.getCurrentSeriesAndPoint();
          if (seriesSelection == null) {
            Toast.makeText(XYChartBuilder.this, "No chart element", Toast.LENGTH_SHORT).show();
          } else {
            // display information of the clicked point
            Toast.makeText(
                XYChartBuilder.this,
                "Chart element in series index " + seriesSelection.getSeriesIndex()
                    + " data point index " + seriesSelection.getPointIndex() + " was clicked"
                    + " closest point value X=" + seriesSelection.getXValue() + ", Y="
                    + seriesSelection.getValue(), Toast.LENGTH_SHORT).show();
          }
        }
      });
      layout.addView(mChartView, new LayoutParams(LayoutParams.FILL_PARENT,
          LayoutParams.FILL_PARENT));
      boolean enabled = mDataset.getSeriesCount() > 0;
    } else {
      mChartView.repaint();
    }
  }
}
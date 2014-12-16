package ODMonitor.App;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.util.Log;

public class OD_calculate {
	/** 
	 * Beer-Lambert equation
	 * c=(A x e) / b
	 * c is concentration, 
	 * A is the absorbance in AU, 
	 * e is the wavelength-dependent extinction coefficient in ng-cm/£gl,
	 * b is the pathlength 
	 */
	public static String Tag = "OD_calculate";
	public static final double e_double_DNA = 50; //ng-cm/£gl, Double-stranded DNA
	public static final double e_single_DNA = 33; //ng-cm/£gl, Single-stranded DNA
	public static final double e_RNA = 40; //ng-cm/£gl, RNA
	
	public static final int year_index = 0;
	public static final int month_index = 1;
	public static final int day_index = 2;
	public static final int hour_index = 3;
	public static final int minute_index = 4;
	public static final int second_index = 5;
	public static final int index_index = 6;
	public static final int sensor_ch1_index = 7;
	public static final int sensor_ch2_index = 8;
	public static final int sensor_ch3_index = 9;
	public static final int sensor_ch4_index = 10;
	public static final int sensor_ch5_index = 11;
	public static final int sensor_ch6_index = 12;
	public static final int sensor_ch7_index = 13;
	public static final int sensor_ch8_index = 14;
	
	public static final int total_sensor_channel = 8;
	public static final int experiment_data_size = 15;
	
	public static final double[] Upscale_factors = new double[] {13000/10, 13000/30.9, 13000/78.7, 13000/260, 13000/549, 13000/1500, 13000/5100, 1};
	public static final double[] Adjecency_Channel_Ratio = new double[] {30.9/10, 78.7/30.9, 260/78.7, 549/260, 1500/549, 5100/1500, 13000/5100};
	
	
	
	public static double sample_OD_value(double I1, double I2) {
        double ODvalue = 0;
        
		ODvalue = (-1)*Math.log10(I2/I1);
		return ODvalue;
	}
	
	public static void parse_raw_data(String s, int[] data) {
		//Pattern p = Pattern.compile("(\\d+)/(\\d+)/(\\d+) (\\d+):(\\d+):(\\d+)  index: (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+), (\\d+)");
		Pattern p = Pattern.compile("\\d+");
		Matcher m = p.matcher(s);
		
		for (int i = 0; i < data.length; i++) {
			if (m.find())
				data[i] = Integer.parseInt(m.group());
		}
	}
	
	public static double calculate_od(int[] data, double[] upscale_raw_data, double[] channels_od) {
		int ret = -1;
		double channel_ratio = 0;
		boolean ratio_check_ok = false;
		int channel_index = 0;
		int raw_data = 0;
		double upscale_data = 0;
		int max_val = 0;
		int channel_count = 0;
		double primitive_od = 0;
		double final_od = 0;
		
		
        if (data.length == total_sensor_channel) {
        	channel_index = 0;
        	while (channel_index < total_sensor_channel) {
        		raw_data = data[channel_index];
        	    if ((channel_index > 0) && channel_index < total_sensor_channel) {
        		    if (data[channel_index-1] > 0) {
        	            channel_ratio = ((double)data[channel_index]/(double)data[channel_index-1])/Adjecency_Channel_Ratio[channel_index-2];
        	            if (channel_ratio > 0.9 && channel_ratio < 1.11) {
        	            	if ((max_val/data[channel_index]) > 1.5) {
        	        	        ratio_check_ok = true;
        	        	        channel_count = 0;
        	        	        primitive_od = 0;
        	        	        break;
        	                } else {
        	        	        ratio_check_ok = false;
        	        	        if (max_val < data[channel_index])
        	        	    	    max_val = data[channel_index];
        	                }
        	            } else {
        	            	ratio_check_ok = false;
        	            }
        		    }
        	    } else {
        	        if (data[channel_index] > data[channel_index+1])
        	        	ratio_check_ok = false;
        	        else
        	        	ratio_check_ok = true;
        	        max_val = raw_data;
        	    }
        	    
        	    if ((raw_data < 4010) && (raw_data > 80) && (ratio_check_ok == true)) 
        	    	upscale_raw_data[channel_index] = raw_data * Upscale_factors[channel_index - 1];
        	    else
        	    	upscale_raw_data[channel_index] = 0;
        	    
        	    upscale_data = upscale_raw_data[channel_index];
        	    if (upscale_data > 0) {
        	    	//channels_od[channel_index] =  CDbl(Log((4095 * Upscale_factors[0]) / upscale_data));
        	    	//channels_od[channel_index] = channels_od[channel_index]/Log(10);
        	    	primitive_od = primitive_od + channels_od[channel_index];
        	    	channel_count++;
        	    } else {
        	    	channels_od[channel_index] = 0;
        	    }
        	    channel_index++;  
        	}
        	
        	if (channel_count > 0)
        		final_od = primitive_od/channel_count;
        	
        
        		
        	
  
   /*       If Ref_OD_Count < 25 Then
            Ref_OD = Ref_OD + Final_OD
            Ref_OD_Count = Ref_OD_Count + 1
            Final_OD = 0
            If Ref_OD_Count = 25 Then
              Ref_OD = Ref_OD / Ref_OD_Count
              Sheet3.Range("C1").Value = Ref_OD
            End If
          Else
             If channel_count > 0 Then
               Final_OD = Final_OD - Ref_OD
             End If
             'If Abs(Last_OD - Final_OD) > 0.2 Then
               'Final_OD = Last_OD
             'End If
             Last_OD = Final_OD
          End If*/
        } else {
         
        }
        
        return final_od;
	}
}

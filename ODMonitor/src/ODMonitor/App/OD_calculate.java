package ODMonitor.App;

public class OD_calculate {
	/** 
	 * Beer-Lambert equation
	 * c=(A x e) / b
	 * c is concentration, 
	 * A is the absorbance in AU, 
	 * e is the wavelength-dependent extinction coefficient in ng-cm/£gl,
	 * b is the pathlength 
	 */
	public static final double e_double_DNA = 50; //ng-cm/£gl, Double-stranded DNA
	public static final double e_single_DNA = 33; //ng-cm/£gl, Single-stranded DNA
	public static final double e_RNA = 40; //ng-cm/£gl, RNA
	
	public double sample_OD_value(double I1, double I2) {
        double ODvalue = 0;
        
		ODvalue = (-1)*Math.log10(I2/I1);
		return ODvalue;
	}
}

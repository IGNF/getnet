/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab.test;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import fr.ign.ignfab.bdtopo.BDTopoRoute;
import fr.ign.ignfab.bdtopo.RouteWFS;

/**
 * 
 *
 */
public class Test {

	public static final Boolean PROXY = false;

	public static void testBDTopoSeule(String bbox) {
		BDTopoRoute bdRoute = RouteWFS.getTroncon(bbox);
		System.out.println("Nb troncon = " + bdRoute.getFeatures().size());
	}


	public static void testGetNetwork(String bbox) {
		List<String> csv = RouteWFS.getBDTopoNetwork(bbox, 10, false);
		System.out.println("Nb troncon avec une tolérance de 10 = " + csv.size());
		System.out.println(csv.get(0));
		System.out.println(csv.get(1));
		System.out.println(csv.get(2));
		System.out.println("...");
	}

	


	// Main file for extracting file

	public static void main(String[] args) {
		
		double xmin = Double.parseDouble(args[0]);
		double ymin = Double.parseDouble(args[1]);
		double xmax = Double.parseDouble(args[2]);
		double ymax = Double.parseDouble(args[3]);

		ArrayList<Double> BBOX = new ArrayList<>();
		BBOX.add(xmin);
		BBOX.add(ymin);
		BBOX.add(xmax);
		BBOX.add(ymax);

		double tolerance = Double.parseDouble(args[4]);

		String output_file = args[5];
		
		RouteWFS.key = "PRATIQUE";

		getNetwork2(BBOX, tolerance, output_file);
		
	}


	public static void test() {

		RouteWFS.key = "PRATIQUE";
		
		double xmin = -0.6298;
		double ymin = 44.8675;
		double xmax = -0.4940;
		double ymax = 44.8952;

		ArrayList<Double> BBOX = new ArrayList<>();
		BBOX.add(xmin);
		BBOX.add(ymin);
		BBOX.add(xmax);
		BBOX.add(ymax);

		String output_file = "C:/Users/ymeneroux/Desktop/network_test.wkt";

		double tolerance = 0.01;

		RouteWFS.getNetwork(BBOX, tolerance, output_file, false);

	}


	public static String getNetwork2(ArrayList<Double> BBOX, double tolerance, String output){

		String out = "";

		String bbox = BBOX.get(1)+","+BBOX.get(0)+","+BBOX.get(3)+","+BBOX.get(2);

		double xmin = BBOX.get(0);
		double ymin = BBOX.get(1);
		double xmax = BBOX.get(2);
		double ymax = BBOX.get(3);


		double R = 6371000;

		double coeff = Math.cos((ymin+ymax)/2*Math.PI/180);

		double Delta_x = R*Math.abs(xmax-xmin)*Math.PI/180*coeff;
		double Delta_y = R*Math.abs(ymax-ymin)*Math.PI/180;

		if ((Delta_x < 1000) || (Delta_y < 1000)){


		}
		else{

			Delta_x /= 1000;
			Delta_y /= 1000;

		}


		List<String> csv = RouteWFS.getBDTopoNetwork(bbox, tolerance, false);

		try {

			BufferedWriter writer = new BufferedWriter(new FileWriter(output));

			writer.write("link_id,abs_key,source,target,direction,level,wkt\r\n");

			for (int i=0; i<csv.size(); i++){

				csv.set(i, csv.get(i).replace("LINESTRING (", "LINESTRING("));

				csv.set(i, csv.get(i).replace("LINESTRING", "\"LINESTRING"));
				csv.set(i, csv.get(i).replace(")", ")\""));

				writer.write(csv.get(i)+"\r\n");


			}

			writer.close();

		} catch (IOException e) {
			e.printStackTrace();
		}


		return out;


	}


	// -----------------------------------------------------------------------------
	// Method to create a progress bar
	// -----------------------------------------------------------------------------
	public static void progressPercentageConsole(int remain, int total) {

		if (remain > total) {
			throw new IllegalArgumentException();
		}

		int remainProcent =(int) (((double) remain / (double) total) * 65);

		char defaultChar = '-';
		String icon = "*";
		String bare = new String(new char[65]).replace('\0', defaultChar) + "]";

		StringBuilder bareDone = new StringBuilder();
		bareDone.append("[");

		for (int i = 0; i < remainProcent; i++) {

			bareDone.append(icon);

		}

		String bareRemain = bare.substring(remainProcent, bare.length());
		System.out.print("\r" + bareDone + bareRemain + " " + remainProcent*100/65 + " %");

		if (remain == total) {
			System.out.print("\n");
		}

	}


}

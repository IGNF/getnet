/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab;

import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;



import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import fr.ign.ignfab.bdtopo.RouteWFS;
import fr.ign.ignfab.gui.MapPanel;
import fr.ign.ignfab.gui.SettingsPanel;

/**
 * 
 * 
 * @author Yann Méneroux
 */
public class Main {

	private static int inDrag = 0;

	private static Point initialPosition;
	private static Point finalPosition;
	private static String key = "";

	private static final double tolerance = 0.001;

	private static ArrayList<String> EPSG = null;

	@SuppressWarnings("resource")
	public static void main(final String[] args) {


		// Get parameters and launch console mode
		if (args.length == 8) {

			RouteWFS.key = args[0];
			RouteWFS.projection = args[7];

			String[] bbox = {args[1], args[2], args[3], args[4], args[5], args[6]};

			launch(bbox);

			System.exit(0);

		}


		try {

			String os = System.getProperty("os.name").toLowerCase();

			// For windows os
			if (os.contains("windows")){

				UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");

			}

			// For linux os
			if ((os.contains("linux")) || (os.contains("unix"))){

				UIManager.setLookAndFeel("com.sun.java.swing.plaf.gtk.GTKLookAndFeel");

			}


		}
		catch (UnsupportedLookAndFeelException e) {
			// handle exception
		}
		catch (ClassNotFoundException e) {
			// handle exception
		}
		catch (InstantiationException e) {
			// handle exception
		}
		catch (IllegalAccessException e) {
			// handle exception
		}


		// Get key in args or external file
		if (args.length == 0){

			File keyFile = new File("key.dat");

			try {

				Scanner scan = new Scanner(keyFile);
				key = scan.nextLine();

			} catch (FileNotFoundException e1) {

			    SettingsPanel settingsPanel = new SettingsPanel();
			    JOptionPane.showMessageDialog(null, settingsPanel, "Settings", JOptionPane.YES_NO_CANCEL_OPTION);
			  
			    key = settingsPanel.getKey();
			    /*if (settingsPanel.getDistance() != null && !settingsPanel.getDistance().equals("")) {
			      tolerance = Double.parseDouble(settingsPanel.getDistance());
			    }*/
			    // TODO proxy
			}

		}
		else{

			key = args[0];

		}

		final JFrame fen = new JFrame();
		fen.setSize(600, 600);

		// just a JPanel extension, add to any swing/awt container
		final MapPanel mapPanel = new MapPanel(); 

		fen.setContentPane(mapPanel);
		fen.setLocationRelativeTo(null);
		fen.setResizable(false);
		fen.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fen.setTitle("Network Extraction");
		fen.setVisible(true);


		mapPanel.setZoom(15); // set some zoom level (1-18 are valid)
		double lon = 2.445;
		double lat = 48.848;
		Point position = mapPanel.computePosition(new Point2D.Double(lon, lat));
		mapPanel.setCenterPosition(position); // sets to the computed position
		mapPanel.repaint(); // if already visible trigger a repaint here


		mapPanel.addMouseListener(new MouseListener() {

			@SuppressWarnings("unchecked")
			@Override
			public void mouseReleased(MouseEvent e) {

				inDrag = 0;


				// Rescaling
				if ((e.getButton() == 3) && (mapPanel.selectRectangle)){

					mapPanel.selectRectangle = false;

					mapPanel.repaint();

					finalPosition = mapPanel.getCursorPosition();

					Point.Double pi = mapPanel.getLongitudeLatitude(initialPosition);
					Point.Double pf = mapPanel.getLongitudeLatitude(finalPosition);


					double xmin = Math.min(pi.x, pf.x);
					double ymin = Math.min(pi.y, pf.y);
					double xmax = Math.max(pi.x, pf.x);
					double ymax = Math.max(pi.y, pf.y);

					final ArrayList<Double> BBOX = new ArrayList<Double>();
					BBOX.add(xmin);
					BBOX.add(ymin);
					BBOX.add(xmax);
					BBOX.add(ymax);

					JFileChooser chooser = new JFileChooser("."); 
					int rep2 = chooser.showOpenDialog(null);
					
					if (rep2 != 0){return;}

					final File output_file = chooser.getSelectedFile();

					fillProjections();

					@SuppressWarnings("rawtypes")
					JComboBox comboBox = new JComboBox();


					for (int i=0; i<EPSG.size(); i++){

						comboBox.addItem(EPSG.get(i));

					}

					comboBox.setSelectedItem("2154");

					int rep = JOptionPane.showConfirmDialog(null, comboBox, "Select a projection code", JOptionPane.OK_CANCEL_OPTION);

					if (rep != 0){
						return;
					}

					RouteWFS.projection = (String) comboBox.getSelectedItem();


					if (output_file != null){


						Thread t = new Thread(){

							public void run(){

								String output = output_file.getAbsolutePath();

								final java.awt.Cursor cursor = mapPanel.getCursor();
								mapPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.WAIT_CURSOR));

								RouteWFS.key = key;
								RouteWFS.externalFrame = fen;
								String message = RouteWFS.getNetwork(BBOX, tolerance, output, true);

								mapPanel.setCursor(cursor);

								if (!message.equals("")){

									JOptionPane.showMessageDialog(null, message, "Information", JOptionPane.INFORMATION_MESSAGE);

								}

							}};

							t.start();

					}


				}


			}

			@Override
			public void mousePressed(MouseEvent e) {

				Point p = e.getPoint();

				int startX = p.x;
				int startY = p.y;

				inDrag = e.getButton();

				if (inDrag == 3){

					mapPanel.xrect = startX;
					mapPanel.yrect = startY;

					initialPosition = mapPanel.getCursorPosition();

				}

			}

			@Override
			public void mouseExited(MouseEvent e) {


			}

			@Override
			public void mouseEntered(MouseEvent e) {


			}

			@Override
			public void mouseClicked(MouseEvent e) {


			}
		});

		mapPanel.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {

			}

			@Override
			public void mouseDragged(MouseEvent e) {

				Point p = e.getPoint();

				// Rescaling
				if (inDrag == 3) {

					mapPanel.selectRectangle = true;

					mapPanel.wrect = p.x - mapPanel.xrect;
					mapPanel.hrect = p.y - mapPanel.yrect;

					mapPanel.repaint();

				}

			}

		});

	}

	private static void fillProjections(){

		EPSG = new ArrayList<String>();

		EPSG.add("2154");
		EPSG.add("21781");
		EPSG.add("23030");
		EPSG.add("23031");
		EPSG.add("23032");
		EPSG.add("27561");
		EPSG.add("27562");
		EPSG.add("27563");
		EPSG.add("27564");
		EPSG.add("27571");
		EPSG.add("27572");
		EPSG.add("27573");
		EPSG.add("27574");
		EPSG.add("27581");
		EPSG.add("27582");
		EPSG.add("27583");
		EPSG.add("27584");
		EPSG.add("27591");
		EPSG.add("27592");
		EPSG.add("27593");
		EPSG.add("27594");
		EPSG.add("2969");
		EPSG.add("2970");
		EPSG.add("2971");
		EPSG.add("2972");
		EPSG.add("2973");
		EPSG.add("2975");
		EPSG.add("2976");
		EPSG.add("2977");
		EPSG.add("2978");
		EPSG.add("2980");
		EPSG.add("2981");
		EPSG.add("2986");
		EPSG.add("2987");
		EPSG.add("2988");
		EPSG.add("2989");
		EPSG.add("2990");
		EPSG.add("3034");
		EPSG.add("3035");
		EPSG.add("3042");
		EPSG.add("3043");
		EPSG.add("3044");
		EPSG.add("3170");
		EPSG.add("3171");
		EPSG.add("3172");
		EPSG.add("32620");
		EPSG.add("32621");
		EPSG.add("32622");
		EPSG.add("32630");
		EPSG.add("32631");
		EPSG.add("32632");
		EPSG.add("32705");
		EPSG.add("32706");
		EPSG.add("32707");
		EPSG.add("32738");
		EPSG.add("32739");
		EPSG.add("32740");
		EPSG.add("3296");
		EPSG.add("3297");
		EPSG.add("3298");
		EPSG.add("3302");
		EPSG.add("3303");
		EPSG.add("3304");
		EPSG.add("3305");
		EPSG.add("3306");
		EPSG.add("3727");
		EPSG.add("3857");
		EPSG.add("3942");
		EPSG.add("3943");
		EPSG.add("3944");
		EPSG.add("3945");
		EPSG.add("3946");
		EPSG.add("3947");
		EPSG.add("3948");
		EPSG.add("3949");
		EPSG.add("3950");
		EPSG.add("4171");
		EPSG.add("4258");
		EPSG.add("4275");
		EPSG.add("4326");
		EPSG.add("4463");
		EPSG.add("4467");
		EPSG.add("4470");
		EPSG.add("4471");
		EPSG.add("4558");
		EPSG.add("4559");
		EPSG.add("4621");
		EPSG.add("4622");
		EPSG.add("4623");
		EPSG.add("4624");
		EPSG.add("4625");
		EPSG.add("4626");
		EPSG.add("4627");
		EPSG.add("4628");
		EPSG.add("4629");
		EPSG.add("4630");
		EPSG.add("4632");
		EPSG.add("4633");
		EPSG.add("4636");
		EPSG.add("4637");
		EPSG.add("4638");
		EPSG.add("4639");
		EPSG.add("4641");
		EPSG.add("4642");
		EPSG.add("4643");
		EPSG.add("4644");
		EPSG.add("4687");
		EPSG.add("4688");
		EPSG.add("4689");
		EPSG.add("4690");
		EPSG.add("4691");
		EPSG.add("4692");
		EPSG.add("4749");
		EPSG.add("4807");
		EPSG.add("900913");

	}


	public static String getNetwork(ArrayList<Double> BBOX, double tolerance, String output){

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

		if ((Delta_x < 1000) || (Delta_y < 1000)) {


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


	// Main method to launch process
	public static void launch(String[] args) {

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

		getNetwork(BBOX, tolerance, output_file);

	}


}

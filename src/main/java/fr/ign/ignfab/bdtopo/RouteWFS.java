/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab.bdtopo;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.vividsolutions.jts.geom.Geometry;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.conversion.JtsGeOxygene;
import fr.ign.ignfab.util.SimpleHttpClient;
import fr.ign.ignfab.cartetopo.ChargeurCarteTopo;


/**
 * 
 * @author M.-D. Van Damme
 */
public class RouteWFS {

	private static String URL_TYPENAME = "BDTOPO_BDD_WLD_WGS84G:route";
	private static String URL_WFS = "";
	private static String URL_HITS = "";

	public static int NB_NODES = 0;
	public static int NB_EDGES = 0;

	public static JFrame externalFrame;

	public static JProgressBar progressBar = new JProgressBar();
	private static JLabel label = new JLabel("Downloading...");
	
	private static JFrame f = new JFrame("Network extraction");

	private static int NB_PER_PAGE = 1000;
	
	public static String key = "PRATIQUE";
	public static String projection = "2154";
	public static boolean hasProxy = false;
	public static String proxyHost = "";
	public static String proxyPort = "";
	
	private static void createURL(){

		URL_WFS = "http://wxs.ign.fr/"+key+"/geoportail/wfs?"
				+ "service=WFS&request=GetFeature&typeName=" + URL_TYPENAME 
				+ "&srsName=EPSG:"+projection+"&version=2.0.0&outputFormat=json";
		URL_HITS = URL_WFS + "&resulttype=hits";

	}


	/**
	 * Retourne le nombre de troncon de route dans l'emprise.
	 * @param emprise : a1,b1,a2,b2
	 * @return int
	 */
	private static int getNbRouteEmprise(String bbox) {
		
		createURL();

		String urlService = URL_HITS + "&BBOX=" + bbox;
		// System.out.println(urlService);
		int nbHits = 0;

		try {
			SimpleHttpClient client = new SimpleHttpClient(urlService);
			if (RouteWFS.hasProxy) {
				client.connectProxy(RouteWFS.proxyHost, RouteWFS.proxyPort, "GET", "application/json");
			} else {
				client.connect("GET");
			}

			String response = client.getResponse();
			System.out.println(response);

			DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
			builderFactory.setNamespaceAware(true);
			builderFactory.setValidating(false);
			DocumentBuilder documentBuilder = builderFactory.newDocumentBuilder();

			Document rawDocument = documentBuilder.parse(new ByteArrayInputStream(response.getBytes(StandardCharsets.UTF_8)));

			String numberMatched = rawDocument.getDocumentElement().getAttribute("numberMatched");
			nbHits = Integer.parseInt(numberMatched);
			// System.out.println("Nb hits = " + nbHits);

			client.disconnect();

		} catch(Exception e) {
			e.printStackTrace();
		}

		return nbHits;

	}


	/**
	 * 
	 * @param bbox
	 */
	private static BDTopoRoute getTroncon(String bbox) {


		BDTopoRoute bdRoute = new BDTopoRoute();

		int nbRoute = getNbRouteEmprise(bbox);
		int nbiter = nbRoute / NB_PER_PAGE + 1;
		
		progressPercentageConsole(0, nbiter);
	
		int offset = 0;
		
		for (int j = 0; j < nbiter; j++) {

			progressPercentageConsole(j, nbiter);
			
			final int percent = (int)(j*100.0/nbiter);
			
			 SwingUtilities.invokeLater(new Runnable() {
				 
                 public void run() {
                	 
                	 progressBar.setValue(percent);
                	 
                 }}); 

			String urlService = URL_WFS + "&BBOX=" + bbox + "&count=" + NB_PER_PAGE + "&startIndex=" + offset;
			// System.out.println(urlService);
			try {
				SimpleHttpClient client = new SimpleHttpClient(urlService);
				// client.connect("GET");
				if (RouteWFS.hasProxy) {
	                client.connectProxy(RouteWFS.proxyHost, RouteWFS.proxyPort, "GET", "application/json");
	            } else {
	                client.connect("GET");
	            }

				String txtJson = client.getResponse();

				Gson gsonListe = new GsonBuilder().create();
				bdRoute.add(gsonListe.fromJson(txtJson, BDTopoRoute.class));
				// System.out.println("Nb feature recupere = " + bdRoute.getFeatures().size());

				client.disconnect();
				
			} catch(Exception e) {
				e.printStackTrace();
				break;
			}
			offset = offset + NB_PER_PAGE;
		}

		progressPercentageConsole(nbiter, nbiter);

		return bdRoute;

	}


	/**
	 * 
	 * @param bbox
	 */
	@SuppressWarnings("deprecation")
	public static List<String> getBDTopoNetwork(String bbox, double tolerance, boolean gui) {

		List<String> CSV = new ArrayList<>();

		if (gui){
			
			f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
			Container content = f.getContentPane();
			progressBar.setValue(0);
			progressBar.setStringPainted(true);
			

			progressBar.setForeground(Color.GREEN.darker());
			content.add(label, BorderLayout.NORTH);
			content.add(progressBar, BorderLayout.SOUTH);
			
			int x = externalFrame.location().x + externalFrame.size().width/2 - 150;
			int y =  externalFrame.location().y + + externalFrame.size().height/2 - 30;

			f.setLocation(x, y);
			f.setResizable(false);
			f.setSize(300, 60);
			f.setVisible(true);


		}

		// BDTopo
		System.out.println("Network download:");
		BDTopoRoute bdRoute = getTroncon(bbox);

		if (gui){
			
			progressBar.setValue(100);
			label.setText("Building topology...");
			
		}

		System.out.print("Building topology...");
		FT_FeatureCollection<DefaultFeature> collection = bdRoute.getCollectionRoute();
		// System.out.println(collection.size());   

		// Création de la carte TOPO
		String attribute = "sens";
		String groundAttribute = "posSol";
		Map<Object, Integer> orientationMap = new HashMap<Object, Integer>(2);
		orientationMap.put("Direct", new Integer(1));
		orientationMap.put("Inverse", new Integer(-1));
		orientationMap.put("Double", new Integer(2));
		orientationMap.put("NC", new Integer(2));

		CarteTopo networkMap = new CarteTopo("BDTopo network Map");
		ChargeurCarteTopo.importAsEdges(collection, networkMap, attribute,
				orientationMap, groundAttribute, tolerance);


		NB_NODES = networkMap.getPopNoeuds().size();
		NB_EDGES = networkMap.getPopArcs().size();
		
		
		for (Arc arc : networkMap.getPopArcs()) {

			String ligne = "";

			ligne += arc.getAttribute("id") + ",";
			ligne += arc.getAttribute("cleabs") + ",";
			
			if (arc.getAttribute("sens").equals("Inverse")) {
				String source = Integer.toString(arc.getNoeudFin().getId());
				ligne += source + ",";
				String target = Integer.toString(arc.getNoeudIni().getId());
				ligne += target + ",";
				
				String sens = "direct";
				ligne += sens + ",";
				
				ligne += arc.getAttribute("posSol") + ",";
				
				IGeometry geOxyGeom = ((GM_LineString)arc.getGeom()).reverse();
				Geometry geom = null;
				try {
					geom = JtsGeOxygene.makeJtsGeom(geOxyGeom);
					ligne += geom.toText();
				} catch (Exception e) {
					e.printStackTrace();
				}
				
				
			} else {
				String source = Integer.toString(arc.getNoeudIni().getId());
				ligne += source + ",";
				String target = Integer.toString(arc.getNoeudFin().getId());
				ligne += target + ",";
				
				String sens = arc.getAttribute("sens").toString();
				sens = sens.toLowerCase();
				ligne += sens + ",";
				
				ligne += arc.getAttribute("posSol") + ",";
				
				IGeometry geOxyGeom = arc.getGeom();
				Geometry geom = null;
				try {
					geom = JtsGeOxygene.makeJtsGeom(geOxyGeom);
					ligne += geom.toText();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			


			
			// ONE_WAY.add(false);

			// Plus les attributs 

			/*
	      String nature = ""+arc.getAttribute("nature");
	      nature = nature.replace("à", "a");
	      nature = nature.replace("é", "e");
	      nature = nature.replace("è", "e");
	      nature = nature.replace("ê", "e");
	      nature = nature.replace("ç", "c");
	      nature = nature.replace(" ", "_");
	      nature = nature.toLowerCase();
			 */

			

			
			
			//  ligne += nature + ",";

			// -------------------------------------------
			//    Register geometry
			// -------------------------------------------
			
			
			
			

			CSV.add(ligne);
		}

		System.out.println(" ok");

		System.out.println("Nb edges = " + networkMap.getPopArcs().size());
		System.out.println("Nb nodes = " + networkMap.getPopNoeuds().size());

		if (gui){
			f.setVisible(false);
		}

		return CSV;

	}
	
	
	public static String getNetwork(ArrayList<Double> BBOX, double tolerance, String output, boolean gui){

        String out = "";

        System.out.println("----------------------------------------------------------------------");
        System.out.println("ROAD MAP EXTRACTION");
        System.out.println("----------------------------------------------------------------------");

        long tini = System.currentTimeMillis();

        String bbox = BBOX.get(1)+","+BBOX.get(0)+","+BBOX.get(3)+","+BBOX.get(2);

        double xmin = BBOX.get(0);
        double ymin = BBOX.get(1);
        double xmax = BBOX.get(2);
        double ymax = BBOX.get(3);

        System.out.println("xmin  = "+ xmin+" °");
        System.out.println("ymin  = "+ ymin+" °");
        System.out.println("xmax  = "+ xmax+" °");
        System.out.println("ymax  = "+ ymax+" °");

        out += "Extraction terminated with success\r\n";
        out += "------------------------------------------------\r\n";
        out += "xmin  = "+ xmin+" °\r\n";
        out += "ymin  = "+ ymin+" °\r\n";
        out += "xmax  = "+ xmax+" °\r\n";
        out += "xmax  = "+ ymax+" °\r\n";
        out += "------------------------------------------------\r\n";

        double R = 6371000;

        double coeff = Math.cos((ymin+ymax)/2*Math.PI/180);

        double Delta_x = R*Math.abs(xmax-xmin)*Math.PI/180*coeff;
        double Delta_y = R*Math.abs(ymax-ymin)*Math.PI/180;

        if ((Delta_x < 1000) || (Delta_y < 1000)){

            System.out.println("Size: "+(int)Math.floor(Delta_x)+" x "+(int)Math.floor(Delta_y) + " m2");
            out += "Size: "+(int)Math.floor(Delta_x)+" x "+(int)Math.floor(Delta_y) + " m²\r\n";

        }
        else{

            Delta_x /= 1000;
            Delta_y /= 1000;

            System.out.println("Size: "+(int)Math.floor(Delta_x)+" x "+(int)Math.floor(Delta_y) + " km2");
            out += "Size: "+(int)Math.floor(Delta_x)+" x "+(int)Math.floor(Delta_y) + " km²\r\n";

        }

        out += "------------------------------------------------\r\n";

        System.out.println("----------------------------------------------------------------------");

        List<String> csv = RouteWFS.getBDTopoNetwork(bbox, tolerance, gui);

        out += "Tolerance = "+tolerance+"\r\n";
        out += "Number of vertices = "+RouteWFS.NB_NODES+"\r\n";
        out += "Number of edges = "+RouteWFS.NB_EDGES+"\r\n";
        
        try {

            BufferedWriter writer = new BufferedWriter(new FileWriter(output));

            writer.write("link_id,keyabs,source,target,direction,level,wkt\r\n");

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


        long tfin = System.currentTimeMillis();

        int elaps = (int) ((tfin - tini)/1000);

        out += "------------------------------------------------\r\n";
        out += "Network written in \r\n"+output+"\r\n";
        out += "------------------------------------------------\r\n";

        System.out.println("----------------------------------------------------------------------");
        System.out.print("Extraction terminated with success ");
        System.out.println("(elapsed time: " + elaps + " s)");
        System.out.println("----------------------------------------------------------------------");

        out += "Elapsed time: " + elaps + " s\r\n";

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
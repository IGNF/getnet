/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab.bdtopo;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPosition;
import fr.ign.cogit.geoxygene.spatial.coordgeom.DirectPositionList;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;

/**
 * 
 * @author M.-D. Van Damme
 */
public class Geometry {
  
  private String type;
  public String getType() { return this.type; }
  
  private double[][][] coordinates;
  public double[][][] getCoordinates() { return this.coordinates; }
  
  
  /**
   * On construit la géométrie à la main (je me suis perdue dans les readers :o) )
   * 
   */
  public IGeometry getGeom() {
    
    if (this.coordinates != null) {
      if (this.coordinates[0].length != 1) {
        
        // C'est une LINESTRING, on boucle sur les points
        DirectPositionList pointList = new DirectPositionList();
        for (int j = 0; j < this.coordinates[0].length; j++) {
          // On récupère les coordonnées en 2D (format JSON)
          double x = this.coordinates[0][j][0];
          double y = this.coordinates[0][j][1];
          pointList.add(new DirectPosition(x, y));
        }
        
        // On construit la linestring
        GM_LineString ls = new GM_LineString(pointList);
        return ls;
        
      } else {
        // TODO
        System.out.println("MULTILINESTRING");
      }
    }
    
    return null;
    
  }
  
}

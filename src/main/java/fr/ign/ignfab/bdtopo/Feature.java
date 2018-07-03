/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab.bdtopo;

/**
 * 
 * @author M.-D. Van Damme
 */
public class Feature {
  
  private String id;
  public String getId() { return this.id; }
  
  private Geometry geometry;
  public Geometry getGeometry() { return this.geometry; }
  
  private Properties properties;
  public Properties getProperties() { return this.properties; }
  
}

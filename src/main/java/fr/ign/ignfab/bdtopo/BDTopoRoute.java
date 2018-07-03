/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab.bdtopo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.spatial.geomroot.IGeometry;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.FT_FeatureCollection;
import fr.ign.cogit.geoxygene.feature.SchemaDefaultFeature;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.AttributeType;
import fr.ign.cogit.geoxygene.schema.schemaConceptuelISOJeu.FeatureType;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;


/**
 * 
 * @author M.-D. Van Damme
 */
public class BDTopoRoute {
  
    private List<Feature> features;
    
    public BDTopoRoute() {
        this.features = new ArrayList<Feature>();
    }
  
    public List<Feature> getFeatures() { return this.features; }
  
    public void add(BDTopoRoute autresRoutes) {
        this.features.addAll(autresRoutes.getFeatures());
    }
  
  
    public FT_FeatureCollection<DefaultFeature> getCollectionRoute() {
    
        FT_FeatureCollection<DefaultFeature> collection = new FT_FeatureCollection<DefaultFeature>();
        
        FeatureType newFeatureType = new FeatureType();
        newFeatureType.setTypeName("route");
        newFeatureType.setGeometryType(GM_LineString.class);
        // newFeatureType.setGeometryType(ILineString.class);
        
        AttributeType idTroncon = new AttributeType("id", "id", "String");
        AttributeType natureTroncon = new AttributeType("nature", "nature", "String");
        AttributeType posSolTroncon = new AttributeType("posSol", "posSol", "String");
        AttributeType sensTroncon = new AttributeType("sens", "sens", "String");
        AttributeType cleabsTroncon = new AttributeType("cleabs", "cleabs", "String");
        
        newFeatureType.addFeatureAttribute(idTroncon);
        newFeatureType.addFeatureAttribute(natureTroncon);
        newFeatureType.addFeatureAttribute(posSolTroncon);
        newFeatureType.addFeatureAttribute(sensTroncon);
        newFeatureType.addFeatureAttribute(cleabsTroncon);
        
        // Création d'un schéma associé au featureType
        SchemaDefaultFeature schema = new SchemaDefaultFeature();
        schema.setFeatureType(newFeatureType);
            
        newFeatureType.setSchema(schema);
                        
        Map<Integer, String[]> attLookup = new HashMap<Integer, String[]>(0);
        attLookup.put(new Integer(0), new String[] { idTroncon.getNomField(), idTroncon.getMemberName() });
        attLookup.put(new Integer(1), new String[] { natureTroncon.getNomField(), natureTroncon.getMemberName() });
        attLookup.put(new Integer(2), new String[] { posSolTroncon.getNomField(), posSolTroncon.getMemberName() });
        attLookup.put(new Integer(3), new String[] { sensTroncon.getNomField(), sensTroncon.getMemberName() });
        attLookup.put(new Integer(4), new String[] { cleabsTroncon.getNomField(), cleabsTroncon.getMemberName() });
        schema.setAttLookup(attLookup);
        
        collection.setFeatureType(newFeatureType);
        
        for (Feature feature : features) {
      
            // System.out.println(feature.getProperties().getId());
          
            IGeometry line = feature.getGeometry().getGeom();
            DefaultFeature f = new DefaultFeature(line);
              
            f.setFeatureType(newFeatureType);
            f.setSchema(schema);
              
            f.setAttributes(new Object[schema.getAttLookup().size()]);
            f.setAttribute("id", feature.getProperties().getId());
            f.setAttribute("nature", feature.getProperties().getNature());
            f.setAttribute("posSol", feature.getProperties().getPosSol());
            f.setAttribute("sens", feature.getProperties().getSens());
            f.setAttribute("cleabs", feature.getProperties().getId());
              
            // System.out.println(feature.getProperties().getSens());
              
            collection.add(f);
          
        }
        
        return collection;
    }
  

}

/*******************************************************************************
 * 
 * @copyright IGN - 2018
 * 
 * This software is released under the licence CeCILL
 * see <a href="https://fr.wikisource.org/wiki/Licence_CeCILL_version_2">https://fr.wikisource.org/wiki/Licence_CeCILL_version_2</a>
 *
 ******************************************************************************/
package fr.ign.ignfab.cartetopo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import fr.ign.cogit.geoxygene.api.feature.IFeature;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPosition;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IDirectPositionList;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.IEnvelope;
import fr.ign.cogit.geoxygene.api.spatial.coordgeom.ILineString;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Arc;
import fr.ign.cogit.geoxygene.contrib.cartetopo.CarteTopo;
import fr.ign.cogit.geoxygene.contrib.cartetopo.Noeud;
import fr.ign.cogit.geoxygene.feature.DefaultFeature;
import fr.ign.cogit.geoxygene.feature.SchemaDefaultFeature;
import fr.ign.cogit.geoxygene.spatial.coordgeom.GM_LineString;
import fr.ign.cogit.geoxygene.util.index.Tiling;

/**
 * 
 * 
 *
 */
public class ChargeurCarteTopo {
  
  public static void importAsEdges(Collection<? extends IFeature> edges,
      CarteTopo map, String orientationAttribute,
      Map<Object, Integer> orientationMap, String groundPositionAttribute,
      double tolerance) {
    
      // Import des arcs
      for (IFeature element : edges) {
          
        Arc arc = map.getPopArcs().nouvelElement();
        
        ILineString ligne = new GM_LineString((IDirectPositionList) element.getGeom().coord().clone());
        arc.setGeometrie(ligne);
        
        if (orientationAttribute == null || orientationAttribute.isEmpty()) {
          arc.setOrientation(2);
        } else {
          Object value = element.getAttribute(orientationAttribute);
          if (orientationMap != null) {
            Integer orientation = orientationMap.get(value);
            if (orientation != null) {
              arc.setOrientation(orientation.intValue());
            } 
          } else {
            if (value instanceof Number) {
              Number v = (Number) value;
              arc.setOrientation(v.intValue());
            } else {
              if (value instanceof String) {
                String v = (String) value;
                try {
                  arc.setOrientation(Integer.parseInt(v));
                } catch (Exception e) {
                  e.printStackTrace();
                }
              } 
            }
          }
        }
        arc.addCorrespondant(element);
        arc.setPoids(arc.getGeometrie().length());
        
        // Plus les attributs
        SchemaDefaultFeature schema = ((DefaultFeature)element).getSchema();
        arc.setSchema(schema);
        Object[] valAttribute = new Object[schema.getFeatureType().getFeatureAttributes().size()];
        for (int k = 0; k < element.getFeatureType().getFeatureAttributes().size(); k++) {
          valAttribute[k] = element.getAttribute(element.getFeatureType().getFeatureAttributes().get(k));
        }
        
        arc.setAttributes(valAttribute);
          
      } // fin de la boucle sur edges
    
      
      // initialisation de l'index au besoin
      // si on peut, on prend les mêmes paramètres que le dallage des arcs
      if (!map.getPopNoeuds().hasSpatialIndex()) {
        if (map.getPopArcs().hasSpatialIndex()) {
          map.getPopNoeuds().initSpatialIndex(map.getPopArcs().getSpatialIndex());
          map.getPopNoeuds().getSpatialIndex().setAutomaticUpdate(true);
        } else {
          IEnvelope enveloppe = map.getPopArcs().envelope();
          int nb = (int) Math.sqrt(map.getPopArcs().size() / 20);
          if (nb == 0) {
            nb = 1;
          }
          map.getPopNoeuds().initSpatialIndex(Tiling.class, true, enveloppe, nb);
        }
      }
      
      //
      for (Arc arc : map.getPopArcs()) {
        IDirectPosition p1 = arc.getGeometrie().getControlPoint(0);
        IDirectPosition p2 = arc.getGeometrie().getControlPoint(
                  arc.getGeometrie().sizeControlPoint() - 1);
        int posSol = 0;
        if (groundPositionAttribute != null) {
          posSol = Integer.parseInt(arc.getCorrespondant(0)
                      .getAttribute(groundPositionAttribute).toString());
        }
        Collection<Noeud> candidates = map.getPopNoeuds().select(p1, tolerance);
        if (candidates.isEmpty()) {
          Noeud n1 = map.getPopNoeuds().nouvelElement(p1.toGM_Point());
          arc.setNoeudIni(n1);
          n1.setDistance(posSol);
        } else {
          for (Noeud n : candidates) {
                  if (n.getDistance() == posSol) {
                      arc.setNoeudIni(n);
                      break;
                  }
              }
              if (arc.getNoeudIni() == null) {
                  Noeud n1 = map.getPopNoeuds().nouvelElement(p1.toGM_Point());
                  arc.setNoeudIni(n1);
                  n1.setDistance(posSol);
              }
          }
          candidates = map.getPopNoeuds().select(p2, tolerance);
      if (candidates.isEmpty()) {
        Noeud n1 = map.getPopNoeuds().nouvelElement(p2.toGM_Point());
        arc.setNoeudFin(n1);
        n1.setDistance(posSol);
      } else {
        for (Noeud n : candidates) {
          if (n.getDistance() == posSol) {
            arc.setNoeudFin(n);
            break;
          }
        }
        if (arc.getNoeudFin() == null) {
          Noeud n1 = map.getPopNoeuds().nouvelElement(p2.toGM_Point());
          arc.setNoeudFin(n1);
          n1.setDistance(posSol);
        }
      }
    }
    List<Noeud> toRemove = new ArrayList<Noeud>(0);
    // connect the single nodes
    for (Noeud n : map.getPopNoeuds()) {
      if (n.arcs().size() == 1) {
        Collection<Noeud> candidates = map.getPopNoeuds().select(n.getCoord(),
            tolerance);
        candidates.remove(n);
        candidates.removeAll(toRemove);
        if (candidates.size() == 1) {
          Noeud candidate = candidates.iterator().next();
          for (Arc a : new ArrayList<Arc>(n.getEntrants())) {
            candidate.addEntrant(a);
          }
          for (Arc a : new ArrayList<Arc>(n.getSortants())) {
            candidate.addSortant(a);
          }
          toRemove.add(n);
        }
      }
    }
    map.enleveNoeuds(toRemove);
  }

}

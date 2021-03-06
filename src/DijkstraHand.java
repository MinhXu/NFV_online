import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.PriorityQueue;
import java.util.Set;

//import org.iitj.complex.graph.Vertex;



import org.jgrapht.graph.DefaultDirectedWeightedGraph;

import edu.uci.ics.jung.graph.DirectedGraph;
import edu.uci.ics.jung.graph.Graph;

/**
 * @author Anupam
 *
 */
public class DijkstraHand {

 //private DirectedGraph<Vertex, String> g;
	private DirectedGraph<Vertex, String> g;
 private Set<List<Vertex>> allShortestPaths;
 
 public DijkstraHand(DirectedGraph<Vertex, String> g){
 this.g = g;
 }
 
 private Vertex getSourceFromId(Integer sourceId){
 Collection<Vertex> vertices = g.getVertices();
 for (Iterator<Vertex> iterator = vertices.iterator(); iterator.hasNext();) {
 Vertex vertex = (Vertex) iterator.next();
 if(vertex.getId() == sourceId)
 return vertex;
 }
 
 return null;
 }
 
 /**
  * Computes all shortest paths to all the vertices in the graph
  * using the Dijkstra's shortest path algorithm.
  * 
  * @param sourceId : Starting node from which to find the shortest paths.
  */
 public void computeAllShortestPaths(Integer sourceId){
 
 Vertex source = getSourceFromId(sourceId);
 if(source==null)
	 return;
 source.sourceDistance = 0;
        PriorityQueue<Vertex> vertexQueue = new PriorityQueue<Vertex>();
       vertexQueue.add(source);
       List<Vertex> prev = null;

 while (!vertexQueue.isEmpty()) {
 Vertex u = vertexQueue.poll();

 Collection<Vertex> neighbs = g.getSuccessors(u);
 //Collection<Vertex> neighbs = g.getNeighbors(u);
 for (Iterator<Vertex> iterator = neighbs.iterator(); iterator.hasNext();) {
 Vertex nv = (Vertex) iterator.next();
 prev = nv.getPrev();
 double weight = 1;
 double distanceThroughU = u.sourceDistance + weight;
 if (distanceThroughU < nv.sourceDistance) {
 vertexQueue.remove(nv);
 nv.sourceDistance = distanceThroughU;
 nv.setPrevious(u);
 vertexQueue.add(nv);
 prev = new ArrayList<Vertex>();
 prev.add(u);
 nv.setPrev(prev);
 } else if(distanceThroughU == nv.sourceDistance){
 if(prev != null)
 prev.add(u);
 else {
 prev = new ArrayList<Vertex>();
 prev.add(u);
 nv.setPrev(prev);
 }
 }
 }
 }
 }
 
 /**
  * @param target
  * @return A List of nodes in order as they would appear in a shortest path.
  * (There can be multiple shortest paths present. This method returns just one
  * of those paths.)
  */
 public List<Vertex> getShortestPathTo(Vertex target){
        List<Vertex> path = new ArrayList<Vertex>();
        for (Vertex vertex = target; vertex != null; vertex = vertex.getPrevious())
            path.add(vertex);
        Collections.reverse(path);
        return path;
    }
 
 /**
  * @param target
  * @return A set of all possible shortest paths from the source to the given
  * target.
  */
 public Set<List<Vertex>> getAllShortestPathsTo(Vertex target){
 allShortestPaths = new HashSet<List<Vertex>>();
 
 getShortestPath(new ArrayList<Vertex>(), target);
 
 return allShortestPaths;
 }
 
 /**
  * Recursive method to enumerate all possible shortest paths and
  * add each path in the set of all possible shortest paths.
  * 
  * @param shortestPath
  * @param target
  * @return
  * 
  */
 private List<Vertex> getShortestPath(List<Vertex> shortestPath, Vertex target){
 List<Vertex> prev = target.getPrev();
 if(prev == null){
 shortestPath.add(target);
 Collections.reverse(shortestPath);
 allShortestPaths.add(shortestPath);
 } else {
 List<Vertex> updatedPath = new ArrayList<Vertex>(shortestPath);
 updatedPath.add(target);
 
 for (Iterator<Vertex> iterator = prev.iterator(); iterator.hasNext();) {
 Vertex vertex = (Vertex) iterator.next();
 getShortestPath(updatedPath, vertex);
 }
 }
 return shortestPath;
 }
}
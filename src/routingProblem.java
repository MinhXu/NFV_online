import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
//import java.util.ListIterator;
import java.util.Vector;
import java.awt.image.AreaAveragingScaleFilter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.PasswordAuthentication;
import java.util.*;

import javax.swing.SwingUtilities;

import org.apache.commons.io.FileUtils;
//import org.apache.commons.collections15.Transformer;
//import gurobi.*;

import org.jgraph.JGraph;
import org.jgrapht.DirectedGraph;
import org.jgrapht.GraphPath;
import org.jgrapht.UndirectedGraph;
import org.jgrapht.VertexFactory;
import org.jgrapht.alg.*;
import org.jgrapht.graph.*;
import org.jgrapht.Graph;
import org.jgrapht.generate.*;
import org.jgrapht.traverse.*;
import org.omg.DynamicAny._DynEnumStub;

import edu.uci.ics.jung.graph.*;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.algorithms.layout.KKLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.VertexLabel.Position;

public class routingProblem {
	static BufferedWriter out;
	static BufferedReader in;
	static int c,noVertex,noFunction,noDemand,z,E,_no;
	static int limitedNo;
	static double alpha, beta,gama,theta;
	static nGraph g;
	static nGraph g_edit;
	static ArrayList<nFunction> functionArr;
	static ArrayList<nDemand> DemandArray;
	static double[][] link_load;
	static double[] node_load;
	static long _duration=0;
	static double maxlinkload=0.000000000;
	static double maxnodeload=0.0;
	static double acceptRatio=0.0;
	static Double[] zero ={0.0,0.0,0.0};
	static ArrayList<ArrayList<Integer>> solution_node;
	static ArrayList<ArrayList<Integer>> solution_func;
	static ArrayList<Integer> solution_id;
	static Vector<Vector<Double>> dataReal;
	static double prob;
	static ArrayList<Pair> funLoc;
	static ArrayList<Integer> h ;
	static boolean fl=false;
	static int flag = 0;
	static int source=-1;
	static int destination = -1;
	static int importNo =0;
	static boolean anchorNode = false;
	
	public static Vector<Double> getLamdaF(int id)
	{
		if(id==0) return new Vector<Double>(Arrays.asList(zero));
		for(int i=0;i<functionArr.size();i++)
			if (functionArr.get(i).id() ==id)
				return functionArr.get(i).getLamda();
		return null;
	}
	public static nFunction getFunction(int id)
	{
		if(id==0) return null;
		for(int i=0;i<functionArr.size();i++)
			if (functionArr.get(i).id() ==id)
				return functionArr.get(i);
		return null;
	}
	
	public static double getBwService(int id)
	{
		if(id==0) return 0;
		for(int i=0;i<DemandArray.size();i++)
			if(DemandArray.get(i).getId()==id)
				return DemandArray.get(i).getBw();
		return -1;
	}
	public static nDemand getDemand(int id)
	{
		for (int i=0;i<DemandArray.size();i++)
			if(DemandArray.get(i).getId()==id)
				return DemandArray.get(i);
		return null;
	}
	
	public static boolean IsCapacity()
	{
		Vector<Double> resourceRequirement = new Vector<Double>(Arrays.asList(zero));
		Vector<Double> resourceCapacity = new Vector<Double>(Arrays.asList(zero));
		for (int i=0;i<noDemand;i++)
		{
			ArrayList<Integer> fArr = DemandArray.get(i).getFunctions();
			for (int j=0;j<fArr.size();j++)
			{
				resourceRequirement = UtilizeFunction.add(resourceRequirement,getFunction(fArr.get(j)).getLamda());
			}
		}
		for (int i=0;i<noVertex;i++)
			resourceCapacity = UtilizeFunction.add(resourceCapacity, g.getCap(i+1));
		if(UtilizeFunction.isBig(resourceRequirement, resourceCapacity))
			return false;
		return true;
	}
	
	
	public static void ReadInputFile(String fileName)
	{
		
		File file = new File(fileName);
		functionArr = new ArrayList<nFunction>();
		DemandArray = new ArrayList<nDemand>();
		ArrayList<Integer> vnfNodeLst = new ArrayList<Integer>();
        try {
			in = new BufferedReader(new FileReader(file));
			//First line -> set of parameters
			String[] tempLine=in.readLine().split(" ");
			noVertex= Integer.parseInt(tempLine[0]);
			noFunction= Integer.parseInt(tempLine[1]);
			noDemand= Integer.parseInt(tempLine[2]);
			
			//second line -> set of Functions
			for (int i=0;i<noFunction;i++)
			{
				vnfNodeLst = new ArrayList<Integer>();
				tempLine = in.readLine().split(";"); 
				Vector<Double> lamda= new Vector<>(3);
				for (int j=0;j<3;j++)
		        	lamda.addElement(Double.parseDouble(tempLine[0].split(" ")[j]));
				String[] nodeLine = tempLine[1].split(" ");
				for(int j=0; j< nodeLine.length;j++)
					vnfNodeLst.add(Integer.parseInt(nodeLine[j]));
				functionArr.add(new nFunction(i+1,lamda,vnfNodeLst));
			}
			
			
			int id, src,des;
			double bandwidth;
			String[] tempSubLine;
			ArrayList<Integer> f;
			
			//set of new demands
			for (int i=0;i<noDemand;i++)
			{
				tempLine = in.readLine().split(";");
				id =Integer.parseInt(tempLine[0]);
				src =Integer.parseInt(tempLine[1]);
				des =Integer.parseInt(tempLine[2]);
				bandwidth =Double.parseDouble(tempLine[3]);
				tempSubLine = tempLine[4].split(" ");
				//set of Function
				f= new ArrayList<Integer>();
				for (int j=0;j<tempSubLine.length;j++)
				{
					f.add(Integer.parseInt(tempSubLine[j]));
				}
				
				DemandArray.add(new nDemand(id, src, des, bandwidth,  f));
			}
			
			//luu vao mang noVertex+1 chieu
			
			Vector<Vector<Double>> cap = new Vector<Vector<Double>>(noVertex+1);
			ArrayList<List<Double>> w = new ArrayList<List<Double>>();			
			
			for (int i=0;i <noVertex;i++)
			{
				tempLine = in.readLine().split(" ");
				Vector<Double> t= new Vector<>(3);
	   	        for (int j=0;j<3;j++)
	   	        	t.addElement(Double.parseDouble(tempLine[j]));
	   	        cap.add(t);
			}
			
			for (int i=0;i<noVertex;i++)
			{
				ArrayList<Double> temp= new ArrayList<>();
				tempLine = in.readLine().split(" ");
				for(int j=0;j<noVertex;j++)
				{
					temp.add(Double.parseDouble(tempLine[j]));
				}
				w.add(temp);
			}
			g= new nGraph(cap,w);
			g_edit =new nGraph(cap,w);
			link_load= new double[noVertex][noVertex];
			solution_node= new ArrayList<ArrayList<Integer>>();
			solution_id = new ArrayList<Integer>();
			if (noVertex*noFunction <  noFunction+4)
				_no=5;
			else
				_no = 5;
			
            // Always close files.
            in.close();  
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	static ArrayList<List<Integer>> Dist;// Dist(u,v) is a distance between u and v
	public static boolean _Dist()
	{
		SimpleWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
		Dist = new ArrayList<List<Integer>>();
		for(int i=0;i<noVertex+1;i++)
		{
			ArrayList<Integer> temp= new ArrayList<Integer>();
        	for (int j=0;j<noVertex+1;j++)
        	{
        		if(i==j)
        			temp.add(0);
        		else
        			temp.add(-1);
        	}
        	Dist.add(temp);
		}
		for (int j=0;j<noVertex;j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        DefaultWeightedEdge[] e= new DefaultWeightedEdge[(noVertex*(noVertex-1))/2];
        int id=0;
        
        for (int j=0;j<noVertex-1;j++)
        {	        	
        	for(int k=j+1;k<noVertex;k++)
        	{
        		e[id]=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
        		g_i.setEdgeWeight(e[id], g.getEdgeWeight((j +1), (k+1)));
        		id++;
        	}
        }
        for(int i=0;i<noVertex-1;i++)
        	for (int j=i+1;j<noVertex;j++)
        	{
        		List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+(i+1), "node"+(j+1));
        		if(_p!=null)
        		{
        			Dist.get(i+1).set(j+1, _p.size());
        			Dist.get(j+1).set(i+1, _p.size());
        		}
        		else
        		{
        			Dist.get(i+1).set(j+1, -1);
        			Dist.get(j+1).set(i+1, -1);
        		}
        	} 
        return true;
        
	}
	
	public static ArrayList<Integer> ShortestPath(int src, int dest, ExGraph _g,double maxBw,ArrayList<ArrayList<Integer>> mark,boolean flag)
	{
		ArrayList<Integer> _shortestPath = new ArrayList<Integer>();
		//SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
		SimpleWeightedGraph<String, DefaultWeightedEdge> g_i = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
        		
		for (int j=0;j<_g.getV();j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        //DefaultWeightedEdge[] e= new DefaultWeightedEdge[(_g.getV()*(_g.getV()-1))/2];
        //int id=0;        
        for (int j=0;j<_g.getV();j++)
        {	        	
        	for(int k=j+1;k<_g.getV();k++)
        	{
        		if(g.getEdgeWeight(j+1, k+1)>=maxBw)
        		{
        			DefaultWeightedEdge e=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e, _g.getEdgeWeight((j+1), (k+1)));
        		}
        	}
        } 
        for(int i=0;i<mark.size();i++)
        {
        	for (int j=0;j<(mark.get(i).size()/3+1);j++)
        	{
        		int del= UtilizeFunction.randInt(0, mark.get(i).size()-2);
        		g_i.removeEdge("node"+mark.get(i).get(del), "node"+mark.get(i).get(del+1));
        	}
        }
        List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+src, "node"+dest);
        int source;
		if(_p!=null)
		{
			_shortestPath.add(src);
			source=src;
			while (_p.size()>0)
			{	
				int ix =0;
				for(int l=0;l<_p.size();l++)
				{
					int int_s =Integer.parseInt(g_i.getEdgeSource(_p.get(l)).replaceAll("[\\D]", ""));
					int int_t =Integer.parseInt(g_i.getEdgeTarget(_p.get(l)).replaceAll("[\\D]", ""));
					if( int_s == source )
					{
						_shortestPath.add(int_t);
						source = int_t;
						ix = l;
						g_edit.setEdgeWeight(int_s, int_t, g_edit.getEdgeWeight(int_s, int_t)-maxBw);
						break;
					}
					if( int_t == source)
					{
						_shortestPath.add(int_s);
						source = int_s;
						ix = l;
						g_edit.setEdgeWeight(int_s, int_t, g_edit.getEdgeWeight(int_s, int_t)-maxBw);
						break;
					}
				}
				_p.remove(ix);
			}
//			for(int _i:_shortestPath)
//				{
//					System.out.print(_i+",");
//				}						
		}
		else
		{
			//System.out.print("khong tim duoc duong di giua"+src+"va"+ dest);
			return null;
			
		}
        
        
		return _shortestPath;
	}
	
	public static ArrayList<Integer> ShortestPath(int src, int dest, nGraph _g,double maxBw)
	{
		ArrayList<Integer> _shortestPath = new ArrayList<Integer>();
		//SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
        SimpleWeightedGraph<String, DefaultWeightedEdge> g_i = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		for (int j=0;j<g.getV();j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        //DefaultWeightedEdge[] e= new DefaultWeightedEdge[(g.getV()*(g.getV()-1))/2];
        //int id=0;        
        for (int j=0;j<g.getV();j++)
        {	        	
        	for(int k=j+1;k<g.getV();k++)
        	{
        		if(j!=k&&_g.getEdgeWeight(j+1, k+1)>=maxBw)
        		{
        			DefaultWeightedEdge e=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e, g.getEdgeWeight((j+1), (k+1)));
        		}
        	}
        }       
        List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+src, "node"+dest);
        int source;
		if(_p!=null)
		{
			_shortestPath.add(src);
			source=src;
			while (_p.size()>0)
			{	
				int ix =0;
				for(int l=0;l<_p.size();l++)
				{
					int int_s =Integer.parseInt(g_i.getEdgeSource(_p.get(l)).replaceAll("[\\D]", ""));
					int int_t =Integer.parseInt(g_i.getEdgeTarget(_p.get(l)).replaceAll("[\\D]", ""));
					if( int_s == source )
					{
						_shortestPath.add(int_t);
						source = int_t;
						ix = l;
						//_g.setEdgeWeight(int_s, int_t, _g.getEdgeWeight(int_s, int_t)-maxBw);
						break;
					}
					if( int_t == source)
					{
						_shortestPath.add(int_s);
						source = int_s;
						ix = l;
						//_g.setEdgeWeight(int_s, int_t, _g.getEdgeWeight(int_s, int_t)-maxBw);
						break;
					}
				}
				_p.remove(ix);
			}
//			for(int _i:_shortestPath)
//				{
//					System.out.print(_i+",");
//				}						
		}
		else
		{
			//System.out.print("khong tim duoc duong di giua "+src+" va "+ dest);
			return null;
			
		}
        
        
		return _shortestPath;
	}
	
	
	
	public static ArrayList<Integer> ShortestPath(int src, int dest, double maxBw)
	{
		ArrayList<Integer> _shortestPath = new ArrayList<Integer>();
		//SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
		SimpleWeightedGraph<String, DefaultWeightedEdge> g_i = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		for (int j=0;j<g_edit.getV();j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        //DefaultWeightedEdge[] e= new DefaultWeightedEdge[(g_edit.getV()*(g_edit.getV()-1))/2];
        //int id=0;        
        for (int j=0;j<g_edit.getV();j++)
        {	        	
        	for(int k=j+1;k<g_edit.getV();k++)
        	{
        		if(g_edit.getEdgeWeight(j+1, k+1)>=maxBw)
        		{
        			DefaultWeightedEdge e=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e, g.getEdgeWeight((j+1), (k+1)));
	        		//id++;
        		}
        	}
        }       
        List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+src, "node"+dest);
        int source;
		if(_p!=null)
		{
			_shortestPath.add(src);
			source=src;
			while (_p.size()>0)
			{	
				int ix =0;
				for(int l=0;l<_p.size();l++)
				{
					int int_s =Integer.parseInt(g_i.getEdgeSource(_p.get(l)).replaceAll("[\\D]", ""));
					int int_t =Integer.parseInt(g_i.getEdgeTarget(_p.get(l)).replaceAll("[\\D]", ""));
					if( int_s == source )
					{
						_shortestPath.add(int_t);
						source = int_t;
						ix = l;
						break;
					}
					if( int_t == source)
					{
						_shortestPath.add(int_s);
						source = int_s;
						ix = l;
						break;
					}
				}
				_p.remove(ix);
			}
//			for(int _i:_shortestPath)
//				{
//					System.out.print(_i+",");
//				}						
		}
		else
		{
			//System.out.print("khong tim duoc duong di giua"+src+"va"+ dest);
			return null;
			
		}
        
        
		return _shortestPath;
	}
	
	public static ArrayList<ArrayList<Integer>> allShortestPath(int src, int dest,nGraph _g, double maxBw)
	{
		ArrayList<ArrayList<Integer>> _shortestPathLst = new ArrayList<>();
		ArrayList<Integer> _shortestPath = new ArrayList<>();
		ArrayList<Link> linkSet = new ArrayList<>();
		//SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		SimpleWeightedGraph<String, DefaultWeightedEdge> g_i = new SimpleWeightedGraph<>(DefaultWeightedEdge.class);
		for (int j=0;j<_g.getV();j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        //DefaultWeightedEdge[] e= new DefaultWeightedEdge[(g_edit.getV()*(g_edit.getV()-1))/2];
        //int id=0;        
        for (int j=0;j<_g.getV();j++)
        {	        	
        	for(int k=j+1;k<_g.getV();k++)
        	{
        		if(_g.getEdgeWeight(j+1, k+1)>=maxBw)
        		{
        			DefaultWeightedEdge e=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		//g_i.setEdgeWeight(e, _g.getEdgeWeight((j+1), (k+1)));
        			g_i.setEdgeWeight(e, 1.0);
        		}
        	}
        }       
        //List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+src, "node"+dest);
        FloydWarshallShortestPaths<String,DefaultWeightedEdge> floyGraph = new FloydWarshallShortestPaths<>(g_i);
        String srcStr = "node"+src;
        String destStr ="node"+ dest;
        GraphPath<String, DefaultWeightedEdge> spLst = floyGraph.getShortestPath(srcStr, destStr);
        
		if(spLst!=null)
		{
			srcStr= spLst.getStartVertex();
	        destStr = spLst.getEndVertex();
	        for (DefaultWeightedEdge i: spLst.getEdgeList())
	        {
	        	String strStart = g_i.getEdgeSource(i);
	        	String strEnd = g_i.getEdgeTarget(i);
	        	int int_s =Integer.parseInt(strStart.replaceAll("[\\D]", ""));
				int int_t =Integer.parseInt(strEnd.replaceAll("[\\D]", ""));
				linkSet.add(new Link(int_s, int_t));
				System.out.print("["+int_s+","+ int_t+"]" + ";");
	        }  
	        System.out.println();
	        //xet tung dinh o trong linkSet

        	
        	int start =src;
        	Queue<Integer> startLst = new LinkedList<>(); 
        	startLst.add(src);
	        int lenLinkSet =linkSet.size();
	        _shortestPath.add(src);
	        _shortestPathLst.add(_shortestPath);
	        while (lenLinkSet>0)
	        {
	        	int lenShortestList = _shortestPathLst.size();
	        	for (int i=0;i<lenShortestList;i++)
    			{
    				_shortestPath = _shortestPathLst.get(i);
    				start = _shortestPath.get(_shortestPath.size()-1);
    				ArrayList<Integer> endLst = new ArrayList<>(); 
            		for (Link l : linkSet)
    	        	{
    	        		if(l.getStart()==start)
    	        		{
    	        			endLst.add(linkSet.indexOf(l));//luu lai index cua link l in linkSet
    	        		} 	        		
    	        	}
            		if(endLst.size()>0)
            		{
            			ArrayList<Integer> tempPath =_shortestPath;
                		_shortestPath.add(linkSet.get(endLst.get(0)).getEnd());
                		_shortestPathLst.set(i, _shortestPath);
                		for (int idEnd=1;idEnd < endLst.size();idEnd++)
                		{
                			_shortestPath = tempPath;
                			_shortestPath.add(linkSet.get(idEnd).getEnd()); //and them end node vao mang
                			_shortestPathLst.add(_shortestPath);
                		}
                		for(int idLink:endLst)
                		{
                			linkSet.remove(idLink);
                		}        
            		}
            		    		
    			}
	        	lenLinkSet =linkSet.size();
	        }
		}
		else
		{
			//System.out.print("khong tim duoc duong di giua"+src+"va"+ dest);
			return null;
			
		}       
        for (int i = 0;i< _shortestPathLst.size();i++)
        {
        	System.out.print(i+ ": [");
        	for (int j=0;j<_shortestPathLst.get(i).size();j++)
        		System.out.print(_shortestPathLst.get(i).get(j)+" ");
        	System.out.println("]");
        }
		return _shortestPathLst;
	}
	
	
	public static boolean nonNFV(String outFile)
	{
		List<DefaultWeightedEdge> _p;
		List<Integer> nodeList;
		//final long startTime = System.currentTimeMillis();
		
		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
			int[] rank_service= new int[noDemand];
			if(noDemand>1)
			{
				for (int i=0;i<noDemand;i++)
					rank_service[i]= i+1;
				for(int i=0;i<noDemand-1;i++)
				{
					int temp=i;
					for (int j=i+1;j<noDemand;j++)
						if(getDemand(rank_service[j]).getBw()>getDemand(rank_service[temp]).getBw())
							temp=j;
					int k= rank_service[i];
					rank_service[i]=rank_service[temp];
					rank_service[temp]=k;
				}
			}
			else
			{
				rank_service[0]=1;
			}
			SimpleWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
	        for (int j=0;j<noVertex;j++)
	        {
	        	g_i.addVertex("node"+(j+1));
	        }
	        DefaultWeightedEdge[] e= new DefaultWeightedEdge[(noVertex*(noVertex-1))/2];
	        int id=0;
	        
	        for (int j=0;j<noVertex-1;j++)
	        {	        	
	        	for(int k=j+1;k<noVertex;k++)
	        	{
	        		e[id]=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e[id], g.getEdgeWeight((j +1), (k+1)));
	        		id++;
	        	}
	        }
	        int i=0;
			while(i<noDemand)
			{
				//tim duong di cho moi demand
				//tuy thuoc vao bandwidth
				nDemand _d= getDemand(rank_service[i]);
				if(_d.getSrc()==_d.getDest())
					
				{
					out.write("["+_d.getSrc()+"]");
					out.newLine();
				}
				else
				{
				//remove edges which haven't enough capacity
				DefaultWeightedEdge[] removed_edge = new DefaultWeightedEdge[id];
				int no_removed_edge =0;
				for( DefaultWeightedEdge v:g_i.edgeSet())
				{
					if(g_i.getEdgeWeight(v)<_d.getBw())
						removed_edge[no_removed_edge++]=v;				
				}
				for (int j=0;j<no_removed_edge;j++)
					g_i.removeEdge(removed_edge[j]);
				_p =  DijkstraShortestPath.findPathBetween(g_i, "node"+_d.getSrc(), "node"+_d.getDest());
				
				int source = _d.getSrc();
				if(_p!=null)
				{
					
					nodeList = new ArrayList<Integer>();
					// sau do chon duong ngan nhat 
					nodeList.add(source);
					while (_p.size()>0)
					{	
						int ix =0;
						for(int l=0;l<_p.size();l++)
						{
							int int_s =Integer.parseInt(g_i.getEdgeSource(_p.get(l)).replaceAll("[\\D]", ""));
							int int_t =Integer.parseInt(g_i.getEdgeTarget(_p.get(l)).replaceAll("[\\D]", ""));
							//value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth() ;
							//value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth() ;	
							if( int_s == source )
							{
								nodeList.add(int_t);
								source = int_t;
								ix = l;
								g.setEdgeWeight(int_s, int_t, g.getEdgeWeight(int_s, int_t)-_d.getBw());
								break;
							}
							if( int_t == source)
							{
								nodeList.add(int_s);
								source = int_s;
								ix = l;
								g.setEdgeWeight(int_s, int_t, g.getEdgeWeight(int_s, int_t)-_d.getBw());
								break;
							}
						}
						_p.remove(ix);	
					}
					//in ra file
					out.write("[");
					for(int _i:nodeList)
					{
						//System.out.print(_i+",");
						out.write(_i+", ");
					}
					//System.out.println();
					out.write("]");
					out.newLine();
				}
				else
				{
					//khong tim duoc duong di -> khong tim ra giai phap
					return false;
				}
				}
				//value_bandwidth +=weight_path * g.getPriceBandwidth() *_d.bwS();
				i++;			
			
			}
			//_duration = System.currentTimeMillis() - startTime;
			//System.out.println(_duration);
			//out.write("Runtime (mS): "+ _duration);
			out.newLine();
			//out.write("Value bandwidth: "+ value_bandwidth);
			
		} catch ( IOException e1 ) {
			e1.printStackTrace();
			} finally {
				if ( out != null )
					try {
						out.close();
						} catch (IOException e) {
							e.printStackTrace();}
				}    
	try {
  		out.close();
  		} catch (IOException e2) {
  			e2.printStackTrace();
  			}
			return true;
	}
	static List<List<Integer>> v_solution;
	static List<List<Integer>> f_solution;
	static ArrayList<Integer> list_v = new ArrayList<Integer>();
	static ArrayList<Integer> list_f = new ArrayList<Integer>();
	static int f_id=0;
	static boolean _finished=false;
	static List<List<Integer>> BFStree;
	static int START, END;
	public static void DFS(double bwMax,LinkedList<Integer> visited,ArrayList<ArrayList<Integer>> _DFSTree)
	{
		ArrayList<Integer> _inter= new ArrayList<Integer>();
		int last= visited.getLast();
		//LinkedList<String> nodes = graph.adjacentNodes(visited.getLast());
		LinkedList<Integer> nodes = new LinkedList<>();
		for (int i=0;i<g_edit.getV();i++)
		{
			if(g_edit.getEdgeWeight(last, i+1)>bwMax)
				nodes.add(i+1);
		}
        // examine adjacent nodes
        for (Integer node : nodes) {
            if (visited.contains(node)) {
                continue;
            }
            if (node.equals(END)) {
                visited.add(node);
                for (Integer nodeP : visited) {
                	_inter.add(nodeP);
                }
                _DFSTree.add(_inter);
                visited.removeLast();
                break;
            }
        }
        for (Integer node : nodes) {
            if (visited.contains(node) || node.equals(END)) {
                continue;
            }
            visited.addLast(node);
            DFS(bwMax, visited,_DFSTree);
            visited.removeLast();
        }
        nodes=null;
	}
	public static boolean BFS(int V,int start, int finish, double bwMax,ArrayList<ArrayList<Integer>> BFStree)
	{
		//BFStree = new ArrayList<List<Integer>>();
		int color[]= new int[V+1];
		int back[]= new int[V+1];
		Queue<Integer> qList= new LinkedList<Integer>();
		for (int i=0;i<V;i++)
		{
			//color = 0-> chua di qua lan nao
			//color = 1 -> da di qua 1 lan
			//color = 2 -> tat ca dinh ke da duoc danh dau
			color[i+1]=0;
			back[i+1]=-1;//mang luu cac dinh cha cua i
		}
		color[start]=1;
		qList.add(start);
		while(!qList.isEmpty())
		{
			//lay gia tri dau tien trong hang doi
			int u=qList.poll();
			if(u==finish)
			{
				//tim duoc duong roi
				list_v = new ArrayList<Integer>();
				return_path(start, finish, back);
				BFStree.add(list_v);
				list_v=null;
				list_v = new ArrayList<Integer>();
			}
			else
			{
				//tim dinh ke chua di qua lan nao
				for(int v=0;v<V;v++)
				{
					if(g_edit.getEdgeWeight(u, v+1)>=bwMax && color[v+1]==0)
					{
						color[u]=1;
						//luu lai nut cha cua v
						back[v+1]=u;
						qList.add(v+1);
					}
				}
				//da duyet het dinh ke cua dinh u
				color[u]=2;				
			}
		}
		return true;
		
	}
	
	
	public static boolean return_path(int u, int v,int back[])
	{
		
		if(u==v)
			list_v.add(v);
		else
		{
			if(back[v]==-1)
				return false;
			else
			{
				return_path(u, back[v], back);
				list_v.add(v);
			}
		}
		return true;
			
	}
	public static ArrayList<Integer> Alg1(nDemand _d,nGraph _g)
	{
		fl=false;
		funLoc = new ArrayList<>();
		int noBlock=4;
		int _count=0;
		boolean  _flag=false;
		ArrayList<Integer> _sol = new ArrayList<Integer>();
		ArrayList<Integer> path;
		Random rand = new Random(); 
		nGraph g_tam = new nGraph(_g.cap,_g.w);//luu lai graph truoc moi lan lap
		nGraph g_save = new nGraph(_g.cap,_g.w);//luu lai graph truoc moi lan lap
		int _src = _d.getSrc();
		int _dest= _d.getDest();
		ArrayList<Integer> _fLst=_d.getFunctions();
		//nFunction f = getFunction(_fLst.get(0));
		ArrayList<Integer> _vnfLst= getFunction(_fLst.get(0)).getVnfNode();
		int node1=-1;
		int node2=-1;
		while (!_flag)
		{
			//thuc hien random noBlock lan neu k duoc
			node1 = _vnfLst.get(rand.nextInt(_vnfLst.size()));
			if(!UtilizeFunction.isBig(g_tam.getCap(node1),getFunction(_fLst.get(0)).getLamda()))
			{
				_count++;
				if(_count<noBlock)
				{
					//neu nhu muon thuc hien lai.g_tam ko thay doi
					continue;
				}
				else
				{
					//neu het quyen quay lai -> demand se ko duoc phuc vu. g_tam= g_save ban dau
					//duoc thuc hien khi su dung lenh kiem tra _flag nen k can dung o day
					_flag=true;
					break;
				}
			}
				
			//find SP from source to n1
			if(node1 != _src)
			{
				//path = ShortestPath(_src, node1, _d.getBw());
				path = ShortestPath(_src, node1,g_tam, _d.getBw());
				if (path ==null)
				{
					_count++;
					if(_count < noBlock)//if until limited
					{
						//neu nhu muon thuc hien lai.g_tam ko thay doi
						continue;
					}
					else
					{
						//neu het quyen quay lai -> demand se ko duoc phuc vu. g_tam= g_save ban dau
						_flag=true;//danh dau
						break;
					}
	
				}
				else
				{
					//thang g_tam se bi thay doi. doi link thuoc path. doi cap node cung cap function
					for (int _node:path)
					{
						_sol.add(_node);
					}
					for (int _node=0;_node<path.size()-1;_node++)
					{
						double w_temp= g_tam.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
						g_tam.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
					}
					Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node1),getFunction(_fLst.get(0)).getLamda());
					g_tam.setCap(node1,c_temp );
					funLoc.add(new Pair(node1,_fLst.get(0)));
					break;
					
				}
			}
			else
			{
				//cap cua g_tam tai node src se thay doi
				Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node1),getFunction(_fLst.get(0)).getLamda());
				g_tam.setCap(node1,c_temp );
				funLoc.add(new Pair(node1,_fLst.get(0)));
				_sol.add(_src);
			}
		}
		if (_flag)
		{
			//truong hop demand k duoc phuc vu. g_tam se duoc gan bang g_save
			g_tam = new nGraph(g_save.cap,g_save.w);
			fl= false;
			return null;
		}
			
		//find SP from n_i to n_j
		for (int j=1;j<_fLst.size();j++)
		{					
			ArrayList<Integer> _vnfLst1 = getFunction(_fLst.get(j)).getVnfNode();
			while(!_flag)
			{					
				node2 = _vnfLst1.get(rand.nextInt(_vnfLst1.size()));
				if(!UtilizeFunction.isBig(g_tam.getCap(node2),getFunction(_fLst.get(j)).getLamda()))
				{
					_count++;
					if(_count<noBlock)
					{
						//neu nhu muon thuc hien lai.g_tam ko thay doi
						continue;
					}
					else
					{
						//neu het quyen quay lai -> demand se ko duoc phuc vu. g_tam= g_save ban dau
						//duoc thuc hien khi su dung lenh kiem tra _flag nen k can dung o day
						_flag=true;
						break;
					}
				}
				path = ShortestPath(node1, node2,g_tam,  _d.getBw());
				if (path ==null)
				{
					_count++;
					if(_count<noBlock)
						continue;
					else
					{
						_flag=true;
						break;
					}
				}
				else
				{
					for (int _id=1;_id<path.size();_id++)
					{
						_sol.add(path.get(_id));
					}
					for (int _node=0;_node<path.size()-1;_node++)
					{
						double w_temp= g_tam.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
						g_tam.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
					}
					Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node2),getFunction(_fLst.get(j)).getLamda());
					g_tam.setCap(node2,c_temp );
					funLoc.add(new Pair(node2,_fLst.get(j)));
					
				}
				node1= node2;
				break;
			}
			if(_flag)
				break;
		}
		if(_flag)
		{
			//truong hop demand k duoc phuc vu. g_tam se duoc gan bang g_save
			g_tam = new nGraph(g_save.cap,g_save.w);
			fl=false;
			return null;
		}
		//find SP from n_j to destination
		if(node1 != _dest)
		{
			path = ShortestPath(node1, _dest,g_tam, _d.getBw());
			if (path ==null)
			{
				//ko ton tai duong di 
				_flag=true;
				g_tam = new nGraph(g_save.cap,g_save.w);
				fl=false;
				return null;
			}
			else
			{
				for (int _id=1;_id<path.size();_id++)
				{
					_sol.add(path.get(_id));
				}
				for (int _node=0;_node<path.size()-1;_node++)
				{
					double w_temp= g_tam.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
					g_tam.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
				}
				
			}
		}
		//System.out.println();
		fl=true;
		return _sol;
	}
	public static boolean Alg1(String fileName)
	{
		maxlinkload=0.0;
		
		int noBlock=4;
		int _count=0;
		boolean  _flag=false;
		final long startTime = System.currentTimeMillis();
		acceptRatio=0;
		//Select at random 1 node in Z1, Z2,...Zn
		
		try {
			File file = new File(fileName);
			out = new BufferedWriter(new FileWriter(file));

			for (int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
					link_load[i][j]=0.0;
			
			ArrayList<Integer> _sol = new ArrayList<Integer>();
			ArrayList<Integer> path;
			Random rand = new Random(); 
			for (int i=0;i<noDemand;i++)
			{
				_flag=false;
				_count=0;
				nGraph g_save = new nGraph(g_edit.cap,g_edit.w);//luu lai graph truoc moi lan lap
				_sol = new ArrayList<Integer>();
				nDemand _d = DemandArray.get(i);
				int _src = _d.getSrc();
				int _dest= _d.getDest();
				ArrayList<Integer> _fLst=_d.getFunctions();
				ArrayList<Integer> _vnfLst= getFunction(_fLst.get(0)).getVnfNode();
				int node1=-1;
				int node2=-1;
				while (!_flag)
				{
					//thuc hien random noBlock lan neu k duoc
					node1 = _vnfLst.get(rand.nextInt(_vnfLst.size()));
					if(!UtilizeFunction.isBig(g_edit.getCap(node1),getFunction(_fLst.get(0)).getLamda()))
					{
						_count++;
						if(_count<noBlock)
						{
							//neu nhu muon thuc hien lai.g_edit ko thay doi
							continue;
						}
						else
						{
							//neu het quyen quay lai -> demand se ko duoc phuc vu. g_edit= g_save ban dau
							//duoc thuc hien khi su dung lenh kiem tra _flag nen k can dung o day
							_flag=true;
							break;
						}
					}
						
					//find SP from source to n1
					if(node1 != _src)
					{
						//path = ShortestPath(_src, node1, _d.getBw());
						path = ShortestPath(_src, node1,g_edit, _d.getBw());
						if (path ==null)
						{
							_count++;
							if(_count < noBlock)//if until limited
							{
								//neu nhu muon thuc hien lai.g_edit ko thay doi
								continue;
							}
							else
							{
								//neu het quyen quay lai -> demand se ko duoc phuc vu. g_edit= g_save ban dau
								_flag=true;//danh dau
								break;
							}
			
						}
						else
						{
							//thang g_edit se bi thay doi. doi link thuoc path. doi cap node cung cap function
							for (int _node:path)
							{
								_sol.add(_node);
							}
							for (int _node=0;_node<path.size()-1;_node++)
							{
								double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
								g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
							}
							Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(node1),getFunction(_fLst.get(0)).getLamda());
							g_edit.setCap(node1,c_temp );
							break;
							
						}
					}
					else
					{
						//cap cua g_edit tai node src se thay doi
						Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(node1),getFunction(_fLst.get(0)).getLamda());
						g_edit.setCap(node1,c_temp );
						_sol.add(_src);
					}
				}
				if (_flag)
				{
					//truong hop demand k duoc phuc vu. g_edit se duoc gan bang g_save
					g_edit = new nGraph(g_save.cap,g_save.w);
					continue;
				}
					
				//find SP from n_i to n_j
				for (int j=1;j<_fLst.size();j++)
				{					
					ArrayList<Integer> _vnfLst1 = getFunction(_fLst.get(j)).getVnfNode();
					while(!_flag)
					{					
						node2 = _vnfLst1.get(rand.nextInt(_vnfLst1.size()));
						if(!UtilizeFunction.isBig(g_edit.getCap(node2),getFunction(_fLst.get(j)).getLamda()))
						{
							_count++;
							if(_count<noBlock)
							{
								//neu nhu muon thuc hien lai.g_edit ko thay doi
								continue;
							}
							else
							{
								//neu het quyen quay lai -> demand se ko duoc phuc vu. g_edit= g_save ban dau
								//duoc thuc hien khi su dung lenh kiem tra _flag nen k can dung o day
								_flag=true;
								break;
							}
						}
						path = ShortestPath(node1, node2,g_edit,  _d.getBw());
						if (path ==null)
						{
							_count++;
							if(_count<noBlock)
								continue;
							else
							{
								_flag=true;
								break;
							}
						}
						else
						{
							for (int _id=1;_id<path.size();_id++)
							{
								_sol.add(path.get(_id));
							}
							for (int _node=0;_node<path.size()-1;_node++)
							{
								double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
								g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
							}
							Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(node2),getFunction(_fLst.get(j)).getLamda());
							g_edit.setCap(node2,c_temp );
							
						}
						node1= node2;
						break;
					}
					if(_flag)
						break;
				}
				if(_flag)
				{
					//truong hop demand k duoc phuc vu. g_edit se duoc gan bang g_save
					g_edit = new nGraph(g_save.cap,g_save.w);
					continue;
				}
					//find SP from n_j to destination
					if(node1 != _dest)
					{
						path = ShortestPath(node1, _dest,g_edit, _d.getBw());
						if (path ==null)
						{
							//ko ton tai duong di 
							_flag=true;
							g_edit = new nGraph(g_save.cap,g_save.w);
							continue;
						}
						else
						{
							for (int _id=1;_id<path.size();_id++)
							{
								_sol.add(path.get(_id));
							}
							for (int _node=0;_node<path.size()-1;_node++)
							{
								double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
								g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
							}
							
						}
					}
					for(int _id=0;_id<_sol.size()-1;_id++)
					{
						if(g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1))>0)
							link_load[_sol.get(_id)-1][_sol.get(_id+1)-1]+=_d.getBw()/g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1));
						else
						{
							out.write("!Hai node nay bi sai?"+ _sol.get(_id)+"?"+ _sol.get(_id+1)+"!");
						}
						//link_load[_sol.get(_id+1)][_sol.get(_id)]+=_d.getBw()/g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1));
					}			
					solution_node.add(_sol);
					solution_id.add(_d.getId());
					out.write(_d.getId()+ ":");
					for (int _node: _sol)
					{
						out.write(_node+" ");
					}
					out.newLine();
				
				
			}
			for (int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
					if(link_load[i][j]>maxlinkload)
						maxlinkload = link_load[i][j];
			acceptRatio = solution_id.size()*1.0/noDemand;
			out.write("Number of accepted demands: " + acceptRatio);
			_duration = System.currentTimeMillis() - startTime;
			out.newLine();
			out.write("Maximum link load: "+ maxlinkload);
			out.newLine();
			
		}
		catch ( IOException e1 ) {
			e1.printStackTrace();
			} finally {
				if ( out != null )
					try {
						out.close();
						} catch (IOException e) {
							e.printStackTrace();}
				}    
	try {
  		out.close();
  		} catch (IOException e2) {
  			e2.printStackTrace();
  			}
	
		return true;
	}
	public static ArrayList<Integer> GreedyBackward(nDemand _d, nGraph _g)
	{

		fl=false;
		funLoc = new ArrayList<>();
		boolean  _flag=false;
		ArrayList<Integer> _finalsol = new ArrayList<Integer>();
		Stack <Integer> _sol = new Stack<Integer>();
		ArrayList<Integer> path = new ArrayList<>();
		ArrayList<ArrayList<Integer>> pathLst = new ArrayList<>();
		nGraph g_tam= new nGraph(_g.cap,_g.w);
		nGraph g_save= new nGraph(_g.cap,_g.w);
		int _src = _d.getSrc();
		int _dest= _d.getDest();
		ArrayList<Integer> _fLst=_d.getFunctions();
		if(_fLst.size()==0)
		{
			//tinh duong di ngan nhat tu src den dest
			path= ShortestPath(_d.getSrc(),_d.getDest(),_g, _d.getBw());
			return path;
		}
		ArrayList<Integer> _z1= getFunction(_fLst.get(_fLst.size()-1)).getVnfNode();//tap z1
		int node1=-1;
		int node2=-1;
		int minNode=-1;
		ArrayList<Integer> _sp = new ArrayList<>();//luu tru duong di ngan nhat hien tai
		double _minVal=Double.MAX_VALUE;//luu tru do dai ngan nhat cua duong di.
		// Chon 1 duong ngan nhat tu src den tap z1 ->
		
		for (int _id=0;_id<_z1.size();_id++)
		{
			//xet tung nut trong z1 va tim duong di
			node1 = _z1.get(_id);
			if(UtilizeFunction.isBig(g_tam.getCap(node1), getFunction(_fLst.get(0)).getLamda()))
			{
				//path= ShortestPath(node1,_dest,g_tam, _d.getBw());
				pathLst= allOfShortestPaths(node1,_dest,g_tam, _d.getBw());
				if (pathLst==null || pathLst.size()==0)
				{
					//ko ton tai duong di -> blocking can thu lai
					continue;
				}
				else
				{
					path = pathLst.get(0);
					double _weight = 0.0;
					_weight+=path.size();
//					for (int _id1=0;_id1<path.size()-1;_id1++)
//					{
//						_weight += g.getEdgeWeight(path.get(_id1), path.get(_id1+1));
//					}
					if (_weight<_minVal)
					{
						_sp= new ArrayList<>();
						for (int _id1=0;_id1<path.size();_id1++)
							_sp.add(path.get(_id1));
						_minVal = _weight;
						minNode= node1;//luu lai node co gia tri nho nhat
					}
					
				}
			}
			else
			{
				continue;
			}					
		}
		
			
		_flag=false;	
		if (minNode ==-1)
		{
			//truong hop ko the tim duoc bat ky 1 node nao trong z1 -> demand ko thoa man
			fl=false;
			return null;
		}
		else
		{
			node1= minNode;
			minNode=-1;
			_minVal=Double.MAX_VALUE;
			//truoc het pai add duong di ngan nhat giua src va z1 vao _sol
//			for (int _id1=0;_id1<_sp.size();_id1++)
//				_sol.add(_sp.get(_id1));
			for (int _id1=_sp.size()-1;_id1>=0;_id1--)
				_sol.push(_sp.get(_id1));
			//cap nhat lai do thi g_tam
			
			for (int _node=0;_node<_sp.size()-1;_node++)
			{
				double w_temp= g_tam.getEdgeWeight(_sp.get(_node), _sp.get(_node+1))-_d.getBw();
				g_tam.setEdgeWeight(_sp.get(_node), _sp.get(_node+1),w_temp );
			}
			Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node1),getFunction(_fLst.get(_fLst.size()-1)).getLamda());
			g_tam.setCap(node1,c_temp );
			funLoc.add(new Pair(node1,_fLst.get(_fLst.size()-1)));
			//thuc hien voi cac cap z2, z3..
			for (int _id1=_fLst.size()-2;_id1>=0;_id1--)
			{
				_flag=false;
				minNode=-1;
				_minVal=Double.MAX_VALUE;
				ArrayList<Integer> _zi= getFunction(_fLst.get(_id1)).getVnfNode();
				for (int _id2=0;_id2<_zi.size();_id2++)
				{
					//xet tung nut trong z1 va tim duong di
					node2 = _zi.get(_id2);
					if(UtilizeFunction.isBig(g_tam.getCap(node2), getFunction(_fLst.get(_id1)).getLamda()))
					{
						//path= ShortestPath(node2, node1, g_tam, _d.getBw());
						pathLst= allOfShortestPaths(node2, node1, g_tam, _d.getBw());
						
						if (pathLst ==null || pathLst.size()==0)
						{
							//ko ton tai duong di -> blocking can thu lai
							continue;
						}
						else
						{
							path = pathLst.get(0);
							double _weight = 0.0;
							_weight+=path.size();
//							for (int _id3=0;_id3<path.size()-1;_id3++)
//							{
//								_weight += g.getEdgeWeight(path.get(_id3), path.get(_id3+1));
//							}
							if (_weight<_minVal)
							{
								_flag=true;
								_sp= new ArrayList<>();
								for (int _id3=0;_id3<path.size();_id3++)
									_sp.add(path.get(_id3));
								_minVal = _weight;
								minNode= node2;//luu lai node co gia tri nho nhat
							}
							
						}
					}
					else
					{
						//tiep tuc thu voi node khac
						continue;
					}
					
				}
				
				if(!_flag)//neu ko tim duoc duong di ngan nhat giua 2 cap zi va zj -> demand nay ko the tim ra duong di
				{							
					break;
				}
				else
				{
//					for (int _id2=1;_id2<_sp.size();_id2++)
//						_sol.add(_sp.get(_id2));
					for (int _id2=_sp.size()-2;_id2>=0;_id2--)
						_sol.push(_sp.get(_id2));
					node1=minNode;
					//cap nhat lai do thi g_tam
					
					for (int _node=0;_node<_sp.size()-1;_node++)
					{
						double w_temp= g_tam.getEdgeWeight(_sp.get(_node), _sp.get(_node+1))-_d.getBw();
						g_tam.setEdgeWeight(_sp.get(_node), _sp.get(_node+1),w_temp );
					}
					c_temp= UtilizeFunction.minus(g_tam.getCap(node1),getFunction(_fLst.get(_id1)).getLamda());
					g_tam.setCap(node1,c_temp );
					funLoc.add(new Pair(node1,_fLst.get(_id1)));
				}
			}
			if(!_flag)//neu demand nay ko the tim ra yeu cau thoa man
			{
				g_tam = new nGraph(g_save.cap,g_save.w);//chuyen ve do thi binh thuong
				fl=false;
				return null;
			}
			else 
			{
				//tim duong di giua zn va dest
				if(minNode != _src)
				{
					path= ShortestPath(_src,minNode,g_tam, _d.getBw());
					pathLst= allOfShortestPaths(_src,minNode,g_tam, _d.getBw());
					if (pathLst ==null || pathLst.size()==0)
					{
						//ko ton tai duong di -> blocking can thu lai
						g_tam = new nGraph(g_save.cap,g_save.w);
						return null;
					}
					else
					{
						path = pathLst.get(0);
//						for (int _id3=1;_id3<path.size();_id3++)
//							_sol.add(path.get(_id3));
						for (int _id3=path.size()-2;_id3>=0;_id3--)
							_sol.push(path.get(_id3));
						for (int _node=0;_node<path.size()-1;_node++)
						{
							double w_temp= g_tam.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
							g_tam.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
						}
						
					}
				}
			}
			
		}	
		fl=true;
		while(!_sol.isEmpty())
			_finalsol.add(_sol.pop());
		return _finalsol;
	
	}
	public static ArrayList<Integer> GreedyMinh(nDemand _d, nGraph _g)
	{

		fl=false;
		funLoc = new ArrayList<>();
		boolean  _flag=false;
		ArrayList<Integer> _sol = new ArrayList<Integer>();
		ArrayList<Integer> path;
		ArrayList<Integer> restPath;
		nGraph g_tam= new nGraph(_g.cap,_g.w);
		nGraph g_save= new nGraph(_g.cap,_g.w);
		int _src = _d.getSrc();
		int _dest= _d.getDest();
		ArrayList<Integer> _fLst=_d.getFunctions();
		ArrayList<Integer> _z1= getFunction(_fLst.get(0)).getVnfNode();//tap z1
		int node1=-1;
		int node2=-1;
		int minNode=-1;
		ArrayList<Integer> _sp = new ArrayList<>();//luu tru duong di ngan nhat hien tai
		double _minVal=Double.MAX_VALUE;//luu tru do dai ngan nhat cua duong di.
		// Chon 1 duong ngan nhat tu src den tap z1 ->
		for (int _id=0;_id<_z1.size();_id++)
		{
			//xet tung nut trong z1 va tim duong di
			node1 = _z1.get(_id);
			if(UtilizeFunction.isBig(g_tam.getCap(node1), getFunction(_fLst.get(0)).getLamda()))
			{
				path= ShortestPath(_src, node1,g_tam, _d.getBw());
				restPath = ShortestPath(node1, _dest, g,_d.getBw());
				if (path ==null || restPath==null)
				{
					if(restPath==null)
						System.out.println("bo qua");
					//ko ton tai duong di -> blocking can thu lai
					continue;
				}
				else
				{
					double _weight = 0.0;
					_weight+=path.size()+restPath.size()/2.5;
//					for (int _id1=0;_id1<path.size()-1;_id1++)
//					{
//						_weight += g.getEdgeWeight(path.get(_id1), path.get(_id1+1));
//					}
//					for (int _id1=0;_id1<restPath.size()-1;_id1++)
//					{
//						_weight += g.getEdgeWeight(restPath.get(_id1), restPath.get(_id1+1));
//					}
					if (_weight<_minVal)
					{
						_sp= new ArrayList<>();
						for (int _id1=0;_id1<path.size();_id1++)
							_sp.add(path.get(_id1));
						_minVal = _weight;
						minNode= node1;//luu lai node co gia tri nho nhat
					}
					
				}
			}
			else
			{
				continue;
			}					
		}
		
			
		_flag=false;	
		if (minNode ==-1)
		{
			//truong hop ko the tim duoc bat ky 1 node nao trong z1 -> demand ko thoa man
			fl=false;
			return null;
		}
		else
		{
			node1= minNode;
			minNode=-1;
			_minVal=Double.MAX_VALUE;
			//truoc het pai add duong di ngan nhat giua src va z1 vao _sol
			for (int _id1=0;_id1<_sp.size();_id1++)
				_sol.add(_sp.get(_id1));
			//cap nhat lai do thi g_tam
			
			for (int _node=0;_node<_sp.size()-1;_node++)
			{
				double w_temp= g_tam.getEdgeWeight(_sp.get(_node), _sp.get(_node+1))-_d.getBw();
				g_tam.setEdgeWeight(_sp.get(_node), _sp.get(_node+1),w_temp );
			}
			Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node1),getFunction(_fLst.get(0)).getLamda());
			g_tam.setCap(node1,c_temp );
			funLoc.add(new Pair(node1,_fLst.get(0)));
			//thuc hien voi cac cap z2, z3..
			for (int _id1=1;_id1<_fLst.size();_id1++)
			{
				_flag=false;
				minNode=-1;
				_minVal=Double.MAX_VALUE;
				ArrayList<Integer> _zi= getFunction(_fLst.get(_id1)).getVnfNode();
				for (int _id2=0;_id2<_zi.size();_id2++)
				{
					//xet tung nut trong z1 va tim duong di
					node2 = _zi.get(_id2);
					if(UtilizeFunction.isBig(g_tam.getCap(node2), getFunction(_fLst.get(_id1)).getLamda()))
					{
						path= ShortestPath(node1, node2, g_tam, _d.getBw());
						restPath = ShortestPath(node2, _dest, g,_d.getBw());
						if (path ==null||restPath==null)
						{
							if(restPath==null)
								System.out.println("bo qua");
							//ko ton tai duong di -> blocking can thu lai
							continue;
						}
						else
						{
							double _weight = 0.0;
							_weight+=path.size()+restPath.size()/2.5;
//							for (int _id3=0;_id3<path.size()-1;_id3++)
//							{
//								_weight += g.getEdgeWeight(path.get(_id3), path.get(_id3+1));
//							}
//							for (int _id3=0;_id3<restPath.size()-1;_id3++)
//							{
//								_weight += g.getEdgeWeight(restPath.get(_id3), restPath.get(_id3+1));
//							}
							if (_weight<_minVal)
							{
								_flag=true;
								_sp= new ArrayList<>();
								for (int _id3=0;_id3<path.size();_id3++)
									_sp.add(path.get(_id3));
								_minVal = _weight;
								minNode= node2;//luu lai node co gia tri nho nhat
							}
							
						}
					}
					else
					{
						//tiep tuc thu voi node khac
						continue;
					}
					
				}
				
				if(!_flag)//neu ko tim duoc duong di ngan nhat giua 2 cap zi va zj -> demand nay ko the tim ra duong di
				{							
					break;
				}
				else
				{
					for (int _id2=1;_id2<_sp.size();_id2++)
						_sol.add(_sp.get(_id2));
					node1=minNode;
					//cap nhat lai do thi g_tam
					
					for (int _node=0;_node<_sp.size()-1;_node++)
					{
						double w_temp= g_tam.getEdgeWeight(_sp.get(_node), _sp.get(_node+1))-_d.getBw();
						g_tam.setEdgeWeight(_sp.get(_node), _sp.get(_node+1),w_temp );
					}
					c_temp= UtilizeFunction.minus(g_tam.getCap(node1),getFunction(_fLst.get(_id1)).getLamda());
					g_tam.setCap(node1,c_temp );
					funLoc.add(new Pair(node1,_fLst.get(_id1)));
				}
			}
			if(!_flag)//neu demand nay ko the tim ra yeu cau thoa man
			{
				g_tam = new nGraph(g_save.cap,g_save.w);//chuyen ve do thi binh thuong
				fl=false;
				return null;
			}
			else 
			{
				//tim duong di giua zn va dest
				if(minNode != _dest)
				{
					path= ShortestPath(minNode, _dest,g_tam, _d.getBw());
					
					if (path ==null)
					{
						//ko ton tai duong di -> blocking can thu lai
						g_tam = new nGraph(g_save.cap,g_save.w);
						return null;
					}
					else
					{
						for (int _id3=1;_id3<path.size();_id3++)
							_sol.add(path.get(_id3));
						for (int _node=0;_node<path.size()-1;_node++)
						{
							double w_temp= g_tam.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
							g_tam.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
						}
						
					}
				}
			}
			
		}	
		fl=true;
		return _sol;
	
	}
	
	public static ArrayList<Integer> GreedyForward(nDemand _d, nGraph _g)
	{
		fl=false;
		funLoc = new ArrayList<>();
		boolean  _flag=false;
		ArrayList<Integer> _sol = new ArrayList<Integer>();
		ArrayList<Integer> path = new ArrayList<>();
		ArrayList<ArrayList<Integer>> pathLst = new ArrayList<>();
		nGraph g_tam= new nGraph(_g.cap,_g.w);
		int _src = _d.getSrc();
		int _dest= _d.getDest();
		ArrayList<Integer> _fLst=_d.getFunctions();
		if(_fLst.size()==0)
		{
			//tinh duong di ngan nhat tu src den dest
			//path= ShortestPath(_d.getSrc(),_d.getDest(),_g, _d.getBw());
			pathLst = shortestPaths(_src, _dest, _g, _d.getBw());
			if(pathLst!=null && pathLst.size()>0)
				return pathLst.get(0);
			else
				return null;
		}
		ArrayList<Integer> _z1= getFunction(_fLst.get(0)).getVnfNode();//tap z1
		int node1=-1;
		int node2=-1;
		int minNode=-1;
		ArrayList<Integer> _sp = new ArrayList<>();//luu tru duong di ngan nhat hien tai
		int _minVal = Integer.MAX_VALUE;//luu tru do dai ngan nhat cua duong di.
		
		
		// Chon 1 duong ngan nhat tu src den tap z1 ->
		
		for (int _id=0;_id<_z1.size();_id++)
		{
			_flag=false;
			g_tam = new nGraph(_g.cap, _g.w);
			//xet tung nut trong z1 va tim duong di
			node1 = _z1.get(_id);
			if(!UtilizeFunction.isBig( getFunction(_fLst.get(0)).getLamda(),_g.getCap(node1)))
			{
				
				pathLst = shortestPaths(_src, node1,_g, _d.getBw());
				if (pathLst!=null && pathLst.size()>0)
				{
					path = pathLst.get(0);
					for(int i=0;i<path.size()-1;i++)
						if(g_tam.getEdgeWeight(path.get(i), path.get(i+1))<_d.getBw())
						{
							_flag=true;
							break;
							}
						else
							g_tam.setEdgeWeight(path.get(i), path.get(i+1),g_tam.getEdgeWeight(path.get(i), path.get(i+1))-_d.getBw() );
					
						if(_flag)
						{
							_flag=false;
							continue;
						}
					if (path.size()<_minVal)
					{
						_sp= new ArrayList<>();
						for (int _id1=0;_id1<path.size();_id1++)
							_sp.add(path.get(_id1));
						_minVal = path.size();
						minNode= node1;//luu lai node co gia tri nho nhat
					}					
				}
			}				
		}
		
			
		_flag=false;	
		if (minNode ==-1)
		{
			//truong hop ko the tim duoc bat ky 1 node nao trong z1 -> demand ko thoa man
			fl=false;
			return null;
		}
		else
		{
			node1= minNode;
			minNode=-1;
			_minVal=Integer.MAX_VALUE;
			//truoc het pai add duong di ngan nhat giua src va z1 vao _sol
			
			for (int _id1=0;_id1<_sp.size();_id1++)
				_sol.add(_sp.get(_id1));
			//cap nhat lai do thi g_tam
			
			for (int _node=0;_node<_sp.size()-1;_node++)
			{
				double w_temp= _g.getEdgeWeight(_sp.get(_node), _sp.get(_node+1))-_d.getBw();
				_g.setEdgeWeight(_sp.get(_node), _sp.get(_node+1),w_temp );
			}
			Vector<Double> c_temp= UtilizeFunction.minus(_g.getCap(node1),getFunction(_fLst.get(0)).getLamda());
			_g.setCap(node1,c_temp );
			funLoc.add(new Pair(node1,_fLst.get(0)));
			
			//thuc hien voi cac cap z2, z3..
			for (int _id1=1;_id1<_fLst.size();_id1++)
			{
				_flag=false;
				minNode=-1;
				_minVal=Integer.MAX_VALUE;
				ArrayList<Integer> _zi= getFunction(_fLst.get(_id1)).getVnfNode();
				for (int _id2=0;_id2<_zi.size();_id2++)
				{
					_flag= false;
					g_tam = new nGraph(_g.cap, _g.w);
					//xet tung nut trong z1 va tim duong di
					node2 = _zi.get(_id2);
					if(!UtilizeFunction.isBig(getFunction(_fLst.get(_id1)).getLamda(),g_tam.getCap(node2)))
					{
						c_temp= UtilizeFunction.minus(g_tam.getCap(node2),getFunction(_fLst.get(_id1)).getLamda());
						g_tam.setCap(node2,c_temp );
						//path= ShortestPath(node1, node2, g_tam, _d.getBw());
						pathLst= shortestPaths(node1, node2, g_tam, _d.getBw());
						
						if (pathLst!=null && pathLst.size()>0)
						{
							path = pathLst.get(0);
							for(int i=0;i<path.size()-1;i++)
							{
								if(g_tam.getEdgeWeight(path.get(i), path.get(i+1))<_d.getBw())
								{
									_flag=true;
									break;
									}
								else
									g_tam.setEdgeWeight(path.get(i), path.get(i+1),g_tam.getEdgeWeight(path.get(i), path.get(i+1))-_d.getBw() );
							}
							if(_flag)
							{
								_flag=false;
								continue;
							}
							if (path.size()<_minVal)
							{
								_flag=true;
								_sp= new ArrayList<>();
								for (int _id3=0;_id3<path.size();_id3++)
									_sp.add(path.get(_id3));
								_minVal = path.size();
								minNode= node2;//luu lai node co gia tri nho nhat
							}
							
						}
					}
					
				}				
				if(minNode==-1)//neu ko tim duoc duong di ngan nhat giua 2 cap zi va zj -> demand nay ko the tim ra duong di
				{
					_flag=false;
					break;
				}
				else
				{
					_flag=true;
					for (int _id2=1;_id2<_sp.size();_id2++)
						_sol.add(_sp.get(_id2));
					node1=minNode;
					//cap nhat lai do thi g_tam
					
					for (int _node=0;_node<_sp.size()-1;_node++)
					{
						double w_temp= g_tam.getEdgeWeight(_sp.get(_node), _sp.get(_node+1))-_d.getBw();
						_g.setEdgeWeight(_sp.get(_node), _sp.get(_node+1),w_temp );
					}
					c_temp= UtilizeFunction.minus(_g.getCap(node1),getFunction(_fLst.get(_id1)).getLamda());
					_g.setCap(node1,c_temp );
					funLoc.add(new Pair(node1,_fLst.get(_id1)));
				}
			}
			if(!_flag)//neu demand nay ko the tim ra yeu cau thoa man
			{
				fl=false;
				return null;
			}
			else 
			{
				//tim duong di giua zn va dest
				if(minNode != _dest)
				{
					pathLst= shortestPaths(minNode, _dest,_g, _d.getBw());
					
					if (pathLst==null || pathLst.size()==0)
					{
						fl=false;
						return null;
					}
					else
					{
						path = pathLst.get(0);
						for(int i=0;i<path.size()-1;i++)
						{
							if(_g.getEdgeWeight(path.get(i), path.get(i+1))<_d.getBw())
							{
								fl=false;
								return null;
								}
							else
								_g.setEdgeWeight(path.get(i), path.get(i+1),_g.getEdgeWeight(path.get(i), path.get(i+1))-_d.getBw() );
						}
						for (int _id3=1;_id3<path.size();_id3++)
							_sol.add(path.get(_id3));
						
					}
				}
			}
			
		}	
		fl=true;
		return _sol;
	}
	public static boolean CheckFunc(nFunction _f, int n)
	{
		ArrayList<Integer> nodes = _f.getVnfNode();
		for (Integer i : nodes) {
			if(i==n)
				return true;
		}
		return false;
	}
	public static ArrayList<Integer> GreedyAnchor(nDemand _d,nGraph _g)
	{
		fl=true;
		int w1= Integer.MAX_VALUE;
		int w2 = Integer.MAX_VALUE;
		anchorNode =true;
		ArrayList<Integer> _minSol = new ArrayList<>();
		ArrayList<Pair> _minFunLoc= new ArrayList<>();
		ArrayList<Integer> _tempSol= new ArrayList<>();
		ArrayList<Pair> _tempFunLoc = new ArrayList<>();
		ArrayList<ArrayList<Integer>> pathLst = new ArrayList<>();
		int _minVal = Integer.MAX_VALUE;
		_minSol = GreedyBest(_d, _g);
		if(_minSol!=null&& _minSol.size()>0)
		{
			_minVal =  _minSol.size();
			for (int i=0;i<funLoc.size();i++)
				_minFunLoc.add(funLoc.get(i));
		}
		if(_minVal != Integer.MAX_VALUE)
		{
			//thuc hien test xem co nen thuc hien new alg
			//ArrayList<Integer> sp = ShortestPath(_d.getSrc(), _d.getDest(), _g, _d.getBw());
			ArrayList<Integer> sp = new ArrayList<>();
			pathLst = allOfShortestPaths(_d.getSrc(), _d.getDest(), _g, _d.getBw());
			if(pathLst!=null &&pathLst.size()>0)
			{
				sp= pathLst.get(0);
				System.out.print("SP:[");
				for(int i=0;i<sp.size();i++)
					System.out.print(sp.get(i)+" ");
				System.out.println("]");
				if(Math.abs(sp.size()-_minVal)>=4)
				{

					anchorNode=false;
					for (int i=1;i<sp.size()-1;i++)
					{
						int node = sp.get(i);
						for (int index=0;index<_d.getFunctions().size();index++)
						{
							nFunction _f= getFunction(_d.getFunctions().get(index));
							if(CheckFunc(_f,node))//node nay chua function nay
							{
								_tempFunLoc = new ArrayList<>();
								_tempSol= new ArrayList<>();
								anchorNode=true;
								_tempFunLoc.add(new Pair(node,_f.id()));
								//sub-flow 1
								ArrayList<Integer> fArr = new ArrayList<>();
								for (int i1 = 0;i1<index;i1++)
									fArr.add(_d.getFunctions().get(i1));
								//tu src->node
								nDemand _newd = new nDemand(_d.getId(),_d.getSrc(),node,_d.getBw(),fArr);
								funLoc = new ArrayList<>();
								
								ArrayList<Integer> _Sol1 = GreedyBest(_newd, _g);
								if(_Sol1!=null)
								{
									
									if(funLoc!=null)
									{
										for (int i1=0;i1<funLoc.size();i1++)
											_tempFunLoc.add(funLoc.get(i1));
									}
									
									nGraph g_tam= new nGraph(_g.cap,_g.w);
									for(int i1=0;i1<_Sol1.size()-1;i1++)
									{
										double weight = _g.getEdgeWeight(_Sol1.get(i1), _Sol1.get(i1+1));
										g_tam.setEdgeWeight(_Sol1.get(i1), _Sol1.get(i1+1), weight - _d.getBw());
									}
									
									for (int j=0;j<_tempFunLoc.size();j++)
			    					{
			    						Pair pr = _tempFunLoc.get(j);
			    									    						
			    						Vector<Double> mul =  getFunction(pr.getfunction()).getLamda();
		    							if(UtilizeFunction.isBig(_g.getCap(pr.getnode()), mul))
		    							{
		    								Vector<Double> c_temp= UtilizeFunction.minus(_g.getCap(pr.getnode()),mul);
		    								g_tam.setCap(pr.getnode(),c_temp );
		    							}
			    						
			    					}	
									//Sub-flows 2
									fArr = new ArrayList<>();
									for (int i1 = index+1;i1<_d.getFunctions().size();i1++)
										fArr.add(_d.getFunctions().get(i1));
									funLoc = new ArrayList<>();
									_newd= new nDemand(_d.getId(), node, _d.getDest(), _d.getBw(), fArr );
									ArrayList<Integer> _Sol2 = GreedyBest(_newd, g_tam);
									if(funLoc!=null)
									{
										for (int i1=0;i1<funLoc.size();i1++)
											_tempFunLoc.add(funLoc.get(i1));
									}
									if(_Sol2!=null)
									{

										for(int i1=0;i1<_Sol1.size();i1++)
											_tempSol.add(_Sol1.get(i1));
										for (int i1=1;i1<_Sol2.size();i1++)
											_tempSol.add(_Sol2.get(i1));
										
									}
									if(_Sol2!=null && _tempSol.size()<_minVal)
									{
										_minVal = _tempSol.size();
										_minSol= new ArrayList<>();
										_minFunLoc = new ArrayList<>();
										for (int i1=0;i1<_tempSol.size();i1++)
											_minSol.add(_tempSol.get(i1));
										for (int i1=0;i1<_tempFunLoc.size();i1++)
											_minFunLoc.add(_tempFunLoc.get(i1));
									}
								}
							}
						}
						
					}				
					if(!anchorNode)
					{
						//truong hop tren duong ay ko ton tai mot node nao chua function
						//Chon mot node bat ky chinh giua sp;
						
						int index= 2;						
						//nFunction _f= getFunction(_d.getFunctions().get(index));
						//ArrayList<Integer> nodes = _f.getVnfNode();
						//int id = UtilizeFunction.randInt(0, nodes.size()-1);
						//int node= nodes.get(id);
						int node = sp.get(sp.size()/2);
						_tempFunLoc = new ArrayList<>();
						_tempSol= new ArrayList<>();
						//_tempFunLoc.add(new Pair(node,_f.id()));
						//sub-flow 1
						ArrayList<Integer> fArr = new ArrayList<>();
						for (int i1 = 0;i1<=index;i1++)
							fArr.add(_d.getFunctions().get(i1));
						//tu src->node
						nDemand _newd = new nDemand(_d.getId(),_d.getSrc(),node,_d.getBw(),fArr);
						funLoc = new ArrayList<>();
						
						ArrayList<Integer> _Sol1 = GreedyBest(_newd, _g);
						if(_Sol1!=null)
						{
							
							if(funLoc!=null)
							{
								for (int i1=0;i1<funLoc.size();i1++)
									_tempFunLoc.add(funLoc.get(i1));
							}
							
							nGraph g_tam= new nGraph(_g.cap,_g.w);
							for(int i1=0;i1<_Sol1.size()-1;i1++)
							{
								double weight = _g.getEdgeWeight(_Sol1.get(i1), _Sol1.get(i1+1));
								g_tam.setEdgeWeight(_Sol1.get(i1), _Sol1.get(i1+1), weight - _d.getBw());
							}
							
							for (int j=0;j<_tempFunLoc.size();j++)
	    					{
	    						Pair pr = _tempFunLoc.get(j);
	    									    						
	    						Vector<Double> mul =  getFunction(pr.getfunction()).getLamda();
    							if(UtilizeFunction.isBig(_g.getCap(pr.getnode()), mul))
    							{
    								Vector<Double> c_temp= UtilizeFunction.minus(_g.getCap(pr.getnode()),mul);
    								g_tam.setCap(pr.getnode(),c_temp );
    							}
	    						
	    					}	
							//Sub-flows 2
							fArr = new ArrayList<>();
							for (int i1 = index+1;i1<_d.getFunctions().size();i1++)
								fArr.add(_d.getFunctions().get(i1));
							funLoc = new ArrayList<>();
							_newd= new nDemand(_d.getId(), node, _d.getDest(), _d.getBw(), fArr );
							ArrayList<Integer> _Sol2 = GreedyBest(_newd, g_tam);
							if(funLoc!=null)
							{
								for (int i1=0;i1<funLoc.size();i1++)
									_tempFunLoc.add(funLoc.get(i1));
							}
							if(_Sol2!=null)
							{

								for(int i1=0;i1<_Sol1.size();i1++)
									_tempSol.add(_Sol1.get(i1));
								for (int i1=1;i1<_Sol2.size();i1++)
									_tempSol.add(_Sol2.get(i1));
								
							}
							if(_Sol2!=null && _tempSol.size()<_minVal)
							{
								_minVal = _tempSol.size();
								_minSol= new ArrayList<>();
								_minFunLoc = new ArrayList<>();
								for (int i1=0;i1<_tempSol.size();i1++)
									_minSol.add(_tempSol.get(i1));
								for (int i1=0;i1<_tempFunLoc.size();i1++)
									_minFunLoc.add(_tempFunLoc.get(i1));
							}
						}
					
					
					}
				}
			}
		
		}
		else
		{
			//truong hop ko ton tai best solution cho giai thuat 1 va 2
			_minVal = Integer.MAX_VALUE;
			_minSol = new ArrayList<>();
			_minFunLoc = new ArrayList<>();
			System.out.println("Khong ton tai the best solution");
			//boolean co = false;
			anchorNode=true;
			//ArrayList<Integer> sp = ShortestPath(_d.getSrc(), _d.getDest(), _g, _d.getBw());
			ArrayList<Integer> sp = new ArrayList<>();
			pathLst = allOfShortestPaths(_d.getSrc(), _d.getDest(), _g, _d.getBw());
			if(pathLst!=null && pathLst.size()>0)
			{
				sp = pathLst.get(0);
				for (int i=1;i<sp.size()-1;i++)
				{
					int node = sp.get(i);
					for (int index=0;index<_d.getFunctions().size();index++)
					{
						nFunction _f= getFunction(_d.getFunctions().get(index));
						if(CheckFunc(_f,node))//node nay chua function nay
						{
							_tempFunLoc = new ArrayList<>();
							_tempSol= new ArrayList<>();
							_tempFunLoc.add(new Pair(node,_f.id()));
							//sub-flow 1
							ArrayList<Integer> fArr = new ArrayList<>();
							for (int i1 = 0;i1<index;i1++)
								fArr.add(_d.getFunctions().get(i1));
							//tu src->node
							nDemand _newd = new nDemand(_d.getId(),_d.getSrc(),node,_d.getBw(),fArr);
							funLoc = new ArrayList<>();
							
							ArrayList<Integer> _Sol1 = GreedyBest(_newd, _g);
							if(_Sol1!=null)
							{
								
								if(funLoc!=null)
								{
									for (int i1=0;i1<funLoc.size();i1++)
										_tempFunLoc.add(funLoc.get(i1));
								}
								
								nGraph g_tam= new nGraph(_g.cap,_g.w);
								for(int i1=0;i1<_Sol1.size()-1;i1++)
								{
									double weight = _g.getEdgeWeight(_Sol1.get(i1), _Sol1.get(i1+1));
									g_tam.setEdgeWeight(_Sol1.get(i1), _Sol1.get(i1+1), weight - _d.getBw());
								}
								
								for (int j=0;j<_tempFunLoc.size();j++)
		    					{
		    						Pair pr = _tempFunLoc.get(j);
		    									    						
		    						Vector<Double> mul =  getFunction(pr.getfunction()).getLamda();
	    							if(UtilizeFunction.isBig(_g.getCap(pr.getnode()), mul))
	    							{
	    								Vector<Double> c_temp= UtilizeFunction.minus(_g.getCap(pr.getnode()),mul);
	    								g_tam.setCap(pr.getnode(),c_temp );
	    							}
		    						
		    					}	
								//Sub-flows 2
								fArr = new ArrayList<>();
								for (int i1 = index+1;i1<_d.getFunctions().size();i1++)
									fArr.add(_d.getFunctions().get(i1));
								funLoc = new ArrayList<>();
								_newd= new nDemand(_d.getId(), node, _d.getDest(), _d.getBw(), fArr );
								ArrayList<Integer> _Sol2 = GreedyBest(_newd, g_tam);
								if(funLoc!=null)
								{
									for (int i1=0;i1<funLoc.size();i1++)
										_tempFunLoc.add(funLoc.get(i1));
								}
								if(_Sol2!=null)
								{

									for(int i1=0;i1<_Sol1.size();i1++)
										_tempSol.add(_Sol1.get(i1));
									for (int i1=1;i1<_Sol2.size();i1++)
										_tempSol.add(_Sol2.get(i1));
									
								}
								if(_Sol2!=null && _tempSol.size()<_minVal)
								{
									_minVal = _tempSol.size();
									_minSol= new ArrayList<>();
									_minFunLoc = new ArrayList<>();
									for (int i1=0;i1<_tempSol.size();i1++)
										_minSol.add(_tempSol.get(i1));
									for (int i1=0;i1<_tempFunLoc.size();i1++)
										_minFunLoc.add(_tempFunLoc.get(i1));
								}
							}
						}
					}
					
				}				

			}
		}
		fl=true;
		if(_minVal== Integer.MAX_VALUE)
			return null;
		
		funLoc = new ArrayList<>();
		for (int i1=0;i1<_minFunLoc.size();i1++)
			funLoc.add(_minFunLoc.get(i1));
		return _minSol;	
		
	}
	
	public static ArrayList<Integer> GreedyBest(nDemand _d, nGraph _g)
	{
		fl=true;
		ArrayList<Integer> _forwardSol = GreedyForward(_d, _g);
		ArrayList<Pair> _forwardFunLoc = new ArrayList<>();
		
		if(funLoc!=null)
		{
			for (int i=0;i<funLoc.size();i++)
				_forwardFunLoc.add(funLoc.get(i));
		}
		funLoc = new ArrayList<>();
		ArrayList<Integer> _backwardSol= GreedyBackward(_d, _g);
		ArrayList<Pair> _backwardFunLoc = new ArrayList<>();
		if(funLoc!=null)
		{
			for (int i=0;i<funLoc.size();i++)
				_backwardFunLoc.add(funLoc.get(i));
		}
		funLoc= new ArrayList<>();
		int w1=Integer.MAX_VALUE;
		int w2=Integer.MAX_VALUE;
		//double w3 = Double.MAX_VALUE;
		if(_forwardSol!=null && _forwardSol.size()>0)
		{
			w1=_forwardSol.size();
//			w1=0.0;
//			for (int i=0;i<_forwardSol.size()-1;i++)
//				w1+=g.getEdgeWeight(_forwardSol.get(i), _forwardSol.get(i+1));
		}
		if(_backwardSol!=null && _backwardSol.size()>0)
		{
			w2=_backwardSol.size();
//			w2=0.0;
//			for (int i=0;i<_backwardSol.size()-1;i++)
//				w2+=g.getEdgeWeight(_backwardSol.get(i), _backwardSol.get(i+1));
		}
		if(w1!=Integer.MAX_VALUE || w2!=Integer.MAX_VALUE )
		{
			if(w1<w2)
			{
				//1 nho nhat
				for (int i=0;i<_forwardFunLoc.size();i++)
					funLoc.add(_forwardFunLoc.get(i));
				return _forwardSol;
								
			}
			else
			{
				//2 nho nhat
				for(int i=0;i<_backwardFunLoc.size();i++)
					funLoc.add(_backwardFunLoc.get(i));
				return _backwardSol;
							
			}
		}
		else
			return null;
	}
	
	
	public static boolean GreedyForward(String fileName)
	{
		maxlinkload=0.0;
		boolean  _flag=false;
		final long startTime = System.currentTimeMillis();
		acceptRatio=0;
		//Select at random 1 node in Z1, Z2,...Zn
		
		try {
			File file = new File(fileName);
			out = new BufferedWriter(new FileWriter(file));

			for (int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
					link_load[i][j]=0.0;
			
			ArrayList<Integer> _sol = new ArrayList<Integer>();
			ArrayList<Integer> path;
			for (int i=0;i<noDemand;i++)
			{
				_flag=false;
				_sol = new ArrayList<Integer>();
				nDemand _d = DemandArray.get(i);
				nGraph g_save= new nGraph(g_edit.cap,g_edit.w);
				int _src = _d.getSrc();
				int _dest= _d.getDest();
				ArrayList<Integer> _fLst=_d.getFunctions();
				ArrayList<Integer> _z1= getFunction(_fLst.get(0)).getVnfNode();//tap z1
				int node1=-1;
				int node2=-1;
				int minNode=-1;
				ArrayList<Integer> _sp = new ArrayList<>();//luu tru duong di ngan nhat hien tai
				double _minVal=Double.MAX_VALUE;//luu tru do dai ngan nhat cua duong di.
				// Chon 1 duong ngan nhat tu src den tap z1 ->
				for (int _id=0;_id<_z1.size();_id++)
				{
					//xet tung nut trong z1 va tim duong di
					node1 = _z1.get(_id);
					if(UtilizeFunction.isBig(g_edit.getCap(node1), getFunction(_fLst.get(0)).getLamda()))
					{
						path= ShortestPath(_src, node1,g_edit, _d.getBw());
						if (path ==null)
						{
							//ko ton tai duong di -> blocking can thu lai
							continue;
						}
						else
						{
							double _weight = 0.0;
							for (int _id1=0;_id1<path.size()-1;_id1++)
							{
								_weight += g.getEdgeWeight(path.get(_id1), path.get(_id1+1));
							}
							if (_weight<_minVal)
							{
								_sp= new ArrayList<>();
								for (int _id1=0;_id1<path.size();_id1++)
									_sp.add(path.get(_id1));
								_minVal = _weight;
								minNode= node1;//luu lai node co gia tri nho nhat
							}
							
						}
					}
					else
					{
						continue;
					}					
				}
				
					
				_flag=false;	
				if (minNode ==-1)
				{
					//truong hop ko the tim duoc bat ky 1 node nao trong z1 -> demand ko thoa man
					continue;
				}
				else
				{
					node1= minNode;
					minNode=-1;
					_minVal=Double.MAX_VALUE;
					//truoc het pai add duong di ngan nhat giua src va z1 vao _sol
					for (int _id1=0;_id1<_sp.size();_id1++)
						_sol.add(_sp.get(_id1));
					//cap nhat lai do thi g_edit
					
					for (int _node=0;_node<_sp.size()-1;_node++)
					{
						double w_temp= g_edit.getEdgeWeight(_sp.get(_node), _sp.get(_node+1))-_d.getBw();
						g_edit.setEdgeWeight(_sp.get(_node), _sp.get(_node+1),w_temp );
					}
					Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(node1),getFunction(_fLst.get(0)).getLamda());
					g_edit.setCap(node1,c_temp );
					
					//thuc hien voi cac cap z2, z3..
					for (int _id1=1;_id1<_fLst.size();_id1++)
					{
						minNode=-1;
						_minVal=Double.MAX_VALUE;
						ArrayList<Integer> _zi= getFunction(_fLst.get(_id1)).getVnfNode();
						for (int _id2=0;_id2<_zi.size();_id2++)
						{
							//xet tung nut trong z1 va tim duong di
							node2 = _zi.get(_id2);
							if(UtilizeFunction.isBig(g_edit.getCap(node2), getFunction(_fLst.get(_id1)).getLamda()))
							{
								path= ShortestPath(node1, node2, g_edit, _d.getBw());
								if (path ==null)
								{
									//ko ton tai duong di -> blocking can thu lai
									continue;
								}
								else
								{
									double _weight = 0.0;
									for (int _id3=0;_id3<path.size()-1;_id3++)
									{
										_weight += g.getEdgeWeight(path.get(_id3), path.get(_id3+1));
									}
									if (_weight<_minVal)
									{
										_flag=true;
										_sp= new ArrayList<>();
										for (int _id3=0;_id3<path.size();_id3++)
											_sp.add(path.get(_id3));
										_minVal = _weight;
										minNode= node2;//luu lai node co gia tri nho nhat
									}
									
								}
							}
							else
							{
								//tiep tuc thu voi node khac
								continue;
							}
							
						}
						
						if(!_flag)//neu ko tim duoc duong di ngan nhat giua 2 cap zi va zj -> demand nay ko the tim ra duong di
						{							
							break;
						}
						else
						{
							for (int _id2=1;_id2<_sp.size();_id2++)
								_sol.add(_sp.get(_id2));
							node1=minNode;
							//cap nhat lai do thi g_edit
							
							for (int _node=0;_node<_sp.size()-1;_node++)
							{
								double w_temp= g_edit.getEdgeWeight(_sp.get(_node), _sp.get(_node+1))-_d.getBw();
								g_edit.setEdgeWeight(_sp.get(_node), _sp.get(_node+1),w_temp );
							}
							c_temp= UtilizeFunction.minus(g_edit.getCap(node1),getFunction(_fLst.get(_id1)).getLamda());
							g_edit.setCap(node1,c_temp );
						}
					}
					if(!_flag)//neu demand nay ko the tim ra yeu cau thoa man
					{
						g_edit = new nGraph(g_save.cap,g_save.w);//chuyen ve do thi binh thuong
						continue;
					}
					else 
					{
						//tim duong di giua zn va dest
						if(minNode != _dest)
						{
							path= ShortestPath(minNode, _dest,g_edit, _d.getBw());
							if (path ==null)
							{
								//ko ton tai duong di -> blocking can thu lai
								g_edit = new nGraph(g_save.cap,g_save.w);
								continue;
							}
							else
							{
								for (int _id3=1;_id3<path.size();_id3++)
									_sol.add(path.get(_id3));
								for (int _node=0;_node<path.size()-1;_node++)
								{
									double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_d.getBw();
									g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
								}
								
							}
						}
					}
					solution_node.add(_sol);
					solution_id.add(_d.getId());
					for(int _id=0;_id<_sol.size()-1;_id++)
					{
						if(g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1))>0)
							link_load[_sol.get(_id)-1][_sol.get(_id+1)-1]+=_d.getBw()/g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1));
						else
						{
							out.write("!Hai node nay bi sai?"+ _sol.get(_id)+"?"+ _sol.get(_id+1)+"!");
						}
					}			
					out.write(_d.getId()+ ":");
					for (int _node: _sol)
					{
						out.write(_node+" ");
					}
					out.newLine();
				}
				
				
			}
			for (int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
					if(link_load[i][j]>maxlinkload)
						maxlinkload = link_load[i][j];
			acceptRatio = solution_id.size()*1.0/noDemand;
			out.write("Number of accepted demands: " + acceptRatio);
			_duration = System.currentTimeMillis() - startTime;
			out.newLine();
			out.write("Maximum link load: "+ maxlinkload);
			out.newLine();
		}
		catch ( IOException e1 ) {
			e1.printStackTrace();
			} finally {
				if ( out != null )
					try {
						out.close();
						} catch (IOException e) {
							e.printStackTrace();}
				}    
	try {
  		out.close();
  		} catch (IOException e2) {
  			e2.printStackTrace();
  			}
	
		return true;
	}
	
	
	public static ArrayList<Integer> Alg3(nDemand _d, nGraph _g)
	{
		fl=false;
		funLoc = new ArrayList<>();
		ArrayList<Integer> p1,p2;
		int noBlock=4;
		boolean flag=false;
		nGraph g_tam= new nGraph(_g.cap,_g.w);
		nGraph g_save= new nGraph(_g.cap,_g.w);
		ArrayList<Integer> _sol = new ArrayList<Integer>();
		Random rand = new Random(); 
		int _src = _d.getSrc();
		int _dest= _d.getDest();
		ArrayList<BNode> _bNode = new ArrayList<>();
		_bNode.add(new BNode(_src,g_tam,null,null));
		ArrayList<Integer> _fLst=_d.getFunctions();
		int node1=-1;
		int node2=-1;
		
		//find SP from n_i to n_j
		for (int j=0;j<_fLst.size();j++)
		{
			
			ArrayList<Integer> _vnfLst1 = getFunction(_fLst.get(j)).getVnfNode();

			int lastNode=-1;
			for (int _id1=0;_id1<j+1;_id1++)
				lastNode+=Math.pow(2, _id1);
			int marked=-1;
			for(int _id1=0;_id1<Math.pow(2, j);_id1++)
			{
				int index=lastNode-(int) Math.pow(2, j)+_id1+1;
				BNode _b = _bNode.get(index);
				g_save = _b.getGraph();
				//if g_save=null thi sao?????
				while (noBlock>0)
				{
					node1 = _vnfLst1.get(rand.nextInt(_vnfLst1.size()));
					node2=node1;
					while (node1 == node2)
						node2 = _vnfLst1.get(rand.nextInt(_vnfLst1.size()));
					if(g_save!=null&&UtilizeFunction.isBig(g_save.getCap(node1),getFunction(_fLst.get(j)).getLamda())&&UtilizeFunction.isBig(g_save.getCap(node2),getFunction(_fLst.get(j)).getLamda()))
					{
						flag=true;
						break;
					}
					else
						noBlock--;
				}
				if(flag)
				{
					marked=_id1;
					break;
				}
				else
					flag=false;
			}
			if(!flag)
				break;
			for(int _id1=0;_id1<Math.pow(2, j);_id1++)
			{
				int index=lastNode-(int) Math.pow(2, j)+_id1+1;
				if(_id1<marked)
				{
					//duong cut -> add duong di thanh bang null het, do thi cung bang null
					BNode b1 = new BNode(node1,null,null,null);
					BNode b2 = new BNode(node2,null,null,null);
					_bNode.add(b1);
					_bNode.add(b2);
					continue;
				}
				
				BNode _b = _bNode.get(index);
				g_save = _b.getGraph();
				funLoc = _b.getFLoc();
				ArrayList<Integer> pCurrent = new ArrayList<>();
				p1=new ArrayList<>();
				p2=new ArrayList<>();
				pCurrent = _b.getPath();
				if(g_save==null)
				{
					//cung bi cut o day
					BNode b1 = new BNode(node1,null,null,null);
					BNode b2 = new BNode(node2,null,null,null);
					_bNode.add(b1);
					_bNode.add(b2);
					continue;
				}
				if(UtilizeFunction.isBig(g_save.getCap(node1),getFunction(_fLst.get(j)).getLamda()))
					p1 = ShortestPath(_b.getPreNode(), node1,g_save, _d.getBw());
				else
					p1=null;
				if(UtilizeFunction.isBig(g_save.getCap(node2),getFunction(_fLst.get(j)).getLamda()))
					p2=  ShortestPath(_b.getPreNode(), node2,g_save,_d.getBw());
				else
					p2=null;
				
				if(p1==null)
				{
					BNode b1 = new BNode(node1,null,null,null);
					_bNode.add(b1);
				}
				else
				{
					funLoc = _b.getFLoc();
					ArrayList<Integer> np1 = new ArrayList<>();
					if(pCurrent !=null)
					{
						for(int _id2 =0;_id2<pCurrent.size();_id2++)
						{
							np1.add(pCurrent.get(_id2));
						}
					}
					else
					{
						np1.add(p1.get(0));
					}
						
					for(int _id2 =1;_id2<p1.size();_id2++)
					{
						np1.add(p1.get(_id2));
					}
					
					for (int _node=0;_node<p1.size()-1;_node++)
					{
						double w_temp= g_save.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
						g_save.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
					}
					Vector<Double> c_temp= UtilizeFunction.minus(g_save.getCap(node1),getFunction(_fLst.get(j)).getLamda());
					g_save.setCap(node1,c_temp );
					if(funLoc==null)
						funLoc = new ArrayList<>();
					funLoc.add(new Pair(node1,_fLst.get(j)));
					
					BNode b1 = new BNode(node1,g_save,np1,funLoc);
					_bNode.add(b1);
					
				}
				if(p2==null)
				{
					BNode b2 = new BNode(node2,null,null,null);
					_bNode.add(b2);
				}
				else
				{
					funLoc = _b.getFLoc();
					g_save = _b.getGraph();
					ArrayList<Integer> np2 = new ArrayList<>();
					if(pCurrent !=null)
					{
						for(int _id2 =0;_id2<pCurrent.size();_id2++)
						{
							np2.add(pCurrent.get(_id2));
						}
					}
					else
					{
						np2.add(p2.get(0));
					}
						
					for(int _id2 =1;_id2<p2.size();_id2++)
					{
						np2.add(p2.get(_id2));
					}
					
					for (int _node=0;_node<p2.size()-1;_node++)
					{
						double w_temp= g_save.getEdgeWeight(p2.get(_node), p2.get(_node+1))-_d.getBw();
						g_save.setEdgeWeight(p2.get(_node), p2.get(_node+1),w_temp );
					}
					Vector<Double> c_temp= UtilizeFunction.minus(g_save.getCap(node1),getFunction(_fLst.get(j)).getLamda());
					g_save.setCap(node2,c_temp );
					if(funLoc==null)
						funLoc = new ArrayList<>();
					funLoc.add(new Pair(node2,_fLst.get(j)));
					BNode b2 = new BNode(node2,g_save,np2,funLoc);
					_bNode.add(b2);
				}					
				
			}				
		}	
		if(!flag)
		{
			//cap nhat lai do thi ban dau khi ko the phuc vu duoc demand nay
			fl=false;
			return null;
		}
		//tim duong cuoi cung
		int j = _fLst.size();
		int lastNode=-1;
		for (int _id1=0;_id1<j;_id1++)
			lastNode+=Math.pow(2, _id1);
		for (int _id1 = lastNode+1;_id1< _bNode.size();_id1++)
		{
			BNode _b = _bNode.get(_id1);
			g_save = _b.getGraph();
			ArrayList<Integer> pCurrent = new ArrayList<>();
			p1=new ArrayList<>();
			pCurrent = _b.getPath();
			if(g_save!=null)
				p1 = ShortestPath(_b.getPreNode(), _dest,g_save, _d.getBw());
			else
				p1=null;
			if(p1!=null)
			{
				ArrayList<Integer> np1 = new ArrayList<>();
				if(pCurrent!=null)
				{
					for(int _id2 =0;_id2<pCurrent.size();_id2++)
					{
						np1.add(pCurrent.get(_id2));
					}
				}
				else
					np1.add(p1.get(0));
				
				for(int _id2 =1;_id2<p1.size();_id2++)
				{
					np1.add(p1.get(_id2));
				}
				for (int _node=0;_node<p1.size()-1;_node++)
				{
					double w_temp= g_save.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
					g_save.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
				}
				_b.setPath(np1);
				_b.setGraph(g_save);
			}
		}
		//tim ra nho nhat trong cac duong.
		double minValue = Double.MAX_VALUE;
		double temp =0;
		flag=false;
		for (int _id1 = lastNode+1;_id1< _bNode.size();_id1++)
		{
			BNode _b = _bNode.get(_id1);
			ArrayList<Integer> pCurrent = _b.getPath();
			if(pCurrent!=null)
			{
				temp =0.0;
				for (int _id2 = 0;_id2<pCurrent.size()-1;_id2++)
					temp+= g.getEdgeWeight(pCurrent.get(_id2), pCurrent.get(_id2+1));
				if(temp <minValue )
				{
					flag=true;
					_sol = new ArrayList<>();
					minValue=temp;
					for (int _id2=0;_id2<pCurrent.size();_id2++)
						_sol.add(pCurrent.get(_id2));
					g_save= _b.getGraph();
					funLoc = _b.getFLoc();
				
				}
			}
		}
		if(!flag)
		{
			//demand nay ko tim duoc
			fl=false;
			return null;
		}
		g_tam= new nGraph(g_save.cap,g_save.w);
		fl=true;
		return _sol;
	
	
	
	}
	public static boolean Alg3(String fileName)
	{
		funLoc = new ArrayList<>();
		maxlinkload=0.0;
		ArrayList<Integer> p1,p2;
		int noBlock=4;
		boolean flag=false;
		nGraph g1= new nGraph(g_edit.cap,g_edit.w);
		//Select at random 2 node in Z1, Z2,...Zn
		final long startTime = System.currentTimeMillis();
		acceptRatio=0;
		try {
			File file = new File(fileName);
			out = new BufferedWriter(new FileWriter(file));

			for (int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
					link_load[i][j]=0.0;
			
			ArrayList<Integer> _sol = new ArrayList<Integer>();
			Random rand = new Random(); 
			for (int i=0;i<noDemand;i++)
			{
				_sol = new ArrayList<Integer>();
				nDemand _d = DemandArray.get(i);
				int _src = _d.getSrc();
				int _dest= _d.getDest();
				ArrayList<BNode> _bNode = new ArrayList<>();
				_bNode.add(new BNode(_src,g_edit,null,null));
				ArrayList<Integer> _fLst=_d.getFunctions();
				int node1=-1;
				int node2=-1;
				
				//find SP from n_i to n_j
				for (int j=0;j<_fLst.size();j++)
				{
					
					ArrayList<Integer> _vnfLst1 = getFunction(_fLst.get(j)).getVnfNode();

					int lastNode=-1;
					for (int _id1=0;_id1<j+1;_id1++)
						lastNode+=Math.pow(2, _id1);
					int marked=-1;
					for(int _id1=0;_id1<Math.pow(2, j);_id1++)
					{
						int index=lastNode-(int) Math.pow(2, j)+_id1+1;
						BNode _b = _bNode.get(index);
						g1 = _b.getGraph();
						//if g1=null thi sao?????
						while (noBlock>0)
						{
							node1 = _vnfLst1.get(rand.nextInt(_vnfLst1.size()));
							node2=node1;
							while (node1 == node2)
								node2 = _vnfLst1.get(rand.nextInt(_vnfLst1.size()));
							if(g1!=null&&UtilizeFunction.isBig(g1.getCap(node1),getFunction(_fLst.get(j)).getLamda())&&UtilizeFunction.isBig(g1.getCap(node2),getFunction(_fLst.get(j)).getLamda()))
							{
								flag=true;
								break;
							}
							else
								noBlock--;
						}
						if(flag)
						{
							marked=_id1;
							break;
						}
						else
							flag=false;
					}
					if(!flag)
						break;
					for(int _id1=0;_id1<Math.pow(2, j);_id1++)
					{
						int index=lastNode-(int) Math.pow(2, j)+_id1+1;
						if(_id1<marked)
						{
							//duong cut -> add duong di thanh bang null het, do thi cung bang null
							BNode b1 = new BNode(node1,null,null,null);
							BNode b2 = new BNode(node2,null,null,null);
							_bNode.add(b1);
							_bNode.add(b2);
							continue;
						}
						
						BNode _b = _bNode.get(index);
						g1 = _b.getGraph();
						funLoc=_b.getFLoc();
						ArrayList<Integer> pCurrent = new ArrayList<>();
						p1=new ArrayList<>();
						p2=new ArrayList<>();
						pCurrent = _b.getPath();
						if(g1==null)
						{
							//cung bi cut o day
							BNode b1 = new BNode(node1,null,null,null);
							BNode b2 = new BNode(node2,null,null,null);
							_bNode.add(b1);
							_bNode.add(b2);
							continue;
						}
						if(UtilizeFunction.isBig(g1.getCap(node1),getFunction(_fLst.get(j)).getLamda()))
							p1 = ShortestPath(_b.getPreNode(), node1,g1, _d.getBw());
						else
							p1=null;
						if(UtilizeFunction.isBig(g1.getCap(node2),getFunction(_fLst.get(j)).getLamda()))
							p2=  ShortestPath(_b.getPreNode(), node2,g1,_d.getBw());
						else
							p2=null;
						
						if(p1==null)
						{
							BNode b1 = new BNode(node1,null,null,null);
							_bNode.add(b1);
						}
						else
						{
							ArrayList<Integer> np1 = new ArrayList<>();
							if(pCurrent !=null)
							{
								for(int _id2 =0;_id2<pCurrent.size();_id2++)
								{
									np1.add(pCurrent.get(_id2));
								}
							}
							else
							{
								np1.add(p1.get(0));
							}
								
							for(int _id2 =1;_id2<p1.size();_id2++)
							{
								np1.add(p1.get(_id2));
							}
							
							for (int _node=0;_node<p1.size()-1;_node++)
							{
								double w_temp= g1.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
								g1.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
							}
							Vector<Double> c_temp= UtilizeFunction.minus(g1.getCap(node1),getFunction(_fLst.get(j)).getLamda());
							g1.setCap(node1,c_temp );
							funLoc.add(new Pair(node1,_fLst.get(j)));
							BNode b1 = new BNode(node1,g1,np1,funLoc);
							_bNode.add(b1);
							
						}
						if(p2==null)
						{
							BNode b2 = new BNode(node2,null,null,null);
							_bNode.add(b2);
						}
						else
						{
							funLoc = _b.getFLoc();
							g1 = _b.getGraph();
							ArrayList<Integer> np2 = new ArrayList<>();
							if(pCurrent !=null)
							{
								for(int _id2 =0;_id2<pCurrent.size();_id2++)
								{
									np2.add(pCurrent.get(_id2));
								}
							}
							else
							{
								np2.add(p2.get(0));
							}
								
							for(int _id2 =1;_id2<p2.size();_id2++)
							{
								np2.add(p2.get(_id2));
							}
							
							for (int _node=0;_node<p2.size()-1;_node++)
							{
								double w_temp= g1.getEdgeWeight(p2.get(_node), p2.get(_node+1))-_d.getBw();
								g1.setEdgeWeight(p2.get(_node), p2.get(_node+1),w_temp );
							}
							Vector<Double> c_temp= UtilizeFunction.minus(g1.getCap(node1),getFunction(_fLst.get(j)).getLamda());
							g1.setCap(node2,c_temp );
							funLoc.add(new Pair(node2,_fLst.get(j)));
							BNode b2 = new BNode(node2,g1,np2,funLoc);
							_bNode.add(b2);
						}					
						
					}				
				}	
				if(!flag)
				{
					//cap nhat lai do thi ban dau khi ko the phuc vu duoc demand nay
					continue;
				}
				//tim duong cuoi cung
				int j = _fLst.size();
				int lastNode=-1;
				for (int _id1=0;_id1<j;_id1++)
					lastNode+=Math.pow(2, _id1);
				for (int _id1 = lastNode+1;_id1< _bNode.size();_id1++)
				{
					BNode _b = _bNode.get(_id1);
					g1 = _b.getGraph();
					ArrayList<Integer> pCurrent = new ArrayList<>();
					p1=new ArrayList<>();
					pCurrent = _b.getPath();
					if(g1!=null)
						p1 = ShortestPath(_b.getPreNode(), _dest,g1, _d.getBw());
					else
						p1=null;
					if(p1!=null)
					{
						ArrayList<Integer> np1 = new ArrayList<>();
						if(pCurrent!=null)
						{
							for(int _id2 =0;_id2<pCurrent.size();_id2++)
							{
								np1.add(pCurrent.get(_id2));
							}
						}
						else
							np1.add(p1.get(0));
						
						for(int _id2 =1;_id2<p1.size();_id2++)
						{
							np1.add(p1.get(_id2));
						}
						for (int _node=0;_node<p1.size()-1;_node++)
						{
							double w_temp= g1.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
							g1.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
						}
						_b.setPath(np1);
						_b.setGraph(g1);
					}
				}
				//tim ra nho nhat trong cac duong.
				double minValue = Double.MAX_VALUE;
				double temp =0;
				flag=false;
				for (int _id1 = lastNode+1;_id1< _bNode.size();_id1++)
				{
					BNode _b = _bNode.get(_id1);
					ArrayList<Integer> pCurrent = _b.getPath();
					if(pCurrent!=null)
					{
						temp =0.0;
						for (int _id2 = 0;_id2<pCurrent.size()-1;_id2++)
							temp+= g.getEdgeWeight(pCurrent.get(_id2), pCurrent.get(_id2+1));
						if(temp <minValue )
						{
							flag=true;
							_sol = new ArrayList<>();
							minValue=temp;
							for (int _id2=0;_id2<pCurrent.size();_id2++)
								_sol.add(pCurrent.get(_id2));
							g1= _b.getGraph();
							funLoc =_b.getFLoc();
						
						}
					}
				}
				if(!flag)
				{
					//demand nay ko tim duoc
					continue;
				}
				g_edit= new nGraph(g1.cap,g1.w);
				for(int _id=0;_id<_sol.size()-1;_id++)
				{
					if(g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1))>0)
						link_load[_sol.get(_id)-1][_sol.get(_id+1)-1]+=_d.getBw()/g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1));
					else
					{
						out.write("!Hai node nay bi sai?"+ _sol.get(_id)+"?"+ _sol.get(_id+1)+"!");
					}
					
				}			
				solution_node.add(_sol);
				solution_id.add(_d.getId());
				out.write(_d.getId()+ ":");
				for (int _node: _sol)
				{
					out.write(_node+" ");
				}
				out.newLine();
			}
			for (int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
					if(link_load[i][j]>maxlinkload)
						maxlinkload = link_load[i][j];
			acceptRatio = solution_id.size()*1.0/noDemand;
			out.write("Number of accepted demands: " + acceptRatio);
			_duration = System.currentTimeMillis() - startTime;
			out.newLine();
			out.write("Maximum link load: "+ maxlinkload);
			out.newLine();
		}
		catch ( IOException e1 ) {
			e1.printStackTrace();
			} finally {
				if ( out != null )
					try {
						out.close();
						} catch (IOException e) {
							e.printStackTrace();}
				}    
	try {
  		out.close();
  		} catch (IOException e2) {
  			e2.printStackTrace();
  			}
	
		return true;
	
	}
	public static ArrayList<Integer> Alg4(nDemand _d,nGraph _g)
	{
		//Chon 2 node bat ky va giu lai duong ngan nhat
		fl=false;
		funLoc = new ArrayList<>();
		int noBlock=4;
		int _count=0;
		boolean  _flag=false;
		ArrayList<Integer> p1,p2;		
		ArrayList<Integer> _sol = new ArrayList<Integer>();
		Random rand = new Random(); 
		nGraph g_tam= new nGraph(_g.cap,_g.w);
		nGraph g_save = new nGraph(_g.cap,_g.w);
		int _src = _d.getSrc();
		int _dest= _d.getDest();
		int preNode =_src;
		ArrayList<Integer> _fLst=_d.getFunctions();
		ArrayList<Integer> _vnfLst;
		int node1=-1;
		int node2=-1;
		//find SP from n_i to n_j
		for (int j=0;j<_fLst.size();j++)
		{
			
			_vnfLst = getFunction(_fLst.get(j)).getVnfNode();
			while(!_flag)
			{
				node1 = _vnfLst.get(rand.nextInt(_vnfLst.size()));
				node2=node1;
				while (node1 == node2)
					node2 = _vnfLst.get(rand.nextInt(_vnfLst.size()));
				if(!UtilizeFunction.isBig(g_tam.getCap(node1),getFunction(_fLst.get(j)).getLamda())&&!UtilizeFunction.isBig(g_tam.getCap(node2),getFunction(_fLst.get(j)).getLamda()))
				{
					_count++;
					if(_count<noBlock)
						continue;
					else
					{
						_flag=true;
						break;
					}
				}
				p1 = ShortestPath(preNode, node1, g_tam, _d.getBw());
				p2=  ShortestPath(preNode, node2, g_tam,_d.getBw());
				if(p1 !=null || p2!=null)
				{
					if(p1==null)
					{
						//chon duong p2
						for (int _id1=0;_id1<p2.size()-1;_id1++)
							_sol.add(p2.get(_id1));
						for (int _node=0;_node<p2.size()-1;_node++)
						{
							double w_temp= g_tam.getEdgeWeight(p2.get(_node), p2.get(_node+1))-_d.getBw();
							g_tam.setEdgeWeight(p2.get(_node), p2.get(_node+1),w_temp );
						}
						Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node2),getFunction(_fLst.get(j)).getLamda());
						g_tam.setCap(node2,c_temp );
						funLoc.add(new Pair(node2,_fLst.get(j)));
						preNode = node2;
						break;
					}
					if(p2==null)
					{
						//chon duong p1
						for (int _id1=0;_id1<p1.size()-1;_id1++)
							_sol.add(p1.get(_id1));
						
						for (int _node=0;_node<p1.size()-1;_node++)
						{
							double w_temp= g_tam.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
							g_tam.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
						}
						Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node1),getFunction(_fLst.get(j)).getLamda());
						g_tam.setCap(node1,c_temp );
						funLoc.add(new Pair(node1,_fLst.get(j)));
						preNode = node1;
						break;
					}
					double cost1=0.0;
					double cost2=0.0;
					for (int _id1=0;_id1<p1.size()-1;_id1++)
					{
						cost1+=g.getEdgeWeight(p1.get(_id1), p1.get(_id1+1));
					}
					for (int _id1=0;_id1<p2.size()-1;_id1++)
					{
						cost2+=g.getEdgeWeight(p2.get(_id1), p2.get(_id1+1));
					}
					if(cost1>cost2)
					{
						//chon duong p2
						for (int _id1=0;_id1<p2.size()-1;_id1++)
							_sol.add(p2.get(_id1));
						for (int _node=0;_node<p2.size()-1;_node++)
						{
							double w_temp= g_tam.getEdgeWeight(p2.get(_node), p2.get(_node+1))-_d.getBw();
							g_tam.setEdgeWeight(p2.get(_node), p2.get(_node+1),w_temp );
						}
						Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node2),getFunction(_fLst.get(j)).getLamda());
						g_tam.setCap(node2,c_temp );
						funLoc.add(new Pair(node2,_fLst.get(j)));
						preNode = node2;
						break;
					}
					else
					{
						//chon duong p1
						for (int _id1=0;_id1<p1.size()-1;_id1++)
							_sol.add(p1.get(_id1));
						for (int _node=0;_node<p1.size()-1;_node++)
						{
							double w_temp= g_tam.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
							g_tam.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
						}
						Vector<Double> c_temp= UtilizeFunction.minus(g_tam.getCap(node1),getFunction(_fLst.get(j)).getLamda());
						g_tam.setCap(node1,c_temp );
						funLoc.add(new Pair(node1,_fLst.get(j)));
						preNode = node1;
						break;
					}
				
				}
				else
				{
					//ko ton tai duong di -> blocking can thu lai
					_count++;
					if(_count<noBlock)
						continue;
					else
					{
						_flag=true;
						break;
					}
				}
			}
			if(_flag)
				break;
		}
			
		if(_flag)
		{
			//truong hop demand k duoc phuc vu. g_tam se duoc gan bang g_save
			g_tam = new nGraph(g_save.cap,g_save.w);
			fl=false;
			return null;
		}
		//tim duong cuoi cung
		
		p1 = ShortestPath(preNode, _dest,_g,_d.getBw());
		if(p1 !=null)
		{
			for (int _id1=0;_id1<p1.size();_id1++)
				_sol.add(p1.get(_id1));
			for (int _node=0;_node<p1.size()-1;_node++)
			{
				double w_temp= _g.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
				_g.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
			}
		}
		else
		{
			//ko ton tai duong di 
			_flag=true;
			_g = new nGraph(g_save.cap,g_save.w);
			fl=false;
			return null;
		}
		fl=true;
		return _sol;
	
	
	}
	public static boolean Alg4(String fileName)
	{
		maxlinkload=0.0;
		//Chon 2 node bat ky va giu lai duong ngan nhat
		int noBlock=4;
		int _count=0;
		boolean  _flag=false;
		ArrayList<Integer> p1,p2;
		final long startTime = System.currentTimeMillis();
		acceptRatio=0;
		//Select at random 2 node in Z1, Z2,...Zn
		
		try {
			File file = new File(fileName);
			out = new BufferedWriter(new FileWriter(file));

			for (int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
					link_load[i][j]=0.0;
			
			ArrayList<Integer> _sol = new ArrayList<Integer>();
			Random rand = new Random(); 
			for (int i=0;i<noDemand;i++)
			{
				_flag=false;
				_count=0;
				_sol = new ArrayList<Integer>();
				nGraph g_save = new nGraph(g_edit.cap,g_edit.w);
				nDemand _d = DemandArray.get(i);
				int _src = _d.getSrc();
				int _dest= _d.getDest();
				int preNode =_src;
				ArrayList<Integer> _fLst=_d.getFunctions();
				ArrayList<Integer> _vnfLst;
				int node1=-1;
				int node2=-1;
				//find SP from n_i to n_j
				for (int j=0;j<_fLst.size();j++)
				{
					
					_vnfLst = getFunction(_fLst.get(j)).getVnfNode();
					while(!_flag)
					{
						node1 = _vnfLst.get(rand.nextInt(_vnfLst.size()));
						node2=node1;
						while (node1 == node2)
							node2 = _vnfLst.get(rand.nextInt(_vnfLst.size()));
						if(!UtilizeFunction.isBig(g_edit.getCap(node1),getFunction(_fLst.get(j)).getLamda())&&!UtilizeFunction.isBig(g_edit.getCap(node2),getFunction(_fLst.get(j)).getLamda()))
						{
							_count++;
							if(_count<noBlock)
								continue;
							else
							{
								_flag=true;
								break;
							}
						}
						p1 = ShortestPath(preNode, node1, g_edit, _d.getBw());
						p2=  ShortestPath(preNode, node2, g_edit,_d.getBw());
						if(p1 !=null || p2!=null)
						{
							if(p1==null)
							{
								//chon duong p2
								for (int _id1=0;_id1<p2.size()-1;_id1++)
									_sol.add(p2.get(_id1));
								for (int _node=0;_node<p2.size()-1;_node++)
								{
									double w_temp= g_edit.getEdgeWeight(p2.get(_node), p2.get(_node+1))-_d.getBw();
									g_edit.setEdgeWeight(p2.get(_node), p2.get(_node+1),w_temp );
								}
								Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(node2),getFunction(_fLst.get(j)).getLamda());
								g_edit.setCap(node2,c_temp );
								preNode = node2;
								break;
							}
							if(p2==null)
							{
								//chon duong p1
								for (int _id1=0;_id1<p1.size()-1;_id1++)
									_sol.add(p1.get(_id1));
								
								for (int _node=0;_node<p1.size()-1;_node++)
								{
									double w_temp= g_edit.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
									g_edit.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
								}
								Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(node1),getFunction(_fLst.get(j)).getLamda());
								g_edit.setCap(node1,c_temp );
								preNode = node1;
								break;
							}
							double cost1=0.0;
							double cost2=0.0;
							for (int _id1=0;_id1<p1.size()-1;_id1++)
							{
								cost1+=g.getEdgeWeight(p1.get(_id1), p1.get(_id1+1));
							}
							for (int _id1=0;_id1<p2.size()-1;_id1++)
							{
								cost2+=g.getEdgeWeight(p2.get(_id1), p2.get(_id1+1));
							}
							if(cost1>cost2)
							{
								//chon duong p2
								for (int _id1=0;_id1<p2.size()-1;_id1++)
									_sol.add(p2.get(_id1));
								for (int _node=0;_node<p2.size()-1;_node++)
								{
									double w_temp= g_edit.getEdgeWeight(p2.get(_node), p2.get(_node+1))-_d.getBw();
									g_edit.setEdgeWeight(p2.get(_node), p2.get(_node+1),w_temp );
								}
								Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(node2),getFunction(_fLst.get(j)).getLamda());
								g_edit.setCap(node2,c_temp );
								preNode = node2;
								break;
							}
							else
							{
								//chon duong p1
								for (int _id1=0;_id1<p1.size()-1;_id1++)
									_sol.add(p1.get(_id1));
								for (int _node=0;_node<p1.size()-1;_node++)
								{
									double w_temp= g_edit.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
									g_edit.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
								}
								Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(node1),getFunction(_fLst.get(j)).getLamda());
								g_edit.setCap(node1,c_temp );
								preNode = node1;
								break;
							}
						
						}
						else
						{
							//ko ton tai duong di -> blocking can thu lai
							_count++;
							if(_count<noBlock)
								continue;
							else
							{
								_flag=true;
								break;
							}
						}
					}
					if(_flag)
						break;
				}
					
				if(_flag)
				{
					//truong hop demand k duoc phuc vu. g_edit se duoc gan bang g_save
					g_edit = new nGraph(g_save.cap,g_save.w);
					continue;
				}
				//tim duong cuoi cung
				
				//xet node dau tien bat dau tu source, khi do preNode1=preNode2 
				//g_tam1 = new nGraph(g_edit.cap,g_edit.w);
				p1 = ShortestPath(preNode, _dest,g_edit,_d.getBw());
				if(p1 !=null)
				{
					for (int _id1=0;_id1<p1.size();_id1++)
						_sol.add(p1.get(_id1));
					for (int _node=0;_node<p1.size()-1;_node++)
					{
						double w_temp= g_edit.getEdgeWeight(p1.get(_node), p1.get(_node+1))-_d.getBw();
						g_edit.setEdgeWeight(p1.get(_node), p1.get(_node+1),w_temp );
					}
				}
				else
				{
					//ko ton tai duong di 
					_flag=true;
					g_edit = new nGraph(g_save.cap,g_save.w);
					continue;
				}
			
				
				solution_node.add(_sol);
				solution_id.add(_d.getId());
				for(int _id=0;_id<_sol.size()-1;_id++)
				{
					if(g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1))>0)
						link_load[_sol.get(_id)-1][_sol.get(_id+1)-1]+=_d.getBw()/g.getEdgeWeight(_sol.get(_id), _sol.get(_id+1));
					else
					{
						out.write("!Hai node nay bi sai?"+ _sol.get(_id)+"?"+ _sol.get(_id+1)+"!");
					}
					
				}			
				out.write(_d.getId()+ ":");
				for (int _node: _sol)
				{
					out.write(_node+" ");
				}
				out.newLine();
				
				
			}
			for (int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
					if(link_load[i][j]>maxlinkload)
						maxlinkload = link_load[i][j];
			acceptRatio = solution_id.size()*1.0/noDemand;
			out.write("Number of accepted demands: " + acceptRatio);
			_duration = System.currentTimeMillis() - startTime;
			out.newLine();
			out.write("Maximum link load: "+ maxlinkload);
			out.newLine();
		}
		catch ( IOException e1 ) {
			e1.printStackTrace();
			} finally {
				if ( out != null )
					try {
						out.close();
						} catch (IOException e) {
							e.printStackTrace();}
				}    
	try {
  		out.close();
  		} catch (IOException e2) {
  			e2.printStackTrace();
  			}
	
		return true;
	
	}
	

	public static int indexOfSet(Link l,ArrayList<Link> set)
	{
		int id=-1;
		for(int i=0;i<set.size();i++)
			if(set.get(i).CompareTo(l))
				id=i;
		return id;
	}
	public static ArrayList<Link> ConnectionSetSimple(ArrayList<Integer> srcLst, ArrayList<Integer> destLst,double maxBandwidth)
	{
		ArrayList<Link> sets = new ArrayList<>();
		int V=g.getV();
		for (int i=0;i<V;i++)
			for (int j=0;j<V;j++)
			{
				if(!(srcLst.contains(i+1)&& srcLst.contains(j+1))||!(destLst.contains(i+1)&& destLst.contains(j+1)))
				{
					if (g.getEdgeWeight(i+1, j+1)>=maxBandwidth)
					{
						Link l= new Link(i+1, j+1);
						sets.add(l);
					}
				}
			}
		for (int i=0;i<srcLst.size();i++)
			for (int j=0;j<destLst.size();j++)
			{
				if(srcLst.get(i)==destLst.get(j))
				{
					Link l = new Link(srcLst.get(i), destLst.get(j));
					sets.add(l);
				}
				
			}
		return sets;
		
	
	
	}
	public static ArrayList<Link> ConnectionSets(int src, int dest)
	{		
		ArrayList<Link> sets = new ArrayList<>();
		ArrayList<Link> setZero = new ArrayList<>();
		ArrayList<Link> idLst = new ArrayList<>();
		nGraph g_tam = new nGraph(g.cap,g.w);
		for (int i=0;i<g.getV();i++)
			for (int j=0;j<g.getV();j++)
			{
				if (g.getEdgeWeight(i+1, j+1)>0)
				{
					Link l= new Link(i+1, j+1);
					setZero.add(l);
				}
			}
		ArrayList<Integer> p = ShortestPath(src, dest, g_tam,0.0000001);
		if(p!=null)
		{
			for(int i=0;i<p.size()-1;i++)
			{
				Link l= new Link(p.get(i),p.get(i+1));
				sets.add(l);
				//remove l from setZero
				int id= indexOfSet(l, setZero);
				if(id!=-1)
					setZero.remove(id);
				g_tam.setEdgeWeight(p.get(i), p.get(i+1), 0);
				
			}
		}
		int len = setZero.size();
		while(len>0)
		{
			idLst = new ArrayList<>();
			int id= UtilizeFunction.randInt(0, len-1);
			Link l = setZero.get(id);
			ArrayList<Integer> p1 = ShortestPath(src, l.getStart(), g_tam,0.0000001);
			ArrayList<Integer> p2 = ShortestPath(l.getEnd(), dest, g_tam,0.0000001);
			if(p1!=null && p2!=null)
			{
				//add p1, p2, l to set
				sets.add(l);
				idLst.add(l);
					//setZero.remove(id1);
				for(int i=0;i<p1.size()-1;i++)
				{
					Link l1= new Link(p1.get(i),p1.get(i+1));
					sets.add(l1);
					//remove l from setZero
					idLst.add(l1);
						//setZero.remove(id1);
					g_tam.setEdgeWeight(p1.get(i), p1.get(i+1), 0);
					
				}
				for(int i=0;i<p2.size()-1;i++)
				{
					Link l1= new Link(p2.get(i),p2.get(i+1));
					sets.add(l1);
					//remove l from setZero
					idLst.add(l1);
						//setZero.remove(id1);
					g_tam.setEdgeWeight(p2.get(i), p2.get(i+1), 0);
					
				}
				//remove p1,p2, l from setzero
			}
			else
			{
				//remove l from setzero
				idLst.add(l);
					//setZero.remove(id1);
			}
			g_tam.setEdgeWeight(l.getStart(), l.getEnd(), 0);
			
			for(Link i:idLst)
			{
				setZero.remove(i);
			}
			len = setZero.size();	
			p1=null;
			p2=null;
		}
		p=null;
		return sets;
		
	}
	
	//tra ve gia tri cua tap set neu la giong nhau
	public static wLink linkContain_wLinkList (Link l,ArrayList<wLink> wl)
	{
		for (wLink _wl: wl)
		{
			if(_wl.getStart()==l.getStart() && _wl.getEnd() == l.getEnd())
				return _wl;
		}
		return null;
	}
	
	public static ArrayList<Integer> compairToLink (Link l, wLink wL)
	{
		if(l.getStart()==wL.getStart() && l.getEnd() == wL.getEnd())
			return wL.getCnnSet();
		else
			return null;
	}
	public static nGraph ExpandedGraphNew(nGraph _g, nDemand _d, ArrayList<wLink> violatedLink, ArrayList<Node> updatedNode)
	{
		int NumV=0;
		int src = _d.getSrc();
		int dest= _d.getDest();
		int lastNode = 0;
		ArrayList<Integer> fLst = _d.getFunctions();
		Vector<Vector<Double>> k = new Vector<>();
		h = new ArrayList<Integer>();//Function h is a mapping from an exspanded node to an origin node
		ArrayList<WeitEdge> edgeLst= new ArrayList<>();
		k.add(_g.getCap(src));
		h.add(src);
		source = 1;
		ArrayList<Integer> ZSize = new ArrayList<>();
		ArrayList<ArrayList<Integer>> Z = new ArrayList<>();
		ArrayList<Integer> Ztam= new ArrayList<>();
		
		for(int i=0;i<fLst.size();i++)
		{
			ArrayList<Integer> Zi= getFunction(fLst.get(i)).getVnfNode();
			Ztam= new ArrayList<>();
			int dem=0;
			for(int j=0;j<Zi.size();j++)
			{
				if(!UtilizeFunction.isBig(getFunction(fLst.get(i)).getLamda(),_g.getCap(Zi.get(j))))
				{
					dem++;			
					
					boolean violateNode = false;
					Node _nodeTemp = new Node(Zi.get(j));
					if(updatedNode.size()>0)
					{
						for (int id =0;id<updatedNode.size();id++)
						{
							if(updatedNode.get(id).CompareTo(_nodeTemp))
							{
								violateNode=true;
								ArrayList<Integer> setNode = updatedNode.get(id).getvSetLst();
								if(setNode.contains(i+1))
								{
									Ztam.add(Zi.get(j));
									k.add(_g.getCap(Zi.get(j)));
									h.add(Zi.get(j));
								}							
							}
						}
					}
					
					if(!violateNode)
					{
						Ztam.add(Zi.get(j));
						k.add(_g.getCap(Zi.get(j)));
						h.add(Zi.get(j));
					}
					
				}
		
			}
			Z.add(Ztam);
			ZSize.add(dem);
			NumV=NumV+dem;
		}
		
		
		k.add(_g.getCap(dest));
		h.add(dest);
		NumV=NumV+2;//tinh ca dau va cuoi 0->lastnode-1;
		destination=NumV;
		ArrayList<Integer> Z1 = new ArrayList<>();
		ArrayList<Integer> Z2= new ArrayList<>();
		//ArrayList<Integer> Ztam= new ArrayList<>();
		int node1=-1;
		int node2 =-1;
		for (int i=-1;i<fLst.size();i++)
		{
			
			//last node truoc Zi+1		
			if(i==-1)
			{
				node1 = 0;
				Z1 = new ArrayList<>();
				Z1.add(src);
				node2 = 1;
			}
			else
			{
				node1=1;
				for (int temp=0;temp<i;temp++)
					node1 = node1+ ZSize.get(temp);//last node truoc Zi
				node2= node1 + ZSize.get(i);
				
				Z1 = Z.get(i);
				//Z1 = getFunction(fLst.get(i)).getVnfNode();
				
			}
			if(i==fLst.size()-1)
			{
				Z2= new ArrayList<>();
				Z2.add(dest);				
			}
			else
			{
				Z2 = Z.get(i+1);
			}
			
			//add tat ca cac dinh trong G
			//ArrayList<Integer> nodeLst = new ArrayList<>();
			for (int j=0;j<_g.getV();j++)
			{
				//nodeLst.add(j+1);				
				k.add(_g.getCap(j+1));
				h.add(j+1);
			}
			lastNode = NumV;
			NumV+=_g.getV();
			
			//add tat ca cac link tu s_k den s_K+1
			for(int j1=0;j1<Z1.size();j1++)
			{
				for(int j2=0;j2<_g.getV();j2++)
				{
					if(Z1.get(j1)==j2+1)
						edgeLst.add(new WeitEdge(node1+j1+1, lastNode+j2+1, 100.0));//truong hop canh zero
				}
			}
			
			for(int j1=0;j1<_g.getV();j1++)
			{
				for (int j2=0;j2<Z2.size();j2++)
				{
					if(j1+1==Z2.get(j2))
						edgeLst.add(new WeitEdge(lastNode+j1+1, node2+j2+1, 100.0));//truong hop canh zero
				}
				for(int j2=0;j2<_g.getV();j2++)
				{
					if(j1!=j2 && _g.getEdgeWeight(j1+1, j2+1)>=_d.getBw())
					{
						wLink _l = new wLink(j1+1, j2+1, 1, null);
						
						boolean violateLink = false;
						for (int id =0;id<violatedLink.size();id++)
						{
							if(violatedLink.get(id).CompareTo(_l))
							{
								violateLink=true;
								ArrayList<Integer> setLink = violatedLink.get(id).getCnnSet();
								if(setLink.contains(i+2))
								{
									edgeLst.add(new WeitEdge(lastNode+j1+1, lastNode+j2+1, _g.getEdgeWeight(j1+1, j2+1)));
								}	
								break;
							}
						}
						if(!violateLink)
						{
							edgeLst.add(new WeitEdge(lastNode+j1+1, lastNode+j2+1, _g.getEdgeWeight(j1+1, j2+1)));
						}
					}
				}
			}
		}
		
		
		nGraph g_save = new nGraph(NumV);
		
		System.out.println("Number of Extended Link: "+edgeLst.size());
		if(edgeLst.size()==0)
			return null;
        for (WeitEdge edges : edgeLst) {
        	int s = edges.getO();
        	int t = edges.getD();
			double w= edges.getW();
			g_save.setEdgeWeight(s, t, w);
		}
	   	for (int i=0;i<g.getV();i++)
       {
	   		Vector<Double> t= k.get(i);
	   		g_save.setCap(i+1, t);
       }    
	   	//noVertex = g_save.getV();
	   	edgeLst =null;
	   	
		
		return g_save;
		
	
	
	
	}
	public static nGraph ConstructingExtendGraph(nGraph _g, nDemand _d, ArrayList<wLink> violatedLink, ArrayList<Node> updatedNode)
	{


		int NumV=0;
		int src = _d.getSrc();
		int dest= _d.getDest();
		int lastNode = 0;
		int idStart,idEnd;
		ArrayList<Integer> fLst = _d.getFunctions();
		ArrayList<Link> outLink = new ArrayList<>();
		Vector<Vector<Double>> k = new Vector<>();
		h = new ArrayList<Integer>();//Function h is a mapping from an exspanded node to an origin node
		ArrayList<WeitEdge> edgeLst= new ArrayList<>();
		k.add(_g.getCap(src));
		h.add(src);
		source = 1;
		ArrayList<Integer> ZSize = new ArrayList<>();
		ArrayList<ArrayList<Integer>> Z = new ArrayList<>();
		for(int i=0;i<fLst.size();i++)
		{
			ArrayList<Integer> Zi= getFunction(fLst.get(i)).getVnfNode();
			ArrayList<Integer> Z_tamp= new ArrayList<>();
			for(int j=0;j<Zi.size();j++)
			{
				boolean violateNode = false;
				Node _nodeTemp = new Node(Zi.get(j));
				for (int id =0;id<updatedNode.size();id++)
				{
					if(updatedNode.get(id).CompareTo(_nodeTemp))
					{
						violateNode=true;
						if(updatedNode.get(id).getusedNo()>0)
						{
							k.add(_g.getCap(Zi.get(j)));
							h.add(Zi.get(j));
							Z_tamp.add(Zi.get(j));
						}							
					}
				}
				if(!violateNode)
				{
					k.add(_g.getCap(Zi.get(j)));
					h.add(Zi.get(j));
					Z_tamp.add(Zi.get(j));
				}			
				
			}
			ZSize.add(Z_tamp.size());
			NumV = NumV + Z_tamp.size();
			Z.add(Z_tamp);
			for(int j1=0;j1<Z_tamp.size();j1++)
			{
				for (int j2=0;j2<Z_tamp.size();j2++)
				{
					if(Z_tamp.get(j1)!=Z_tamp.get(j2))
					{
						Link l = new Link(Z_tamp.get(j1), Z_tamp.get(j2));
						outLink.add(l);
						l= new Link(Z_tamp.get(j2), Z_tamp.get(j1));
						outLink.add(l);
//						if(_g.getEdgeWeight(Zi.get(j1),Zi.get(j2))>=_d.getBw())
//						{
//							Link l = new Link(Zi.get(j1), Zi.get(j2));
//							outLink.add(l);
//						}
					}
				}
				
			}
		}
		k.add(_g.getCap(dest));
		h.add(dest);
		NumV=NumV+2;//tinh ca dau va cuoi 0->lastnode-1;
		destination=NumV;
		ArrayList<Integer> Z1 = new ArrayList<>();
		ArrayList<Integer> Z2= new ArrayList<>();
		int node1=-1;
		int node2 =-1;
		int setNumber = 0;
		///xay ra truong hop khi add link, tuy thuoc vao so node khong duoc add
		for (int i=-1;i<fLst.size();i++)
		{
			setNumber++;
			
			//last node truoc Zi+1		
			if(i==-1)
			{
				node1 = 0;
				Z1 = new ArrayList<>();
				Z1.add(src);
				node2 = 1;
			}
			else
			{
				node1=1;
				for (int temp=0;temp<i;temp++)
					node1 = node1+ ZSize.get(temp);//last node truoc Zi
				node2= node1 + ZSize.get(i);
				//Z1 = getFunction(fLst.get(i)).getVnfNode();
				Z1 = Z.get(i);
			}
			if(i==fLst.size()-1)
			{
				Z2= new ArrayList<>();
				Z2.add(dest);				
			}
			else
			{
				//Z2 = getFunction(fLst.get(i+1)).getVnfNode();
				Z1 = Z.get(i+1);
			}
			ArrayList<Link> cnnSet = ConnectionSetSimple(Z1, Z2,_d.getBw());
			if(cnnSet!=null)
			{
				ArrayList<Integer> nodeLst = new ArrayList<>();//chua tat ca cac node chua ton tai trong do thi g -> phai add them vao
				for(Link l: cnnSet)
				{
//					wLink _wL = linkContain_wLinkList(l, violatedLink);
//					if(_wL!=null)
//					{
//						//neu no la link da bi vi pham
//						//kiem tra xem link hien tai co nam trong set co the add ko?
//						ArrayList<Integer> cnnSetLink = _wL.getCnnSet();
//						if (cnnSetLink.contains(setNumber))
//						{
//							if(!Z1.contains(l.getStart())&&!Z2.contains(l.getStart()) && !nodeLst.contains(l.getStart()))
//								nodeLst.add(l.getStart());
//							if(!Z1.contains(l.getEnd())&&!Z2.contains(l.getEnd())&& !nodeLst.contains(l.getEnd()))
//								nodeLst.add(l.getEnd());
//						}
//					}
//					else
//					{
//						if(!Z1.contains(l.getStart())&&!Z2.contains(l.getStart()) && !nodeLst.contains(l.getStart()))
//							nodeLst.add(l.getStart());
//						if(!Z1.contains(l.getEnd())&&!Z2.contains(l.getEnd())&& !nodeLst.contains(l.getEnd()))
//							nodeLst.add(l.getEnd());
//					}
					if(!Z1.contains(l.getStart())&&!Z2.contains(l.getStart()) && !nodeLst.contains(l.getStart()))
						nodeLst.add(l.getStart());
					if(!Z1.contains(l.getEnd())&&!Z2.contains(l.getEnd())&& !nodeLst.contains(l.getEnd()))
						nodeLst.add(l.getEnd());
									
				}
				lastNode = NumV;//lastnode truoc khi add them node;
				for (int j=0;j<nodeLst.size();j++)
				{
					k.add(_g.getCap(nodeLst.get(j)));
					h.add(nodeLst.get(j));
				}
				NumV+= nodeLst.size();
				//cac node da duoc add, quan trong la add link-> dua no vao mot tap cac link
				for (Link l:cnnSet)
				{
					if(!outLink.contains(l))
					{
						wLink _wL = linkContain_wLinkList(l, violatedLink);
						if(_wL!=null)
						{
							//neu no la link da bi vi pham
							//kiem tra xem link hien tai co nam trong set co the add ko?
							ArrayList<Integer> cnnSetLink = _wL.getCnnSet();
							if (!cnnSetLink.contains(setNumber))
							{
								continue;
							}
						}
						idStart =-1;
						idEnd =-1;
						for (int j=0;j<nodeLst.size();j++)
						{
							if(l.getStart() == nodeLst.get(j))
							{
								idStart =j;
							}
							if(l.getEnd() == nodeLst.get(j))
							{
								idEnd =j;
							}
							if(idStart!=-1 && idEnd !=-1)
								break;
						}
						if(idStart==-1 && idEnd==-1)
						{
							//add canh Z1.get(i1) den Z2.get(i)
							idStart=Z1.indexOf(l.getStart());
							idEnd = Z2.indexOf(l.getEnd());
							if(idStart!=-1 && idEnd!=-1)
							{
								if(l.getStart()==l.getEnd())
								{
									edgeLst.add(new WeitEdge(node1+idStart+1, idEnd+node2+1, 10.0));
								}
								else
									if(_g.getEdgeWeight(l.getStart(), l.getEnd())>=_d.getBw())
									//if(_g.getEdgeWeight(l.getStart(), l.getEnd())>0)
										edgeLst.add(new WeitEdge(node1+idStart+1, idEnd+node2+1, _g.getEdgeWeight(l.getStart(), l.getEnd())));
							}
							
						}
						else
						{
							if(idStart==-1)
							{
								//add canh bat dau tu Z1.get(i1)
								idStart=Z1.indexOf(l.getStart());
								if(idStart!=-1)
								{
									if(_g.getEdgeWeight(l.getStart(), l.getEnd())>=_d.getBw())
									//if(_g.getEdgeWeight(l.getStart(), l.getEnd())>0)
										edgeLst.add(new WeitEdge(node1+idStart+1, idEnd+lastNode+1, _g.getEdgeWeight(l.getStart(), l.getEnd())));
								}
								continue;
							}
							if(idEnd==-1)
							{
								idEnd = Z2.indexOf(l.getEnd());
								if(idEnd!=-1&& _g.getEdgeWeight(l.getStart(), l.getEnd())>=_d.getBw())
								//if(idEnd!=-1&& _g.getEdgeWeight(l.getStart(), l.getEnd())>0)
									edgeLst.add(new WeitEdge(idStart+lastNode+1, idEnd+node2+1, _g.getEdgeWeight(l.getStart(), l.getEnd())));
								continue;
							}
							if(l.getStart()==l.getEnd())
							{
								edgeLst.add(new WeitEdge(idStart+lastNode+1,idEnd+lastNode+1, 10.0));
							}
							else
								//if(idEnd!=-1&& _g.getEdgeWeight(l.getStart(), l.getEnd())>0)
								if(idEnd!=-1&& _g.getEdgeWeight(l.getStart(), l.getEnd())>=_d.getBw())
									edgeLst.add(new WeitEdge(idStart+lastNode+1,idEnd+lastNode+1, _g.getEdgeWeight(l.getStart(), l.getEnd())));
							
						}						
					}
				}
				
			}
		}
		nGraph g_save = new nGraph(NumV);
		
		System.out.println("Number of Extended Link: "+edgeLst.size());
		if(edgeLst.size()==0)
			return null;
        for (WeitEdge edges : edgeLst) {
        	int s = edges.getO();
        	int t = edges.getD();
			double w= edges.getW();
			g_save.setEdgeWeight(s, t, w);
		}
	   	for (int i=0;i<g.getV();i++)
       {
	   		Vector<Double> t= k.get(i);
	   		g_save.setCap(i+1, t);
       }    
	   	//noVertex = g_save.getV();
	   	edgeLst =null;
	   	
		
		return g_save;
		
	
	
	}
	
	public static nGraph CreateExGraph(nGraph _g, nDemand _d)
	{
		int NumV=0;
		int src = _d.getSrc();
		int dest= _d.getDest();
		int lastNode = 0;
		ArrayList<Integer> fLst = _d.getFunctions();
		Vector<Vector<Double>> k = new Vector<>();
		h = new ArrayList<Integer>();//Function h is a mapping from an exspanded node to an origin node
		ArrayList<WeitEdge> edgeLst= new ArrayList<>();
		
		//add node and link !=0
		source = src;
		
		for (int i=0;i<fLst.size()+1;i++)
		{
			lastNode = NumV;
			for(int j1=0;j1<_g.getV();j1++)
			{
				k.add(_g.getCap(j1+1));
				h.add(j1+1);
				for(int j2=0;j2<_g.getV();j2++)
				{
					if(j1!=j2 && _g.getEdgeWeight(j1+1, j2+1)>=_d.getBw())
						edgeLst.add(new WeitEdge(lastNode+j1+1, lastNode+j2+1, _g.getEdgeWeight(j1+1, j2+1)));
				}
				NumV++;
			}
		}
		
		//add link =0;
		lastNode =0;
		for (int i=0;i<fLst.size();i++)
		{
			
			ArrayList<Integer> Zi= getFunction(fLst.get(i)).getVnfNode();
			for(int j=0;j<Zi.size();j++)
			{
				if(!UtilizeFunction.isBig(getFunction(fLst.get(i)).getLamda(),_g.getCap(Zi.get(j))))
				{
					int node = Zi.get(j);
					edgeLst.add(new WeitEdge(lastNode+node, lastNode+node+_g.getV(), 100.0));
				}
			}
			lastNode += _g.getV();
		}
		
		destination = NumV -g.getV() + dest;
		nGraph g_save = new nGraph(NumV);
		
		System.out.println("Number of Extended Link: "+edgeLst.size());
		if(edgeLst.size()==0)
			return null;
        for (WeitEdge edges : edgeLst) {
        	int s = edges.getO();
        	int t = edges.getD();
			double w= edges.getW();
			//System.out.print("("+s+","+t+"),");
			g_save.setEdgeWeight(s, t, w);
		}
	   	for (int i=0;i<g_save.getV();i++)
       {
	   		Vector<Double> t= k.get(i);
	   		g_save.setCap(i+1, t);
       }    
	   	//noVertex = g_save.getV();
	   	edgeLst =null;   	
		
		return g_save;	
	}
	
	public static nGraph CreateExGraph(nGraph _g, nDemand _d, ArrayList<wLink> violatedLink, ArrayList<Node> updatedNode)
	{
		int NumV=0;
		int src = _d.getSrc();
		int dest= _d.getDest();
		int lastNode = 0;
		ArrayList<Integer> fLst = _d.getFunctions();
		Vector<Vector<Double>> k = new Vector<>();
		h = new ArrayList<Integer>();//Function h is a mapping from an exspanded node to an origin node
		ArrayList<WeitEdge> edgeLst= new ArrayList<>();
		
		//add node and link !=0
		source = src;
		
		for (int i=0;i<fLst.size()+1;i++)
		{
			lastNode = NumV;
			for(int j1=0;j1<_g.getV();j1++)
			{
				k.add(_g.getCap(j1+1));
				h.add(j1+1);
				for(int j2=0;j2<_g.getV();j2++)
				{
					if(j1!=j2 && _g.getEdgeWeight(j1+1, j2+1)>=_d.getBw())
					{
						
						wLink _l = new wLink(j1+1, j2+1, 1, null);
						
						boolean violateLink = false;
						if(violatedLink.size()>0)
						{
							for (int id =0;id<violatedLink.size();id++)
							{
								if(violatedLink.get(id).CompareTo(_l))
								{
									violateLink=true;
									ArrayList<Integer> setLink = violatedLink.get(id).getCnnSet();
									if(setLink.contains(i+1))
									{
										edgeLst.add(new WeitEdge(lastNode+j1+1, lastNode+j2+1, _g.getEdgeWeight(j1+1, j2+1)));
									}	
									break;
								}
							}
							if(!violateLink)
							{
								edgeLst.add(new WeitEdge(lastNode+j1+1, lastNode+j2+1, _g.getEdgeWeight(j1+1, j2+1)));
							}
						}
						
					}
				}
				NumV++;
			}
		}
		
		//add link =0;
		lastNode =0;
		for (int i=0;i<fLst.size();i++)
		{
			
			ArrayList<Integer> Zi= getFunction(fLst.get(i)).getVnfNode();
			for(int j=0;j<Zi.size();j++)
			{
				
				if(!UtilizeFunction.isBig(getFunction(fLst.get(i)).getLamda(),_g.getCap(Zi.get(j))))
				{
					boolean violateNode = false;
					Node _nodeTemp = new Node(Zi.get(j));
					if(updatedNode.size()>0)
					{
						for (int id =0;id<updatedNode.size();id++)
						{
							if(updatedNode.get(id).CompareTo(_nodeTemp))
							{
								violateNode=true;
								if(updatedNode.get(id).getvSetLst().contains(i+1))
								{
									int node = Zi.get(j);
									edgeLst.add(new WeitEdge(lastNode+node, lastNode+node+_g.getV(), 100.0));
								}
								break;
							}
						}
					}
					
					if(!violateNode)
					{
						int node = Zi.get(j);
						edgeLst.add(new WeitEdge(lastNode+node, lastNode+node+_g.getV(), 100.0));
					}
					
				}		
				
				
				
			}
			lastNode += _g.getV();
		}
		
		destination = NumV -g.getV() + dest;
		nGraph g_save = new nGraph(NumV);
		
		System.out.println("Number of Extended Link: "+edgeLst.size());
		if(edgeLst.size()==0)
			return null;
        for (WeitEdge edges : edgeLst) {
        	int s = edges.getO();
        	int t = edges.getD();
			double w= edges.getW();
			//System.out.print("("+s+","+t+"),");
			g_save.setEdgeWeight(s, t, w);
		}
	   	for (int i=0;i<g_save.getV();i++)
       {
	   		Vector<Double> t= k.get(i);
	   		g_save.setCap(i+1, t);
       }    
	   	//noVertex = g_save.getV();
	   	edgeLst =null;   	
		
		return g_save;	
	}
	
	public static nGraph ExpandedGraph(nGraph _g, nDemand _d)
	{


		int NumV=0;
		int src = _d.getSrc();
		int dest= _d.getDest();
		int lastNode = 0;
		ArrayList<Integer> fLst = _d.getFunctions();
		Vector<Vector<Double>> k = new Vector<>();
		h = new ArrayList<Integer>();//Function h is a mapping from an exspanded node to an origin node
		ArrayList<WeitEdge> edgeLst= new ArrayList<>();
		k.add(_g.getCap(src));
		h.add(src);
		source = 1;
		ArrayList<Integer> ZSize = new ArrayList<>();
		for(int i=0;i<fLst.size();i++)
		{
			ArrayList<Integer> Zi= getFunction(fLst.get(i)).getVnfNode();
			//ZSize.add(Zi.size());
			//NumV=NumV+Zi.size();
			int dem=0;
			for(int j=0;j<Zi.size();j++)
			{
				if(!UtilizeFunction.isBig(getFunction(fLst.get(i)).getLamda(),_g.getCap(Zi.get(j))))
				{
					dem++;
					k.add(_g.getCap(Zi.get(j)));
					h.add(Zi.get(j));
				}
				
			}
			ZSize.add(dem);
			NumV=NumV+dem;
		}
		k.add(_g.getCap(dest));
		h.add(dest);
		NumV=NumV+2;//tinh ca dau va cuoi 0->lastnode-1;
		destination=NumV;
		ArrayList<Integer> Z1 = new ArrayList<>();
		ArrayList<Integer> Z2= new ArrayList<>();
		ArrayList<Integer> Ztam= new ArrayList<>();
		int node1=-1;
		int node2 =-1;
		for (int i=-1;i<fLst.size();i++)
		{
			
			//last node truoc Zi+1		
			if(i==-1)
			{
				node1 = 0;
				Z1 = new ArrayList<>();
				Z1.add(src);
				node2 = 1;
			}
			else
			{
				node1=1;
				for (int temp=0;temp<i;temp++)
					node1 = node1+ ZSize.get(temp);//last node truoc Zi
				node2= node1 + ZSize.get(i);
				Ztam = getFunction(fLst.get(i)).getVnfNode();
				Z1 = new ArrayList<>();
				for(int j=0;j<Ztam.size();j++)
				{
					if(!UtilizeFunction.isBig(getFunction(fLst.get(i)).getLamda(),_g.getCap(Ztam.get(j))))
					{
						
						Z1.add(Ztam.get(j));
					}
					
				}
				//Z1 = getFunction(fLst.get(i)).getVnfNode();
				
			}
			if(i==fLst.size()-1)
			{
				Z2= new ArrayList<>();
				Z2.add(dest);				
			}
			else
			{
				//Z2 = getFunction(fLst.get(i+1)).getVnfNode();
				Ztam = getFunction(fLst.get(i+1)).getVnfNode();
				Z2 = new ArrayList<>();
				for(int j=0;j<Ztam.size();j++)
				{
					if(!UtilizeFunction.isBig(getFunction(fLst.get(i+1)).getLamda(),_g.getCap(Ztam.get(j))))
					{
						
						Z2.add(Ztam.get(j));
					}
					
				}
			}
			
			//add tat ca cac dinh trong G
			//ArrayList<Integer> nodeLst = new ArrayList<>();
			for (int j=0;j<_g.getV();j++)
			{
				//nodeLst.add(j+1);				
				k.add(_g.getCap(j+1));
				h.add(j+1);
			}
			lastNode = NumV;
			NumV+=_g.getV();
			
			//add tat ca cac link tu s_k den s_K+1
			for(int j1=0;j1<Z1.size();j1++)
			{
//				for (int j2=0;j2<Z2.size();j2++)
//				{
//					if(Z1.get(j1)==Z2.get(j2))
//						edgeLst.add(new WeitEdge(node1+j1+1, node2+j2+1, 100.0));//truong hop canh zero
//					else
//					{
//						if(_g.getEdgeWeight(Z1.get(j1), Z2.get(j2))>=_d.getBw())
//							edgeLst.add(new WeitEdge(node1+j1+1, node2+j2+1, _g.getEdgeWeight(Z1.get(j1), Z2.get(j2))));
//					}
//				}
				for(int j2=0;j2<_g.getV();j2++)
				{
					if(Z1.get(j1)==j2+1)
						edgeLst.add(new WeitEdge(node1+j1+1, lastNode+j2+1, 100.0));//truong hop canh zero
//					else
//					{
//						if(_g.getEdgeWeight(Z1.get(j1), j2+1)>=_d.getBw())
//							edgeLst.add(new WeitEdge(node1+j1+1, lastNode+j2+1, _g.getEdgeWeight(Z1.get(j1), j2+1)));
//					}
				}
			}
			
			for(int j1=0;j1<_g.getV();j1++)
			{
				for (int j2=0;j2<Z2.size();j2++)
				{
					if(j1+1==Z2.get(j2))
						edgeLst.add(new WeitEdge(lastNode+j1+1, node2+j2+1, 100.0));//truong hop canh zero
//					else
//					{
//						if(_g.getEdgeWeight(j1+1, Z2.get(j2))>=_d.getBw())
//							edgeLst.add(new WeitEdge(lastNode+j1+1, node2+j2+1, _g.getEdgeWeight(j1+1, Z2.get(j2))));
//					}
				}
				for(int j2=0;j2<_g.getV();j2++)
				{
					if(j1!=j2 && _g.getEdgeWeight(j1+1, j2+1)>=_d.getBw())
						edgeLst.add(new WeitEdge(lastNode+j1+1, lastNode+j2+1, _g.getEdgeWeight(j1+1, j2+1)));
				}
			}
		}
		nGraph g_save = new nGraph(NumV);
		
		System.out.println("Number of Extended Link: "+edgeLst.size());
		if(edgeLst.size()==0)
			return null;
        for (WeitEdge edges : edgeLst) {
        	int s = edges.getO();
        	int t = edges.getD();
			double w= edges.getW();
			//System.out.print("("+s+","+t+"),");
			g_save.setEdgeWeight(s, t, w);
		}
	   	for (int i=0;i<g_save.getV();i++)
       {
	   		Vector<Double> t= k.get(i);
	   		g_save.setCap(i+1, t);
       }    
	   	//noVertex = g_save.getV();
	   	edgeLst =null;
	   	
		
		return g_save;	
	
	
		
	}
	public static nGraph ConstructingSimpleGraph(nGraph _g,nDemand _d)
	{

		int NumV=0;
		int src = _d.getSrc();
		int dest= _d.getDest();
		int lastNode = 0;
		int idStart,idEnd;
		ArrayList<Integer> fLst = _d.getFunctions();
		ArrayList<Link> outLink = new ArrayList<>();
		Vector<Vector<Double>> k = new Vector<>();
		h = new ArrayList<Integer>();//Function h is a mapping from an exspanded node to an origin node
		ArrayList<WeitEdge> edgeLst= new ArrayList<>();
		k.add(_g.getCap(src));
		h.add(src);
		source = 1;
		ArrayList<Integer> ZSize = new ArrayList<>();
		for(int i=0;i<fLst.size();i++)
		{
			ArrayList<Integer> Zi= getFunction(fLst.get(i)).getVnfNode();
			ZSize.add(Zi.size());
			NumV=NumV+Zi.size();
			for(int j=0;j<Zi.size();j++)
			{
				k.add(_g.getCap(Zi.get(j)));
				h.add(Zi.get(j));
				
			}
			for(int j1=0;j1<Zi.size();j1++)
			{
				for (int j2=0;j2<Zi.size();j2++)
				{
					if(Zi.get(j1)!=Zi.get(j2))
					{
						Link l = new Link(Zi.get(j1), Zi.get(j2));
						outLink.add(l);
						l= new Link(Zi.get(j2), Zi.get(j1));
						outLink.add(l);
//						if(_g.getEdgeWeight(Zi.get(j1),Zi.get(j2))>=_d.getBw())
//						{
//							Link l = new Link(Zi.get(j1), Zi.get(j2));
//							outLink.add(l);
//						}
					}
				}
				
			}
		}
		k.add(_g.getCap(dest));
		h.add(dest);
		NumV=NumV+2;//tinh ca dau va cuoi 0->lastnode-1;
		destination=NumV;
		ArrayList<Integer> Z1 = new ArrayList<>();
		ArrayList<Integer> Z2= new ArrayList<>();
		int node1=-1;
		int node2 =-1;
		for (int i=-1;i<fLst.size();i++)
		{
			
			//last node truoc Zi+1		
			if(i==-1)
			{
				node1 = 0;
				Z1 = new ArrayList<>();
				Z1.add(src);
				node2 = 1;
			}
			else
			{
				node1=1;
				for (int temp=0;temp<i;temp++)
					node1 = node1+ ZSize.get(temp);//last node truoc Zi
				node2= node1 + ZSize.get(i);
				Z1 = getFunction(fLst.get(i)).getVnfNode();
			}
			if(i==fLst.size()-1)
			{
				Z2= new ArrayList<>();
				Z2.add(dest);				
			}
			else
			{
				Z2 = getFunction(fLst.get(i+1)).getVnfNode();
			}
			ArrayList<Link> cnnSet = ConnectionSetSimple(Z1, Z2,_d.getBw());
			if(cnnSet!=null)
			{
				ArrayList<Integer> nodeLst = new ArrayList<>();//chua tat ca cac node chua ton tai trong do thi g -> phai add them vao
				for(Link l: cnnSet)
				{
					if(!Z1.contains(l.getStart())&&!Z2.contains(l.getStart()) && !nodeLst.contains(l.getStart()))
						nodeLst.add(l.getStart());
					if(!Z1.contains(l.getEnd())&&!Z2.contains(l.getEnd())&& !nodeLst.contains(l.getEnd()))
						nodeLst.add(l.getEnd());					
				}
				lastNode = NumV;//lastnode truoc khi add them node;
				for (int j=0;j<nodeLst.size();j++)
				{
					k.add(_g.getCap(nodeLst.get(j)));
					h.add(nodeLst.get(j));
				}
				NumV+= nodeLst.size();
				//cac node da duoc add, quan trong la add link-> dua no vao mot tap cac link
				for (Link l:cnnSet)
				{
					if(!outLink.contains(l))
					{
						idStart =-1;
						idEnd =-1;
						for (int j=0;j<nodeLst.size();j++)
						{
							if(l.getStart() == nodeLst.get(j))
							{
								idStart =j;
							}
							if(l.getEnd() == nodeLst.get(j))
							{
								idEnd =j;
							}
							if(idStart!=-1 && idEnd !=-1)
								break;
						}
						if(idStart==-1 && idEnd==-1)
						{
							//add canh Z1.get(i1) den Z2.get(i)
							idStart=Z1.indexOf(l.getStart());
							idEnd = Z2.indexOf(l.getEnd());
							if(idStart!=-1 && idEnd!=-1)
							{
								if(l.getStart()==l.getEnd())
								{
									edgeLst.add(new WeitEdge(node1+idStart+1, idEnd+node2+1, 10.0));
								}
								else
									if(_g.getEdgeWeight(l.getStart(), l.getEnd())>=_d.getBw())
									//if(_g.getEdgeWeight(l.getStart(), l.getEnd())>0)
										edgeLst.add(new WeitEdge(node1+idStart+1, idEnd+node2+1, _g.getEdgeWeight(l.getStart(), l.getEnd())));
							}
							
						}
						else
						{
							if(idStart==-1)
							{
								//add canh bat dau tu Z1.get(i1)
								idStart=Z1.indexOf(l.getStart());
								if(idStart!=-1)
								{
									if(_g.getEdgeWeight(l.getStart(), l.getEnd())>=_d.getBw())
									//if(_g.getEdgeWeight(l.getStart(), l.getEnd())>0)
										edgeLst.add(new WeitEdge(node1+idStart+1, idEnd+lastNode+1, _g.getEdgeWeight(l.getStart(), l.getEnd())));
								}
								continue;
							}
							if(idEnd==-1)
							{
								idEnd = Z2.indexOf(l.getEnd());
								if(idEnd!=-1&& _g.getEdgeWeight(l.getStart(), l.getEnd())>=_d.getBw())
								//if(idEnd!=-1&& _g.getEdgeWeight(l.getStart(), l.getEnd())>0)
									edgeLst.add(new WeitEdge(idStart+lastNode+1, idEnd+node2+1, _g.getEdgeWeight(l.getStart(), l.getEnd())));
								continue;
							}
							if(l.getStart()==l.getEnd())
							{
								edgeLst.add(new WeitEdge(idStart+lastNode+1,idEnd+lastNode+1, 10.0));
							}
							else
								//if(idEnd!=-1&& _g.getEdgeWeight(l.getStart(), l.getEnd())>0)
								if(idEnd!=-1&& _g.getEdgeWeight(l.getStart(), l.getEnd())>=_d.getBw())
									edgeLst.add(new WeitEdge(idStart+lastNode+1,idEnd+lastNode+1, _g.getEdgeWeight(l.getStart(), l.getEnd())));
							
						}						
					}
				}
				
			}
		}
		nGraph g_save = new nGraph(NumV);
		
		System.out.println("Number of Extended Link: "+edgeLst.size());
		if(edgeLst.size()==0)
			return null;
        for (WeitEdge edges : edgeLst) {
        	int s = edges.getO();
        	int t = edges.getD();
			double w= edges.getW();
			g_save.setEdgeWeight(s, t, w);
		}
	   	for (int i=0;i<g_save.getV();i++)
       {
	   		Vector<Double> t= k.get(i);
	   		g_save.setCap(i+1, t);
       }    
	   	//noVertex = g_save.getV();
	   	edgeLst =null;
	   	
		
		return g_save;
		
	
	}
	public static ArrayList<ArrayList<Integer>> shortestPaths (int src, int dest,nGraph _g, double maxBw)
	{

		ArrayList<ArrayList<Integer>> _shortestPathLst = new ArrayList<>();
		ArrayList<Integer> _shortestPath = new ArrayList<>();
	DefaultDirectedWeightedGraph<Vertex, DefaultWeightedEdge> g_i = new DefaultDirectedWeightedGraph<>(DefaultWeightedEdge.class);
		List<Vertex> vertexList = new ArrayList<Vertex>();
 
		for (int i = 0; i < _g.getV(); i++) {
			int s= i+1;
			Vertex v = new Vertex(s);
			vertexList.add(v);
			g_i.addVertex(v);
			//g_new.addVertex("node" + s);
		}
		for (int j=0;j<_g.getV();j++)
        {	        	
        	for(int k=0;k<_g.getV();k++)
        	{
        		if(j!=k&&_g.getEdgeWeight(j+1, k+1)>= maxBw)
        		{
        			
        			DefaultWeightedEdge e = g_i.addEdge(vertexList.get(j), vertexList.get(k));
        			if(_g.getEdgeWeight(j+1, k+1) ==100.0)
        				g_i.setEdgeWeight(e,0.0);
        			else
        				g_i.setEdgeWeight(e, 1.0);
        		}
        	}
        }  
		//System.out.println("src: "+ src);
		DijkstraMinh d = new DijkstraMinh(g_i);
		d.computeAllShortestPaths(src);
		//Collection<Vertex> vertices = g_i.getVertices();
		Vertex v = vertexList.get(dest-1);
		int i = 1;
		boolean check=false;
		
//		for (Iterator<Vertex> iterator = vertices.iterator(); iterator.hasNext();) {
//			v = (Vertex) iterator.next();
//			if(v.getId()!=dest)
//				continue;			
			Set<List<Vertex>> allShortestPaths = d.getAllShortestPathsTo(v);
 
			for (Iterator<List<Vertex>> iter = allShortestPaths.iterator(); iter.hasNext(); i++)
			{
				check=false;
				_shortestPath = new ArrayList<>();
				List<Vertex> p = (List<Vertex>) iter.next();
//				for(int j=0;j<p.size()-1;j++)
//				{
//					if(_g.getEdgeWeight(p.get(j).getId(), p.get(j+1).getId())==0)
//					{
//						check=true;
//						break;
//					}
//				}
				if(!check && p.get(0).getId()==src)
				{
					
					for (Vertex v1:p)
					{
						_shortestPath.add(v1.getId());
					}
					_shortestPathLst.add(_shortestPath);
					//System.out.println("Path " + i + ": " + p);
				}
			}
//			i = 1;
//			break;
//		} 
		if(_shortestPathLst.size()==0)
			return null;
		return _shortestPathLst;
	
	}

	public static ArrayList<ArrayList<Integer>> allOfShortestPaths(int src, int dest,nGraph _g, double maxBw)
	{
		ArrayList<ArrayList<Integer>> _shortestPathLst = new ArrayList<>();
		ArrayList<Integer> _shortestPath = new ArrayList<>();
		
		//DirectedWeightedMultigraph<String, DefaultWeightedEdge> g_new = new DirectedWeightedMultigraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
		
		//SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class);
	
		//edu.uci.ics.jung.graph.Graph<Vertex, String> g_i = new SparseMultigraph<Vertex, String>();
		edu.uci.ics.jung.graph.DirectedSparseMultigraph<Vertex, String> g_i= new DirectedSparseMultigraph<Vertex, String>();
		List<Vertex> vertexList = new ArrayList<Vertex>();
 
		for (int i = 0; i < _g.getV(); i++) {
			int s= i+1;
			Vertex v = new Vertex(s);
			vertexList.add(v);
			//g_i.addVertex(v);
			//g_new.addVertex("node" + s);
		}
		for (int j=0;j<_g.getV();j++)
        {	        	
        	for(int k=0;k<_g.getV();k++)
        	{
        		if(j!=k&&_g.getEdgeWeight(j+1, k+1)>= maxBw)
        		{
        			int s=j+1;
        			int t= k+1;
        			//DefaultWeightedEdge e = g_new.addEdge("node"+s, "node"+t);
        			//g_new.setEdgeWeight(e, _g.getEdgeWeight(j+1, k+1));
        			g_i.addEdge(s + "-to-" + t, vertexList.get(j),vertexList.get(k),EdgeType.DIRECTED);
        		}
        	}
        }  
		//System.out.println("src: "+ src);
		DijkstraHand d = new DijkstraHand(g_i);
		d.computeAllShortestPaths(src);
		//Collection<Vertex> vertices = g_i.getVertices();
		Vertex v = vertexList.get(dest-1);
		int i = 1;
		boolean check=false;
		
//		for (Iterator<Vertex> iterator = vertices.iterator(); iterator.hasNext();) {
//			v = (Vertex) iterator.next();
//			if(v.getId()!=dest)
//				continue;			
			Set<List<Vertex>> allShortestPaths = d.getAllShortestPathsTo(v);
 
			for (Iterator<List<Vertex>> iter = allShortestPaths.iterator(); iter.hasNext(); i++)
			{
				check=false;
				_shortestPath = new ArrayList<>();
				List<Vertex> p = (List<Vertex>) iter.next();
//				for(int j=0;j<p.size()-1;j++)
//				{
//					if(_g.getEdgeWeight(p.get(j).getId(), p.get(j+1).getId())==0)
//					{
//						check=true;
//						break;
//					}
//				}
				if(!check && p.get(0).getId()==src)
				{
					
					for (Vertex v1:p)
					{
						_shortestPath.add(v1.getId());
					}
					_shortestPathLst.add(_shortestPath);
					//System.out.println("Path " + i + ": " + p);
				}
			}
//			i = 1;
//			break;
//		} 
		if(_shortestPathLst.size()==0)
			return null;
		return _shortestPathLst;
	}
	public static ArrayList<ArrayList<Integer>> Alg5(nDemand _d,nGraph _g)
	{
		fl=true;
		int maxSize=-1;
		//link_load, node load duoc cap nhat o day
		ArrayList<Integer> p = new ArrayList<>();
		ArrayList<ArrayList<Integer>> pLst = new ArrayList<>();
		funLoc = new ArrayList<>();
		h = new ArrayList<>();
		//nGraph g_save = ConstructingGraph(_g, _d);
		nGraph g_save = ConstructingSimpleGraph(_g, _d);
		//can tim ra source va destination o day
		ArrayList<ArrayList<Integer>> pathLst = allOfShortestPaths(source, destination, g_save, 0.00000001);
		if(pathLst!=null)
		{
//			if(pathLst.size()>10)
//				maxSize=10;
//			else
				maxSize= pathLst.size();
			for (int i=0;i<maxSize;i++)
			{

				int id=-1;
				int node=1;
				int idFunc = 0;
				int start_org = 0;
				int end_org =0;
				ArrayList<Integer> path = pathLst.get(i);
				p = new ArrayList<>();
				for (int j=0;j<path.size()-1;j++)
				{
					int start = path.get(j);
					int end =	path.get(j+1);
					start_org = h.get(start-1);
					end_org = h.get(end-1);
					//link_load[start_org-1][end_org-1]+=_d.getBw()/noOfPaths;
					//if(start_org!=end_org)
					//{
						
						p.add(start_org);
					//}
				}
				if(end_org!=0)
					p.add(end_org);
				pLst.add(p);
				int len=-1;
				for (int j=1;j<path.size()-1;j++)
				{
					id=-1;
					int n= path.get(j);
					for (int i1 = idFunc;i1<_d.getFunctions().size();i1++)
					{	
						node=1;
						if(i1!=0)
						{
							for (int i2=0;i2<idFunc;i2++)
								node+=getFunction(_d.getFunctions().get(i2)).getVnfNode().size();
						}
						len = getFunction(_d.getFunctions().get(i1)).getVnfNode().size();
						if(n>node+len|| n<node)
							break;
						for (int i2 = 0;i2 <len;i2++ )
						{
							if (n==node+i2+1)
							{
								id=n;
								break;
							}
						}
						if(id!=-1)
						{
							idFunc=i1+1;
						}
						break;
					}
					if(id!=-1)
					{
						
						//node_load[h.get(n-1)]+=1/noOfPaths;
						funLoc.add(new Pair(h.get(n-1), _d.getFunctions().get(idFunc-1)));
					}
					
				}
			}
		}	
		else
		{
			fl=false;
			return null;
		}
		
		//Kiem tra xem tat ca cac duong co thoa man hay ko?
		for (int i=0;i<pLst.size();i++)
		{
			ArrayList<Integer> path = pLst.get(i);
			for (int j=0;j<path.size()-1;j++)
			{
				if(path.get(j)!=path.get(j+1))
				{
					double wei= _g.getEdgeWeight(path.get(j), path.get(j+1));
					if(wei<_d.getBw()/pLst.size())
					{
						fl=false;
						return null;
					}
					else
					{
						_g.setEdgeWeight(path.get(j), path.get(j+1), wei- _d.getBw()/pLst.size());
					}
				}
				
					
			}
		}
		for (int i=0;i<_d.getFunctions().size();i++)
		{
			int idFun= _d.getFunctions().get(i);
			int count =0;
			for (int j=0;j<funLoc.size();j++)
			{
				Pair pr = funLoc.get(j);
				if (pr.getfunction()==idFun)
					count++;
			}
			
			for (int j=0;j<funLoc.size();j++)
			{
				Pair pr = funLoc.get(j);
				if(pr.getfunction() == idFun)
				{
					Vector<Double> mul = UtilizeFunction.multi(getFunction(idFun).getLamda(), 1.0/count);
					if(UtilizeFunction.isBig(_g.getCap(pr.getnode()), mul))
					{
						Vector<Double> c_temp= UtilizeFunction.minus(_g.getCap(pr.getnode()),mul);
						_g.setCap(pr.getnode(),c_temp );
					}
					else
					{
						fl=false;
						return null;
					}
					
				}
			}
		}
		fl=true;
		return pLst;	
	}
	
	
	public static ArrayList<Integer> OptimalSimpleGraph(nDemand _d, nGraph _g)//theo new expanded graph (optimal 1)
	{

		//link_load, node load duoc cap nhat o day
		ArrayList<Integer> p = new ArrayList<>();
		nGraph g_used= new nGraph(_g.cap, _g.w);
		funLoc = new ArrayList<>();
		h = new ArrayList<>();
		boolean okFlag= false;
		ArrayList<Integer> minP = new ArrayList<>();
		ArrayList<Pair> minFunLoc= new ArrayList<>();
		int minLenth= Integer.MAX_VALUE;
		//nGraph g_save = ExpandedGraph(_g, _d);
		nGraph g_save = CreateExGraph(_g, _d);
		if(g_save==null)
		{
			fl=false;
			return null;
		}
		System.out.println("source: "+ source + ", destination: "+ destination);
		
		//ArrayList<ArrayList<Integer>> pathLst = allOfShortestPaths(source, destination, g_save, _d.getBw());
		ArrayList<ArrayList<Integer>> pathLst = shortestPaths(source, destination, g_save, _d.getBw());
		
		
		if(pathLst!=null && pathLst.size()>0)
		{
//			for(int i=0;i<pathLst.size();i++)
//			{
//				System.out.print(i+":[");
//				for (int j=0;j<pathLst.get(i).size();j++)
//					System.out.print( pathLst.get(i).get(j)+" ");
//				System.out.println("]");
//			}
			for(int i=0;i<pathLst.size();i++)
			{

				funLoc = new ArrayList<>();
				okFlag=false;
				p=new ArrayList<>();
				g_used= new nGraph(_g.cap, _g.w);
				ArrayList<Integer> path = pathLst.get(i);			
				int start_org = 0;
				int end_org =0;
//				System.out.print("noreal 4: [");
//				for (int j=0;j<path.size();j++)
//					System.out.print(path.get(j)+ " ");
//				System.out.println("]");
				//System.out.print("real 4:[");
				int i1=0;
				for (int j=0;j<path.size()-1;j++)
				{
					int start = path.get(j);
					int end =	path.get(j+1);
					start_org = h.get(start-1);
					end_org = h.get(end-1);
					if(start_org!= end_org)
						p.add(start_org);
					else
					{
						funLoc.add(new Pair(start_org, _d.getFunctions().get(i1)));
						i1++;
					}
					//System.out.print(start_org+ " ");
				}
				if(end_org!=0)
					p.add(end_org);
				//System.out.println(end_org+ "]");
				for (int j=0;j<p.size()-1;j++)
				{
					int start = p.get(j);
					int end = p.get(j+1);
					if(start != end)
					{
						double wei= g_used.getEdgeWeight(start, end);
						if(wei<_d.getBw())
						{
							okFlag=true;
							break;
						}
						else
						{
							g_used.setEdgeWeight(start, end, wei- _d.getBw());
						}
					}
					
						
				}
				if(okFlag)
					continue;
				else
				{
					if(p.size()<minLenth)
					{
						for (i1=0;i1<_d.getFunctions().size();i1++)
						{
							int idFun= _d.getFunctions().get(i1);
							
							for (int j=0;j<funLoc.size();j++)
							{
								Pair pr = funLoc.get(j);
								if(pr.getfunction() == idFun)
								{
									Vector<Double> mul = getFunction(idFun).getLamda();
									if(UtilizeFunction.isBig(mul,g_used.getCap(pr.getnode())))
									{
										okFlag=true;
										break;
										
									}
									else
									{
										Vector<Double> c_temp= UtilizeFunction.minus(g_used.getCap(pr.getnode()),mul);
										g_used.setCap(pr.getnode(),c_temp );
									}
									
								}
							}
							if(okFlag)
								break;
						}
						if(!okFlag)
						{
							minLenth = p.size();
							minP= new ArrayList<>();
							minFunLoc = new ArrayList<>();
							for (i1 = 0;i1<p.size();i1++)
								minP.add(p.get(i1));
							for (i1=0;i1<funLoc.size();i1++)
								minFunLoc.add(funLoc.get(i1));
						}

						
					}
					
				}
				continue;				
			
			}
		}	
		else
		{
			flag = 1;
			fl=false;
			return null;
		}
		if(minP==null || minP.size()<=0)
			flag=2;
		else
			flag=0;

		
		if(minP==null || minP.size()<=0)
		{
			fl = false;
			return null;
		}
		
		fl=true;
		funLoc = new ArrayList<>();
		for (int i1=0;i1<minFunLoc.size();i1++)
			funLoc.add(minFunLoc.get(i1));
		return minP;	
	
	
	
	
	
	}
	public static ArrayList<Integer> newOptimal (nDemand _d, nGraph _g)//theo old expanded graph (optimal 1)
	{

		//link_load, node load duoc cap nhat o day
		ArrayList<Integer> p = new ArrayList<>();
		nGraph g_used= new nGraph(_g.cap, _g.w);
		funLoc = new ArrayList<>();
		h = new ArrayList<>();
		boolean okFlag= false;
		ArrayList<Integer> minP = new ArrayList<>();
		ArrayList<Pair> minFunLoc= new ArrayList<>();
		int minLenth= Integer.MAX_VALUE;
		nGraph g_save = ExpandedGraph(_g, _d);
		if(g_save==null)
		{
			fl=false;
			return null;
		}
		System.out.println("source: "+ source + ", destination: "+ destination);
		
		//ArrayList<ArrayList<Integer>> pathLst = allOfShortestPaths(source, destination, g_save, _d.getBw());
		ArrayList<ArrayList<Integer>> pathLst = shortestPaths(source, destination, g_save, _d.getBw());
		
		
		if(pathLst!=null && pathLst.size()>0)
		{
//			for(int i=0;i<pathLst.size();i++)
//			{
//				System.out.print(i+":[");
//				for (int j=0;j<pathLst.get(i).size();j++)
//					System.out.print( pathLst.get(i).get(j)+" ");
//				System.out.println("]");
//			}
			for(int i=0;i<pathLst.size();i++)
			{

				funLoc = new ArrayList<>();
				okFlag=false;
				p=new ArrayList<>();
				g_used= new nGraph(_g.cap, _g.w);
				ArrayList<Integer> path = pathLst.get(i);			
				int start_org = 0;
				int end_org =0;
//				System.out.print("noreal 4: [");
//				for (int j=0;j<path.size();j++)
//					System.out.print(path.get(j)+ " ");
//				System.out.println("]");
				//System.out.print("real 4:[");
				for (int j=0;j<path.size()-1;j++)
				{
					int start = path.get(j);
					int end =	path.get(j+1);
					start_org = h.get(start-1);
					end_org = h.get(end-1);
					if(start_org!= end_org)
						p.add(start_org);
					//System.out.print(start_org+ " ");
				}
				if(end_org!=0)
					p.add(end_org);
				//System.out.println(end_org+ "]");
				for (int j=0;j<p.size()-1;j++)
				{
					if(p.get(j)!=p.get(j+1))
					{
						double wei= g_used.getEdgeWeight(p.get(j), p.get(j+1));
						if(wei<_d.getBw())
						{
							okFlag=true;
							break;
						}
						else
						{
							g_used.setEdgeWeight(p.get(j), p.get(j+1), wei- _d.getBw());
						}
					}
					
						
				}
				if(okFlag)
					continue;
				else
				{
					if(p.size()<minLenth)
					{

						int i1=0;
						for (int j=1;j<path.size()-1;j++)
						{
							int n= path.get(j);
							if(n< destination)
							{
								funLoc.add(new Pair(h.get(n-1), _d.getFunctions().get(i1)));
								i1++;
							}
						}
						for (i1=0;i1<_d.getFunctions().size();i1++)
						{
							int idFun= _d.getFunctions().get(i1);
							
							for (int j=0;j<funLoc.size();j++)
							{
								Pair pr = funLoc.get(j);
								if(pr.getfunction() == idFun)
								{
									Vector<Double> mul = getFunction(idFun).getLamda();
									if(UtilizeFunction.isBig(mul,g_used.getCap(pr.getnode())))
									{
										okFlag=true;
										break;
										
									}
									else
									{
										Vector<Double> c_temp= UtilizeFunction.minus(g_used.getCap(pr.getnode()),mul);
										g_used.setCap(pr.getnode(),c_temp );
									}
									
								}
							}
							if(okFlag)
								break;
						}
						if(!okFlag)
						{
							minLenth = p.size();
							minP= new ArrayList<>();
							minFunLoc = new ArrayList<>();
							for (i1 = 0;i1<p.size();i1++)
								minP.add(p.get(i1));
							for (i1=0;i1<funLoc.size();i1++)
								minFunLoc.add(funLoc.get(i1));
						}

						
					}
					
				}
				continue;
				
			
			}
		}	
		else
		{
			flag = 1;
			fl=false;
			return null;
		}
		if(minP==null || minP.size()<=0)
			flag=2;
		else
			flag=0;

		
		if(minP==null || minP.size()<=0)
		{
			fl = false;
			return null;
		}
		
		fl=true;
		funLoc = new ArrayList<>();
		for (int i1=0;i1<minFunLoc.size();i1++)
			funLoc.add(minFunLoc.get(i1));
		return minP;	
	
	
	
	
	}
	
	public static int combinations(int noComb, ArrayList<Integer> arr, ArrayList<ArrayList<Integer>> list) {
		int numArrays = (int)Math.pow(arr.size(), noComb);
	    // Create each array
	    for(int i = 0; i < numArrays; i++) {
	        ArrayList<Integer> current = new ArrayList<>();
	        // Calculate the correct item for each position in the array
	        for(int j = 0; j < noComb; j++) {
	            // This is the period with which this position changes, i.e.
	            // a period of 5 means the value changes every 5th array
	            int period = (int) Math.pow(arr.size(), noComb - j - 1);
	            // Get the correct item and set it
	            int index = i / period % arr.size();
	            current.add(arr.get(index));
	        }
	        list.add(current);
	    	}
	    return list.size();
	    }
	public static ArrayList<Integer> optimal(nDemand _d, nGraph _g)//theo new expanded graph (optimal 2)
	{

		//link_load, node load duoc cap nhat o day
		ArrayList<Integer> p = new ArrayList<>();
		nGraph g_used= new nGraph(_g.cap, _g.w);
		funLoc = new ArrayList<>();
		h = new ArrayList<>();
		boolean okFlag= false;
		ArrayList<Integer> minP = new ArrayList<>();
		ArrayList<Pair> minFunLoc= new ArrayList<>();
		int minLenth= Integer.MAX_VALUE;
		//nGraph g_save = ConstructingGraph(_g, _d);
		nGraph g_save = CreateExGraph(_g, _d);
		if(g_save==null)
		{
			fl=false;
			return null;
		}
		System.out.println("source: "+ source + ", destination: "+ destination);
		
		//ArrayList<ArrayList<Integer>> pathLst = allOfShortestPaths(source, destination, g_save, _d.getBw());
		ArrayList<ArrayList<Integer>> pathLst = shortestPaths(source, destination, g_save, _d.getBw());
		
		
		if(pathLst!=null && pathLst.size()>0)
		{
//			for(int i=0;i<pathLst.size();i++)
//			{
//				System.out.print(i+":[");
//				for (int j=0;j<pathLst.get(i).size();j++)
//					System.out.print( pathLst.get(i).get(j)+" ");
//				System.out.println("]");
//			}
			for(int i=0;i<pathLst.size();i++)
			{

				funLoc = new ArrayList<>();
				okFlag=false;
				p=new ArrayList<>();
				g_used= new nGraph(_g.cap, _g.w);
				ArrayList<Integer> path = pathLst.get(i);			
				int start_org = 0;
				int end_org =0;
//				System.out.print("noreal 4: [");
//				for (int j=0;j<path.size();j++)
//					System.out.print(path.get(j)+ " ");
//				System.out.println("]");
				//System.out.print("real 4:[");
				int i1=0;
				for (int j=0;j<path.size()-1;j++)
				{
					int start = path.get(j);
					int end =	path.get(j+1);
					start_org = h.get(start-1);
					end_org = h.get(end-1);
					if(start_org!= end_org)
						p.add(start_org);
					else
					{
						funLoc.add(new Pair(start_org, _d.getFunctions().get(i1)));
						i1++;
					}
					//System.out.print(start_org+ " ");
				}
				if(end_org!=0)
					p.add(end_org);
				//System.out.println(end_org+ "]");
				for (int j=0;j<p.size()-1;j++)
				{
					int start = p.get(j);
					int end = p.get(j+1);
					if(start != end)
					{
						double wei= g_used.getEdgeWeight(start, end);
						if(wei<_d.getBw())
						{
							okFlag=true;
							break;
						}
						else
						{
							g_used.setEdgeWeight(start, end, wei- _d.getBw());
						}
					}
					
						
				}
				if(okFlag)
					continue;
				else
				{
					if(p.size()<minLenth)
					{
						for (i1=0;i1<_d.getFunctions().size();i1++)
						{
							int idFun= _d.getFunctions().get(i1);
							
							for (int j=0;j<funLoc.size();j++)
							{
								Pair pr = funLoc.get(j);
								if(pr.getfunction() == idFun)
								{
									Vector<Double> mul = getFunction(idFun).getLamda();
									if(UtilizeFunction.isBig(mul,g_used.getCap(pr.getnode())))
									{
										okFlag=true;
										break;
										
									}
									else
									{
										Vector<Double> c_temp= UtilizeFunction.minus(g_used.getCap(pr.getnode()),mul);
										g_used.setCap(pr.getnode(),c_temp );
									}
									
								}
							}
							if(okFlag)
								break;
						}
						if(!okFlag)
						{
							minLenth = p.size();
							minP= new ArrayList<>();
							minFunLoc = new ArrayList<>();
							for (i1 = 0;i1<p.size();i1++)
								minP.add(p.get(i1));
							for (i1=0;i1<funLoc.size();i1++)
								minFunLoc.add(funLoc.get(i1));
						}

						
					}
					
				}			
			
			}
		}	
		else
		{
			fl=false;
			return null;
		}
		if(minP==null || minP.size()<=0)
		{
			System.out.println("Blocking of type 2");
			//thuc hien buoc 5
			//tim tat ca nhung link bi vuot 
			
			//Chon 1 duong ngau nhien, kiem tra xem co du tai nguyen ko
			int dem= 0;
			for (int pId = 0;pId < pathLst.size();pId++)
			{
				
				okFlag=true;
				funLoc = new ArrayList<>();
				p=new ArrayList<>();
				ArrayList<Integer> path = pathLst.get(pId);			
				int start_org = 0;
				int end_org =0;
				//System.out.print("real 4:[");
				int i1=0;
				for (int j=0;j<path.size()-1;j++)
				{
					int start = path.get(j);
					int end =	path.get(j+1);
					start_org = h.get(start-1);
					end_org = h.get(end-1);
					if(start_org!= end_org)
						p.add(start_org);
					else
					{
						funLoc.add(new Pair(start_org, _d.getFunctions().get(i1)));
						i1++;
					}
				}
				if(end_org!=0)
					p.add(end_org);
				//System.out.println(end_org+ "]");
				ArrayList<wLink> linkLst = new ArrayList<>();
				boolean existLink = false;
				
				ArrayList<Integer> oldLst = new ArrayList<>();
				for (int j=0;j<=_d.getFunctions().size();j++)
				{
					oldLst.add(j+1);
				}
				
				for (int j=0;j<p.size()-1;j++)
				{
					int start = p.get(j);
					int end = p.get(j+1);
					if(start != end)
					{
						existLink = false;
						wLink l1 = new wLink(start,end,1,oldLst);
						for (int j1=0;j1<linkLst.size();j1++) {
							if (linkLst.get(j1).CompareTo(l1))
							{
								existLink = true;
								l1.setWeight(linkLst.get(j1).getWeight()+1);
								linkLst.set(j1, l1);
								break;
							}
						}
						if(!existLink)
							linkLst.add(l1);
					}
				}	
				
				// ktra xem link nao bi vi pham
				ArrayList<wLink> violatedLink = new ArrayList<>();
				for (wLink l:linkLst)
				{
					if(_g.getEdgeWeight(l.getStart(), l.getEnd())<l.getWeight()*_d.getBw())
					{
						Double t = _g.getEdgeWeight(l.getStart(), l.getEnd())/_d.getBw();
						l.setWeight(t);
						violatedLink.add(l);
					}
				}
					
				
				
				
				ArrayList<Node> nodeLst= new ArrayList<>();
				boolean existNode = false;
				//Dang xet la function require nhu nhau.				
				//ktra xem node nao bi vi pham
				oldLst = new ArrayList<>();
				for (int j=1;j<=_d.getFunctions().size();j++)
				{
					oldLst.add(j);
				}
				
				for (int j=0;j<funLoc.size();j++)
				{
					Pair pr = funLoc.get(j);
					Vector<Double> mul = getFunction(pr.getfunction()).getLamda();
					Node _node = new Node(pr.getnode(), mul,1,oldLst);
					existNode= false;
					for (int i=0;i<nodeLst.size();i++)
					{
						if(nodeLst.get(i).CompareTo(_node))
						{
							existNode = true;
							Vector<Double> c_temp = UtilizeFunction.add(nodeLst.get(i).getReq(),mul);
							nodeLst.get(i).setReq(c_temp);
							nodeLst.get(i).setusedNo(nodeLst.get(i).getusedNo()+1);
							break;
						}
					}
					if(!existNode)
						nodeLst.add(_node);
				}
				
				ArrayList<Node> violatedNode = new ArrayList<>();
				for (Node node:nodeLst)
				{
					if(UtilizeFunction.isBig(node.getReq(), _g.getCap(node.getid())))
					{
						int t= UtilizeFunction.divide(_g.getCap(node.getid()), getFunction(_d.getFunctions().get(0)).getLamda());
						node.setusedNo(t);
						violatedNode.add(node);
					}
				}
								
				if(violatedLink.size()==0&& violatedNode.size()==0)
				{
					fl=false;
					return null;
				}
				
				int combSum1=1;
				int combSum2=1;

				int limitLoop =10;
				for (wLink l:violatedLink)
				{
					int length = (int) l.getWeight();
					ArrayList<ArrayList<Integer>> listComb = new ArrayList<>();
					int numberofComb = combinations(length, l.getCnnSet(), listComb);
					l.setCnnSetComb(listComb);	
					combSum1=combSum1*numberofComb;
				}
				for (Node node:violatedNode)
				{
					//int length = (int) l.getWeight();
					ArrayList<ArrayList<Integer>> listComb = new ArrayList<>();
					int numberofComb = combinations(node.getusedNo(), node.getvSetLst(), listComb);
					node.setvSetComb(listComb);	
					combSum2=combSum2*numberofComb;
				}
				
				
				//quyet dinh remove nhung link ay trong tap cnnset nao
				ArrayList<Integer> demArr = new ArrayList<>();
				for (int i=0;i<violatedLink.size();i++)
					demArr.add(0);
				//minP = new ArrayList<>();
				//minFunLoc= new ArrayList<>();
				//minLenth= Integer.MAX_VALUE;
				while (dem<limitLoop)
				{
					ArrayList<wLink> updateLink = new ArrayList<>();
					System.out.println("Thu lan::: "+ dem);
					dem++;
					if(violatedLink.size()>0)
					{
						for (int i= 0;i<violatedLink.size();i++)
						{
							//xet tung link
							wLink linkTemp = violatedLink.get(i);
							ArrayList<ArrayList<Integer>> listComb = linkTemp.getCnnSetComb();
							int rand = UtilizeFunction.randInt(0, listComb.size()-1);
							linkTemp.setCnnSet(listComb.get(rand));
							updateLink.add(linkTemp);
						}
					}
					ArrayList<Node> updateNode = new ArrayList<>();
					if(violatedNode.size()>0)
					{
						for(int i=0;i<violatedNode.size();i++)
						{
							
							//update lai so node co the su dung cho node do bang cach chia cap cho max function
							Node _node = violatedNode.get(i);
							ArrayList<ArrayList<Integer>> listComb = _node.getvSetComb();
							int rand = UtilizeFunction.randInt(0, listComb.size()-1);
							_node.setvSetLst(listComb.get(rand));
							updateNode.add(_node);
						}
					}
					
					
					p = new ArrayList<>();
					g_used= new nGraph(_g.cap, _g.w);
					funLoc = new ArrayList<>();
					h = new ArrayList<>();
					okFlag= false;
					
					g_save = CreateExGraph(_g, _d, updateLink,updateNode);
					if(g_save==null)
					{
						fl=false;
						return null;
					}
					System.out.println("source: "+ source + ", destination: "+ destination);
					ArrayList<ArrayList<Integer>> pathLstNew = shortestPaths(source, destination, g_save, _d.getBw());
					if(pathLstNew!=null && pathLstNew.size()>0)
					{

						for(int i=0;i<pathLstNew.size();i++)
						{
							System.out.print(i+":[");
							for (int j=0;j<pathLstNew.get(i).size();j++)
								System.out.print( pathLstNew.get(i).get(j)+" ");
							System.out.println("]");
						}
						for(int i=0;i<pathLstNew.size();i++)
						{

							funLoc = new ArrayList<>();
							okFlag=false;
							p=new ArrayList<>();
							//Chon 1 duong ngau nhien, kiem tra xem co du tai nguyen ko
							g_used= new nGraph(_g.cap, _g.w);
							path = pathLstNew.get(i);			
							start_org = 0;
							end_org =0;
//							System.out.print("noreal 4: [");
//							for (int j=0;j<path.size();j++)
//								System.out.print(path.get(j)+ " ");
//							System.out.println("]");
							i1=0;
							for (int j=0;j<path.size()-1;j++)
							{
								int start = path.get(j);
								int end =	path.get(j+1);
								start_org = h.get(start-1);
								end_org = h.get(end-1);
								if(start_org!= end_org)
									p.add(start_org);
								else
								{
									funLoc.add(new Pair(start_org, _d.getFunctions().get(i1)));
									i1++;
								}
								System.out.print(start_org+ " ");
							}
							if(end_org!=0)
								p.add(end_org);
							System.out.println(end_org+ "]");
							for (int j=0;j<p.size()-1;j++)
							{
								int start = p.get(j);
								int end = p.get(j+1);
								if(start!=end)
								{
									double wei= g_used.getEdgeWeight(start, end);
									if(wei<_d.getBw())
									{
										okFlag=true;
										break;
									}
									else
									{
										g_used.setEdgeWeight(start, end, wei- _d.getBw());
									}
								}
								
									
							}
							if(okFlag)
								continue;
							okFlag=false;
							for (i1=0;i1<_d.getFunctions().size();i1++)
							{
								int idFun= _d.getFunctions().get(i1);
								
								for (int j=0;j<funLoc.size();j++)
								{
									Pair pr = funLoc.get(j);
									if(pr.getfunction() == idFun)
									{
										Vector<Double> mul = getFunction(idFun).getLamda();
										if(UtilizeFunction.isBig(mul,g_used.getCap(pr.getnode())))
										{
											okFlag=true;
											break;											
										}
										else
										{
											Vector<Double> c_temp= UtilizeFunction.minus(g_used.getCap(pr.getnode()),mul);
											g_used.setCap(pr.getnode(),c_temp );
										}
										
									}
								}
								if(okFlag)
									break;
							}

							
							if(!okFlag)
							{
								if(p.size()<minLenth)
								{
									minLenth = p.size();
									minP= new ArrayList<>();
									minFunLoc = new ArrayList<>();
									for (i1 = 0;i1<p.size();i1++)
										minP.add(p.get(i1));
									for (i1=0;i1<funLoc.size();i1++)
										minFunLoc.add(funLoc.get(i1));
								}
							}
							if(minP!=null && minP.size()>0)
								break;
						
						}				
					}
					if(minP!=null && minP.size()>0)
						break;		
				}
				if(minP!=null && minP.size()>0)
				{
					System.out.println("aaaaa: "+ minP.size());
					break;
				}
			}
			
		}
		
		if(minP==null || minP.size()<=0)
		{
			fl = false;
			return null;
		}
		
		fl=true;
		funLoc = new ArrayList<>();
		for (int i1=0;i1<minFunLoc.size();i1++)
			funLoc.add(minFunLoc.get(i1));
		return minP;	
	
	
	
	
	
	}	
	public static ArrayList<Integer> newHeuristic (nDemand _d, nGraph _g)//theo old expanded graph (optimal 2)
	{



		//link_load, node load duoc cap nhat o day
		ArrayList<Integer> p = new ArrayList<>();
		nGraph g_used= new nGraph(_g.cap, _g.w);
		funLoc = new ArrayList<>();
		h = new ArrayList<>();
		boolean okFlag= false;
		ArrayList<Integer> minP = new ArrayList<>();
		ArrayList<Pair> minFunLoc= new ArrayList<>();
		int minLenth= Integer.MAX_VALUE;
		//nGraph g_save = ConstructingGraph(_g, _d);
		nGraph g_save = ExpandedGraph(_g, _d);
		if(g_save==null)
		{
			fl=false;
			return null;
		}
		System.out.println("source: "+ source + ", destination: "+ destination);
		
		//ArrayList<ArrayList<Integer>> pathLst = allOfShortestPaths(source, destination, g_save, _d.getBw());
		ArrayList<ArrayList<Integer>> pathLst = shortestPaths(source, destination, g_save, _d.getBw());
		
		
		if(pathLst!=null && pathLst.size()>0)
		{
//			for(int i=0;i<pathLst.size();i++)
//			{
//				System.out.print(i+":[");
//				for (int j=0;j<pathLst.get(i).size();j++)
//					System.out.print( pathLst.get(i).get(j)+" ");
//				System.out.println("]");
//			}
			for(int i=0;i<pathLst.size();i++)
			{

				funLoc = new ArrayList<>();
				okFlag=false;
				p=new ArrayList<>();
				g_used= new nGraph(_g.cap, _g.w);
				ArrayList<Integer> path = pathLst.get(i);			
				int start_org = 0;
				int end_org =0;
//				System.out.print("noreal 4: [");
//				for (int j=0;j<path.size();j++)
//					System.out.print(path.get(j)+ " ");
//				System.out.println("]");
				//System.out.print("real 4:[");
				for (int j=0;j<path.size()-1;j++)
				{
					int start = path.get(j);
					int end =	path.get(j+1);
					start_org = h.get(start-1);
					end_org = h.get(end-1);
					if(start_org!= end_org)
						p.add(start_org);
					//System.out.print(start_org+ " ");
				}
				if(end_org!=0)
					p.add(end_org);
				//System.out.println(end_org+ "]");
				for (int j=0;j<p.size()-1;j++)
				{
					if(p.get(j)!=p.get(j+1))
					{
						double wei= g_used.getEdgeWeight(p.get(j), p.get(j+1));
						if(wei<_d.getBw())
						{
							okFlag=true;
							break;
						}
						else
						{
							g_used.setEdgeWeight(p.get(j), p.get(j+1), wei- _d.getBw());
						}
					}
					
						
				}
				if(okFlag)
					continue;
				else
				{
					int i1 = 0;
					if(p.size()<minLenth)
					{
						for (int j=1;j<path.size()-1;j++)
						{
							int n= path.get(j);
							if(n< destination)
							{
								funLoc.add(new Pair(h.get(n-1), _d.getFunctions().get(i1)));
								i1++;
							}
						}
						for (i1=0;i1<_d.getFunctions().size();i1++)
						{
							int idFun= _d.getFunctions().get(i1);
							
							for (int j=0;j<funLoc.size();j++)
							{
								Pair pr = funLoc.get(j);
								if(pr.getfunction() == idFun)
								{
									Vector<Double> mul = getFunction(idFun).getLamda();
									if(UtilizeFunction.isBig(mul,g_used.getCap(pr.getnode())))
									{
										okFlag=true;
										break;
										
									}
									else
									{
										Vector<Double> c_temp= UtilizeFunction.minus(g_used.getCap(pr.getnode()),mul);
										g_used.setCap(pr.getnode(),c_temp );
									}
									
								}
							}
							if(okFlag)
								break;
						}
					

						if(!okFlag)
						{
							minLenth = p.size();
							minP= new ArrayList<>();
							minFunLoc = new ArrayList<>();
							for (i1 = 0;i1<p.size();i1++)
								minP.add(p.get(i1));
							for (i1=0;i1<funLoc.size();i1++)
								minFunLoc.add(funLoc.get(i1));
						}
					
					}
						
				}
			continue;
			}
		}	
		else
		{
			flag = 1;
			fl=false;
			return null;
		}
		if(minP==null || minP.size()<=0)
			flag=2;
		else
			flag=0;
		if(minP==null || minP.size()<=0)
		{
			System.out.println("Blocking of type 2");
			//thuc hien buoc 5
			//tim tat ca nhung link bi vuot 
			
			//Chon 1 duong ngau nhien, kiem tra xem co du tai nguyen ko
			int dem= 0;
			for (int pId = 0;pId < pathLst.size();pId++)
			{
				
				okFlag=true;
				funLoc = new ArrayList<>();
				p=new ArrayList<>();
				ArrayList<Integer> path = pathLst.get(pId);			
				int start_org = 0;
				int end_org =0;
				System.out.print("real 4:[");
				for (int j=0;j<path.size()-1;j++)
				{
					int start = path.get(j);
					int end =	path.get(j+1);
					start_org = h.get(start-1);
					end_org = h.get(end-1);
					p.add(start_org);
					System.out.print(start_org+ " ");
				}
				if(end_org!=0)
					p.add(end_org);
				System.out.println(end_org+ "]");
				int i1=0;
				for (int j=1;j<path.size()-1;j++)
				{
					int n= path.get(j);
					if(n< destination)
					{
						funLoc.add(new Pair(h.get(n-1), _d.getFunctions().get(i1)));
						i1++;
					}
				}
				ArrayList<wLink> linkLst = new ArrayList<>();
				boolean existLink = false;
				
				ArrayList<Integer> oldLst = new ArrayList<>();
				for (int j=0;j<=_d.getFunctions().size();j++)
				{
					oldLst.add(j+1);
				}
				
				for (int j=0;j<p.size()-1;j++)
				{
					int start = p.get(j);
					int end = p.get(j+1);
					if(start != end)
					{
						existLink = false;
						wLink l1 = new wLink(start,end,1,oldLst);
						for (int j1=0;j1<linkLst.size();j1++) {
							if (linkLst.get(j1).CompareTo(l1))
							{
								existLink = true;
								l1.setWeight(linkLst.get(j1).getWeight()+1);
								linkLst.set(j1, l1);
								break;
							}
						}
						if(!existLink)
							linkLst.add(l1);
					}
				}	
				
				// ktra xem link nao bi vi pham
				ArrayList<wLink> violatedLink = new ArrayList<>();
				for (wLink l:linkLst)
				{
					if(_g.getEdgeWeight(l.getStart(), l.getEnd())<l.getWeight()*_d.getBw())
					{
						Double t = _g.getEdgeWeight(l.getStart(), l.getEnd())/_d.getBw();
						l.setWeight(t);
						violatedLink.add(l);
					}
				}
					
				
				
				
				ArrayList<Node> nodeLst= new ArrayList<>();
				boolean existNode = false;
				//Dang xet la function require nhu nhau.				
				//ktra xem node nao bi vi pham
				oldLst = new ArrayList<>();
				for (int j=1;j<=_d.getFunctions().size();j++)
				{
					oldLst.add(j);
				}
				
				for (int j=0;j<funLoc.size();j++)
				{
					Pair pr = funLoc.get(j);
					Vector<Double> mul = getFunction(pr.getfunction()).getLamda();
					Node _node = new Node(pr.getnode(), mul,1,oldLst);
					existNode= false;
					for (int i=0;i<nodeLst.size();i++)
					{
						if(nodeLst.get(i).CompareTo(_node))
						{
							existNode = true;
							Vector<Double> c_temp = UtilizeFunction.add(nodeLst.get(i).getReq(),mul);
							nodeLst.get(i).setReq(c_temp);
							nodeLst.get(i).setusedNo(nodeLst.get(i).getusedNo()+1);
							break;
						}
					}
					if(!existNode)
						nodeLst.add(_node);
				}
				
				ArrayList<Node> violatedNode = new ArrayList<>();
				for (Node node:nodeLst)
				{
					if(UtilizeFunction.isBig(node.getReq(), _g.getCap(node.getid())))
					{
						int t= UtilizeFunction.divide(_g.getCap(node.getid()), getFunction(_d.getFunctions().get(0)).getLamda());
						node.setusedNo(t);
						violatedNode.add(node);
					}
				}
								
				if(violatedLink.size()==0&& violatedNode.size()==0)
				{
					fl=false;
					return null;
				}
				
				int combSum=1;

				int limitLoop =10;
				for (wLink l:violatedLink)
				{
					int length = (int) l.getWeight();
					ArrayList<ArrayList<Integer>> listComb = new ArrayList<>();
					int numberofComb = combinations(length, l.getCnnSet(), listComb);
					l.setCnnSetComb(listComb);	
					combSum=combSum*numberofComb;
				}
				for (Node node:violatedNode)
				{
					//int length = (int) l.getWeight();
					ArrayList<ArrayList<Integer>> listComb = new ArrayList<>();
					int numberofComb = combinations(node.getusedNo(), node.getvSetLst(), listComb);
					node.setvSetComb(listComb);	
					//combSum=combSum*numberofComb;
				}
				if(combSum+dem<limitLoop)
					limitLoop= combSum+dem;
				else
					limitLoop=10;
				
				//quyet dinh remove nhung link ay trong tap cnnset nao
				ArrayList<Integer> demArr = new ArrayList<>();
				for (int i=0;i<violatedLink.size();i++)
					demArr.add(0);
				while (dem<limitLoop)
				{
					ArrayList<wLink> updateLink = new ArrayList<>();
					System.out.println("Thu lan::: "+ dem);
					dem++;
					if(violatedLink.size()>0)
					{
						for (int i= 0;i<violatedLink.size();i++)
						{
							//xet tung link
							wLink linkTemp = violatedLink.get(i);
							ArrayList<ArrayList<Integer>> listComb = linkTemp.getCnnSetComb();
							int rand = UtilizeFunction.randInt(0, listComb.size()-1);
							linkTemp.setCnnSet(listComb.get(rand));
							updateLink.add(linkTemp);
						}
					}
					ArrayList<Node> updateNode = new ArrayList<>();
					if(violatedNode.size()>0)
					{
						for(int i=0;i<violatedNode.size();i++)
						{
							
							//update lai so node co the su dung cho node do bang cach chia cap cho max function
							Node _node = violatedNode.get(i);
							ArrayList<ArrayList<Integer>> listComb = _node.getvSetComb();
							int rand = UtilizeFunction.randInt(0, listComb.size()-1);
							_node.setvSetLst(listComb.get(rand));
							updateNode.add(_node);
						}
					}
					
					
					p = new ArrayList<>();
					g_used= new nGraph(_g.cap, _g.w);
					funLoc = new ArrayList<>();
					h = new ArrayList<>();
					okFlag= false;
					minP = new ArrayList<>();
					minFunLoc= new ArrayList<>();
					minLenth= Integer.MAX_VALUE;
					g_save = ExpandedGraphNew(_g, _d, updateLink,updateNode);
					if(g_save==null)
					{
						fl=false;
						return null;
					}
					System.out.println("source: "+ source + ", destination: "+ destination);
					ArrayList<ArrayList<Integer>> pathLstNew = shortestPaths(source, destination, g_save, _d.getBw());
					if(pathLstNew!=null && pathLstNew.size()>0)
					{

						for(int i=0;i<pathLstNew.size();i++)
						{
							System.out.print(i+":[");
							for (int j=0;j<pathLstNew.get(i).size();j++)
								System.out.print( pathLstNew.get(i).get(j)+" ");
							System.out.println("]");
						}
						for(int i=0;i<pathLstNew.size();i++)
						{

							funLoc = new ArrayList<>();
							okFlag=false;
							p=new ArrayList<>();
							//Chon 1 duong ngau nhien, kiem tra xem co du tai nguyen ko
							g_used= new nGraph(_g.cap, _g.w);
							path = pathLstNew.get(i);			
							start_org = 0;
							end_org =0;
//							System.out.print("noreal 4: [");
//							for (int j=0;j<path.size();j++)
//								System.out.print(path.get(j)+ " ");
//							System.out.println("]");
							System.out.print("real 4:[");
							for (int j=0;j<path.size()-1;j++)
							{
								int start = path.get(j);
								int end =	path.get(j+1);
								start_org = h.get(start-1);
								end_org = h.get(end-1);
								if(start_org!= end_org)
									p.add(start_org);
								System.out.print(start_org+ " ");
							}
							if(end_org!=0)
								p.add(end_org);
							System.out.println(end_org+ "]");
							for (int j=0;j<p.size()-1;j++)
							{
								if(p.get(j)!=p.get(j+1))
								{
									double wei= g_used.getEdgeWeight(p.get(j), p.get(j+1));
									if(wei<_d.getBw())
									{
										okFlag=true;
										break;
									}
									else
									{
										g_used.setEdgeWeight(p.get(j), p.get(j+1), wei- _d.getBw());
									}
								}
								
									
							}
							if(okFlag)
								continue;
							i1=0;
							for (int j=1;j<path.size()-1;j++)
							{
								int n= path.get(j);
								if(n< destination)
								{
									funLoc.add(new Pair(h.get(n-1), _d.getFunctions().get(i1)));
									i1++;
								}
							}
							okFlag=false;
							for (i1=0;i1<_d.getFunctions().size();i1++)
							{
								int idFun= _d.getFunctions().get(i1);
								
								for (int j=0;j<funLoc.size();j++)
								{
									Pair pr = funLoc.get(j);
									if(pr.getfunction() == idFun)
									{
										Vector<Double> mul = getFunction(idFun).getLamda();
										if(UtilizeFunction.isBig(mul,g_used.getCap(pr.getnode())))
										{
											okFlag=true;
											break;											
										}
										else
										{
											Vector<Double> c_temp= UtilizeFunction.minus(g_used.getCap(pr.getnode()),mul);
											g_used.setCap(pr.getnode(),c_temp );
										}
										
									}
								}
								if(okFlag)
									break;
							}

							
							if(!okFlag)
							{
								if(p.size()<minLenth)
								{
									minLenth = p.size();
									minP= new ArrayList<>();
									minFunLoc = new ArrayList<>();
									for (i1 = 0;i1<p.size();i1++)
										minP.add(p.get(i1));
									for (i1=0;i1<funLoc.size();i1++)
										minFunLoc.add(funLoc.get(i1));
								}
							}
							continue;
							
						
						}				
					}
					if(minP!=null && minP.size()>0)
						break;		
				}
				if(minP!=null && minP.size()>0)
					break;	
			}
			
		}
		
		if(minP==null || minP.size()<0)
		{
			fl = false;
			return null;
		}
		
		fl=true;
		funLoc = new ArrayList<>();
		for (int i1=0;i1<minFunLoc.size();i1++)
			funLoc.add(minFunLoc.get(i1));
		return minP;	
	
	
	
	
	}
	
	public static ArrayList<Integer> GreedyOptimal(nDemand _d,nGraph _g)
	{

		flag= 0;
		//link_load, node load duoc cap nhat o day
		ArrayList<Integer> p = new ArrayList<>();
		nGraph g_used= new nGraph(_g.cap, _g.w);
		funLoc = new ArrayList<>();
		h = new ArrayList<>();
		boolean okFlag= false;
		ArrayList<Integer> minP = new ArrayList<>();
		ArrayList<Pair> minFunLoc= new ArrayList<>();
		int minLenth= Integer.MAX_VALUE;
		//nGraph g_save = ConstructingGraph(_g, _d);
		nGraph g_save = ConstructingSimpleGraph(_g, _d);
		if(g_save==null)
		{
			fl=false;
			return null;
		}
		System.out.println("source: "+ source + ", destination: "+ destination);
		//ArrayList<ArrayList<Integer>> pathLst = allShortestPath(source, destination, g_save, 0.0);
		ArrayList<ArrayList<Integer>> pathLst = allOfShortestPaths(source, destination, g_save, 0.000000001);
		
		if(pathLst!=null && pathLst.size()>0)
		{
			for(int i=0;i<pathLst.size();i++)
			{
				System.out.print(i+":[");
				for (int j=0;j<pathLst.get(i).size();j++)
					System.out.print( pathLst.get(i).get(j)+" ");
				System.out.println("]");
			}
			for(int i=0;i<pathLst.size();i++)
			{

				funLoc = new ArrayList<>();
				okFlag=false;// flag to stop
				p=new ArrayList<>();
				//Chon 1 duong ngau nhien, kiem tra xem co du tai nguyen ko
				g_used= new nGraph(_g.cap, _g.w);
				ArrayList<Integer> path = pathLst.get(i);			
				int start_org = 0;
				int end_org =0;
//				System.out.print("noreal 4: [");
//				for (int j=0;j<path.size();j++)
//					System.out.print(path.get(j)+ " ");
//				System.out.println("]");
				System.out.print("real 4:[");
				for (int j=0;j<path.size()-1;j++)
				{
					int start = path.get(j);
					int end =	path.get(j+1);
					start_org = h.get(start-1);
					end_org = h.get(end-1);
					if(start_org!= end_org)
						p.add(start_org);
					System.out.print(start_org+ " ");
				}
				if(end_org!=0)
					p.add(end_org);
				System.out.println(end_org+ "]");
				//ktra neu chi can ton tai mot canh vi pham -> path nay khong thoa man
				for (int j=0;j<p.size()-1;j++)
				{
					if(p.get(j)!=p.get(j+1))
					{
						double wei= g_used.getEdgeWeight(p.get(j), p.get(j+1));
						if(wei<_d.getBw())
						{
							okFlag=true;
							break;
						}
						else
						{
							g_used.setEdgeWeight(p.get(j), p.get(j+1), wei- _d.getBw());
						}
					}
					
						
				}
				if(okFlag)
					continue;
				int idFunc =-1;
				int i1=0;
				int len=-1;
				int node=1;
				for (int j=1;j<path.size()-1;j++)
				{
					int n= path.get(j);
					if(i1<_d.getFunctions().size())
					{
						node=1;
						if(i1!=0)
						{
							for (int i2=0;i2<i1;i2++)
								node+=getFunction(_d.getFunctions().get(i2)).getVnfNode().size();
						}
						len = getFunction(_d.getFunctions().get(i1)).getVnfNode().size();
						if(n>node && n<=node+len)
						{
							idFunc= i1;
							i1++;
						}
						else
							idFunc=-1;
						if(idFunc!=-1)
						{
							funLoc.add(new Pair(h.get(n-1), _d.getFunctions().get(idFunc)));
						}
					
					}
					else
						break;
					
					
				}
				okFlag= false;
				for (i1=0;i1<_d.getFunctions().size();i1++)
				{
					int idFun= _d.getFunctions().get(i1);
					
					for (int j=0;j<funLoc.size();j++)
					{
						Pair pr = funLoc.get(j);
						if(pr.getfunction() == idFun)
						{
							Vector<Double> mul = getFunction(idFun).getLamda();
							if(UtilizeFunction.isBig(mul,g_used.getCap(pr.getnode())))
							{
								okFlag=true;
								break;
							}
							else
							{
								Vector<Double> c_temp= UtilizeFunction.minus(g_used.getCap(pr.getnode()),mul);
								g_used.setCap(pr.getnode(),c_temp );
							}
							
						}
					}
					if(okFlag)
						break;
				}

				
				if(!okFlag)
				{
					if(p.size()<minLenth)
					{
						minLenth = p.size();
						minP= new ArrayList<>();
						minFunLoc = new ArrayList<>();
						for (i1 = 0;i1<p.size();i1++)
							minP.add(p.get(i1));
						for (i1=0;i1<funLoc.size();i1++)
							minFunLoc.add(funLoc.get(i1));
					}
				}
				continue;
				
			
			}
		}	
		else
		{
			flag = 1;
			fl=false;
			return null;
		}

		if(minP==null || minP.size()==0)
			flag=2;
		else
			flag=0;
		fl=true;
		funLoc = new ArrayList<>();
		for (int i1=0;i1<minFunLoc.size();i1++)
			funLoc.add(minFunLoc.get(i1));
		return minP;	
	
	
	}
	
	public static void mainAA(String[] args)// dung de check giai thuat migration
	{
		BufferedWriter out1 = null;
		File dir = new File("test");
		String[] extensions = new String[] { "txt" };
		try {
			System.out.println("Getting all .txt in " + dir.getCanonicalPath()
					+ " including those in subdirectories");
		} catch (IOException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

		File _f = new File("data2Alg4.txt");
		String str="";
		try {
			out1 = new BufferedWriter(new FileWriter(_f,true));
			for (File file : files) {
				try {
					System.out.println("file: " + file.getCanonicalPath());
					ReadInputFile(file.getPath());
					str=file.getName(); 
					str = str.replace("in", "4Alg");
					out1.write(str);
						if(Alg4(str))
						{
							out1.write(" "+noVertex + " " +noFunction + " "+ maxlinkload+" "+ acceptRatio+ " "+ _duration );
							
						}
						else
						{
							out1.write(" Notfound ");
							
						}
						
						out1.newLine();
							
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		} catch ( IOException e1 ) {
			e1.printStackTrace();
			} 
		try {
			out1.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	
	}
	private static VertexFactory<Object> vertexFactory = new VertexFactory<Object>()
    {
        private int i;

        @Override
        public Object createVertex()
        {
            return ++i;
        }
    };
//    private static VertexFactory<Integer> vertexFactoryInteger = new VertexFactory<Integer>()
//    	    {
//    	        private int i=0;
//
//    	        @Override
//    	        public Integer createVertex()
//    	        {
//    	            return ++i;
//    	        }
//    	    };
    public static void CreateRandomGraph(int NoVertex,int NoEdge,double p)
    {
//        DirectedGraph<Object, DefaultEdge> graph = new DefaultDirectedGraph<>(DefaultEdge.class);
//        ScaleFreeGraphGenerator<Object, DefaultEdge> generator = new ScaleFreeGraphGenerator<>(NoVertex);
//        generator.generateGraph(graph, vertexFactory, null);
 

         
    	//UndirectedGraph<Object, DefaultEdge> graph = new SimpleGraph<>(DefaultEdge.class);
    	//RandomGraphGenerator<Object, DefaultEdge> generator= new RandomGraphGenerator<>(NoVertex, NoEdge);
    	//generator.generateGraph(graph, vertexFactory, null);
       
    	final int seed = 1;
        final double edgeProbability = p;
        final int numberVertices = NoVertex;

        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gg =
                new GnpRandomGraphGenerator<Integer, DefaultWeightedEdge>(
                    numberVertices, edgeProbability, seed, false);
//        GraphGenerator<Integer, DefaultWeightedEdge, Integer> gg =
//            new GnpRandomGraphGenerator<Integer, DefaultWeightedEdge>(
//                numberVertices, edgeProbability, seed, false);
//        
//        WeightedPseudograph<Integer, DefaultWeightedEdge> graph =
//                new WeightedPseudograph<>(DefaultWeightedEdge.class);
        
        DefaultDirectedGraph<Integer, DefaultWeightedEdge> graph =
                new DefaultDirectedGraph<>(DefaultWeightedEdge.class);
        VertexFactory<Integer> vertexFactoryInteger= new VertexFactory<Integer>() {
        	 private int i=0;

 	        @Override
 	        public Integer createVertex()
 	        {
 	            return ++i;
 	        }
		};
            gg.generateGraph(graph, vertexFactoryInteger, null);
    	
        //set cap and bandwidth cho do thi g
        g = new nGraph(NoVertex);
        int noE = 0;
        for (DefaultWeightedEdge edges : graph.edgeSet()) {
        	
        	int s = Integer.parseInt(graph.getEdgeSource(edges).toString());
        	int t = Integer.parseInt(graph.getEdgeTarget(edges).toString());
			//System.out.println("Dinh: "+ s+ "..." + t+ "..."+w);
			//double w= UtilizeFunction.randomDouble(new Integer[] {5000,6000,7000,8000,9000,10000});
			if(s!=t)
			{
				g.setEdgeWeight(s, t, 1);
				noE++;
			}
			else
				System.out.println("Loop");
		}
	   	for (int i=0;i<g.getV();i++)
       {
	   		//int index = UtilizeFunction.randInt(0, dataReal.size()-1);
	      	  //Vector<Double> data = dataReal.get(index);
	      	  Vector<Double> t= new Vector<>(3);
	      	  for(int j=0;j<3;j++)
	      		  t.add(100.0);
	      	  g.setCap(i+1, t);
       }    
	   	noVertex = g.getV();
	   	E = noE;
        
        
    }
	
	public static void WriteData(int NoVertex,int NoEdge,int NoFunc,double p, String fileName)
	{

		DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
        try {        	
	    	out= new BufferedWriter(new FileWriter(fileName));
	    	CreateRandomGraph(NoVertex,NoEdge,p);
			Double[] hso_Func= {2.0,20.0,10.0};
			functionArr = new ArrayList<>();
		    for (int i=0;i< 10;i++)
		       functionArr.add(new nFunction(new Vector<Double>(Arrays.asList(hso_Func)),i+1,g.getV()));
		    //ghi ra file
		    out.write(NoVertex+" "+ E+" "+NoFunc +" "+p );
		    out.newLine();
		    for (int i=0;i<NoFunc;i++)
		    {
	               for (int j=0;j<2;j++)
	            	   out.write(df.format(functionArr.get(i).getLamda().get(j))+" ");
	               out.write(df.format(functionArr.get(i).getLamda().get(2)));
	               out.write(";");
	               for (Integer _i : functionArr.get(i).getVnfNode()) {
	            	   
					out.write(_i+" ");
				}
	               out.newLine();
	               
	       	}
	       	for (int i=0;i<NoVertex;i++)
	       	{		            
	       		for (int j=0;j<3;j++)
	            	   out.write(df.format(g.getCap(i+1).get(j))+" ");	            
	       		out.newLine();
	       	}
	       	for (int i=0;i<NoVertex;i++)
	       	{
	       		for (int j=0;j<NoVertex;j++)
	       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
	       		out.newLine();
	       	}
	       	out.close();
			} catch (IOException e) {
			e.printStackTrace();
		}   	
    
	}
	public static void ReadData(String fileName)
	{		
		File file = new File(fileName);
		functionArr = new ArrayList<nFunction>();
		DemandArray = new ArrayList<nDemand>();
		ArrayList<Integer> vnfNodeLst = new ArrayList<Integer>();
        try {
			in = new BufferedReader(new FileReader(file));
			//First line -> set of parameters
			String[] tempLine=in.readLine().split(" ");
			noVertex= Integer.parseInt(tempLine[0]);
			E = Integer.parseInt(tempLine[1]);
			noFunction= Integer.parseInt(tempLine[2]);
			prob = Double.parseDouble(tempLine[3]);
			//second line -> set of Functions
			for (int i=0;i<noFunction;i++)
			{
				vnfNodeLst = new ArrayList<Integer>();
				tempLine = in.readLine().split(";"); 
				Vector<Double> lamda= new Vector<>(3);
				for (int j=0;j<3;j++)
		        	lamda.addElement(Double.parseDouble(tempLine[0].split(" ")[j]));
				String[] nodeLine = tempLine[1].split(" ");
				for(int j=0; j< nodeLine.length;j++)
					vnfNodeLst.add(Integer.parseInt(nodeLine[j]));
				functionArr.add(new nFunction(i+1,lamda,vnfNodeLst));
			}
			//luu vao mang noVertex+1 chieu
			
			Vector<Vector<Double>> cap = new Vector<Vector<Double>>(noVertex+1);
			ArrayList<List<Double>> w = new ArrayList<List<Double>>();			
			
			for (int i=0;i <noVertex;i++)
			{
				tempLine = in.readLine().split(" ");
				Vector<Double> t= new Vector<>(3);
	   	        for (int j=0;j<3;j++)
	   	        	t.addElement(Double.parseDouble(tempLine[j]));
	   	        cap.add(t);
			}
			
			for (int i=0;i<noVertex;i++)
			{
				ArrayList<Double> temp= new ArrayList<>();
				tempLine = in.readLine().split(" ");
				for(int j=0;j<noVertex;j++)
				{
					temp.add(Double.parseDouble(tempLine[j]));
				}
				w.add(temp);
			}
			g= new nGraph(cap,w);
            // Always close files.
            in.close();  
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
	}
	
	public static boolean checkConnect (nGraph _g,double maxBw)
	{
//		SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleDirectedWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
//	    
//		for (int j=0;j<_g.getV();j++)
//	    {
//	    	g_i.addVertex("node"+(j+1));
//	    }      
//	    for (int j=0;j<_g.getV();j++)
//	    {	        	
//	    	for(int k=0;k<_g.getV();k++)
//	    	{
//	    		if(j!=k&&_g.getEdgeWeight(j+1, k+1)>maxBw)
//	    		{
//	    			DefaultWeightedEdge e=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
//	        		g_i.setEdgeWeight(e, _g.getEdgeWeight((j+1), (k+1)));
//	    		}
//	    	}
//	    }  
		ArrayList<ArrayList<Integer>> _p = new ArrayList<>();
	    for (int i =0;i<_g.getV();i++)
	    	for(int j=0;j<_g.getV();j++)
	    	{
	    		if(i!=j)
	    		{
	    			int src = i+1;
	    			int dest = j+1;
	    			_p= allOfShortestPaths(src, dest, _g, maxBw);
	    			//List<DefaultWeightedEdge> _p =   DijkstraShortestPath.findPathBetween(g_i, "node"+src, "node"+dest);
	    			if(_p==null || _p.size()==0)
	    			{
	    				return false;
	    			}
	    		}
	    	}
	    return true;
	    
		}
    public static double nextTime(double rateParameter)
    {
    	Random r = new Random();
    	return -Math.log(1.0 - r.nextDouble()) / rateParameter;
    }
    
    public static boolean simulation_optimal_new(double lamda,double processTime,String fileName,int NumFlow, String infile)
    {
		int dem1=0;
		int dem2=0;
		noFunction=10;
		double arrivalTime=0.0;
		double now=0.0;
		int index=0;
		link_load= new double[noVertex][noVertex];
		//int id_temp=-1;
		ArrayList<Integer> id_temp = new ArrayList<>();
		ArrayList<Integer> sol=new ArrayList<>();
		//ArrayList<ArrayList<Integer>> newsol=new ArrayList<>();
		ArrayList<nOldDemand> processingLst = new ArrayList<>();
		
		ArrayList<ArrayList<nOldDemand>> all4ProcessingLst = new ArrayList<ArrayList<nOldDemand>>();
		//ArrayList<nGraph> all4g_edit= new ArrayList<nGraph>();
		
		ArrayList<Integer> acceptNoLst = new ArrayList<Integer>();
		ArrayList<Double> avgLenLst = new ArrayList<Double>();
		ArrayList<Double> maximumLinkLoad = new ArrayList<>();
		ArrayList<Double> runTime = new ArrayList<>();
		
		//random topology -> nGraph 
		
	    
		//random 10000 flows
	    int dem=NumFlow;
	    int idFlow=0;
	    Queue<Double> evenLst =  new LinkedList<Double>();
	    g_edit= new nGraph(g.cap, g.w);
	    for (int i=0;i<3;i++)
	    {
	    	//all4g_edit.add(g_edit);
	    	acceptNoLst.add(0);
	    	avgLenLst.add(0.0);
	    	all4ProcessingLst.add(null);
	    	maximumLinkLoad.add(0.0);
	    	runTime.add(0.0);
	    }
	    while (idFlow<dem)
	    {
	    	//random flow 1 ()
	    	arrivalTime = now + nextTime(lamda);
	    	evenLst.add(arrivalTime);

	    	now = arrivalTime;
	    	idFlow++;
	    }
	    try {
			File file = new File(fileName);
			out = new BufferedWriter(new FileWriter(file,true));
			while (!evenLst.isEmpty())
		    {
				
		    	now=evenLst.poll();//xet thoi diem den cua package;
		    	nDemand _d = new nDemand(idFlow, g.getV(), functionArr,5,now,processTime);
		    	System.out.println("Flow: "+ index+":"+_d.getSrc()+","+_d.getDest() );
		    	for(int i=0;i<3;i++)
		    	{
		    		fl = false;
		    		//newsol = new ArrayList<>();
		    		sol= new ArrayList<>();
		    		id_temp= new ArrayList<>();
		    		if(all4ProcessingLst.size()>0)
		    			processingLst = all4ProcessingLst.get(i);
		    		else
		    			processingLst =new ArrayList<>();
		    		//g_edit = all4g_edit.get(i);
		    		g_edit= new nGraph(g.cap, g.w);
		    		for (int i1=0;i1<noVertex;i1++)
						for (int i2=0;i2<noVertex;i2++)
							link_load[i1][i2]=0.0000000000000;
		    		if(processingLst!=null && processingLst.size()>0)
			    	{
			    		for(nOldDemand _old: processingLst)
			    		{
			    			
			    			if(now > (_old.GetArrivalTime()+_old.GetProcessTime()))
			    			{
			    				id_temp.add(processingLst.indexOf(_old));						
			    			}
			    			else
			    			{
			    				ArrayList<Integer> path= _old.Get_path();
			    				
			    					for (int _node=0;_node<path.size()-1;_node++)
									{
			    						if(path.get(_node)!=path.get(_node+1))
			    						{
			    							double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth();
											g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
				    						if(g.getEdgeWeight(path.get(_node), path.get(_node+1))>0)
				    							link_load[path.get(_node)-1][path.get(_node+1)-1]+=_old.GetBandwidth()/g.getEdgeWeight(path.get(_node), path.get(_node+1));
				    						
			    						}
			    						
									}
			    				ArrayList<Pair> funLoc1 = _old.Get_funLoc();
			    				
			    				for (int iF=0;iF<_d.getFunctions().size();iF++)
			    				{
			    					int idFun= _d.getFunctions().get(iF);
			    					int count =0;
			    					for (int j=0;j<funLoc1.size();j++)
			    					{
			    						Pair pr = funLoc1.get(j);
			    						if (pr.getfunction()==idFun)
			    							count++;
			    					}
			    					
			    					for (int j=0;j<funLoc1.size();j++)
			    					{
			    						Pair pr = funLoc1.get(j);
			    						if(pr.getfunction() == idFun)
			    						{
			    							Vector<Double> mul = UtilizeFunction.multi(getFunction(idFun).getLamda(), 1.0/count);
			    							if(UtilizeFunction.isBig(g_edit.getCap(pr.getnode()), mul))
			    							{
			    								Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(pr.getnode()),mul);
			    								g_edit.setCap(pr.getnode(),c_temp );
			    							}
			    							
			    						}
			    					}
			    				}
			    				
			    			}
			    		}
			    	}
		    		else
		    			processingLst= new ArrayList<>();
		    		if(id_temp.size()>0)
		    		{
		    			for (int j=0;j<id_temp.size();j++)
		    				processingLst.remove(id_temp.get(j));
		    		}
		    		final long startTime = System.currentTimeMillis();
		    			
		    		switch (i+1) {
					case 1:
						sol = GreedyForward(_d,g_edit);
						break;
					case 2:
						sol = OptimalSimpleGraph(_d,g_edit);
						if(flag==1)
							dem1++;
						if(flag==2)
							dem2++;
						break;
					case 3:
						sol = optimal(_d,g_edit);
						break;
					default:
						break;
					}
		    		_duration = System.currentTimeMillis() - startTime;
		    		runTime.set(i, runTime.get(i)+_duration);
		    		if(fl)
			    	{
		    			if(sol!=null && sol.size()>0)
		    			{
		    				
			    			acceptNoLst.set(i, acceptNoLst.get(i)+1);
			    			avgLenLst.set(i, avgLenLst.get(i)+sol.size());
				    		nOldDemand _old = new nOldDemand(index, now, processTime, _d.getBw(), sol, funLoc);
				    		processingLst.add(_old);
//		    				for (int pId = 0;pId<newsol.size();pId++)
//		    				{
//		    					ArrayList<Integer> path = newsol.get(pId);
//		    					for (int _node=0;_node<path.size()-1;_node++)
//								{
//		    						//double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth()/newsol.size();
//									//g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );	
//		    						if(g.getEdgeWeight(path.get(_node), path.get(_node+1))>0)
//		    							link_load[path.get(_node)-1][path.get(_node+1)-1]+=(1.0/newsol.size())*(_old.GetBandwidth()/g.getEdgeWeight(path.get(_node), path.get(_node+1)));
//		    						
//								}
//		    				}
				    		System.out.print(i+":[");
		    				for (int _node=0;_node<sol.size()-1;_node++)
							{
		    					System.out.print(sol.get(_node)+" ");
	    						//double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth()/newsol.size();
								//g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );	
	    						if(g.getEdgeWeight(sol.get(_node), sol.get(_node+1))>0)
	    							link_load[sol.get(_node)-1][sol.get(_node+1)-1]+=_old.GetBandwidth()/g.getEdgeWeight(sol.get(_node), sol.get(_node+1));
	    						
							}
		    				System.out.print(sol.get(sol.size()-1)+" ");
		    				System.out.print("]");
		    				System.out.println();
		    			}
			    		maxlinkload =maximumLinkLoad.get(i);
			    		for (int i1=0;i1<noVertex;i1++)
							for (int i2=0;i2<noVertex;i2++)
								if(link_load[i1][i2]>maxlinkload)
									maxlinkload = link_load[i1][i2];
			    		System.out.println("max: "+ maxlinkload);
			    		maximumLinkLoad.set(i, maxlinkload);
			    		all4ProcessingLst.set(i, processingLst);
			    	}

		    		
		    		//all4g_edit.set(i, g_edit);
		    	}
		    	index++;
		    	//System.out.println("number of processed flows: "+ index);
		    }
			
			out.write(infile+" "+ NumFlow+" "+ processTime+" "+lamda +" "+ prob+ " ");
			for (int i=0;i<3;i++)
			{
				double blocking= 1-1.0*acceptNoLst.get(i)/dem;
				out.write(blocking+" "+ avgLenLst.get(i)/acceptNoLst.get(i)+ " " + maximumLinkLoad.get(i)+ " "+runTime.get(i)/NumFlow+" ");
				if(i==1)
					out.write(dem1*1.0/NumFlow+" " +1.0*dem2/NumFlow+" ");
			}
			out.newLine();
//		    out.write("Accept number:"+ 1.0*acceptNo/dem);
//		    out.newLine();
//		    out.write("Average Length Path:"+ lengthAverage/acceptNo);
//		    out.newLine();
		    }
			catch ( IOException e1 ) {
				e1.printStackTrace();
				} finally {
					if ( out != null )
						try {
							out.close();
							} catch (IOException e) {
								e.printStackTrace();}
					}    
			try {
		  		out.close();
		  		} catch (IOException e2) {
		  			e2.printStackTrace();
		  			}
		
	    
		return true;
	
    
    }
    
    public static boolean simulation_all(double lamda,double processTime,String fileName,int NumFlow, String infile)
    {

		int demAlgNew = 0;
		int dem1=0;
		int dem2=0;
		noFunction=10;
		double arrivalTime=0.0;
		double now=0.0;
		int index=0;
		link_load= new double[noVertex][noVertex];
		//int id_temp=-1;
		ArrayList<Integer> id_temp = new ArrayList<>();
		ArrayList<Integer> sol=new ArrayList<>();
		//ArrayList<ArrayList<Integer>> newsol=new ArrayList<>();
		ArrayList<nOldDemand> processingLst = new ArrayList<>();
		
		ArrayList<ArrayList<nOldDemand>> all4ProcessingLst = new ArrayList<ArrayList<nOldDemand>>();
		//ArrayList<nGraph> all4g_edit= new ArrayList<nGraph>();
		
		ArrayList<Integer> acceptNoLst = new ArrayList<Integer>();
		ArrayList<Double> avgLenLst = new ArrayList<Double>();
		ArrayList<Double> maximumLinkLoad = new ArrayList<>();
		ArrayList<Double> runTime = new ArrayList<>();
		
		//random topology -> nGraph 
		
	    
		//random 10000 flows
	    int dem=NumFlow;
	    int idFlow=0;
	    Queue<Double> evenLst =  new LinkedList<Double>();
	    g_edit= new nGraph(g.cap, g.w);
	    for (int i=0;i<3;i++)
	    {
	    	//all4g_edit.add(g_edit);
	    	acceptNoLst.add(0);
	    	avgLenLst.add(0.0);
	    	all4ProcessingLst.add(null);
	    	maximumLinkLoad.add(0.0);
	    	runTime.add(0.0);
	    }
	    while (idFlow<dem)
	    {
	    	//random flow 1 ()
	    	arrivalTime = now + nextTime(lamda);
	    	evenLst.add(arrivalTime);

	    	now = arrivalTime;
	    	idFlow++;
	    }
	    try {
			File file = new File(fileName);
			out = new BufferedWriter(new FileWriter(file,true));
			while (!evenLst.isEmpty())
		    {
				
		    	now=evenLst.poll();//xet thoi diem den cua package;
		    	nDemand _d = new nDemand(idFlow, g.getV(), functionArr,5,now,processTime);
		    	System.out.println("Flow: "+ index+":"+_d.getSrc()+","+_d.getDest() );
		    	for(int i=0;i<3;i++)
		    	{
		    		fl = false;
		    		//newsol = new ArrayList<>();
		    		sol= new ArrayList<>();
		    		id_temp= new ArrayList<>();
		    		if(all4ProcessingLst.size()>0)
		    			processingLst = all4ProcessingLst.get(i);
		    		else
		    			processingLst =new ArrayList<>();
		    		//g_edit = all4g_edit.get(i);
		    		g_edit= new nGraph(g.cap, g.w);
		    		for (int i1=0;i1<noVertex;i1++)
						for (int i2=0;i2<noVertex;i2++)
							link_load[i1][i2]=0.0000000000000;
		    		if(processingLst!=null && processingLst.size()>0)
			    	{
			    		for(nOldDemand _old: processingLst)
			    		{
			    			
			    			if(now > (_old.GetArrivalTime()+_old.GetProcessTime()))
			    			{
			    				id_temp.add(processingLst.indexOf(_old));						
			    			}
			    			else
			    			{
			    				ArrayList<Integer> path= _old.Get_path();
			    				
			    					for (int _node=0;_node<path.size()-1;_node++)
									{
			    						if(path.get(_node)!=path.get(_node+1))
			    						{
			    							double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth();
											g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
				    						if(g.getEdgeWeight(path.get(_node), path.get(_node+1))>0)
				    							link_load[path.get(_node)-1][path.get(_node+1)-1]+=_old.GetBandwidth()/g.getEdgeWeight(path.get(_node), path.get(_node+1));
				    						
			    						}
			    						
									}
			    				ArrayList<Pair> funLoc1 = _old.Get_funLoc();
			    				
			    				for (int iF=0;iF<_d.getFunctions().size();iF++)
			    				{
			    					int idFun= _d.getFunctions().get(iF);
			    					int count =0;
			    					for (int j=0;j<funLoc1.size();j++)
			    					{
			    						Pair pr = funLoc1.get(j);
			    						if (pr.getfunction()==idFun)
			    							count++;
			    					}
			    					
			    					for (int j=0;j<funLoc1.size();j++)
			    					{
			    						Pair pr = funLoc1.get(j);
			    						if(pr.getfunction() == idFun)
			    						{
			    							Vector<Double> mul = UtilizeFunction.multi(getFunction(idFun).getLamda(), 1.0/count);
			    							if(UtilizeFunction.isBig(g_edit.getCap(pr.getnode()), mul))
			    							{
			    								Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(pr.getnode()),mul);
			    								g_edit.setCap(pr.getnode(),c_temp );
			    							}
			    							
			    						}
			    					}
			    				}
			    				
			    			}
			    		}
			    	}
		    		else
		    			processingLst= new ArrayList<>();
		    		if(id_temp.size()>0)
		    		{
		    			for (int j=0;j<id_temp.size();j++)
		    				processingLst.remove(id_temp.get(j));
		    		}
		    		
		    		final long startTime = System.currentTimeMillis();
		    			
		    		switch (i+1) {
					case 1:
						sol = GreedyForward(_d,g_edit);
						//newsol.add(sol);
						break;
					case 2:
						sol = newOptimal(_d,g_edit);
						if(flag==1)
							dem1++;
						if(flag==2)
							dem2++;
						//newsol.add(sol);
						break;
					case 3:
						sol = newHeuristic(_d,g_edit);
						break;
					default:
						break;
					}
		    		_duration = System.currentTimeMillis() - startTime;
		    		runTime.set(i, runTime.get(i)+_duration);
		    		if(fl)
			    	{
		    			if(sol!=null && sol.size()>0)
		    			{
		    				
			    			acceptNoLst.set(i, acceptNoLst.get(i)+1);
			    			avgLenLst.set(i, avgLenLst.get(i)+sol.size());
				    		nOldDemand _old = new nOldDemand(index, now, processTime, _d.getBw(), sol, funLoc);
				    		processingLst.add(_old);
//		    				for (int pId = 0;pId<newsol.size();pId++)
//		    				{
//		    					ArrayList<Integer> path = newsol.get(pId);
//		    					for (int _node=0;_node<path.size()-1;_node++)
//								{
//		    						//double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth()/newsol.size();
//									//g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );	
//		    						if(g.getEdgeWeight(path.get(_node), path.get(_node+1))>0)
//		    							link_load[path.get(_node)-1][path.get(_node+1)-1]+=(1.0/newsol.size())*(_old.GetBandwidth()/g.getEdgeWeight(path.get(_node), path.get(_node+1)));
//		    						
//								}
//		    				}
				    		System.out.print(i+":[");
		    				for (int _node=0;_node<sol.size()-1;_node++)
							{
		    					System.out.print(sol.get(_node)+" ");
	    						//double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth()/newsol.size();
								//g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );	
	    						if(g.getEdgeWeight(sol.get(_node), sol.get(_node+1))>0)
	    							link_load[sol.get(_node)-1][sol.get(_node+1)-1]+=_old.GetBandwidth()/g.getEdgeWeight(sol.get(_node), sol.get(_node+1));
	    						
							}
		    				System.out.print(sol.get(sol.size()-1)+" ");
		    				System.out.print("]");
		    				System.out.println();
		    			}
			    		maxlinkload =maximumLinkLoad.get(i);
			    		for (int i1=0;i1<noVertex;i1++)
							for (int i2=0;i2<noVertex;i2++)
								if(link_load[i1][i2]>maxlinkload)
									maxlinkload = link_load[i1][i2];
			    		System.out.println("max: "+ maxlinkload);
			    		maximumLinkLoad.set(i, maxlinkload);
			    		all4ProcessingLst.set(i, processingLst);
			    	}

		    		
		    		//all4g_edit.set(i, g_edit);
		    	}
		    	index++;
		    	//System.out.println("number of processed flows: "+ index);
		    }
			
			out.write(infile+" "+ NumFlow+" "+ processTime+" "+lamda +" "+ prob+ " ");
			for (int i=0;i<3;i++)
			{
				double blocking= 1-1.0*acceptNoLst.get(i)/dem;
				out.write(blocking+" "+ avgLenLst.get(i)/acceptNoLst.get(i)+ " " + maximumLinkLoad.get(i)+ " "+runTime.get(i)/NumFlow+" ");
				if(i==1)
					out.write(dem1*1.0/NumFlow+" " +1.0*dem2/NumFlow+" ");
			}
			out.newLine();
//		    out.write("Accept number:"+ 1.0*acceptNo/dem);
//		    out.newLine();
//		    out.write("Average Length Path:"+ lengthAverage/acceptNo);
//		    out.newLine();
		    }
			catch ( IOException e1 ) {
				e1.printStackTrace();
				} finally {
					if ( out != null )
						try {
							out.close();
							} catch (IOException e) {
								e.printStackTrace();}
					}    
			try {
		  		out.close();
		  		} catch (IOException e2) {
		  			e2.printStackTrace();
		  			}
		
	    
		return true;
	
    }
	public static boolean simulation(double lamda,double processTime,String fileName,int NumFlow, String infile)
	{
		int demAlgNew = 0;
		int dem1=0;
		int dem2=0;
		noFunction=10;
		double arrivalTime=0.0;
		double now=0.0;
		int index=0;
		link_load= new double[noVertex][noVertex];
		//int id_temp=-1;
		ArrayList<Integer> id_temp = new ArrayList<>();
		ArrayList<Integer> sol=new ArrayList<>();
		//ArrayList<ArrayList<Integer>> newsol=new ArrayList<>();
		ArrayList<nOldDemand> processingLst = new ArrayList<>();
		
		//ArrayList<ArrayList<nOldDemand>> all4ProcessingLst = new ArrayList<ArrayList<nOldDemand>>();
		
		
		ArrayList<Integer> acceptNoLst = new ArrayList<Integer>();
		ArrayList<Double> avgLenLst = new ArrayList<Double>();
		ArrayList<Double> maximumLinkLoad = new ArrayList<>();
		ArrayList<Double> runTime = new ArrayList<>();
		
		//random topology -> nGraph 
		
	    
		//random 10000 flows
	    int dem=NumFlow;
	    int idFlow=0;
	    Queue<Double> evenLst =  new LinkedList<Double>();
	    g_edit= new nGraph(g.cap, g.w);
	    for (int i=0;i<1;i++)
	    {
	    	//all4g_edit.add(g_edit);
	    	acceptNoLst.add(0);
	    	avgLenLst.add(0.0);
	    	//all4ProcessingLst.add(null);
	    	maximumLinkLoad.add(0.0);
	    	runTime.add(0.0);
	    }
	    while (idFlow<dem)
	    {
	    	//random flow 1 ()
	    	arrivalTime = now + nextTime(lamda);
	    	evenLst.add(arrivalTime);

	    	now = arrivalTime;
	    	idFlow++;
	    }
	    try {
			File file = new File(fileName);
			out = new BufferedWriter(new FileWriter(file,true));
			while (!evenLst.isEmpty())
		    {
				
		    	now=evenLst.poll();//xet thoi diem den cua package;
		    	nDemand _d = new nDemand(idFlow, g.getV(), functionArr,5,now,processTime);
		    	System.out.println("Flow: "+ index+":"+_d.getSrc()+","+_d.getDest() );
		    	for(int i=0;i<1;i++)
		    	{
		    		fl = false;
		    		//newsol = new ArrayList<>();
		    		sol= new ArrayList<>();
		    		id_temp= new ArrayList<>();
		    		//g_edit = all4g_edit.get(i);
		    		g_edit= new nGraph(g.cap, g.w);
		    		for (int i1=0;i1<noVertex;i1++)
						for (int i2=0;i2<noVertex;i2++)
							link_load[i1][i2]=0.0000000000000;
		    		if(processingLst!=null && processingLst.size()>0)
			    	{
			    		for(nOldDemand _old: processingLst)
			    		{
			    			
			    			if(now > (_old.GetArrivalTime()+_old.GetProcessTime()))
			    			{
			    				id_temp.add(processingLst.indexOf(_old));
			    				//Update lai do thi
//			    				ArrayList<ArrayList<Integer>> _path= _old.Get_path();
//			    				for (int pId = 0;pId<_path.size();pId++)
//			    				{
//			    					ArrayList<Integer> path = _path.get(pId);
//			    					for (int _node=0;_node<path.size()-1;_node++)
//									{
//										double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))+_old.GetBandwidth()/_path.size();
//										g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
//										//link_load[path.get(_node)-1][path.get(_node+1)-1]-=(_old.GetBandwidth()/_path.size())/g.getEdgeWeight(path.get(_node), path.get(_node+1));
//									}
//			    				}
//			    				ArrayList<Pair> funLoc = _old.Get_funLoc();
//			    				for(Pair _pair: funLoc)
//			    				{
//			    					int f_id= _pair.getfunction();
//			    					Vector<Double> c_temp= UtilizeFunction.add(g_edit.getCap(_pair.getnode()),getFunction(f_id).getLamda());
//									g_edit.setCap(_pair.getnode(),c_temp );
//			    				}						
			    			}
			    			else
			    			{
			    				//ArrayList<ArrayList<Integer>> _path= _old.Get_path();
//			    				for (int pId = 0;pId<_path.size();pId++)
//			    				{
//			    					ArrayList<Integer> path = _path.get(pId);
//			    					for (int _node=0;_node<path.size()-1;_node++)
//									{
//			    						if(path.get(_node)!=path.get(_node+1))
//			    						{
//			    							double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth()/_path.size();
//											g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
//				    						if(g.getEdgeWeight(path.get(_node), path.get(_node+1))>0)
//				    							link_load[path.get(_node)-1][path.get(_node+1)-1]+=(1.0/_path.size())*(_old.GetBandwidth()/g.getEdgeWeight(path.get(_node), path.get(_node+1)));
//				    						
//			    						}
//			    						
//									}
//			    				}
			    				ArrayList<Integer> path= _old.Get_path();
			    				
			    					for (int _node=0;_node<path.size()-1;_node++)
									{
			    						if(path.get(_node)!=path.get(_node+1))
			    						{
			    							double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth();
											g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );
				    						if(g.getEdgeWeight(path.get(_node), path.get(_node+1))>0)
				    							link_load[path.get(_node)-1][path.get(_node+1)-1]+=_old.GetBandwidth()/g.getEdgeWeight(path.get(_node), path.get(_node+1));
				    						
			    						}
			    						
									}
			    				ArrayList<Pair> funLoc1 = _old.Get_funLoc();
//			    				for(Pair _pair: funLoc)
//			    				{
//			    					int f_id= _pair.getfunction();
//			    					Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(_pair.getnode()),getFunction(f_id).getLamda());
//									g_edit.setCap(_pair.getnode(),c_temp );
//			    				}
			    				
			    				for (int iF=0;iF<_d.getFunctions().size();iF++)
			    				{
			    					int idFun= _d.getFunctions().get(iF);
			    					int count =0;
			    					for (int j=0;j<funLoc1.size();j++)
			    					{
			    						Pair pr = funLoc1.get(j);
			    						if (pr.getfunction()==idFun)
			    							count++;
			    					}
			    					
			    					for (int j=0;j<funLoc1.size();j++)
			    					{
			    						Pair pr = funLoc1.get(j);
			    						if(pr.getfunction() == idFun)
			    						{
			    							Vector<Double> mul = UtilizeFunction.multi(getFunction(idFun).getLamda(), 1.0/count);
			    							if(UtilizeFunction.isBig(g_edit.getCap(pr.getnode()), mul))
			    							{
			    								Vector<Double> c_temp= UtilizeFunction.minus(g_edit.getCap(pr.getnode()),mul);
			    								g_edit.setCap(pr.getnode(),c_temp );
			    							}
			    							
			    						}
			    					}
			    				}
			    				
			    			}
			    		}
			    	}
		    		else
		    			processingLst= new ArrayList<>();
		    		if(id_temp.size()>0)
		    		{
		    			for (int j=0;j<id_temp.size();j++)
		    				processingLst.remove(id_temp.get(j));
		    		}
		    		
		    		final long startTime = System.currentTimeMillis();
		    		//sol = GreedyOptimal(_d,g_edit);
		    		sol = GreedyForward(_d, g_edit);
					
		    		_duration = System.currentTimeMillis() - startTime;
		    		runTime.set(i, runTime.get(i)+_duration);
		    		if(fl)
			    	{
		    			if(sol!=null && sol.size()>0)
		    			{
		    				
			    			acceptNoLst.set(i, acceptNoLst.get(i)+1);
			    			avgLenLst.set(i, avgLenLst.get(i)+sol.size());
				    		nOldDemand _old = new nOldDemand(index, now, processTime, _d.getBw(), sol, funLoc);
				    		processingLst.add(_old);
//		    				for (int pId = 0;pId<newsol.size();pId++)
//		    				{
//		    					ArrayList<Integer> path = newsol.get(pId);
//		    					for (int _node=0;_node<path.size()-1;_node++)
//								{
//		    						//double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth()/newsol.size();
//									//g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );	
//		    						if(g.getEdgeWeight(path.get(_node), path.get(_node+1))>0)
//		    							link_load[path.get(_node)-1][path.get(_node+1)-1]+=(1.0/newsol.size())*(_old.GetBandwidth()/g.getEdgeWeight(path.get(_node), path.get(_node+1)));
//		    						
//								}
//		    				}
				    		System.out.print(i+":[");
		    				for (int _node=0;_node<sol.size()-1;_node++)
							{
		    					System.out.print(sol.get(_node)+" ");
	    						//double w_temp= g_edit.getEdgeWeight(path.get(_node), path.get(_node+1))-_old.GetBandwidth()/newsol.size();
								//g_edit.setEdgeWeight(path.get(_node), path.get(_node+1),w_temp );	
	    						if(g.getEdgeWeight(sol.get(_node), sol.get(_node+1))>0)
	    							link_load[sol.get(_node)-1][sol.get(_node+1)-1]+=_old.GetBandwidth()/g.getEdgeWeight(sol.get(_node), sol.get(_node+1));
	    						
							}
		    				System.out.print(sol.get(sol.size()-1)+" ");
		    				System.out.print("]");
		    				System.out.println();
		    			}
			    		maxlinkload =maximumLinkLoad.get(i);
			    		for (int i1=0;i1<noVertex;i1++)
							for (int i2=0;i2<noVertex;i2++)
								if(link_load[i1][i2]>maxlinkload)
									maxlinkload = link_load[i1][i2];
			    		System.out.println("max: "+ maxlinkload);
			    		maximumLinkLoad.set(i, maxlinkload);
			    		//all4ProcessingLst.set(i, processingLst);
			    	}

		    		
		    		//all4g_edit.set(i, g_edit);
		    	}
		    	index++;
		    	//System.out.println("number of processed flows: "+ index);
		    }
			
			out.write(infile+" "+ NumFlow+" "+ processTime+" "+lamda +" "+ prob+ " ");
			for (int i=0;i<1;i++)
			{
				double blocking= 1-1.0*acceptNoLst.get(i)/dem;
				out.write(blocking+" "+ avgLenLst.get(i)/acceptNoLst.get(i)+ " " + maximumLinkLoad.get(i)+ " "+runTime.get(i)/NumFlow+" ");
				//out.write(dem1*1.0/NumFlow+" " +1.0*dem2/NumFlow);
			}
			out.newLine();
//		    out.write("Accept number:"+ 1.0*acceptNo/dem);
//		    out.newLine();
//		    out.write("Average Length Path:"+ lengthAverage/acceptNo);
//		    out.newLine();
		    }
			catch ( IOException e1 ) {
				e1.printStackTrace();
				} finally {
					if ( out != null )
						try {
							out.close();
							} catch (IOException e) {
								e.printStackTrace();}
					}    
			try {
		  		out.close();
		  		} catch (IOException e2) {
		  			e2.printStackTrace();
		  			}
		
	    
		return true;
	}
	public static void readDataReal(String filePara)
	{
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));
			String strLine = in.readLine();
			int dataNo= Integer.parseInt(strLine);
			dataReal = new Vector<Vector<Double>>();
			for (int i=0;i<dataNo;i++)
			{
				//doc n hang tiep theo
				strLine = in.readLine();
				String[] _line = strLine.split(" ");
				Vector<Double> t= new Vector<Double>(4);
				for (int j=0;j <4;j++)
				{
					t.addElement(Double.parseDouble(_line[j]));
				}
				dataReal.addElement(t);
			}
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	public static void maina(String[] args)
	{
		//BufferedWriter out1 = null;
		
		noFunction=10;
		int lamda=Integer.parseInt(args[0]);
		double processingTime = Double.parseDouble(args[1]);
		String fileName = args[2];
		int numFlow=Integer.parseInt(args[3]);
		String dirPath = args[4];
		File dir = new File(dirPath);
		String[] extensions = new String[] { "txt" };
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

		
			for (File file : files) {
				try {
					System.out.println("file: " + file.getCanonicalPath());
					System.out.println("lambda: "+ lamda);
					ReadData(file.getPath());
					//simulation(lamda,processingTime,fileName,numFlow,file.getPath());
					
					//simulation_all(lamda,processingTime,fileName,numFlow,file.getPath());
					
					simulation_optimal_new(lamda,processingTime,fileName,numFlow,file.getPath());
					
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
		

		
		
	}
	public static void mainDD(String[] args)
	{
		noFunction=10;
		ReadData("input5.txt");
		SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                drawGraph.createAndShowGui(g);
            }
        });
		
	}
	

	public static void mainI(String[] args) {
	UtilizeFunction.randomRoutingProblem("lib\\inputRouting.txt");
}
	public static void maini(String[] args)
	{
		int noVer = Integer.parseInt(args[0]);
		int noEd = Integer.parseInt(args[1]);
		int NoFun = Integer.parseInt(args[2]);
		double p = 0.02;
		for (int i=0;i<30;i++)
		{
			int j=i+1;
			String fileName = "input"+ j+".txt";
		
			WriteData(noVer,noEd, NoFun,p,fileName);
			p+=0.002;
		}
		
	}
	public static void main(String[] args)
	{
		int noVer = 0;
		int noEd = Integer.parseInt(args[0]);
		int NoFun = Integer.parseInt(args[1]);
		double p = 0.037;
		WriteData(120,noEd, NoFun,p,"in4.txt");
		WriteData(150,noEd, NoFun,p,"in5.txt");
		WriteData(250,noEd, NoFun,p,"in6.txt");
//		for (int i=0;i<3;i++)
//		{
//			noVer=100+i*100;
//			int j=i+1;
//			String fileName = "in"+ j+".txt";
//		
//			WriteData(noVer,noEd, NoFun,p,fileName);
//		}
		
	}


}
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;

import org.apache.commons.io.FileUtils;

import gurobi.*;

import org.jgrapht.alg.*;
import org.jgrapht.graph.*;
import org.jgrapht.traverse.*;
import org.omg.DynamicAny._DynEnumStub;
public class onlineCase {
	static BufferedWriter out;
	static BufferedReader in;
	static int c,noVertex,noFunction,noNewDemand,z,E,_no,noOldDemand;
	static int limitedNo;
	static double alpha, beta,gama,theta;
	static ExGraph g;
	static ExGraph g_edit;
	static ArrayList<Function> functionArr;
	static ArrayList<newDemand> newDemandArray;
	static ArrayList<oldDemand> oldDemandArray;
	static double r_min;
	static int a_min;
	//static GRBVar[][][][] x;// noNewDemand=1->noNewDemand, a=1->noVertex; b=0->noFunction; c=1->position (noVertex*noFunction)
	static GRBVar[][][]x1;//function on node
	static GRBVar[][][] y;//link 
	static GRBVar[][][] y1;//ancestor node
	static GRBVar[]z1;
	static GRBVar r;
	static long _duration=0;
	static double value_final=0.0;
	static double value_penalty=0.0;
	static double value_bandwidth=0.0;
	static double ultilize_resource =0.0;
	static double currentTime=0.0;
	static double averageDelay=0.0;
	static double acceptRate =0.0;
	static Double[] zero ={0.0,0.0,0.0};
	static int prevNode;
	static double numberofCore=0;
	static double numberofEdge=0;
	static double numberofMidle=0;
	static int noMigrate;
	static double hsoA;
	static ArrayList<ArrayList<Integer>> solution_node;
	static ArrayList<ArrayList<Integer>> solution_func;
	static ArrayList<Integer> solution_id;
	
	public static Vector<Double> getLamdaF(int id)
	{
		if(id==0) return new Vector<Double>(Arrays.asList(zero));
		for(int i=0;i<noFunction;i++)
			if (functionArr.get(i).getId() ==id)
				return functionArr.get(i).getLamda();
		return null;
	}
	public static Function getFunction(int id)
	{
		if(id==0) return null;
		for(int i=0;i<noFunction;i++)
			if (functionArr.get(i).getId() ==id)
				return functionArr.get(i);
		return null;
	}
	
	public static double getBwService(int id)
	{
		if(id==0) return 0;
		for(int i=0;i<noFunction;i++)
			if(newDemandArray.get(i).getId()==id)
				return newDemandArray.get(i).getBw();
		return -1;
	}
	public static double getRateService(int id)
	{
		if(id==0) return 0;
		for(int i=0;i<noFunction;i++)
			if(newDemandArray.get(i).getId()==id)
				return newDemandArray.get(i).getRate();
		return -1;
	}
	public static newDemand getDemand(int id)
	{
		for (int i=0;i<noNewDemand;i++)
			if(newDemandArray.get(i).getId()==id)
				return newDemandArray.get(i);
		return null;
	}
	
	public static oldDemand getOldDemand(int id)
	{
		for (int i=0;i<noOldDemand;i++)
			if(oldDemandArray.get(i).GetID()==id)
				return oldDemandArray.get(i);
		return null;
	}
	public static double migrateCost(oldDemand d)
	{
		double cost_temp=0.0;
		double migrate_node=0.00045;
		double migrate_link=0.00045;
		for (int i=0;i<d.GetSetFunc().size();i++)
			cost_temp+=migrate_node * UtilizeFunction.value(getFunction(d.GetSetFunc().get(i)).getLamda());
		ArrayList<Integer> v_sl = d.Get_v_sol();
		for(int j=0;j<v_sl.size()-1;j++)
			for(int k=j+1;k<v_sl.size();k++)
			{
				cost_temp+=g.getEdgeWeight(v_sl.get(j), v_sl.get(k))*migrate_link;
			}
		cost_temp= cost_temp *(d.GetProcessTime()-currentTime+d.GetArrivalTime());
		return cost_temp;
		
	}
	public static boolean IsCapacity()
	{
		Vector<Double> resourceRequirement = new Vector<Double>(Arrays.asList(zero));
		Vector<Double> resourceCapacity = new Vector<Double>(Arrays.asList(zero));
		for (int i=0;i<noNewDemand;i++)
		{
			ArrayList<Integer> fArr = newDemandArray.get(i).getFunctions();
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
		functionArr = new ArrayList<Function>();
		newDemandArray = new ArrayList<newDemand>();
		oldDemandArray = new ArrayList<oldDemand>();
        try {
			in = new BufferedReader(new FileReader(file));
			//First line -> set of parameters
			String[] tempLine=in.readLine().split(" ");
			noVertex= Integer.parseInt(tempLine[0]);
			noFunction= Integer.parseInt(tempLine[1]);
			noNewDemand= Integer.parseInt(tempLine[2]);
			noOldDemand = Integer.parseInt(tempLine[3]);
			
			//second line -> set of Functions
			tempLine = in.readLine().split(";");
			for(int i = 0;i<noFunction;i++)
			{ 
				Vector<Double> lamda= new Vector<>(3);
				for (int j=0;j<3;j++)
		        	lamda.addElement(Double.parseDouble(tempLine[i].split(" ")[j]));
				functionArr.add(new Function(i+1,lamda));
			}
			
			//noOldDemand lines -> set of oldDemands
			int id, src,des;
			double arriveT,processT,bandwidth,rate;
			String[] tempSubLine;
			ArrayList<Integer> f;
			ArrayList<Integer> v_sol;
			ArrayList<Integer> f_sol;
			for(int i=0;i<noOldDemand;i++)
			{
				tempLine = in.readLine().split(";");
				id =Integer.parseInt(tempLine[0]);
				src =Integer.parseInt(tempLine[1]);
				des =Integer.parseInt(tempLine[2]);
				arriveT =Double.parseDouble(tempLine[3]);
				processT =Double.parseDouble(tempLine[4]);
				bandwidth =Double.parseDouble(tempLine[5]);
				rate =Double.parseDouble(tempLine[6]);
				tempSubLine = tempLine[7].split(" ");
				//set of Function
				f= new ArrayList<Integer>();
				for (int j=0;j<tempSubLine.length;j++)
				{
					f.add(Integer.parseInt(tempSubLine[j]));
				}
				tempSubLine = tempLine[8].split(" ");
				//set of v_sol
				v_sol= new ArrayList<Integer>();
				for (int j=0;j<tempSubLine.length;j++)
				{
					v_sol.add(Integer.parseInt(tempSubLine[j]));
				}
				tempSubLine = tempLine[9].split(" ");
				//set of f_sol
				f_sol= new ArrayList<Integer>();
				for (int j=0;j<tempSubLine.length;j++)
				{
					f_sol.add(Integer.parseInt(tempSubLine[j]));
				}
				oldDemandArray.add(new oldDemand(id, src, des, arriveT, processT, bandwidth, rate, f, v_sol, f_sol));
			}
			
			//set of new demands
			for (int i=0;i<noNewDemand;i++)
			{
				tempLine = in.readLine().split(";");
				id =Integer.parseInt(tempLine[0]);
				src =Integer.parseInt(tempLine[1]);
				des =Integer.parseInt(tempLine[2]);
				arriveT =Double.parseDouble(tempLine[3]);
				currentTime = arriveT;
				processT =Double.parseDouble(tempLine[4]);
				bandwidth =Double.parseDouble(tempLine[5]);
				rate =Double.parseDouble(tempLine[6]);
				tempSubLine = tempLine[7].split(" ");
				//set of Function
				f= new ArrayList<Integer>();
				for (int j=0;j<tempSubLine.length;j++)
				{
					f.add(Integer.parseInt(tempSubLine[j]));
				}
				
				newDemandArray.add(new newDemand(id, src, des, arriveT, processT, bandwidth, rate, f));
			}
			
			double price_bandwidth = Double.parseDouble(in.readLine());
			//luu vao mang noVertex+1 chieu
			
			Vector<Vector<Double>> cap = new Vector<Vector<Double>>(noVertex+1);
			Vector<Double> pricePerNode = new Vector<Double>(noVertex+1);
			ArrayList<List<Double>> w = new ArrayList<List<Double>>();			
			
			// virtual network
			//Double[] zero ={0.0,0.0,0.0};
			//cap.add(new Vector<Double>(Arrays.asList(zero)));
			//pricePerNode.add(0.0);
			for (int i=0;i <noVertex;i++)
			{
				tempLine = in.readLine().split(";");
				Vector<Double> t= new Vector<>(3);
	   	        for (int j=0;j<3;j++)
	   	        	t.addElement(Double.parseDouble(tempLine[0].split(" ")[j]));
	   	        cap.add(t);
	   	        pricePerNode.add(Double.parseDouble(tempLine[1]));
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
			g= new ExGraph(cap,pricePerNode,w,price_bandwidth);
			g_edit =new ExGraph(cap,pricePerNode,w,price_bandwidth);
			
			if (noVertex*noFunction <  noFunction+4)
				_no=5;
			else
				_no = 5;
			//x= new GRBVar[noNewDemand+noOldDemand][noVertex][noFunction+1][_no]; 
			x1= new GRBVar[noNewDemand][noFunction][noVertex];//binary
			y= new GRBVar[noNewDemand][noVertex][noVertex];//float (0,1)
			z1= new GRBVar[noNewDemand];//float (0,1)
			
			
            // Always close files.
            in.close();  
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	//heuristic
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
	public static boolean migration (String outFile)
	{
		ArrayList<Integer> f_sol,v_sol;
		ArrayList<Integer> noMutual = new ArrayList<Integer>();
		for (int i=0;i<noVertex;i++)
			noMutual.add(0);
		for(int i=0;i<oldDemandArray.size();i++)
		{
			//double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			v_sol=_demand.Get_v_sol();
			//tim node nao dang host nhieu function nhat.
			//node do pai host function khong lien tiep nhau, tot nhat la ngay vi tri truoc va sau no la mot function khac hoac la 0
			for(int j=0;j<v_sol.size();j++)
			{
				int temp = noMutual.get(v_sol.get(j)-1);
				noMutual.set(v_sol.get(j)-1, temp+1);
			}
		}
		int max = noMutual.get(0);
		int v_busy =1;
		for (int i = 0; i < noVertex; i++) {
		    int value = noMutual.get(i);
		    if (value > max) {
		        max = value;
		        v_busy = i+1;
		    }
		}
		//kiem tra tung old demand, neu function nao do cach xa qua, tuc la function truoc do hoac sau do qua lon.
		for(int i=0;i<oldDemandArray.size();i++)
		{
			//double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
		}
		
		
		return true;
		
	}
	
	public static ArrayList<Integer> ShortestPath(int src, int dest, ExGraph _g,double maxBw,ArrayList<ArrayList<Integer>> mark,boolean flag)
	{
		ArrayList<Integer> _shortestPath = new ArrayList<Integer>();
		SimpleWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
        
        		
		for (int j=0;j<_g.getV();j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        DefaultWeightedEdge[] e= new DefaultWeightedEdge[(_g.getV()*(_g.getV()-1))/2];
        int id=0;        
        for (int j=0;j<_g.getV()-1;j++)
        {	        	
        	for(int k=j+1;k<_g.getV();k++)
        	{
        		if(_g.getEdgeWeight(j+1, k+1)>=maxBw)
        		{
	        		e[id]=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e[id], _g.getEdgeWeight((j+1), (k+1)));
	        		id++;
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
			for(int _i:_shortestPath)
				{
					System.out.print(_i+",");
				}						
		}
		else
		{
			System.out.print("khong tim duoc duong di giua"+src+"va"+ dest);
			return null;
			
		}
        
        
		return _shortestPath;
	}
	
	public static ArrayList<Integer> ShortestPath(int src, int dest, ExGraph _g,double maxBw)
	{
		ArrayList<Integer> _shortestPath = new ArrayList<Integer>();
		SimpleWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
        
		for (int j=0;j<_g.getV();j++)
        {
        	g_i.addVertex("node"+(j+1));
        }
        DefaultWeightedEdge[] e= new DefaultWeightedEdge[(_g.getV()*(_g.getV()-1))/2];
        int id=0;        
        for (int j=0;j<_g.getV()-1;j++)
        {	        	
        	for(int k=j+1;k<_g.getV();k++)
        	{
        		if(_g.getEdgeWeight(j+1, k+1)>=maxBw)
        		{
	        		e[id]=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e[id], _g.getEdgeWeight((j+1), (k+1)));
	        		id++;
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
			for(int _i:_shortestPath)
				{
					System.out.print(_i+",");
				}						
		}
		else
		{
			System.out.print("khong tim duoc duong di giua"+src+"va"+ dest);
			return null;
			
		}
        
        
		return _shortestPath;
	}
	public static int indexFunction(ArrayList<Integer> arr,int Func)//id of function in array
	{		
		for(int x=0;x<arr.size();x++)
		{
			if(arr.get(x)==Func)
			{
				return x;
			}
		}
		return -1;
	}
	public static int nextFunction(ArrayList<Integer> arr,int Func)//id of function in array
	{		
		for(int x=0;x<arr.size()-1;x++)
		{
			if(arr.get(x)==Func)
			{
				int x_temp= x+1;
				while(x_temp<arr.size())
				{
					if(arr.get(x_temp)!=0)
						return x_temp;
					else
						x_temp++;
				}
			}
		}
		return -1;
	}
	public static int prevFunction(ArrayList<Integer> arr,int Func)//id of function in array
	{
		
		for(int x=1;x<arr.size();x++)
		{
			if(arr.get(x)==Func)
			{
				int x_temp= x-1;
				while(x_temp>=0)
				{
					if(arr.get(x_temp)!=0)
						return x_temp;
					else
						x_temp--;
				}
			}
		}
		return -1;
	}
	public static boolean putFunction(ArrayList<Integer> funcSet, ArrayList<Integer> path, ArrayList<Integer> _f , ArrayList<Integer> _v)
	{
		// Check resource		
		//Graph g_tam =new Graph(g_edit.cap,g_edit.pricePernode,g_edit.w,g_edit.getPriceBandwidth());
		Vector<Double> sum_avail= new Vector<Double>(Arrays.asList(0.0,0.0,0.0));
		Vector<Double> sum_req=new Vector<Double>(Arrays.asList(0.0,0.0,0.0));
		boolean fini=false;
		for (int id=0;id<funcSet.size();id++)
		{
			sum_req= UtilizeFunction.add(getLamdaF(funcSet.get(id)),sum_req);
		}
		for (int id=0;id<path.size();id++)
		{
			sum_avail = UtilizeFunction.add(g_edit.getCap(path.get(id)), sum_avail);
		}
		if(UtilizeFunction.isBig(sum_req, sum_avail))
		{
			return true;
		}
		else
		{
			int temp=0;
			int dem=0;
			int pre=-1;
			int max = (int) funcSet.size()/path.size()+1;
			for(int id=0;id<funcSet.size();id++)
			{
				fini=false;				
				if(dem==max)
				{
					//tiep cho node tiep theo
					dem=0;
					temp++;
				}
				for(int id1=temp;id1<path.size();id1++)
				{
					
					temp=id1;
					if(UtilizeFunction.isBig(g_edit.getCap(path.get(id1)),getLamdaF(funcSet.get(id))))
					{
						if(pre==path.get(id1))
							dem++;
						_f.add(funcSet.get(id));
						_v.add(path.get(id1));
						g_edit.setCap(path.get(id1), UtilizeFunction.minus(g_edit.getCap(path.get(id1)),getLamdaF(funcSet.get(id))));
						pre=path.get(id1);
						fini=true;
						break;
					}
					else
					{
						temp++;
						if(id==0)
						{
							_f.add(0);
							_v.add(path.get(id1));
						}
						
					}					
				}
				if(!fini)
				{
					return true;
				}
				
			}
			if(temp<path.size()-1)
			{
				for(int id=temp+1;id<path.size();id++)
				{
					_v.add(path.get(id));
					_f.add(0);
				}
			}
			
		}
		//Put function one by one
		return false;
		
	}
	public static boolean RBP_edit(String outFile)
	{
		g_edit =new ExGraph(g.cap,g.pricePernode,g.w,g.getPriceBandwidth());
		final long startTime = System.currentTimeMillis();
		solution_func=new ArrayList<ArrayList<Integer>>();
		solution_node=new ArrayList<ArrayList<Integer>>();
		solution_id = new ArrayList<Integer>();
		int acceptDemandNo=noNewDemand;
		value_final=0;
		value_bandwidth =0;
		ultilize_resource =0;
		_duration=0;
		ArrayList<Integer> f_sol,v_sol;
		
		ArrayList<Double> red_oldDemand = new ArrayList<Double>();
		ArrayList<Double> cost_oldDemand = new ArrayList<Double>();
		ArrayList<Integer> id_oldDemand = new ArrayList<Integer>();
		double sum_link=0.0;
		double sum_node = 0.0;
		v_solution = new ArrayList<List<Integer>>();
		f_solution=new ArrayList<List<Integer>>();
		for(int i=0;i<noVertex;i++)
			for(int j=0;j<noVertex;j++)
				sum_link+=g.getEdgeWeight(i+1, j+1);
		for (int i=0;i<noVertex;i++)
			sum_node+=UtilizeFunction.value(g.getCap(i+1));
		
		// Depending the migration probability
		for(int i=0;i<oldDemandArray.size();i++)
		{
			double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			id_oldDemand.add(_demand.GetID());
			f_sol=_demand.Get_f_sol();
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					co+= g.getPriceNode(v_sol.get(j))* _demand.GetRate();
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					co+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth();
				}
			cost_oldDemand.add(co);
			f_sol=null;
			v_sol=null;
		}
		for(int i=0;i<oldDemandArray.size();i++)
		{
			double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					co+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))/sum_link;
					co+=(UtilizeFunction.value(g.getCap(v_sol.get(j))) + UtilizeFunction.value(g.getCap(v_sol.get(k))))/(2*sum_node);
				}
			red_oldDemand.add(co);
			v_sol=null;
		}
		int[] rank_d= new int[oldDemandArray.size()];
		for (int i=0;i<oldDemandArray.size();i++)
			rank_d[i]= i;
		
		ArrayList<newDemand> arrDemand = new ArrayList<newDemand>();
		
		int dem=0;
		ArrayList<Integer> setOfOldDemand = new ArrayList<>();
		while(dem<noMigrate)
		{

			oldDemand _old= oldDemandArray.get(rank_d[dem]);
			//value_final+=migrateCost(_old);
			arrDemand.add(new newDemand(_old.GetID(), _old.GetSrc(), _old.GetDest(), _old.GetArrivalTime(), _old.GetProcessTime(), _old.GetBandwidth(), _old.GetRate(), _old.GetSetFunc()));
			f_sol=_old.Get_f_sol();
			v_sol=_old.Get_v_sol();
			dem++;
			f_sol=null;
			v_sol=null;
			setOfOldDemand.add(_old.GetID());
		}
		for (int i=dem;i<oldDemandArray.size();i++)
		{
			oldDemand _old= oldDemandArray.get(rank_d[i]);
			f_sol=_old.Get_f_sol();
			v_sol=_old.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					value_final+=g.getPriceNode(v_sol.get(j));
					
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
				}
		}
		for(int i=0;i<newDemandArray.size();i++)
			arrDemand.add(newDemandArray.get(i));
		
		boolean reject=false;
		System.gc();

		
		
		//LinkedList<Integer> visited;
		newDemand _newDemand;
		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
		boolean _unsatify=false;
		boolean flag_shortestPath=false;
		for(int i=0;i<arrDemand.size();i++)
		{
			ArrayList<ArrayList<Integer>> _mark = new ArrayList<ArrayList<Integer>>();
			flag_shortestPath=false;
			
			
			reject=false;
			_newDemand = arrDemand.get(i);
			if(setOfOldDemand.size()>0 && setOfOldDemand.contains(_newDemand.getId()))
			{
				oldDemand _old= getOldDemand(_newDemand.getId());
				f_sol=_old.Get_f_sol();
				v_sol=_old.Get_v_sol();
				for(int j=0;j<f_sol.size();j++)
				{
					if(f_sol.get(j)>0)
					{
						g_edit.addCap(v_sol.get(j), getLamdaF(f_sol.get(j)));
					}
				}
				for(int j=0;j<v_sol.size()-1;j++)
					for(int k=j+1;k<v_sol.size();k++)
					{
						g_edit.addEdgeWeight(v_sol.get(j), v_sol.get(k), _old.GetBandwidth());
					}
			}
			
			int src=_newDemand.getSrc();
			int dest= _newDemand.getDest();
			while (!reject)
			{
				
				f_sol = new ArrayList<Integer>();
				v_sol = new ArrayList<Integer>();
				ExGraph g_tam =new ExGraph(g_edit.cap,g_edit.pricePernode,g_edit.w,g_edit.getPriceBandwidth());
				ArrayList<Integer> path=ShortestPath(src, dest, g_edit, _newDemand.getBw(), _mark,flag_shortestPath);
				if(path==null)
				{					
					reject=true;
					g_edit =new ExGraph(g_tam.cap,g_tam.pricePernode,g_tam.w,g_tam.getPriceBandwidth());
				}
				else
				{
					_unsatify = putFunction(_newDemand.getFunctions(),path, f_sol, v_sol);
					if(_unsatify)
					{
						flag_shortestPath=true;
						_mark.add(path);
						g_edit =new ExGraph(g_tam.cap,g_tam.pricePernode,g_tam.w,g_tam.getPriceBandwidth());
						continue;
					}
					else
					{
						solution_node.add(v_sol);
						solution_func.add(f_sol);
						solution_id.add(_newDemand.getId());						
						break;
					}
					
				}
			}
			v_sol=null;
			f_sol=null;
			
		}
		
		//Compute the average delay for all new demand
		averageDelay=0.0;
		acceptRate =0.0;
		acceptDemandNo=0;
		int delay=0;
		for(int id=0;id<solution_id.size();id++)
		{
			int index= solution_id.get(id);
			
			if(setOfOldDemand.size()==0 || !setOfOldDemand.contains(index))
			{
				v_sol= new ArrayList<Integer>();
				v_sol = solution_node.get(id);
				f_sol= new ArrayList<>();
				f_sol = solution_func.get(id);
				
				int preN=-1;
				for (int id1=0;id1<v_sol.size();id1++)
				{
					if(v_sol.get(id1)!=preN)
					{
						delay++;
						preN=v_sol.get(id1);
					}
				}
				for(int j=0;j<f_sol.size();j++)
				{
					if(f_sol.get(j)>0)
					{
						//update capacity for node
						value_final+=g.getPriceNode(v_sol.get(j));
						
					}
				}
				for(int j=0;j<v_sol.size()-1;j++)
					for(int k=j+1;k<v_sol.size();k++)
					{
						value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
					}
				acceptDemandNo++;
			}
			else
			{
				
				if(setOfOldDemand.size()>0)
				{
					oldDemand _oldD= getOldDemand(index);
					ArrayList<Integer> v_Oldsol= new ArrayList<Integer>();
					v_Oldsol = _oldD.Get_v_sol();
					ArrayList<Integer> f_Oldsol= new ArrayList<>();
					f_Oldsol = _oldD.Get_f_sol();
					for(int j=0;j<f_Oldsol.size();j++)
					{
						if(f_Oldsol.get(j)>0)
						{
							//update capacity for node
							value_final-=g.getPriceNode(v_Oldsol.get(j));
							
						}
					}
					for(int j=0;j<v_Oldsol.size()-1;j++)
						for(int k=j+1;k<v_Oldsol.size();k++)
						{
							value_final-=g.getEdgeWeight(v_Oldsol.get(j), v_Oldsol.get(k))*g.getPriceBandwidth()*0.001;
						}
					//tinh gia tri cho old demand 
					v_sol= new ArrayList<Integer>();
					v_sol = solution_node.get(id);
					f_sol= new ArrayList<>();
					f_sol = solution_func.get(id);
					for(int j=0;j<f_sol.size();j++)
					{
						if(f_sol.get(j)>0)
						{
							value_final+=g.getPriceNode(v_sol.get(j));
							
						}
					}
					for(int j=0;j<v_sol.size()-1;j++)
						for(int k=j+1;k<v_sol.size();k++)
						{
							value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
						}
					//Tinh chi phi cho viec migrate. = chuyen resource tu a->b (chi phi bandwidth) * tai nguyen can chuyen
					for(int i1=0;i1<f_Oldsol.size();i1++)
					{
						if(f_Oldsol.get(i1)>0)
						{
							for(int i2=0;i2<f_sol.size();i2++)
							{
								if(f_sol.get(i2)>0)
								{
									if(v_Oldsol.get(i1)!=v_sol.get(i2))
									{
										ExGraph _g= new ExGraph(g.cap, g.pricePernode, g.w, g.getPriceBandwidth());
										ArrayList<Integer> shortPath= ShortestPath(v_Oldsol.get(i1), v_sol.get(i2), _g, 1.0);
										if(shortPath==null)
										{
											value_final+=noVertex*g.getPriceBandwidth()/5.0;//truong hop lay duong dai nhat
										}
										else
										{
											for (int i3=0;i3<shortPath.size()-1;i3++)
											{
												value_final+=g.getPriceBandwidth()*0.001*g.getEdgeWeight(shortPath.get(i3), shortPath.get(i3+1))/5.0;
											}
										}
										
									}
									break;
								}
							}
							
						}
					}
					
				}
			}
		}

		averageDelay=(delay*1.0)/acceptDemandNo;
		acceptRate = acceptDemandNo*1.0/noNewDemand;
		
		
		_duration = System.currentTimeMillis() - startTime;
		for (int id=0;id<solution_id.size();id++)
		{
			out.write("demand: "+solution_id.get(id)+":");
			for(int id1=0;id1<solution_func.get(id).size();id1++)
				out.write(solution_node.get(id).get(id1)+",");
			out.write(":");
			for(int id1=0;id1<solution_func.get(id).size();id1++)
				out.write(solution_func.get(id).get(id1)+",");
			out.newLine();
		}
		System.out.println(_duration);
		out.write("Average Delay: "+ averageDelay);
		out.newLine();
		out.write("Runtime (mS): "+ _duration);
		out.write("Acceptation rate: "+ acceptRate);
		//out.write("Resource ultilization: "+ ultilize_resource);
		
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
	public static boolean RBP(String outFile)
	{
		g_edit =new ExGraph(g.cap,g.pricePernode,g.w,g.getPriceBandwidth());
		final long startTime = System.currentTimeMillis();
		solution_func=new ArrayList<ArrayList<Integer>>();
		solution_node=new ArrayList<ArrayList<Integer>>();
		solution_id = new ArrayList<Integer>();
		int acceptDemandNo=noNewDemand;
		value_final=0;
		value_bandwidth =0;
		ultilize_resource =0;
		_duration=0;
		ArrayList<Integer> f_sol,v_sol;
		//double sum_link=0.0;
		//double sum_node = 0.0;
//		for(int i=0;i<noVertex;i++)
//			for(int j=0;j<noVertex;j++)
//				sum_link+=g.getEdgeWeight(i+1, j+1);
//		for (int i=0;i<noVertex;i++)
//			sum_node+=UtilizeFunction.value(g.getCap(i+1));
		
		ArrayList<Double> red_oldDemand = new ArrayList<Double>();
		ArrayList<Double> cost_oldDemand = new ArrayList<Double>();
		ArrayList<Integer> id_oldDemand = new ArrayList<Integer>();
		double sum_link=0.0;
		double sum_node = 0.0;
		v_solution = new ArrayList<List<Integer>>();
		f_solution=new ArrayList<List<Integer>>();
		for(int i=0;i<noVertex;i++)
			for(int j=0;j<noVertex;j++)
				sum_link+=g.getEdgeWeight(i+1, j+1);
		for (int i=0;i<noVertex;i++)
			sum_node+=UtilizeFunction.value(g.getCap(i+1));
		
		// Depending the migration probability
		for(int i=0;i<oldDemandArray.size();i++)
		{
			double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			id_oldDemand.add(_demand.GetID());
			f_sol=_demand.Get_f_sol();
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					co+= g.getPriceNode(v_sol.get(j))* _demand.GetRate();
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					co+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth();
				}
			cost_oldDemand.add(co);
			f_sol=null;
			v_sol=null;
		}
		for(int i=0;i<oldDemandArray.size();i++)
		{
			double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					co+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))/sum_link;
					co+=(UtilizeFunction.value(g.getCap(v_sol.get(j))) + UtilizeFunction.value(g.getCap(v_sol.get(k))))/(2*sum_node);
				}
			red_oldDemand.add(co);
			v_sol=null;
		}
		int[] rank_d= new int[oldDemandArray.size()];
		for (int i=0;i<oldDemandArray.size();i++)
			rank_d[i]= i;
		
		ArrayList<newDemand> arrDemand = new ArrayList<newDemand>();
		
		int dem=0;
		ArrayList<Integer> setOfOldDemand = new ArrayList<>();
		while(dem<noMigrate)
		{

			oldDemand _old= oldDemandArray.get(rank_d[dem]);
			//value_final+=migrateCost(_old);
			arrDemand.add(new newDemand(_old.GetID(), _old.GetSrc(), _old.GetDest(), _old.GetArrivalTime(), _old.GetProcessTime(), _old.GetBandwidth(), _old.GetRate(), _old.GetSetFunc()));
			f_sol=_old.Get_f_sol();
			v_sol=_old.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					g_edit.addCap(v_sol.get(j), getLamdaF(f_sol.get(j)));
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					g_edit.addEdgeWeight(v_sol.get(j), v_sol.get(k), _old.GetBandwidth());
				}
			dem++;
			f_sol=null;
			v_sol=null;
			setOfOldDemand.add(_old.GetID());
		}
		for (int i=dem;i<oldDemandArray.size();i++)
		{
			oldDemand _old= oldDemandArray.get(rank_d[i]);
			f_sol=_old.Get_f_sol();
			v_sol=_old.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					value_final+=g.getPriceNode(v_sol.get(j));
					
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
				}
		}
		for(int i=0;i<newDemandArray.size();i++)
			arrDemand.add(newDemandArray.get(i));
		
		boolean reject=false;
		System.gc();

		
		
		//LinkedList<Integer> visited;
		newDemand _newDemand;
		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
		boolean _unsatify=false;
		boolean flag_shortestPath=false;
		for(int i=0;i<arrDemand.size();i++)
		{
			ArrayList<ArrayList<Integer>> _mark = new ArrayList<ArrayList<Integer>>();
			flag_shortestPath=false;
			
			
			reject=false;
			_newDemand = arrDemand.get(i);
			
			int src=_newDemand.getSrc();
			int dest= _newDemand.getDest();
			while (!reject)
			{
				
				f_sol = new ArrayList<Integer>();
				v_sol = new ArrayList<Integer>();
				ExGraph g_tam =new ExGraph(g_edit.cap,g_edit.pricePernode,g_edit.w,g_edit.getPriceBandwidth());
				ArrayList<Integer> path=ShortestPath(src, dest, g_edit, _newDemand.getBw(), _mark,flag_shortestPath);
				if(path==null)
				{					
					reject=true;
					g_edit =new ExGraph(g_tam.cap,g_tam.pricePernode,g_tam.w,g_tam.getPriceBandwidth());
				}
				else
				{
					_unsatify = putFunction(_newDemand.getFunctions(),path, f_sol, v_sol);
					if(_unsatify)
					{
						flag_shortestPath=true;
						_mark.add(path);
						g_edit =new ExGraph(g_tam.cap,g_tam.pricePernode,g_tam.w,g_tam.getPriceBandwidth());
						continue;
					}
					else
					{
						solution_node.add(v_sol);
						solution_func.add(f_sol);
						solution_id.add(_newDemand.getId());						
						break;
					}
					
				}
			}
			v_sol=null;
			f_sol=null;
			
		}
		
		//Compute the average delay for all new demand
		averageDelay=0.0;
		acceptRate =0.0;
		acceptDemandNo=0;
		int delay=0;
		for(int id=0;id<solution_id.size();id++)
		{
			int index= solution_id.get(id);
			
			if(setOfOldDemand.size()==0 || !setOfOldDemand.contains(index))
			{
				v_sol= new ArrayList<Integer>();
				v_sol = solution_node.get(id);
				f_sol= new ArrayList<>();
				f_sol = solution_func.get(id);
				
				int preN=-1;
				for (int id1=0;id1<v_sol.size();id1++)
				{
					if(v_sol.get(id1)!=preN)
					{
						delay++;
						preN=v_sol.get(id1);
					}
				}
				for(int j=0;j<f_sol.size();j++)
				{
					if(f_sol.get(j)>0)
					{
						//update capacity for node
						value_final+=g.getPriceNode(v_sol.get(j));
						
					}
				}
				for(int j=0;j<v_sol.size()-1;j++)
					for(int k=j+1;k<v_sol.size();k++)
					{
						value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
					}
				acceptDemandNo++;
			}
			else
			{
				
				if(setOfOldDemand.size()>0)
				{
					oldDemand _oldD= getOldDemand(index);
					ArrayList<Integer> v_Oldsol= new ArrayList<Integer>();
					v_Oldsol = _oldD.Get_v_sol();
					ArrayList<Integer> f_Oldsol= new ArrayList<>();
					f_Oldsol = _oldD.Get_f_sol();
//					for(int j=0;j<f_Oldsol.size();j++)
//					{
//						if(f_Oldsol.get(j)>0)
//						{
//							value_final-=g.getPriceNode(v_Oldsol.get(j));
//							
//						}
//					}
//					for(int j=0;j<v_Oldsol.size()-1;j++)
//						for(int k=j+1;k<v_Oldsol.size();k++)
//						{
//							value_final-=g.getEdgeWeight(v_Oldsol.get(j), v_Oldsol.get(k))*g.getPriceBandwidth()*0.001;
//						}
					//tinh gia tri cho old demand 
					v_sol= new ArrayList<Integer>();
					v_sol = solution_node.get(id);
					f_sol= new ArrayList<>();
					f_sol = solution_func.get(id);
					for(int j=0;j<f_sol.size();j++)
					{
						if(f_sol.get(j)>0)
						{
							value_final+=g.getPriceNode(v_sol.get(j));
							
						}
					}
					for(int j=0;j<v_sol.size()-1;j++)
						for(int k=j+1;k<v_sol.size();k++)
						{
							value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
						}
					//Tinh chi phi cho viec migrate. = chuyen resource tu a->b (chi phi bandwidth) * tai nguyen can chuyen
					for(int i1=0;i1<f_Oldsol.size();i1++)
					{
						if(f_Oldsol.get(i1)>0)
						{
							for(int i2=0;i2<f_sol.size();i2++)
							{
								if(f_sol.get(i2)>0)
								{
									if(v_Oldsol.get(i1)!=v_sol.get(i2))
									{
										ExGraph _g= new ExGraph(g.cap, g.pricePernode, g.w, g.getPriceBandwidth());
										ArrayList<Integer> shortPath= ShortestPath(v_Oldsol.get(i1), v_sol.get(i2), _g, 1.0);
										double discP=0.0;
										if(shortPath==null)
										{
											discP +=noVertex *1000;//truong hop lay duong dai nhat
										}
										else
										{
											for (int i3=0;i3<shortPath.size()-1;i3++)
											{
												discP+=g.getEdgeWeight(shortPath.get(i3), shortPath.get(i3+1));
											}
										}
										//TODO
										//value_penalty+=noMigrate* discP*g.getPriceBandwidth()*0.001*hsoA/noOldDemand ;
										//value_final += noMigrate * discP*g.getPriceBandwidth()*0.001*hsoA/noOldDemand;
										value_final += noMigrate * Math.sqrt(discP)*g.getPriceBandwidth()*0.001*hsoA/noOldDemand;
										value_penalty += noMigrate * Math.sqrt(discP)*g.getPriceBandwidth()*0.001*hsoA/noOldDemand;
									}
									break;
								}
							}
							
						}
					}
					
				}
			}
		}

		averageDelay=(delay*1.0)/acceptDemandNo;
		acceptRate = acceptDemandNo*1.0/noNewDemand;
		
		
		_duration = System.currentTimeMillis() - startTime;
		for (int id=0;id<solution_id.size();id++)
		{
			out.write("demand: "+solution_id.get(id)+":");
			for(int id1=0;id1<solution_func.get(id).size();id1++)
				out.write(solution_node.get(id).get(id1)+",");
			out.write(":");
			for(int id1=0;id1<solution_func.get(id).size();id1++)
				out.write(solution_func.get(id).get(id1)+",");
			out.newLine();
		}
		System.out.println(_duration);
		out.write("Average Delay: "+ averageDelay);
		out.newLine();
		out.write("Runtime (mS): "+ _duration);
		out.write("Acceptation rate: "+ acceptRate);
		//out.write("Resource ultilization: "+ ultilize_resource);
		
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
	public static boolean Viterbi(String outFile)
	{
		g_edit =new ExGraph(g.cap,g.pricePernode,g.w,g.getPriceBandwidth());
		final long startTime = System.currentTimeMillis();
		solution_func=new ArrayList<ArrayList<Integer>>();
		solution_node=new ArrayList<ArrayList<Integer>>();
		solution_id = new ArrayList<Integer>();
		int acceptDemandNo=noNewDemand;
		value_final=0;
		value_bandwidth =0;
		ultilize_resource =0;
		_duration=0;
		ArrayList<Integer> f_sol,v_sol;
		ArrayList<Double> red_oldDemand = new ArrayList<Double>();
		ArrayList<Double> cost_oldDemand = new ArrayList<Double>();
		ArrayList<Integer> id_oldDemand = new ArrayList<Integer>();
		double sum_link=0.0;
		double sum_node = 0.0;
		v_solution = new ArrayList<List<Integer>>();
		f_solution=new ArrayList<List<Integer>>();
		for(int i=0;i<noVertex;i++)
			for(int j=0;j<noVertex;j++)
				sum_link+=g.getEdgeWeight(i+1, j+1);
		for (int i=0;i<noVertex;i++)
			sum_node+=UtilizeFunction.value(g.getCap(i+1));
		
		int probMigrate=noMigrate;
		// Depending the migration probability
		for(int i=0;i<oldDemandArray.size();i++)
		{
			double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			id_oldDemand.add(_demand.GetID());
			f_sol=_demand.Get_f_sol();
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					co+= g.getPriceNode(v_sol.get(j))* _demand.GetRate();
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					co+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth();
				}
			cost_oldDemand.add(co);
			f_sol=null;
			v_sol=null;
		}
		for(int i=0;i<oldDemandArray.size();i++)
		{
			double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					co+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))/sum_link;
					co+=(UtilizeFunction.value(g.getCap(v_sol.get(j))) + UtilizeFunction.value(g.getCap(v_sol.get(k))))/(2*sum_node);
				}
			red_oldDemand.add(co);
			v_sol=null;
		}
		int[] rank_d= new int[oldDemandArray.size()];
		for (int i=0;i<oldDemandArray.size();i++)
			rank_d[i]= i;
		
		ArrayList<newDemand> arrDemand = new ArrayList<newDemand>();
		
		int dem=0;
		ArrayList<Integer> setOfOldDemand = new ArrayList<>();
		while(dem<noMigrate)
		{

			oldDemand _old= oldDemandArray.get(rank_d[dem]);
			//value_final+=migrateCost(_old);
			arrDemand.add(new newDemand(_old.GetID(), _old.GetSrc(), _old.GetDest(), _old.GetArrivalTime(), _old.GetProcessTime(), _old.GetBandwidth(), _old.GetRate(), _old.GetSetFunc()));
			f_sol=_old.Get_f_sol();
			v_sol=_old.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					g_edit.addCap(v_sol.get(j), getLamdaF(f_sol.get(j)));
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					g_edit.addEdgeWeight(v_sol.get(j), v_sol.get(k), _old.GetBandwidth());
				}
			dem++;
			f_sol=null;
			v_sol=null;
			setOfOldDemand.add(_old.GetID());
		}
		
		for (int i=dem;i<oldDemandArray.size();i++)
		{
			oldDemand _old= oldDemandArray.get(rank_d[i]);
			f_sol=_old.Get_f_sol();
			v_sol=_old.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					value_final+=g.getPriceNode(v_sol.get(j));
					
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
				}
		}
		
		for(int i=0;i<newDemandArray.size();i++)
			arrDemand.add(newDemandArray.get(i));
		
		boolean reject=false;
		int[][] function_Loc = new int[noFunction+1][noVertex+1]; 
		for (int i=0;i<noFunction;i++)
			for (int j=0;j<noVertex;j++)
				function_Loc[i+1][j+1]=0;
		List<DefaultWeightedEdge> _p = new ArrayList<>();
		List<DefaultWeightedEdge> _p_Min = new ArrayList<>();
		List<Integer> nodeList;
		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
			//Graph _g_tam=	new Graph(g.cap,g.pricePernode,g.link_bandwidth,g.getPriceBandwidth());
		for(int i=0;i<arrDemand.size();i++)
		{
			
			reject=false;
			newDemand _d=arrDemand.get(i);
			ExGraph g_save = new ExGraph(g_edit.cap,g_edit.pricePernode,g_edit.w,g_edit.getPriceBandwidth());
//			// tim 1 duong cho lan luot demand.
//			//xet tung doan 1, xet voi moi nut ktra voi cac nut con lai voi gia tri nho nhat (su dung dijsktra)
			
			
			SimpleWeightedGraph<String, DefaultWeightedEdge>  g_i = new SimpleWeightedGraph<String, DefaultWeightedEdge>(DefaultWeightedEdge.class); 
	        
			for (int j=0;j<g_edit.getV();j++)
	        {
	        	g_i.addVertex("node"+(j+1));
	        }
	        DefaultWeightedEdge[] e= new DefaultWeightedEdge[(noVertex*(noVertex-1))/2];
	        int id=0;
	        
	        for (int j=0;j<g_edit.getV()-1;j++)
	        {	        	
	        	for(int k=j+1;k<g_edit.getV();k++)
	        	{
	        		if(g_edit.getEdgeWeight((j +1), (k+1))>_d.getBw())
	        		{
	        		e[id]=g_i.addEdge("node"+(j+1),"node"+(k+1));	        			
	        		g_i.setEdgeWeight(e[id], g_edit.getEdgeWeight((j +1), (k+1)));
	        		id++;
	        		}
	        	}
	        }
	        DefaultWeightedEdge[] removed_edge = new DefaultWeightedEdge[id];
			int no_removed_edge =0;
			for( DefaultWeightedEdge v:g_i.edgeSet())
			{
				if(g_i.getEdgeWeight(v)<_d.getBw())
					removed_edge[no_removed_edge++]=v;				
			}
			for (int j=0;j<no_removed_edge;j++)
				g_i.removeEdge(removed_edge[j]);
			double cost_temp=0.0;
			double _min=Double.MAX_VALUE;
			int preNode=_d.getSrc();
			int lastNode=preNode;
			int currentNode=-1;
			int source;
			boolean flag=false;
			boolean flag1=false;
			v_sol=new ArrayList<>();
			f_sol=new ArrayList<>();
			for (int j=0;j<_d.getFunctions().size();j++)
			{
				
				removed_edge = new DefaultWeightedEdge[id];
				no_removed_edge =0;
				for( DefaultWeightedEdge v:g_i.edgeSet())
				{
					int int_s =Integer.parseInt(g_i.getEdgeSource(v).replaceAll("[\\D]", ""));
					int int_t =Integer.parseInt(g_i.getEdgeTarget(v).replaceAll("[\\D]", ""));
					if(g_edit.getEdgeWeight(int_s, int_t)<_d.getBw())
					//if(g_i.getEdgeWeight(v)<_d.bwS())
						removed_edge[no_removed_edge++]=v;				
				}
				for (int l=0;l<no_removed_edge;l++)
					g_i.removeEdge(removed_edge[l]);
				flag=false;
				flag1=true;
				_min=Double.MAX_VALUE;
				int vnf=_d.getFunctions().get(j);
				//xet tung function 1 xem dat o dau thi 
				nodeList = new ArrayList<Integer>();
				nodeList.add(preNode);
				for (int j1=0;j1<g_edit.getV();j1++)
				{
					flag=false;
					if(UtilizeFunction.isBig(g_edit.getCap(j1+1), getLamdaF(vnf)))
					{
						int functionOnNode=0;
						for (int j2=0;j2<noFunction;j2++)
							functionOnNode+=function_Loc[j2+1][j1+1];
						if(preNode==j1+1)
						{
							//penalty cost for delay on this node
							cost_temp+=functionOnNode*g.getPriceBandwidth()*0.001;
							cost_temp+= g.getPriceNode(j1+1)*100;						
							flag=true;
						}
						else
						{
							flag=false;
							_p =   DijkstraShortestPath.findPathBetween(g_i, "node"+preNode, "node"+(j1+1));
							if(_p!=null)
							{
								for (DefaultWeightedEdge l:_p)
								{
									cost_temp+=g_i.getEdgeWeight(l);
								}							
								cost_temp=cost_temp * g.getPriceBandwidth()*0.001;
								cost_temp+= g.getPriceNode(j1+1)*100;						
							}
							else
							{
								cost_temp=Double.MAX_VALUE;
							}
							if(lastNode==j1+1)
								cost_temp+=g.getPriceBandwidth()*200;
						}
						if(cost_temp<_min)
						{
							flag1=flag;
							//luu lai duong di
							_p_Min=_p;
							_min=cost_temp;
							currentNode=j1+1;
						}
					}
				}
				if(!flag1 && !flag)
				{
					if(_p_Min!=null)
					{
						source = preNode;
						
						while (_p_Min.size()>0)
							{	
								int ix =0;
								for(int l=0;l<_p_Min.size();l++)
								{
									int int_s =Integer.parseInt(g_i.getEdgeSource(_p_Min.get(l)).replaceAll("[\\D]", ""));
									int int_t =Integer.parseInt(g_i.getEdgeTarget(_p_Min.get(l)).replaceAll("[\\D]", ""));
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth() *0.001;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth() *0.001;	
									if( int_s == source )
									{
										nodeList.add(int_t);
										source = int_t;
										ix = l;
										g_edit.setEdgeWeight(int_s, int_t, g_edit.getEdgeWeight(int_s, int_t)-_d.getBw());
										break;
									}
									if( int_t == source)
									{
										nodeList.add(int_s);
										source = int_s;
										ix = l;
										g_edit.setEdgeWeight(int_s, int_t, g_edit.getEdgeWeight(int_s, int_t)-_d.getBw());
										break;
									}
								}
								_p_Min.remove(ix);
							}
						System.out.println("current Node:::" + currentNode);
							g_edit.setCap(currentNode,UtilizeFunction.minus(g_edit.getCap(currentNode),getLamdaF(vnf)));
							
							function_Loc[vnf][currentNode]++;
							value_final += g.getPriceNode(currentNode);
												
					}
					else
					{
						nodeList=null;
						v_sol=null;
						f_sol=null;
						reject=true;
						break;
					}
				}
				else
				{
					if(flag)
					{
					nodeList.add(preNode);
					g_edit.setCap(preNode,UtilizeFunction.minus(g_edit.getCap(preNode),getLamdaF(vnf)));
					function_Loc[vnf][preNode]++;
					value_final += g.getPriceNode(preNode);
					}
					else
					{
						nodeList=null;
						v_sol=null;
						f_sol=null;
						reject=true;
						break;
					}
				}
				
				//solution_node.add(v_sol);
				//solution_func.add(f_sol);
				//solution_id.add(_d.getId());
				
				if(nodeList.size()>1)
				{
					if(j==0)
					{
						f_sol.add(0);
						v_sol.add(_d.getSrc());
					}
					for(int _i=1;_i<nodeList.size();_i++)
					{
						v_sol.add(nodeList.get(_i));
						System.out.print(nodeList.get(_i)+",");
						out.write(nodeList.get(_i)+", ");
					}
					
					for(int _i=1;_i<nodeList.size()-1;_i++)
					{
						f_sol.add(0);
					}
					f_sol.add(vnf);
				}
				lastNode=preNode;
				if(nodeList.size()>0)
					preNode=nodeList.get(nodeList.size()-1);
				nodeList=null;
			}
			if(reject)
			{
				acceptDemandNo--;
				v_sol=null;
				f_sol=null;
				nodeList=null;
				g_edit =new ExGraph(g_save.cap,g_save.pricePernode,g_save.w,g_save.getPriceBandwidth());
				continue;
			}
			//tu node cuoi cung den destination
			//f_sol.add(_d.getFunctions().get(_d.getFunctions().size()-1));
			nodeList = new ArrayList<Integer>();
			if (preNode!=_d.getDest())
			{
				_p =   DijkstraShortestPath.findPathBetween(g_i, "node"+preNode, "node"+_d.getDest());
				if(_p!=null)
				{
					source = preNode;
					while (_p.size()>0)
					{	
						int ix =0;
						for(int l=0;l<_p.size();l++)
						{
							int int_s =Integer.parseInt(g_i.getEdgeSource(_p.get(l)).replaceAll("[\\D]", ""));
							int int_t =Integer.parseInt(g_i.getEdgeTarget(_p.get(l)).replaceAll("[\\D]", ""));
							value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth() *0.001;
							value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth() *0.001;	
							if( int_s == source )
							{
								nodeList.add(int_t);
								source = int_t;
								ix = l;
								g_edit.setEdgeWeight(int_s, int_t, g_edit.getEdgeWeight(int_s, int_t)-_d.getBw());
								break;
							}
							if( int_t == source)
							{
								nodeList.add(int_s);
								source = int_s;
								ix = l;
								g_edit.setEdgeWeight(int_s, int_t, g_edit.getEdgeWeight(int_s, int_t)-_d.getBw());
								break;
							}
						}
						_p.remove(ix);
					}
				}
				else
				{
					reject=true;
					cost_temp=Double.MAX_VALUE;
				}
				if(reject)
				{
					acceptDemandNo--;
					v_sol=null;
					f_sol=null;
					nodeList=null;
					g_edit =new ExGraph(g_save.cap,g_save.pricePernode,g_save.w,g_save.getPriceBandwidth());
					continue;
				}
				for(int _i:nodeList)
				{
					v_sol.add(_i);
					f_sol.add(0);
					System.out.print(_i+",");
					out.write(_i+", ");
				}
					
				nodeList=null;
			}
			out.newLine();
			solution_node.add(v_sol);
			solution_func.add(f_sol);
			solution_id.add(_d.getId());
			v_sol=null;
			f_sol=null;
		}
		
		//Compute the average delay for all new demand
		averageDelay=0.0;
		acceptRate =0.0;
		acceptDemandNo=0;
		int delay=0;
		for(int id=0;id<solution_id.size();id++)
		{
			int index= solution_id.get(id);
			
			if(setOfOldDemand.size()==0 || !setOfOldDemand.contains(index))
			{
				v_sol= new ArrayList<Integer>();
				v_sol = solution_node.get(id);
				f_sol= new ArrayList<>();
				f_sol = solution_func.get(id);
				
				int preN=-1;
				for (int id1=0;id1<v_sol.size();id1++)
				{
					if(v_sol.get(id1)!=preN)
					{
						delay++;
						preN=v_sol.get(id1);
					}
				}
				for(int j=0;j<f_sol.size();j++)
				{
					if(f_sol.get(j)>0)
					{
						//update capacity for node
						value_final+=g.getPriceNode(v_sol.get(j));
						
					}
				}
				for(int j=0;j<v_sol.size()-1;j++)
					for(int k=j+1;k<v_sol.size();k++)
					{
						value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
					}
				acceptDemandNo++;
			}
			else
			{
				
				if(setOfOldDemand.size()>0)
				{
					oldDemand _oldD= getOldDemand(index);
					ArrayList<Integer> v_Oldsol= new ArrayList<Integer>();
					v_Oldsol = _oldD.Get_v_sol();
					ArrayList<Integer> f_Oldsol= new ArrayList<>();
					f_Oldsol = _oldD.Get_f_sol();
//					for(int j=0;j<f_Oldsol.size();j++)
//					{
//						if(f_Oldsol.get(j)>0)
//						{
//							//update capacity for node
//							value_final-=g.getPriceNode(v_Oldsol.get(j));
//							
//						}
//					}
//					for(int j=0;j<v_Oldsol.size()-1;j++)
//						for(int k=j+1;k<v_Oldsol.size();k++)
//						{
//							value_final-=g.getEdgeWeight(v_Oldsol.get(j), v_Oldsol.get(k))*g.getPriceBandwidth()*0.001;
//						}
					//tinh gia tri cho old demand 
					v_sol= new ArrayList<Integer>();
					v_sol = solution_node.get(id);
					f_sol= new ArrayList<>();
					f_sol = solution_func.get(id);
					for(int j=0;j<f_sol.size();j++)
					{
						if(f_sol.get(j)>0)
						{
							value_final+=g.getPriceNode(v_sol.get(j));
							
						}
					}
					for(int j=0;j<v_sol.size()-1;j++)
						for(int k=j+1;k<v_sol.size();k++)
						{
							value_final+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth()*0.001;
						}
					//Tinh chi phi cho viec migrate. = chuyen resource tu a->b (chi phi bandwidth) * tai nguyen can chuyen
					for(int i1=0;i1<f_Oldsol.size();i1++)
					{
						if(f_Oldsol.get(i1)>0)
						{
							for(int i2=0;i2<f_sol.size();i2++)
							{
								if(f_sol.get(i2)>0)
								{
									if(v_Oldsol.get(i1)!=v_sol.get(i2))
									{
										ExGraph _g= new ExGraph(g.cap, g.pricePernode, g.w, g.getPriceBandwidth());
										ArrayList<Integer> shortPath= ShortestPath(v_Oldsol.get(i1), v_sol.get(i2), _g, 1.0);
										double discP=0.0;
										if(shortPath==null)
										{
											discP +=noVertex *1000;//truong hop lay duong dai nhat
										}
										else
										{
											for (int i3=0;i3<shortPath.size()-1;i3++)
											{
												discP+=g.getEdgeWeight(shortPath.get(i3), shortPath.get(i3+1));
											}
										}
										//TODO
										//value_penalty+=noMigrate * discP*g.getPriceBandwidth()*0.001*hsoA/noOldDemand;
										//value_final += noMigrate* discP*g.getPriceBandwidth()*0.001*hsoA/noOldDemand ;
										value_final += noMigrate* Math.sqrt(discP)*g.getPriceBandwidth()*0.001*hsoA/noOldDemand ;
										value_penalty += noMigrate * Math.sqrt(discP)*g.getPriceBandwidth()*0.001*hsoA/noOldDemand;
										
									}
									break;
								}
							}
							
						}
					}
					
				}
			}
		}


		averageDelay=(delay*1.0)/acceptDemandNo;
		acceptRate = acceptDemandNo*1.0/noNewDemand;
		
		
		_duration = System.currentTimeMillis() - startTime;
		for (int id=0;id<solution_id.size();id++)
		{
			out.write("demand: "+solution_id.get(id)+":");
			for(int id1=0;id1<solution_func.get(id).size();id1++)
				out.write(solution_node.get(id).get(id1)+",");
			out.write(":");
			for(int id1=0;id1<solution_func.get(id).size();id1++)
				out.write(solution_func.get(id).get(id1)+",");
			out.newLine();
		}
		System.out.println(_duration);
		out.write("Average Delay: "+ averageDelay);
		out.newLine();
		out.write("Runtime (mS): "+ _duration);
		out.write("Acceptation rate: "+ acceptRate);
		//out.write("Resource ultilization: "+ ultilize_resource);
		
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
	public static boolean PBR(String outFile)// Placing before Routing
	{
		g_edit =new ExGraph(g.cap,g.pricePernode,g.w,g.getPriceBandwidth());
		final long startTime = System.currentTimeMillis();
		solution_func=new ArrayList<ArrayList<Integer>>();
		solution_node=new ArrayList<ArrayList<Integer>>();
		solution_id = new ArrayList<Integer>();
		int acceptDemandNo=noNewDemand;
		value_final=0;
		value_bandwidth =0;
		ultilize_resource =0;
		_duration=0;
		ArrayList<Integer> f_sol,v_sol;
		//double sum_link=0.0;
		//double sum_node = 0.0;
		v_solution = new ArrayList<List<Integer>>();
		f_solution=new ArrayList<List<Integer>>();
//		for(int i=0;i<noVertex;i++)
//			for(int j=0;j<noVertex;j++)
//				sum_link+=g.getEdgeWeight(i+1, j+1);
//		for (int i=0;i<noVertex;i++)
//			sum_node+=UtilizeFunction.value(g.getCap(i+1));
		
		int probMigrate=noMigrate;
		// Depending the migration probability
		for(int prob=0;prob<probMigrate;prob++)
		{
		
		ArrayList<Integer> v_busy = new ArrayList<Integer>(); 
		//v_busy contains 
		for (int i=0;i<noVertex;i++)
			v_busy.add(0);
		
		for(int i=0;i<oldDemandArray.size();i++)
		{
			oldDemand _demand = oldDemandArray.get(i);
			//id_oldDemand.add(_demand.GetID());
			f_sol=_demand.Get_f_sol();
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					if((j<f_sol.size()-1) && (v_sol.get(j)!=v_sol.get(j+1)))
					{
						v_busy.set(v_sol.get(j)-1, v_busy.get(v_sol.get(j)-1)+1);
					}
				}
			}
			
			f_sol=null;
			v_sol=null;
		}
		
		int max_busy= 0;
		int busyNode=-1;
		for(int i=0;i<v_busy.size();i++)
		{
			//Select a busy node to migrate
			if(v_busy.get(i)>max_busy)
			{
				max_busy = v_busy.get(i);
				busyNode =i+1;
			}	
		}
		ArrayList<Integer> busyDemandArr = new ArrayList<Integer>();
		//busyDemandArr are Id of all demands that contain busyNode
		if(busyNode !=-1)
		{
			//exist a busy node -> select a idle node to migrate
			//first, select a old demand (contains the busy node) to implement migration
			for(int i=0;i<oldDemandArray.size();i++)
			{
				oldDemand _demand = oldDemandArray.get(i);
				f_sol=_demand.Get_f_sol();
				v_sol=_demand.Get_v_sol();
				for(int j=0;j<f_sol.size();j++)
				{
					if(f_sol.get(j)>0)
					{
						if(v_sol.get(j)==busyNode)
							busyDemandArr.add(_demand.GetID());
					}
				}
				f_sol=null;
				v_sol=null;
			}
		}
		//Select randomly 1 demand from busyDemand
		int randInt =-1;
		if(busyDemandArr.size()>0)
			randInt = UtilizeFunction.randInt(0, busyDemandArr.size()-1);
		else
		{
			System.out.print(busyNode + " Sai roi...");
			return false;
		}
		oldDemand busyDemand = new oldDemand();
		for (int i=0;i<oldDemandArray.size();i++)
		{
			oldDemand _demand= oldDemandArray.get(i);
			if(_demand.GetID() == busyDemandArr.get(randInt))
				busyDemand =_demand;
		}
		//busyNode and MigrateFunction
		int MigrateFunc=-1; // Function will be migrated
		f_sol = busyDemand.Get_f_sol();
		v_sol = busyDemand.Get_v_sol();
		for (int i=0;i<f_sol.size();i++)
		{
			if(v_sol.get(i)==busyNode && f_sol.get(i)>0)
				MigrateFunc = f_sol.get(i);
		}
		System.out.print("\n mmm "+ MigrateFunc);
		
		//Select 1 idle node depending on the busyDemand
		
		int idleNode = -1;
		double min_idle=Double.MAX_VALUE;
		for (int i=0;i<noVertex;i++)
		{
			//(so demand di qua/tong so old demand) + (d(source,v)+d(dest,v))/2*(so dinh-1);
			int SrcDist =Dist.get(busyDemand.GetSrc()).get(i+1);
			int DestDist =Dist.get(busyDemand.GetDest()).get(i+1);
			double minV=Double.MAX_VALUE;
			if(SrcDist>=0 && DestDist>=0)
				minV= v_busy.get(i)/noOldDemand + (SrcDist + DestDist)/(2.0*(noVertex -1));
			//Check have enought resource (not done)
			if(minV<min_idle)
			{
				idleNode= i+1;
				min_idle = minV;
			}
		}
		
		//Migrate resources from busynode to idleNode, then re-routing for busyDemand;
		//Decrease resources for idleNode, and Increase resource for busyNode
		g_edit.addCap(busyNode, getFunction(MigrateFunc).getLamda());
		g_edit.minusCap(idleNode, getFunction(MigrateFunc).getLamda());
		int nextFunc = nextFunction(f_sol, MigrateFunc);
		int prevFunc = prevFunction(f_sol, MigrateFunc);
		int indexFunc = indexFunction(f_sol, MigrateFunc);
		if(prevFunc!=-1)
		{
			for (int i=prevFunc;i<indexFunc;i++)
				g_edit.addEdgeWeight(v_sol.get(i), v_sol.get(i+1),g_edit.getEdgeWeight(v_sol.get(i), v_sol.get(i+1))+ busyDemand.GetBandwidth());
		}
		else
		{
			for (int i=0;i<indexFunc;i++)
				g_edit.addEdgeWeight(v_sol.get(i), v_sol.get(i+1),g_edit.getEdgeWeight(v_sol.get(i), v_sol.get(i+1))+ busyDemand.GetBandwidth());
		}
		
		if(nextFunc!=-1)
		{
			for (int i=indexFunc;i<nextFunc;i++)
				g_edit.addEdgeWeight(v_sol.get(i), v_sol.get(i+1),g_edit.getEdgeWeight(v_sol.get(i), v_sol.get(i+1))+ busyDemand.GetBandwidth());
		}
		else {
			for (int i=indexFunc;i<f_sol.size()-1;i++)
				g_edit.addEdgeWeight(v_sol.get(i), v_sol.get(i+1),g_edit.getEdgeWeight(v_sol.get(i), v_sol.get(i+1))+ busyDemand.GetBandwidth());
		}
		
		
		//re-routing for busyDemand including (f-1, migrrateFuction)(migrateFunc,f+1)
		ArrayList<Integer> new_route= new ArrayList<Integer>();
		ArrayList<Integer> v_temp=new ArrayList<Integer>();
		ArrayList<Integer> f_temp = new ArrayList<Integer>();
		if(prevFunc!=-1)
		{
			for (int j=0;j<=prevFunc;j++)
			{
				v_temp.add(v_sol.get(j));
				f_temp.add(f_sol.get(j));
			}
			
			new_route = ShortestPath(v_sol.get(prevFunc), idleNode, g_edit, busyDemand.GetBandwidth());
			for (int j=1;j<new_route.size()-1;j++)
			{
				v_temp.add(new_route.get(j));
				f_temp.add(0);
			}
			
			new_route=null;
		}
		if(indexFunc!=-1)
		{
			v_temp.add(idleNode);
			f_temp.add(f_sol.get(indexFunc));
		}
		else
		{
			continue;
		}
		if(nextFunc!=-1)
		{			
			new_route = ShortestPath(idleNode,v_sol.get(nextFunc),  g_edit, busyDemand.GetBandwidth());
			for (int j=1;j<new_route.size()-1;j++)
			{
				v_temp.add(new_route.get(j));
				f_temp.add(0);
			}
			v_temp.add(v_sol.get(nextFunc));
			f_temp.add(f_sol.get(nextFunc));
			new_route=null;
		}
		for (int j=nextFunc+1;j< v_sol.size();j++)
		{
			v_temp.add(v_sol.get(j));
			f_temp.add(f_sol.get(j));
		}
		busyDemand.updateFSol(f_temp);
		busyDemand.updateVSol(v_temp);
		f_sol=null;
		v_sol=null;
		}
		boolean reject=false;
		
		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
		
		for(int i=0;i<newDemandArray.size();i++)
		{
			reject=false;
			//consider each demand.
			newDemand _newDemand = newDemandArray.get(i);
			f_sol = new ArrayList<Integer>();
			v_sol = new ArrayList<Integer>();
			int src=_newDemand.getSrc();
			int dest= _newDemand.getDest();
			ArrayList<Integer> setF=_newDemand.getFunctions();
			int selectedNode=-1;
			for (int j=0;j<setF.size();j++)
			{
				Function _f = getFunction(setF.get(j));
				
				double minV=Double.MAX_VALUE;
				for (int k=0;k<noVertex;k++)
				{
					//select the best node ()
					if(UtilizeFunction.isBig(g_edit.getCap(k+1),_f.getLamda()))
					{
						double tempV=0;
						int SrcDist =Dist.get(src).get(k+1);
						int DestDist =Dist.get(dest).get(k+1);					
						if(SrcDist>=0 && DestDist>=0)
							tempV+= (SrcDist + DestDist)/(2.0*(noVertex -1));
						for (int h=0;h<noVertex;h++)
						{
							if (g_edit.getEdgeWeight(k+1, h+1)>_newDemand.getBw())
								tempV+=1/(2*noVertex);
						}
						if(tempV<minV)
						{
							selectedNode= k+1;
							minV = tempV;
						}	
					}
				}
				//f is put on selectedNode
				if(selectedNode>0)
				{
					g_edit.setCap(selectedNode, UtilizeFunction.minus(g_edit.getCap(selectedNode),_f.getLamda()));
					if(src!=selectedNode)
					{
						ArrayList<Integer> arr= ShortestPath(src, selectedNode, g_edit, _newDemand.getBw());
						for(int id=0;id<arr.size()-1;id++)
						{
							v_sol.add(arr.get(id));
							f_sol.add(0);
						}
						
					}
					v_sol.add(selectedNode);
					f_sol.add(_f.getId());
					src=selectedNode;
				}
				else
				{
					//khong tim duoc duong di cho demand nay
					acceptDemandNo--;
					reject=true;
					break;
				}
			}
			if(reject)
				continue;
			if(selectedNode!=dest)
			{
				ArrayList<Integer> arr= ShortestPath(selectedNode, dest, g_edit, _newDemand.getBw());
				for(int id=1;id<arr.size();id++)
				{
					v_sol.add(arr.get(id));
					f_sol.add(0);
				}
			}
			solution_func.add(f_sol);
			solution_node.add(v_sol);
			solution_id.add(_newDemand.getId());
			v_sol=null;
			f_sol=null;
		}
		
		//Compute the average delay for all new demand
		averageDelay=0.0;
		acceptRate =0.0;
		for(int id=0;id<solution_id.size();id++)
		{
			v_sol= new ArrayList<Integer>();
			v_sol = solution_node.get(id);
			averageDelay+=(v_sol.size()-1.0)/acceptDemandNo;			
		}
		acceptRate = acceptDemandNo*1.0/noNewDemand;
		
		
		_duration = System.currentTimeMillis() - startTime;
		for (int id=0;id<solution_id.size();id++)
		{
			out.write("demand: "+solution_id.get(id)+":");
			for(int id1=0;id1<solution_func.get(id).size();id1++)
				out.write(solution_node.get(id).get(id1)+",");
			out.write(":");
			for(int id1=0;id1<solution_func.get(id).size();id1++)
				out.write(solution_func.get(id).get(id1)+",");
			out.newLine();
		}
		System.out.println(_duration);
		out.write("Average Delay: "+ averageDelay);
		out.newLine();
		out.write("Runtime (mS): "+ _duration);
		out.write("Acceptation rate: "+ acceptRate);
		//out.write("Resource ultilization: "+ ultilize_resource);
		
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
	public static boolean heuristic(String outFile)
	{
		
		numberofCore=0;
		numberofEdge=0;
		numberofMidle=0;
		value_final=0;
		value_bandwidth =0;
		ultilize_resource =0;
		_duration=0;
		ArrayList<Double> red_oldDemand = new ArrayList<Double>();
		ArrayList<Double> cost_oldDemand = new ArrayList<Double>();
		ArrayList<Integer> id_oldDemand = new ArrayList<Integer>();
		ArrayList<Integer> f_sol,v_sol;
		double sum_link=0.0;
		double sum_node = 0.0;
		v_solution = new ArrayList<List<Integer>>();
		f_solution=new ArrayList<List<Integer>>();
		for(int i=0;i<noVertex;i++)
			for(int j=0;j<noVertex;j++)
				sum_link+=g.getEdgeWeight(i+1, j+1);
		for (int i=0;i<noVertex;i++)
			sum_node+=UtilizeFunction.value(g.getCap(i+1));
		
		
		for(int i=0;i<oldDemandArray.size();i++)
		{
			double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			id_oldDemand.add(_demand.GetID());
			f_sol=_demand.Get_f_sol();
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					co+= g.getPriceNode(v_sol.get(j))* _demand.GetRate();
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					co+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))*g.getPriceBandwidth();
				}
			cost_oldDemand.add(co);
			f_sol=null;
			v_sol=null;
		}
		for(int i=0;i<oldDemandArray.size();i++)
		{
			double co=0.0;
			oldDemand _demand = oldDemandArray.get(i);
			v_sol=_demand.Get_v_sol();
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					co+=g.getEdgeWeight(v_sol.get(j), v_sol.get(k))/sum_link;
					co+=(UtilizeFunction.value(g.getCap(v_sol.get(j))) + UtilizeFunction.value(g.getCap(v_sol.get(k))))/(2*sum_node);
				}
			red_oldDemand.add(co);
			v_sol=null;
		}
		int[] rank_d= new int[oldDemandArray.size()];
		for (int i=0;i<oldDemandArray.size();i++)
			rank_d[i]= i;
//		if(oldDemandArray.size()>1)
//		{
//			for (int i=0;i<oldDemandArray.size();i++)
//				rank_d[i]= i;
//			for(int i=0;i<oldDemandArray.size()-1;i++)
//			{
//				int temp=i;
//				for (int j=i+1;j<oldDemandArray.size();j++)
//					if(red_oldDemand.get(j)>red_oldDemand.get(temp))//sx theo thu tu tang dan cua co
//						temp=j;
//				int k= rank_d[i];
//				rank_d[i]=rank_d[temp];
//				rank_d[temp]=k;
//				Collections.swap(red_oldDemand, i, temp);
//				Collections.swap(cost_oldDemand, i, temp);
//				Collections.swap(id_oldDemand, i, temp);
//			}
//		}
//		else
//		{
//			if(oldDemandArray.size()==1)
//				rank_d[0]=0;
//		}
		
		final long startTime = System.currentTimeMillis();
		
		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
		ExGraph g_temp=	new ExGraph(g.cap,g.pricePernode,g.w,g.getPriceBandwidth());
		//Graph g_temp= new Graph(g.r, g.K, g.link_bandwidth, g.getPriceBandwidth(),true);
		ArrayList<newDemand> arrDemand = new ArrayList<newDemand>();
		for(int i=0;i<newDemandArray.size();i++)
			arrDemand.add(newDemandArray.get(i));
		int dem=0;
		while(dem<noMigrate)
		{

			oldDemand _old= oldDemandArray.get(rank_d[dem]);
			value_final+=migrateCost(_old);
			arrDemand.add(new newDemand(_old.GetID(), _old.GetSrc(), _old.GetDest(), _old.GetArrivalTime(), _old.GetProcessTime(), _old.GetBandwidth(), _old.GetRate(), _old.GetSetFunc()));
			f_sol=_old.Get_f_sol();
			v_sol=_old.Get_v_sol();
			for(int j=0;j<f_sol.size();j++)
			{
				if(f_sol.get(j)>0)
				{
					//update capacity for node
					g.addCap(v_sol.get(j), getLamdaF(f_sol.get(j)));
				}
			}
			for(int j=0;j<v_sol.size()-1;j++)
				for(int k=j+1;k<v_sol.size();k++)
				{
					g.addEdgeWeight(v_sol.get(j), v_sol.get(k), _old.GetBandwidth());
				}
			dem++;
			f_sol=null;
			v_sol=null;
		}
		
		int[][] function_Loc = new int[noFunction+1][noVertex+1]; // if number of function f put on virtual node v
		
		double bw_min=arrDemand.get(0).getBw();//gia tri nho nhat cua canh can duoc dap ung cho tat ca cac demand
		//double r_min =functionArr[0].bw();//gia tri nho nhat cua node can duoc dap ung cho tat ca cac function
		Vector<Double> r_min =functionArr.get(0).getLamda();
		int[] f_rank = new int[noFunction];//rank function decrease
		double[] f_maxbw=new double[noFunction+1];//max bandwidth of function in all services
		int[] no_Function= new int[noFunction+1]; //number of function in all services
		
		List<Integer> nodeList;
		double weight_path=0;
		double min_w ;
		int source=0;
		int destination=0;		
		List<DefaultWeightedEdge> _p;
		
		ArrayList<Integer> srcArr = new ArrayList<Integer>();//chua tat ca cac source
		ArrayList<Integer> desArr= new ArrayList<Integer>();//chua tat ca cac destination
		for (int i=0;i<noVertex;i++)
		{
			boolean _flagS=false;
			boolean _flagD=false;
			for (newDemand d : arrDemand) {
				
				if(!_flagS && d.getSrc()==(i+1))
				{
					srcArr.add(i+1);
					_flagS=true;
				}
				if(!_flagD && d.getDest()==(i+1))
				{
					desArr.add(i+1);
					_flagD=true;
				}
			}
		}		
		
		
		//so luong function trong tat ca cac demand
		for (int i=0;i<noFunction;i++)
		{
			no_Function[i+1]=0;
			f_maxbw[i+1]=0;
		}
		for (int i=0;i<noFunction;i++)
			for (int j=0;j<arrDemand.size();j++)
			{
				ArrayList<Integer> arrF = arrDemand.get(j).getFunctions();				
				for (int k=0;k<arrF.size();k++)
				{					
					if(arrF.get(k)==(i+1))
					{
						no_Function[i+1]+=1;
						if(arrDemand.get(j).getBw()>f_maxbw[i+1])
							f_maxbw[i+1]=arrDemand.get(j).getBw();
					}
				}
			}
		
//		//sap xep cac function theo thu tu giam dan tai nguyen yeu cau
//				for (int i=0;i<noFunction;i++)
//					f_rank[i]= functionArr.get(i).getId();
//				for(int i=0;i<f_rank.length-1;i++)
//				{
//					int temp=i;
//					for (int j=i+1;j<f_rank.length;j++)
//						if(UtilizeFunction.isBig(functionArr.get(j).getLamda(), functionArr.get(temp).getLamda()))
//							temp=j;
//					int k= f_rank[i];
//					f_rank[i]=f_rank[temp];
//					f_rank[temp]=k;
//				}
		
				//sap xep cac function theo thu tu giam dan do phho bien function
				for (int i=0;i<noFunction;i++)
					f_rank[i]= functionArr.get(i).getId();
				for(int i=0;i<f_rank.length-1;i++)
				{
					int temp=i;
					for (int j=i+1;j<f_rank.length;j++)
						if(no_Function[f_rank[j]]>no_Function[f_rank[temp]])
							temp=j;					
					int k= f_rank[i];
					f_rank[i]=f_rank[temp];
					f_rank[temp]=k;
				}
		
				
		//gia tri nho nhat cua cac link tu cac demand, gia tri nho nhat cac requirement for function
		for (int i=0;i<arrDemand.size();i++)
			if(arrDemand.get(i).getBw()< bw_min)
				bw_min = arrDemand.get(i).getBw();
		for (int i=0;i<noFunction;i++)
		{
			if(UtilizeFunction.bigger(r_min, functionArr.get(i).getLamda())==1)
				r_min =functionArr.get(i).getLamda();
		}
		//1. Remove node and edge is not feasible
		for (int i=0;i<noVertex;i++)
		{
			if(UtilizeFunction.isBig(r_min, g_temp.getCap(i+1)))
				g_temp.removeNode(i+1);
			for (int j=0;j<noVertex;j++)
				if(g_temp.getEdgeWeight(i+1, j+1)<bw_min)
					g_temp.removeLink(i+1, j+1);
		}
		
		//B1. Rank larger is put on virtual node smaller (price per resource unit)
		// Prior: same function put on 1 virtual if it has enough capacity
		for (int i=0;i<noFunction;i++)
			for (int j=0;j<noVertex;j++)
				function_Loc[i+1][j+1]=0;
		//sort virtual nodes increase depend on price per resource unit
		double[] x_add= new double[noVertex+1];// gia tri thay doi sau moii lan gan
		for(int j=0;j<noVertex;j++)
			x_add[j+1]=0.0;
		double y= 0.0;
		double sum_r =0.0;
		for (int k=0;k<noVertex;k++)
			//sum_r+=UtilizeFunction.value(g_temp.getPriceNode(k));
			sum_r+=g_temp.getPriceNode(k+1);
		for(int i=0;i<noFunction;i++)
		{
			Function f= getFunction(f_rank[i]);
			int[] indexFunction = new int[arrDemand.size()];
			for (int j=0;j<arrDemand.size();j++)
			{
				//kiem tra function f co the thuoc demand nao? -1 neu thuoc day dau, 1 neu thuoc day sau. 0 neu ko thuoc
				ArrayList<Integer> arrFunction = arrDemand.get(j).getFunctions();
				boolean flag=false;
				for (int k=0;k<arrFunction.size();k++)
				{
					if(arrFunction.get(k)==f.getId())
					{
						if(k<arrFunction.size()/2)
							indexFunction[j]=-1;
						else
							indexFunction[j]=1;
						flag=true;
						break;
					}
				}
				if(flag==false)
					indexFunction[j]=0;
			}
			int temp_noF = no_Function[f.getId()];
			
			//Each function in arrayFunction
			double mauso=0;
			for (int j=0;j<arrDemand.size();j++)
			{
				if(indexFunction[j]!=0)
				{
				for (int k=0;k<noVertex;k++)
				{
					
					mauso+=Dist.get(arrDemand.get(j).getSrc()).get(k);
					mauso+=Dist.get(arrDemand.get(j).getDest()).get(k);
				}
				}
			}
			double[] rank_value = new double[noVertex+1];// function to compute rank for each virtual node
			for (int k=0;k<noVertex;k++)
				rank_value[k+1]=0.0;
			while (temp_noF>0)
			{	
				for (int k=0;k<noVertex;k++)
				{
					if(function_Loc[f.getId()][k+1]>0)
					{
						rank_value[k+1]=theta/function_Loc[f.getId()][k+1];
					}
					else
						rank_value[k+1]=0.0;
				}
				double sum_w = 0.0;				
				for (int k=0;k<noVertex-1;k++)
					for (int j=k+1;j<noVertex;j++)
						sum_w+=g_temp.getEdgeWeight(k+1, j+1)*g_temp.getPriceBandwidth() ;
				sum_w= 2*sum_w -y;//tru di mot luong
				for (int k=0;k<noVertex;k++)
				{
					rank_value[k+1]=0.0;
					if(g_temp.getExistNode(k+1))
					{
						//rank_node[i]= i+1;
						rank_value[k+1]+=alpha * g_temp.getPriceNode(k+1)/sum_r;
						double temp_bw= 0.0;
						for (int j=0;j<noVertex;j++)
						{
							if(g_temp.getEdgeWeight(k+1, j+1)>0)
								temp_bw +=g_temp.getEdgeWeight(k+1, j+1)*g_temp.getPriceBandwidth() ;
						}
						temp_bw = temp_bw - x_add[k+1];
//						if(sum_w >0)
//						rank_value[k+1]+=beta*(1-temp_bw/(2*sum_w));
//						else
//						{
//							rank_value[k+1]=Double.MAX_VALUE;
//						}
						if(temp_bw >0)
							rank_value[k+1]+=beta*sum_w/temp_bw;
						else
						{
							rank_value[k+1]=Double.MAX_VALUE;
						}
						double tuso=0;
						for (int j=0;j<arrDemand.size();j++)
						{
							if (indexFunction[j]!=-0)
							{
								tuso+=Dist.get(arrDemand.get(j).getSrc()).get(k);
								tuso+=Dist.get(arrDemand.get(j).getDest()).get(k);
							}
						}
						if(mauso!=0)
							rank_value[k+1]+=gama*tuso/mauso;
						else
							rank_value[k+1]=Double.MAX_VALUE;
//						for (int d : srcArr) {
//							if(d==(k+1))
//								rank_value[k+1]+=gama;
//						}
//						for (int d : desArr) {
//							if(d==(k+1))
//								rank_value[k+1]+=gama;
//						}
						
					}
					else
						rank_value[k+1]=Double.MAX_VALUE;
				}
				// find node: rank_value min
				double min_rank=Double.MAX_VALUE;
				int v_min = -1;
				for (int k=0;k<noVertex;k++)
				{
					//kiem tra them f1 và f2 có ton tai duong di ko
					if(UtilizeFunction.isBig(g_temp.getCap(k+1), f.getLamda()) && rank_value[k+1]<min_rank)
					{
						min_rank = rank_value[k+1];
						v_min = k+1;
					}
				}
				if(v_min==-1)
				{
					//khong the tim dc v_min -> Bai toan khong co loi giai
					out.write("khong tim dc v_min cho f"+f.getId());
					out.newLine();
					return false;
				}
				function_Loc[f.getId()][v_min]+=1;
				g_temp.setCap(v_min, UtilizeFunction.minus(g_temp.getCap(v_min), f.getLamda()));
				if(UtilizeFunction.isBig(r_min, g_temp.getCap(v_min)))
					g_temp.removeNode(v_min);
				temp_noF--;
				//x_add[v_min] += f_maxbw[f.id()]*g_temp.getPriceBandwidth() ;
				x_add[v_min] += f_maxbw[f.getId()] ;
				y+= x_add[v_min];
			}
			
		}
		//in ra file =======
		for (int i=0;i<noFunction;i++)
			for (int j=0;j<noVertex;j++)
			{
				if(function_Loc[i+1][j+1]>0)
				{
					//value_final+= (1-function_Loc[i+1][j+1])*Gain(g.getPriceNode(j+1));
					System.out.println("function:"+(i+1)+ " node: "+(j+1)+" quantity: "+ function_Loc[i+1][j+1]);
					out.write("function:"+(i+1)+ " node: "+(j+1)+" quantity: "+ function_Loc[i+1][j+1]);
					out.newLine();
				}
			}
//		for(int i=0;i<noFunction;i++)
//			for(int j=0;j<5;j++)
//				if(function_Loc[i+1][j+1]>0)
//					numberofCore+=function_Loc[i+1][j+1];
//		for(int i=0;i<noFunction;i++)
//			for(int j=5;j<15;j++)
//				if(function_Loc[i+1][j+1]>0)
//					numberofMidle+=function_Loc[i+1][j+1];
//		for(int i=0;i<noFunction;i++)
//			for(int j=15;j<noVertex;j++)
//				if(function_Loc[i+1][j+1]>0)
//					numberofEdge+=function_Loc[i+1][j+1];
		//value_final=val_final1;
		//B2. Find path for all demand
		//sort demand depend on decrease bandwidth
		int[] rank_service= new int[arrDemand.size()];
		boolean[] isNewDemand= new boolean[arrDemand.size()];
		
		boolean bol;
		if(arrDemand.size()>1)
		{
			for(int i=0;i<noNewDemand;i++)
				isNewDemand[i]=true;
			for(int i=noNewDemand;i<arrDemand.size();i++)
				isNewDemand[i]=false;
			for (int i=0;i<arrDemand.size();i++)
				rank_service[i]= i;
			for(int i=0;i<arrDemand.size()-1;i++)
			{
				int temp=i;
				for (int j=i+1;j<arrDemand.size();j++)
					if(arrDemand.get(rank_service[j]).getBw()>arrDemand.get(rank_service[temp]).getBw())
						temp=j;
				int k= rank_service[i];
				rank_service[i]=rank_service[temp];
				rank_service[temp]=k;
				bol = isNewDemand[i];
				isNewDemand[i]= isNewDemand[temp];
				isNewDemand[temp]=bol;
			}
		}
		else
		{
			rank_service[0]=0;
			isNewDemand[0]=true;
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
        		g_i.setEdgeWeight(e[id], g_temp.getEdgeWeight((j+1), (k+1)));
        		id++;
        	}
        }
        int i=0;
        //kiem tra neu i= noNewDemand -> het -> tru gia tri di
		while(i<arrDemand.size())
		{
			v_sol= new ArrayList<Integer>();
			f_sol= new ArrayList<Integer>();
			//newDemand _d= arrDemand.get(rank_service[i]);
			newDemand _d= arrDemand.get(rank_service[i]);
			
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
			ArrayList<Integer> fs = _d.getFunctions();//xet demand theo bandwidth
			
			List<List<Integer>> node = new ArrayList<List<Integer>>(noFunction);
			for (int j=0;j<fs.size();j++)
			{
				List<Integer> innerList = new ArrayList<Integer>();
				for (int k=0;k<noVertex;k++)
					for(int h=0;h<function_Loc[fs.get(j)][k+1];h++)
						innerList.add(k+1);
				node.add(innerList);				  
			}
			List<List<Integer>> shortest_tree = new ArrayList<List<Integer>>();// mang cac node cho moi duong di tu d den t
			List<Double> weight = new ArrayList<>();//gia tri cho moi duong di do
			
			System.out.println("demand: "+_d.getId());
			out.write("demand: "+ _d.getId());
			out.newLine();
			if(fs.size()==1)
			{
				boolean fl=false;
				List<Integer> _node = node.get(0);
				if (_node.contains(_d.getSrc()) )
				{
					//giam trong node
					node.set(0,new ArrayList<Integer>(Arrays.asList(_d.getSrc())));
					function_Loc[fs.get(0)][_d.getSrc()]--;
					v_sol.add(_d.getSrc());
					f_sol.add(fs.get(0));
					//ktra xem demand nay la old demand hay new demand;
					if(isNewDemand[i])
					{
						//new demand					
						value_final+=g.getPriceNode(_d.getSrc())*_d.getProcessTime()*0.001;//(ns)
					}
					else
					{
						value_final+=g.getPriceNode(_d.getSrc())*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001;
					}
					//value_final+=_d.getRate()*g.getPriceNode(_d.sourceS())*UtilizeFunction.value(fs[0].getLamda());
					fl=true;
				}
				else
				{
					if( _node.contains(_d.getDest()))
					{
						//giam trong node
						node.set(0,new ArrayList<Integer>(Arrays.asList(_d.getDest())));
						function_Loc[fs.get(0)][_d.getDest()]--;
						v_sol.add(_d.getDest());
						f_sol.add(fs.get(0));
						if(isNewDemand[i])
						{
							//new demand
							value_final+=g.getPriceNode(_d.getDest())*_d.getProcessTime()*0.001;
						}
						else
						{
							//old demand
							value_final+=g.getPriceNode(_d.getDest())*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001;
						}
						fl=true;
					}	
				}
				
				if(fl)
				{	//thuc hien tim duong di giua source va destinaion
					for( DefaultWeightedEdge v:g_i.edgeSet())
					{
						int int_s =Integer.parseInt(g_i.getEdgeSource(v).replaceAll("[\\D]", ""));
						int int_t =Integer.parseInt(g_i.getEdgeTarget(v).replaceAll("[\\D]", ""));
						if(g_temp.getEdgeWeight(int_s, int_t)<_d.getBw())
						//if(g_i.getEdgeWeight(v)<_d.bwS())
							removed_edge[no_removed_edge++]=v;				
					}
					for (int l=0;l<no_removed_edge;l++)
						g_i.removeEdge(removed_edge[l]);
					_p =   DijkstraShortestPath.findPathBetween(g_i, "node"+_d.getSrc(), "node"+_d.getDest());
					if(_p!=null)
					{
						for (DefaultWeightedEdge l:_p)
						{
							weight_path+=g_i.getEdgeWeight(l);
						}
						nodeList = new ArrayList<Integer>();
						nodeList.add(_d.getSrc());
						source=_d.getSrc();
						while (_p.size()>0)
						{	
							int ix =0;
							for(int l=0;l<_p.size();l++)
							{
								int int_s =Integer.parseInt(g_i.getEdgeSource(_p.get(l)).replaceAll("[\\D]", ""));
								int int_t =Integer.parseInt(g_i.getEdgeTarget(_p.get(l)).replaceAll("[\\D]", ""));
								if(isNewDemand[i])
								{
									//new demand
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
								}
								else
								{
									//old demand
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
								}
								if( int_s == source )
								{
									nodeList.add(int_t);
									source = int_t;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
								if( int_t == source)
								{
									nodeList.add(int_s);
									source = int_s;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
							}
							_p.remove(ix);
						}
						for(int _i:nodeList)
							{
								System.out.print(_i+",");
								out.write(_i+", ");
							}	
						out.newLine();						
					}
					else
					{
						System.out.print("1....");
						out.write("khong tim duojc duong di giua:"+ _d.getSrc() +" va "+_d.getDest());
						out.newLine();
						return false;
						
					}
				}
				else
				{
					//neu khong thi tim duong di tu source -> node, và từ node->destination
					int n_max= -1;
					double n_price_max = 0.0;
					for (int _intMax: _node)
						if(n_price_max < g_temp.getPriceNode(_intMax))
						{
							n_price_max = g_temp.getPriceNode(_intMax);
							n_max= _intMax;
						}
					function_Loc[fs.get(0)][n_max]--;
					v_sol.add(n_max);
					f_sol.add(fs.get(0));
					node.set(0,new ArrayList<Integer>(Arrays.asList(n_max)));
					if(isNewDemand[i])
					{
						value_final+=g.getPriceNode(n_max)*_d.getProcessTime()*0.001 ;
					}
					else
					{
						value_final+=g.getPriceNode(n_max)*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
					}
					
					for( DefaultWeightedEdge v:g_i.edgeSet())
					{
						int int_s =Integer.parseInt(g_i.getEdgeSource(v).replaceAll("[\\D]", ""));
						int int_t =Integer.parseInt(g_i.getEdgeTarget(v).replaceAll("[\\D]", ""));
						if(g_temp.getEdgeWeight(int_s, int_t)<_d.getBw())
							removed_edge[no_removed_edge++]=v;				
					}
					for (int l=0;l<no_removed_edge;l++)
						g_i.removeEdge(removed_edge[l]);
					_p =   DijkstraShortestPath.findPathBetween(g_i, "node"+_d.getSrc(), "node"+n_max);
					if(_p!=null)
					{
						for (DefaultWeightedEdge l:_p)
						{
							weight_path+=g_i.getEdgeWeight(l);
						}
						nodeList = new ArrayList<Integer>();
						nodeList.add(_d.getSrc());
						source=_d.getSrc();
						while (_p.size()>0)
						{	
							int ix =0;
							for(int l=0;l<_p.size();l++)
							{
								int int_s =Integer.parseInt(g_i.getEdgeSource(_p.get(l)).replaceAll("[\\D]", ""));
								int int_t =Integer.parseInt(g_i.getEdgeTarget(_p.get(l)).replaceAll("[\\D]", ""));
								if(isNewDemand[i])
								{
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;	
								}
								else
								{
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;	
								}
								if( int_s == source )
								{
									nodeList.add(int_t);
									source = int_t;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
								if( int_t == source)
								{
									nodeList.add(int_s);
									source = int_s;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
							}
							_p.remove(ix);
						}
						for(int _i:nodeList)
							{
								System.out.print(_i+",");
								out.write(_i+", ");
							}	
						out.newLine();
					}
					else
					{
						out.write("khong tim duojc duong di giua:"+ _d.getSrc() +" va "+n_max);
						out.newLine();
						return false;
						
					}
					for( DefaultWeightedEdge v:g_i.edgeSet())
					{
						int int_s =Integer.parseInt(g_i.getEdgeSource(v).replaceAll("[\\D]", ""));
						int int_t =Integer.parseInt(g_i.getEdgeTarget(v).replaceAll("[\\D]", ""));
						if(g_temp.getEdgeWeight(int_s, int_t)<_d.getBw())
							removed_edge[no_removed_edge++]=v;				
					}
					for (int l=0;l<no_removed_edge;l++)
						g_i.removeEdge(removed_edge[l]);
					_p =   DijkstraShortestPath.findPathBetween(g_i, "node"+n_max, "node"+_d.getDest());
					if(_p!=null)
					{
						for (DefaultWeightedEdge l:_p)
						{
							weight_path+=g_i.getEdgeWeight(l);
						}
						nodeList = new ArrayList<Integer>();
						nodeList.add(n_max);
						source=n_max;
						while (_p.size()>0)
						{	
							int ix =0;
							for(int l=0;l<_p.size();l++)
							{
								int int_s =Integer.parseInt(g_i.getEdgeSource(_p.get(l)).replaceAll("[\\D]", ""));
								int int_t =Integer.parseInt(g_i.getEdgeTarget(_p.get(l)).replaceAll("[\\D]", ""));
								if(isNewDemand[i])
								{
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001  ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001  ;	
								}
								else
								{
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001  ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001  ;									
								}
								if( int_s == source )
								{
									nodeList.add(int_t);
									source = int_t;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
								if( int_t == source)
								{
									nodeList.add(int_s);
									source = int_s;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
							}
							_p.remove(ix);
						}
					
						for(int _i:nodeList)
						{
							System.out.print(_i+",");
							out.write(_i+", ");
						}	
					out.newLine();	
				
				}
					else
					{
						out.write("khong tim duojc duong di giua:"+ _d.getDest() +" va "+n_max);
						out.newLine();
						return false;
						
					}

			}
			}
			else
			{
				//xet truong hop source + nguon + thanh phan thu nhat
				
				for( DefaultWeightedEdge v:g_i.edgeSet())
				{
					int int_s =Integer.parseInt(g_i.getEdgeSource(v).replaceAll("[\\D]", ""));
					int int_t =Integer.parseInt(g_i.getEdgeTarget(v).replaceAll("[\\D]", ""));
					if(g_temp.getEdgeWeight(int_s, int_t)<_d.getBw())
						removed_edge[no_removed_edge++]=v;				
				}
				for (int l=0;l<no_removed_edge;l++)
					g_i.removeEdge(removed_edge[l]);
				
				List<Integer> n_s = node.get(0);
				min_w = Double.MAX_VALUE;
				List<DefaultWeightedEdge> sht_path_temp=null;
				boolean _isSrc=false;
				for (int k=0;k<n_s.size();k++)
				{
					if(n_s.get(k)==_d.getSrc())
					{
						node.set(0,new ArrayList<Integer>(Arrays.asList(_d.getSrc())));
						_isSrc =true;
						break;
					}
				}
				if(!_isSrc)
				{
					for (int k=0;k<n_s.size();k++)
					{
					//tinh duong di tu  (source->n_s.get(k))
						weight_path=0;
						_p =   DijkstraShortestPath.findPathBetween(g_i, "node"+_d.getSrc(), "node"+n_s.get(k));
						if(_p!=null)
						{
							for (DefaultWeightedEdge l:_p)
							{
								weight_path+=g_i.getEdgeWeight(l);
							}
							if(weight_path < min_w)
							{
								min_w = weight_path;
								sht_path_temp = new ArrayList<DefaultWeightedEdge>();
								for (DefaultWeightedEdge l:_p)
								{
									sht_path_temp.add(l);
								}
								source=_d.getSrc();
								destination = n_s.get(k);
							}
						}
					}
					
					nodeList = new ArrayList<Integer>();
					// sau do chon duong ngan nhat 
					if(sht_path_temp!=null)
					{
						prevNode=source;
						nodeList.add(source);
						while (sht_path_temp.size()>0)
						{	
							int ix =0;
							for(int l=0;l<sht_path_temp.size();l++)
							{
								int int_s =Integer.parseInt(g_i.getEdgeSource(sht_path_temp.get(l)).replaceAll("[\\D]", ""));
								int int_t =Integer.parseInt(g_i.getEdgeTarget(sht_path_temp.get(l)).replaceAll("[\\D]", ""));
								if(isNewDemand[i])
								{
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
								}
								else
								{
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
								}
								if( int_s == source )
								{
									nodeList.add(int_t);
									source = int_t;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
								if( int_t == source)
								{
									nodeList.add(int_s);
									source = int_s;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
							}
							sht_path_temp.remove(ix);
						}
					}
					else
					{
						out.write("khong tim duojc duong di giua:"+ _d.getSrc() +" va f"+fs.get(0));
						out.newLine();
						return false;
						
					}
					weight.add(min_w);
					shortest_tree.add(nodeList);
					node.set(0,new ArrayList<Integer>(Arrays.asList(destination)));	
					//value_final+=UtilizeFunction.value(g.getPriceNode(destination))*UtilizeFunction.value(fs[0].getLamda());
					//function_Loc[fs[0].id()][destination]--;
					}				
				
					for(int j=0;j<fs.size()-1;j++)
					{
						//Tim duong di ngan nhat giua tung cap (i, i+1) trong fs
						//xet xem cap nao co duong di ngan nhat
						for( DefaultWeightedEdge v:g_i.edgeSet())
						{
							int int_s =Integer.parseInt(g_i.getEdgeSource(v).replaceAll("[\\D]", ""));
							int int_t =Integer.parseInt(g_i.getEdgeTarget(v).replaceAll("[\\D]", ""));
							if(g_temp.getEdgeWeight(int_s, int_t)<_d.getBw())
								removed_edge[no_removed_edge++]=v;				
						}
						for (int l=0;l<no_removed_edge;l++)
							g_i.removeEdge(removed_edge[l]);
						int t1=fs.get(j);
						int t2=fs.get(j+1);
						List<Integer> node_s = node.get(j);
						List<Integer> node_t = node.get(j+1);
						double min_weight = Double.MAX_VALUE;
						List<DefaultWeightedEdge> shortest_path_temp=null;
						boolean temflag=false;
						for (int k=0;k<node_s.size();k++)
						{
							int temp_s=node_s.get(k);
							for(int h=0;h<node_t.size();h++)
							{
								int temp_t=node_t.get(h);
								if(temp_t==temp_s)
								{
								//truong hop 2 function nằm trong 1 node
									min_weight=0;
									source=temp_s;
									destination = temp_t;
									break;
								}
								else
								{
									if(temp_t!=prevNode)
									{
										temflag=true;
										prevNode=temp_t;
									weight_path=0;
									_p =   DijkstraShortestPath.findPathBetween(g_i, "node"+temp_s, "node"+temp_t);
									if(_p!=null)
									{
									for (DefaultWeightedEdge l:_p)
									{
										weight_path+=g_i.getEdgeWeight(l);
									}
									if(weight_path < min_weight)
									{
										//chon duong nay
										min_weight = weight_path;
										shortest_path_temp = new ArrayList<DefaultWeightedEdge>();
										for (DefaultWeightedEdge l:_p)
										{
											shortest_path_temp.add(l);
										}
										source=temp_s;
										destination = temp_t;
									}
									}
									}
								}
							}
						}
						if(shortest_path_temp==null && !temflag)
						{
							for (int k=0;k<node_s.size();k++)
							{
								int temp_s=node_s.get(k);
								for(int h=0;h<node_t.size();h++)
								{
									int temp_t=node_t.get(h);
									weight_path=0;
									_p =   DijkstraShortestPath.findPathBetween(g_i, "node"+temp_s, "node"+temp_t);
									if(_p!=null)
									{
									for (DefaultWeightedEdge l:_p)
									{
										weight_path+=g_i.getEdgeWeight(l);
									}
									if(weight_path < min_weight)
									{
										//chon duong nay
										min_weight = weight_path;
										shortest_path_temp = new ArrayList<DefaultWeightedEdge>();
										for (DefaultWeightedEdge l:_p)
										{
											shortest_path_temp.add(l);
										}
										source=temp_s;
										destination = temp_t;
									}
								}
							}
						}
					}
					nodeList = new ArrayList<Integer>();
					if(min_weight==0)
					{
						nodeList.add(source);
						nodeList.add(destination);
						weight.add(min_weight);
						shortest_tree.add(nodeList);
						node.set(j+1, new ArrayList<Integer>(Arrays.asList(destination)));
						//value_final+=UtilizeFunction.value(g.getPriceNode(destination))*UtilizeFunction.value(fs[j+1].getLamda());
					}
					else
					{
						if(shortest_path_temp!=null)
						{
							nodeList.add(source);
							while (shortest_path_temp.size()>0)
							{	
								int ix =0;
								for(int l=0;l<shortest_path_temp.size();l++)
								{
									
									int int_s =Integer.parseInt(g_i.getEdgeSource(shortest_path_temp.get(l)).replaceAll("[\\D]", ""));
									int int_t =Integer.parseInt(g_i.getEdgeTarget(shortest_path_temp.get(l)).replaceAll("[\\D]", ""));
									if(isNewDemand[i])
									{
										value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
										value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
									}
									else
									{
										value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
										value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
									}
									if( int_s == source )
									{
										nodeList.add(int_t);
										source = int_t;
										ix = l;
										g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
										break;
									}
									if( int_t == source)
									{
										nodeList.add(int_s);
										source = int_s;
										ix = l;
										g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
										break;
									}
								}
								shortest_path_temp.remove(ix);
							}
						}
							else
							{
								out.write("khong tim duojc duong di giua:f"+ t1 +" va f"+t2);
								out.newLine();
								return false;
								
							}
					//nodeList.add(destination);
					weight.add(min_weight);
					shortest_tree.add(nodeList);
					node.set(j+1, new ArrayList<Integer>(Arrays.asList(destination)));
					
					}
					source= nodeList.get(0);
					function_Loc[t1][source]--;
					v_sol.add(source);
					f_sol.add(t1);
					if(isNewDemand[i])
					{
						value_final+=g.getPriceNode(source)*_d.getProcessTime()*0.001;
						ultilize_resource-= g.getPriceNode(source)*_d.getProcessTime()*0.001;
					}
					else
					{
						value_final+=g.getPriceNode(source)*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001;
						ultilize_resource-= g.getPriceNode(source)*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001;
					}
					if(j==fs.size()-2)
					{
						destination = nodeList.get(nodeList.size()-1);
						function_Loc[t2][destination]--;
						f_sol.add(t2);
						v_sol.add(destination);
						if(isNewDemand[i])
						{
							value_final+=g.getPriceNode(destination)*_d.getProcessTime()*0.001;
						}
						else
							value_final+=g.getPriceNode(destination)*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001;
					}
				}
				
				for( DefaultWeightedEdge v:g_i.edgeSet())
				{
					int int_s =Integer.parseInt(g_i.getEdgeSource(v).replaceAll("[\\D]", ""));
					int int_t =Integer.parseInt(g_i.getEdgeTarget(v).replaceAll("[\\D]", ""));
					if(g_temp.getEdgeWeight(int_s, int_t)<_d.getBw())
						removed_edge[no_removed_edge++]=v;				
				}
				for (int l=0;l<no_removed_edge;l++)
					g_i.removeEdge(removed_edge[l]);
				
				n_s = node.get(fs.size()-1);
				min_w = Double.MAX_VALUE;
				sht_path_temp=null;
				boolean _isDes=false;
				for (int k=0;k<n_s.size();k++)
				{
					if(n_s.get(k)==_d.getDest())
					{
						_isDes =true;
						break;
					}
					else
					{
						//tinh duong di tu  (n_s.get(k)->destination)
						weight_path=0;
						_p =   DijkstraShortestPath.findPathBetween(g_i,"node"+n_s.get(k), "node"+_d.getDest());
						if(_p!=null)
						{
						for (DefaultWeightedEdge l:_p)
						{
							weight_path+=g_i.getEdgeWeight(l);
						}
						if(weight_path < min_w)
						{
							//chon duong nay
							min_w = weight_path;
							sht_path_temp = new ArrayList<DefaultWeightedEdge>();
							for (DefaultWeightedEdge l:_p)
							{
								sht_path_temp.add(l);
							}
							source=n_s.get(k);
							destination = _d.getDest();
						}
						}
					}
					
				}
				if(!_isDes)
				{
					nodeList = new ArrayList<Integer>();
					// sau do chon duong ngan nhat 
					if(sht_path_temp!=null)
					{
						nodeList.add(source);
						while (sht_path_temp.size()>0)
						{	
							int ix =0;
							for(int l=0;l<sht_path_temp.size();l++)
							{
								//tinh gia tri cua duong di o day								
								int int_s =Integer.parseInt(g_i.getEdgeSource(sht_path_temp.get(l)).replaceAll("[\\D]", ""));
								int int_t =Integer.parseInt(g_i.getEdgeTarget(sht_path_temp.get(l)).replaceAll("[\\D]", ""));
								if(isNewDemand[i])
								{
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*_d.getProcessTime()*0.001 ;
								}
								else
								{
									value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
									value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth()*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001 ;
								}
								if( int_s == source )
								{
									nodeList.add(int_t);
									source = int_t;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
								if( int_t == source)
								{
									nodeList.add(int_s);
									source = int_s;
									ix = l;
									g_temp.setEdgeWeight(int_s, int_t, g_temp.getEdgeWeight(int_s, int_t)-_d.getBw());
									break;
								}
							}
							sht_path_temp.remove(ix);
						}
					}
					else
					{
						out.write("khong tim duojc duong di giua:f"+fs.get(fs.size()-1) +" va destination"+_d.getDest());
						out.newLine();
						return false;
					}
					weight.add(min_w);
					shortest_tree.add(nodeList);
					node.set(0,new ArrayList<Integer>(Arrays.asList(destination)));	
				}	
			
				for(List<Integer> _list:shortest_tree)
				{
					out.write("[");
					for(int _i:_list)
					{
						System.out.print(_i+",");
						out.write(_i+", ");
					}
					System.out.println();
					out.write("], ");
				}			
				out.newLine();
			}
			//doi voi old demand
			if(!isNewDemand[i])
			{
				double co=0.0;
				for(int j=0;j<id_oldDemand.size();j++)
					if(id_oldDemand.get(j)==_d.getId())
					{
						co=cost_oldDemand.get(j);
						break;
					}
				value_final-=co*(_d.getProcessTime()-currentTime+_d.getArrivalTime())*0.001;
			}
			v_solution.add(v_sol);
			f_solution.add(f_sol);
			i++;
		}
	
		for(int j=i-noNewDemand;j<noOldDemand;j++)
		{
			double co=0.0;
			for(int k=0;k<id_oldDemand.size();k++)
				if(id_oldDemand.get(k)==oldDemandArray.get(rank_d[j]).GetID())
				{
					co=cost_oldDemand.get(k);
					break;
				}
			value_final+=co*(oldDemandArray.get(rank_d[j]).GetProcessTime()-currentTime+ oldDemandArray.get(rank_d[j]).GetArrivalTime() )*0.001;
		}
		
		_duration = System.currentTimeMillis() - startTime;
		System.out.println(_duration);
		out.write("Value solution: "+ value_final);
		out.newLine();
		out.write("Runtime (mS): "+ _duration);
		out.write("Value Bandwidth: "+ value_bandwidth);
		out.write("Resource ultilization: "+ ultilize_resource);
		
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
		
	public static boolean nonNFV(String outFile)
	{
		value_final=0;
		value_bandwidth=0;
		ultilize_resource=0;
		_duration=0;
		for (int i=0;i<noVertex;i++)
		{
			ultilize_resource +=UtilizeFunction.value(g.getCap(i+1)) * g.getPriceNode(i+1);				
		}
		List<DefaultWeightedEdge> _p;
		List<Integer> nodeList;
		final long startTime = System.currentTimeMillis();
		
		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
			int[] rank_service= new int[noNewDemand];
			if(noNewDemand>1)
			{
				for (int i=0;i<noNewDemand;i++)
					rank_service[i]= i+1;
				for(int i=0;i<noNewDemand-1;i++)
				{
					int temp=i;
					for (int j=i+1;j<noNewDemand;j++)
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
			while(i<noNewDemand)
			{
				//tim duong di cho moi demand
				//tuy thuoc vao bandwidth
				newDemand _d= getDemand(rank_service[i]);
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
							value_bandwidth +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth() ;
							value_final +=g.getEdgeWeight(int_s, int_t) * g.getPriceBandwidth() ;	
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
						System.out.print(_i+",");
						out.write(_i+", ");
					}
					System.out.println();
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
			_duration = System.currentTimeMillis() - startTime;
			System.out.println(_duration);
			out.write("Runtime (mS): "+ _duration);
			out.newLine();
			out.write("Value bandwidth: "+ value_bandwidth);
			
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
		private static double Gain(double u) {
		double temp= u/10;
		if(temp>0)
			return Math.sqrt(temp);
		else
			return 0;
		}
	
	public static void newModel (String outFile)
	{

		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
			out.write("number of function:" + noFunction);
			out.newLine();
			for (int i=0;i<noFunction;i++)
	       	{	 
	               out.write(functionArr.get(i).toString());
	               out.newLine();
	       	}
	   		out.write("number of newDemand:" + noNewDemand);
	   		out.newLine();
	       	for (int i=0;i<noNewDemand;i++)
	       	{    		
	       		out.write(newDemandArray.get(i).toString());
	       		out.newLine();
	       	}
	       	out.write("virtual node:"+ noVertex);
//	       	out.newLine();
//	       	for (int i=0;i<noVertex;i++)
//	       	{
//	       		for (int j=0;j<noVertex;j++)
//	       			out.write(g.getEdgeWeight(i, j) + " ");
//	       		out.newLine();
//	       	}
	       	
	       	
			try{
				GRBEnv env = new GRBEnv("qp.log");
				env.set(GRB.DoubleParam.MIPGap, 0.000000001);
				env.set(GRB.DoubleParam.FeasibilityTol, 0.000000001);
				env.set(GRB.IntParam.Threads,8);
				env.set(GRB.DoubleParam.TimeLimit,4000);
				GRBModel model = new GRBModel(env);
				GRBQuadExpr obj = new GRBQuadExpr();
				int constant=1;
				r_min=0;
				a_min=0;
				
				ArrayList<newDemand> arrDemand = new ArrayList<newDemand>();
				for(int i=0;i<newDemandArray.size();i++)
					arrDemand.add(newDemandArray.get(i));
//				for(int i=0;i<oldDemandArray.size();i++)
//				{
//					oldDemand _old= oldDemandArray.get(i);
//					arrDemand.add(new newDemand(_old.GetID(), _old.GetSrc(), _old.GetDest(), _old.GetArrivalTime(), _old.GetProcessTime(), _old.GetBandwidth(), _old.GetRate(), _old.GetSetFunc()));
//					ArrayList<Integer> f_sol=_old.Get_f_sol();
//					ArrayList<Integer> v_sol=_old.Get_v_sol();
//					for(int j=0;j<f_sol.size();j++)
//					{
//						if(f_sol.get(j)>0)
//						{
//							//update capacity for node
//							g.addCap(v_sol.get(j), getLamdaF(f_sol.get(j)));
//						}
//					}
//					for(int j=0;j<v_sol.size()-1;j++)
//						for(int k=j+1;k<v_sol.size();k++)
//						{
//							g.addEdgeWeight(v_sol.get(j), v_sol.get(k), _old.GetBandwidth());
//						}
//				}
				
				
				//variable declaire
				
			
				for(int i = 0; i < arrDemand.size(); i++) 
					for(int k = 0; k < noFunction; k++)
						for(int j = 0; j < noVertex; j++)
				    		{
				    			String st = "x1["+(i)+ "]["+(k)+ "]["+(j)+ "]";
				    			x1[i][k][j] = model.addVar(0, 1, 0, GRB.BINARY, st);
				    		}
				for(int i = 0; i < arrDemand.size(); i++) 
				    for(int j = 0; j < noVertex; j++)
				    	for(int k = 0; k < noVertex; k++)
				    		{
				    			String st = "y["+(i)+ "]["+(j)+ "]["+(k)+ "]";
				    			y[i][j][k] = model.addVar(0, 1.0, 1.0, GRB.BINARY, st);
				    		}
				r=model.addVar(0, 1, 0, GRB.CONTINUOUS, "r");
				for(int i = 0; i < arrDemand.size(); i++) 
				{
	    			String st = "z1["+(i)+ "]";
	    			z1[i] = model.addVar(0, 1, 0, GRB.BINARY, st);
				}
				
				model.update();
				//obj.addTerm(0.5, r);
//				for(int i = 0; i < arrDemand.size(); i++) 
//					obj.addTerm(-0.5, z1[i]);
				//ham muc tieu
				for (int j1=0;j1<noVertex;j1++)
					for(int j2=0;j2<noVertex;j2++)
					{
						if(g.getEdgeWeight(j1+1, j2+1)>0)
						{
							for(int i = 0; i < arrDemand.size(); i++) 
							{
								obj.addTerm(arrDemand.get(i).getBw()/(constant*g.getEdgeWeight(j1+1, j2+1)), y[i][j1][j2]);
							}
						}
					}
				for(int j = 0; j < noVertex; j++)
					for(int i = 0; i < arrDemand.size(); i++)
						for(int k = 0; k < noFunction; k++)
				    		{
				    			obj.addTerm(g.getPriceNode(j+1), x1[i][k][j]);
				    		}
				model.setObjective(obj,GRB.MINIMIZE);		
//				model.setObjective(obj);
				//add constraints
				
//				-> them
				for(int i = 0; i < arrDemand.size(); i++) 
					for (int j1=0;j1<noVertex;j1++)
						for (int j2=0;j2<noVertex;j2++)
				{
							if(j1==j2)
							{
								GRBLinExpr expry= new GRBLinExpr();
				    			expry.addTerm(1, y[i][j1][j2]);
				    			String st = "n["+(i)+"]";
								model.addConstr(expry, GRB.EQUAL, 0 , st);
							}
					
				}
//				
				// Eq 5 ->Ok
				for(int compo=0;compo<3;compo++)
				{
					for(int j = 0; j < noVertex; j++) //node
			    	{
						GRBLinExpr expr1= new GRBLinExpr();
						for(int i = 0; i < arrDemand.size(); i++) //demand
							for(int k = 0; k < noFunction; k++) //function
									expr1.addTerm(getFunction(k+1).getLamda().get(compo),x1[i][k][j]);
						String st = "c["+(j)+ ","+compo+"]";
						model.addConstr(expr1, GRB.LESS_EQUAL, g.getCap(j+1).get(compo) , st);
						expr1 = null;
			    	}
				}
				System.gc();
				
				//Eq 6 ->OK
				for (int j1=0;j1<noVertex;j1++)
					for(int j2=0;j2<noVertex;j2++)
					{
						GRBLinExpr expr2= new GRBLinExpr();
						for (int i =0;i<arrDemand.size();i++) //demand
						{
							expr2.addTerm(arrDemand.get(i).getBw(),y[i][j1][j2]);
						}
						expr2.addTerm(-g.getEdgeWeight(j1+1, j2+1),r);
						String st = "d["+(j1+1)+ "]["+(j2+1)+ "]";
						model.addConstr(expr2, GRB.LESS_EQUAL,0, st);
						expr2 = null;	
					}
					
//				//rang buoc 1 ->Ok
//				for(int compo=0;compo<3;compo++)
//				{
//					for(int j = 0; j < noVertex; j++) //node
//			    	{
//						GRBQuadExpr expr1= new GRBQuadExpr();
//						for(int i = 0; i < arrDemand.size(); i++) //demand
//							for(int k = 0; k < noFunction; k++) //function
//									expr1.addTerm(getFunction(k+1).getLamda().get(compo),z1[i],x1[i][k][j]);
//						String st = "c["+(j)+ ","+compo+"]";
//						model.addQConstr(expr1, GRB.LESS_EQUAL, g.getCap(j+1).get(compo) , st);
//						expr1 = null;
//			    	}
//				}
//				System.gc();
//				
//				//rang buoc 2 ->OK
//				for (int j1=0;j1<noVertex;j1++)
//					for(int j2=0;j2<noVertex;j2++)
//					{
//						GRBQuadExpr expr2= new GRBQuadExpr();
//						for (int i =0;i<arrDemand.size();i++) //demand
//						{
//							expr2.addTerm(arrDemand.get(i).getBw(),z1[i],y[i][j1][j2]);
//							expr2.addTerm(arrDemand.get(i).getBw(),z1[i],y[i][j2][j1]);
//						}
//						expr2.addTerm(-g.getEdgeWeight(j1+1, j2+1),r);
//						String st = "d["+(j1+1)+ "]["+(j2+1)+ "]";
//						model.addQConstr(expr2, GRB.LESS_EQUAL,0, st);
//						expr2 = null;	
//					}
//					
							
				
				
				System.gc();
//				
				
//				//rang buoc 3
//				for(int j = 0; j < noVertex; j++)
//				{
//					GRBLinExpr expr2= new GRBLinExpr();
//					for(int i = 0; i < arrDemand.size(); i++)
//						for(int k = 0; k < noFunction; k++)
//			    		{
//							expr2.addTerm(1,x1[i][k][j]);
//			    		}
//					String st = "dd["+(j+1)+ "]";
//					model.addConstr(expr2, GRB.LESS_EQUAL,limitedNo, st);
//					expr2 = null;	
//				}
				
				
				//rang buoc 4  (Eq )
			
				for (int k = 0;k<noFunction;k++)
				{
					
					for (int i =0;i<arrDemand.size();i++) //demand
					{
						
						int id = arrDemand.get(i).getOrderFunction(k+1);
						GRBQuadExpr expr5= new GRBQuadExpr();	
						for (int j1=0;j1<noVertex;j1++)
						{
							if(j1==arrDemand.get(i).getSrc()-1)
							{
								for (int j2=0;j2<noVertex;j2++)
								{
									expr5.addTerm(1, x1[i][k][j1],y[i][j1][j2]);
								}
							}
							else
							{
								for (int j2=0;j2<noVertex;j2++)
								{
									expr5.addTerm(1, x1[i][k][j1],y[i][j2][j1]);
								}
							}
							
						}
						expr5.addTerm(-1, z1[i]);
						String st = "ff["+(k)+ "t]";
						if (id!=0)//truong hop function in demand =1
						{
							
							model.addQConstr(expr5, GRB.EQUAL, 0, st);
						}
//						else
//							model.addQConstr(expr5, GRB.EQUAL, 0, st);
						
						expr5 = null;
					}
					
					
				}
				
				
				//Eq 10->ok
				for (int i=0;i<arrDemand.size();i++)
				{
					int desti = arrDemand.get(i).getDest();
					int source = arrDemand.get(i).getSrc();
					for (int j1=0;j1<noVertex;j1++)
					{
						GRBLinExpr expr7= new GRBLinExpr();
						for (int j2=0;j2<noVertex;j2++)
						{
							expr7.addTerm(-1, y[i][j2][j1]);
							expr7.addTerm(1, y[i][j1][j2]);
						}
						String st = "h["+(i)+ "]["+(j1+1)+  "s]";
						if(j1 !=source-1 && j1 !=desti-1)
						{
							
							model.addConstr(expr7, GRB.EQUAL, 0, st);
							expr7 = null;
						}
						else
						{
							if(j1==source-1)
							{
								expr7.addTerm(-1, z1[i]);
								model.addConstr(expr7, GRB.EQUAL, 0, st);
								expr7 = null;
							}
							if(j1==desti-1)
							{
								expr7.addTerm(1, z1[i]);
								model.addConstr(expr7, GRB.EQUAL, 0, st);
								expr7 = null;
							}
						}
					}
					
				}
				System.gc();
				
//				//rang buoc 6
//				for(int i = 0; i < arrDemand.size(); i++)
//					for(int k = 0; k < noFunction; k++)
//					{
//						int id = arrDemand.get(i).getOrderFunction(k+1);
//						if (id!=0)//truong hop function in demand =1
//						{
//							GRBQuadExpr expr2= new GRBQuadExpr();
//							for(int j = 0; j < noVertex; j++)
//				    		{
//								expr2.addTerm(1,z1[i],x1[i][k][j]);
//				    		}
//							String st = "dy["+(i+1)+ "]["+(k+1)+ "]";
//							model.addQConstr(expr2, GRB.GREATER_EQUAL,0.5, st);
//							expr2 = null;	
//						}
//						
//					}
				
			//rang buoc 7
				for(int i = 0; i < arrDemand.size(); i++) 
				{
					GRBQuadExpr expr5= new GRBQuadExpr();
				    for(int j1 = 0; j1 < noVertex; j1++)
				    	for(int j2 = 0; j2 < noVertex; j2++)
				    	{
				    		expr5.addTerm(1, z1[i],y[i][j1][j2]);
				    		expr5.addTerm(-1, y[i][j1][j2]);
				    		expr5.addTerm(1, z1[i],y[i][j2][j1]);
				    		expr5.addTerm(-1, y[i][j2][j1]);
				    	}
				    String st = "dk["+(i+1)+ "]";
					model.addQConstr(expr5, GRB.GREATER_EQUAL,-0.1, st);
					expr5 = null;
				}
				for(int i = 0; i < arrDemand.size(); i++) 
				{
					GRBQuadExpr expr5= new GRBQuadExpr();
				    for(int j1 = 0; j1 < noVertex; j1++)
				    	for(int j2 = 0; j2 < noVertex; j2++)
				    	{
				    		expr5.addTerm(1, z1[i],y[i][j1][j2]);
				    		expr5.addTerm(-1, y[i][j1][j2]);
				    		expr5.addTerm(1, z1[i],y[i][j2][j1]);
				    		expr5.addTerm(-1, y[i][j2][j1]);
				    	}
				    String st = "dk["+(i+1)+ "]";
					model.addQConstr(expr5, GRB.EQUAL,0, st);
					expr5 = null;
				}
				
				//rang buoc 8
				for(int i = 0; i < arrDemand.size(); i++) 
				{
					GRBQuadExpr expr5= new GRBQuadExpr();
					for(int k = 0; k < noFunction; k++)
						for(int j = 0; j < noVertex; j++)
				    	{
				    		expr5.addTerm(1, z1[i],x1[i][k][j]);
				    		expr5.addTerm(-1, x1[i][k][j]);
				    	}
				    String st = "dx["+(i+1)+ "]";
					model.addQConstr(expr5, GRB.GREATER_EQUAL,-0.1, st);
					expr5 = null;
				}
				

				
				for(int i = 0; i < arrDemand.size(); i++) 
				for (int j1=0;j1<noVertex-1;j1++)
					for (int j2=j1+1;j2<noVertex;j2++)
						{
							GRBQuadExpr expry= new GRBQuadExpr();
			    			expry.addTerm(1, y[i][j1][j2],y[i][j2][j1]);
			    			String st = "nn["+(i+1)+ "]["+(j1+1)+ "]["+(j2+1)+"]";
							model.addQConstr(expry, GRB.EQUAL, 0 , st);
						}
				
				// Optimize model
				try {
					
					model.optimize();
					
					out.write("Solution for the problem:");
					out.newLine();
				
					int optimstatus = model.get(GRB.IntAttr.Status); 
					if (optimstatus == GRB.Status.OPTIMAL) 
					{ 
						r_min= r.get(GRB.DoubleAttr.X);
						value_final = obj.getValue();
						out.write("Objective optimal Value: "+obj.getValue());
						out.newLine();
						for(int i = 0; i < arrDemand.size(); i++) 
							for(int k = 0; k < noFunction; k++)
								for(int j = 0; j < noVertex; j++)
						    		{	
						    			if(x1[i][k][j].get(GRB.DoubleAttr.X)>0)
						    			{
						    			out.write(x1[i][k][j].get(GRB.StringAttr.VarName)
						    					+ " : " +x1[i][k][j].get(GRB.DoubleAttr.X));
						    			out.newLine();
						    			}
						    		}
						for(int i = 0; i < arrDemand.size(); i++) 
						    for(int j1 = 0; j1 < noVertex; j1++)
						    	for(int j2 = 0; j2 < noVertex; j2++)
						    		{	
						    			if(y[i][j1][j2].get(GRB.DoubleAttr.X)>0)
						    			{
						    			out.write(y[i][j1][j2].get(GRB.StringAttr.VarName)
						    					+ " : " +y[i][j1][j2].get(GRB.DoubleAttr.X));
						    			out.newLine();
						    			}
						    		}
						for(int i = 0; i < arrDemand.size(); i++)
							if(z1[i].get(GRB.DoubleAttr.X)>0)
			    			{
								a_min++;
			    			out.write(z1[i].get(GRB.StringAttr.VarName)
			    					+ " : " +z1[i].get(GRB.DoubleAttr.X));
			    			out.newLine();
			    			}
						out.write(r.get(GRB.StringAttr.VarName)
		    					+ " : " +r.get(GRB.DoubleAttr.X));
		    			out.newLine();
				
					 } else if (optimstatus == GRB.Status.INF_OR_UNBD) 
					 	{ 
					        System.out.println("Model is infeasible or unbounded"); 
					        return;
					 	} else if (optimstatus == GRB.Status.INFEASIBLE) 
					        	{ 
							        System.out.println("Model is infeasible AAAAAAAAAAAAAA"); 
							        return; 
					        	} else if (optimstatus == GRB.Status.INTERRUPTED)
					        	{
					        		r_min= r.get(GRB.DoubleAttr.X);
					        		value_final = obj.getValue();
					        		out.write("Objective interrupt Value: "+obj.getValue());
									out.newLine();
									for(int i = 0; i < arrDemand.size(); i++) 
									for(int k = 0; k < noFunction; k++)
										for(int j = 0; j < noVertex; j++)
								    		{	
								    			if(x1[i][k][j].get(GRB.DoubleAttr.X)>0)
								    			{
								    			out.write(x1[i][k][j].get(GRB.StringAttr.VarName)
								    					+ " : " +x1[i][k][j].get(GRB.DoubleAttr.X));
								    			out.newLine();
								    			}
								    		}
									for(int i = 0; i < arrDemand.size(); i++) 
									    for(int j1 = 0; j1 < noVertex; j1++)
									    	for(int j2 = 0; j2 < noVertex; j2++)
									    		{	
									    			if(y[i][j1][j2].get(GRB.DoubleAttr.X)>0)
									    			{
									    			out.write(y[i][j1][j2].get(GRB.StringAttr.VarName)
									    					+ " : " +y[i][j1][j2].get(GRB.DoubleAttr.X));
									    			out.newLine();
									    			}
									    		}
									for(int i = 0; i < arrDemand.size(); i++)
										if(z1[i].get(GRB.DoubleAttr.X)>0)
						    			{
											a_min++;
						    			out.write(z1[i].get(GRB.StringAttr.VarName)
						    					+ " : " +z1[i].get(GRB.DoubleAttr.X));
						    			out.newLine();
						    			}
									out.write(r.get(GRB.StringAttr.VarName)
					    					+ " : " +r.get(GRB.DoubleAttr.X));
					    			out.newLine();
					        		
					        	}
					
					 else
					 {
						 r_min= r.get(GRB.DoubleAttr.X);
						 value_final = obj.getValue();
						 out.write("Objective feasible Value: "+obj.getValue());
						 out.newLine();
							for(int i = 0; i < arrDemand.size(); i++) 
							for(int k = 0; k < noFunction; k++)
								for(int j = 0; j < noVertex; j++)
						    		{	
						    			if(x1[i][k][j].get(GRB.DoubleAttr.X)>0)
						    			{
						    			out.write(x1[i][k][j].get(GRB.StringAttr.VarName)
						    					+ " : " +x1[i][k][j].get(GRB.DoubleAttr.X));
						    			out.newLine();
						    			}
						    		}
							for(int i = 0; i < arrDemand.size(); i++) 
							    for(int j1 = 0; j1 < noVertex; j1++)
							    	for(int j2 = 0; j2 < noVertex; j2++)
							    		{	
							    			if(y[i][j1][j2].get(GRB.DoubleAttr.X)>0)
							    			{
							    			out.write(y[i][j1][j2].get(GRB.StringAttr.VarName)
							    					+ " : " +y[i][j1][j2].get(GRB.DoubleAttr.X));
							    			out.newLine();
							    			}
							    		}
							for(int i = 0; i < arrDemand.size(); i++)
								if(z1[i].get(GRB.DoubleAttr.X)>0)
				    			{
									a_min++;
				    			out.write(z1[i].get(GRB.StringAttr.VarName)
				    					+ " : " +z1[i].get(GRB.DoubleAttr.X));
				    			out.newLine();
				    			}
							out.write(r.get(GRB.StringAttr.VarName)
			    					+ " : " +r.get(GRB.DoubleAttr.X));
			    			out.newLine();
							
					  }
				
					
				} catch (Exception e) {
					r_min= r.get(GRB.DoubleAttr.X);
					value_final = obj.getValue();
					out.write("Objective interrupt Value: "+obj.getValue());
					out.newLine();
					for(int i = 0; i < arrDemand.size(); i++) 
					for(int k = 0; k < noFunction; k++)
						for(int j = 0; j < noVertex; j++)
				    		{	
				    			if(x1[i][k][j].get(GRB.DoubleAttr.X)>0)
				    			{
				    			out.write(x1[i][k][j].get(GRB.StringAttr.VarName)
				    					+ " : " +x1[i][k][j].get(GRB.DoubleAttr.X));
				    			out.newLine();
				    			}
				    		}
					for(int i = 0; i < arrDemand.size(); i++) 
					    for(int j1 = 0; j1 < noVertex; j1++)
					    	for(int j2 = 0; j2 < noVertex; j2++)
					    		{	
					    			if(y[i][j1][j2].get(GRB.DoubleAttr.X)>0)
					    			{
					    			out.write(y[i][j1][j2].get(GRB.StringAttr.VarName)
					    					+ " : " +y[i][j1][j2].get(GRB.DoubleAttr.X));
					    			out.newLine();
					    			}
					    		}
					for(int i = 0; i < arrDemand.size(); i++)
						if(z1[i].get(GRB.DoubleAttr.X)>0)
		    			{
							a_min++;
		    			out.write(z1[i].get(GRB.StringAttr.VarName)
		    					+ " : " +z1[i].get(GRB.DoubleAttr.X));
		    			out.newLine();
		    			}
					out.write(r.get(GRB.StringAttr.VarName)
	    					+ " : " +r.get(GRB.DoubleAttr.X));
	    			out.newLine();
					
	
				}
					model.dispose();
				env.dispose();
				System.gc();
			
				} catch(GRBException e3){			
					System.out.println("Error code1: " + e3.getErrorCode() + ". " +
							e3.getMessage());
					System.out.print("This problem can't be solved");
					
					
					}
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
	
	}
	public static void model2( String outFile)//chua lam
	{
		try {
			File file = new File(outFile);
			out = new BufferedWriter(new FileWriter(file));
			out.write("number of function:" + noFunction);
			out.newLine();
			for (int i=0;i<noFunction;i++)
	       	{	 
	               out.write(functionArr.get(i).toString());
	               out.newLine();
	       	}
	   		out.write("number of newDemand:" + noNewDemand);
	   		out.newLine();
	       	for (int i=0;i<noNewDemand;i++)
	       	{    		
	       		out.write(newDemandArray.get(i).toString());
	       		out.newLine();
	       	}
	       	out.write("virtual node:"+ noVertex);
//	       	out.newLine();
//	       	for (int i=0;i<noVertex;i++)
//	       	{
//	       		for (int j=0;j<noVertex;j++)
//	       			out.write(g.getEdgeWeight(i, j) + " ");
//	       		out.newLine();
//	       	}
	       	
	       	
			try{
				GRBEnv env = new GRBEnv("qp.log");
				env.set(GRB.DoubleParam.MIPGap, 0.000000001);
				env.set(GRB.DoubleParam.FeasibilityTol, 0.000000001);
				env.set(GRB.IntParam.Threads,8);
				env.set(GRB.DoubleParam.TimeLimit,4000);
				GRBModel model = new GRBModel(env);
				GRBQuadExpr obj = new GRBQuadExpr();
				int constant=1;
				r_min=0;
				a_min=0;
				
				ArrayList<newDemand> arrDemand = new ArrayList<newDemand>();
				for(int i=0;i<newDemandArray.size();i++)
					arrDemand.add(newDemandArray.get(i));
//				for(int i=0;i<oldDemandArray.size();i++)
//				{
//					oldDemand _old= oldDemandArray.get(i);
//					arrDemand.add(new newDemand(_old.GetID(), _old.GetSrc(), _old.GetDest(), _old.GetArrivalTime(), _old.GetProcessTime(), _old.GetBandwidth(), _old.GetRate(), _old.GetSetFunc()));
//					ArrayList<Integer> f_sol=_old.Get_f_sol();
//					ArrayList<Integer> v_sol=_old.Get_v_sol();
//					for(int j=0;j<f_sol.size();j++)
//					{
//						if(f_sol.get(j)>0)
//						{
//							//update capacity for node
//							g.addCap(v_sol.get(j), getLamdaF(f_sol.get(j)));
//						}
//					}
//					for(int j=0;j<v_sol.size()-1;j++)
//						for(int k=j+1;k<v_sol.size();k++)
//						{
//							g.addEdgeWeight(v_sol.get(j), v_sol.get(k), _old.GetBandwidth());
//						}
//				}
				
				
				//variable declaire
				
			
				for(int i = 0; i < arrDemand.size(); i++) 
					for(int k = 0; k < noFunction; k++)
						for(int j = 0; j < noVertex; j++)
				    		{
				    			String st = "x1["+(i)+ "]["+(k)+ "]["+(j)+ "]";
				    			x1[i][k][j] = model.addVar(0, 1, 0, GRB.BINARY, st);
				    		}
				for(int i = 0; i < arrDemand.size(); i++) 
				    for(int j = 0; j < noVertex; j++)
				    	for(int k = 0; k < noVertex; k++)
				    		{
				    			String st = "y["+(i)+ "]["+(j)+ "]["+(k)+ "]";
				    			y[i][j][k] = model.addVar(0, 1.0, 1.0, GRB.BINARY, st);
				    		}
				r=model.addVar(0, 1, 0, GRB.CONTINUOUS, "r");
				for(int i = 0; i < arrDemand.size(); i++) 
				{
	    			String st = "z1["+(i)+ "]";
	    			z1[i] = model.addVar(0, 1, 0, GRB.BINARY, st);
				}
				
				model.update();
				obj.addTerm(0.5, r);
				for(int i = 0; i < arrDemand.size(); i++) 
					obj.addTerm(-0.5, z1[i]);
				//ham muc tieu
//				for (int j1=0;j1<noVertex;j1++)
//					for(int j2=0;j2<noVertex;j2++)
//					{
//						if(g.getEdgeWeight(j1+1, j2+1)>0)
//						{
//							for(int i = 0; i < arrDemand.size(); i++) 
//							{
//								obj.addTerm(arrDemand.get(i).getBw()/(constant*g.getEdgeWeight(j1+1, j2+1)), y[i][j1][j2]);
//							}
//						}
//					}
//				for(int j = 0; j < noVertex; j++)
//					for(int i = 0; i < arrDemand.size(); i++)
//						for(int k = 0; k < noFunction; k++)
//				    		{
//				    			obj.addTerm(1, x1[i][k][j]);
//				    		}
				model.setObjective(obj,GRB.MINIMIZE);		
//				model.setObjective(obj);
				//add constraints
				
//				
				for(int i = 0; i < arrDemand.size(); i++) 
					for (int j1=0;j1<noVertex;j1++)
						for (int j2=0;j2<noVertex;j2++)
				{
							if(j1==j2)
							{
								GRBLinExpr expry= new GRBLinExpr();
				    			expry.addTerm(1, y[i][j1][j2]);
				    			String st = "n["+(i)+"]";
								model.addConstr(expry, GRB.EQUAL, 0 , st);
							}
					
				}
//				
				//rang buoc 1 ->Ok
				for(int compo=0;compo<3;compo++)
				{
					for(int j = 0; j < noVertex; j++) //node
			    	{
						GRBLinExpr expr1= new GRBLinExpr();
						for(int i = 0; i < arrDemand.size(); i++) //demand
							for(int k = 0; k < noFunction; k++) //function
									expr1.addTerm(getFunction(k+1).getLamda().get(compo),x1[i][k][j]);
						String st = "c["+(j)+ ","+compo+"]";
						model.addConstr(expr1, GRB.LESS_EQUAL, g.getCap(j+1).get(compo) , st);
						expr1 = null;
			    	}
				}
				System.gc();
				
				//rang buoc 2 ->OK
				for (int j1=0;j1<noVertex;j1++)
					for(int j2=0;j2<noVertex;j2++)
					{
						GRBLinExpr expr2= new GRBLinExpr();
						for (int i =0;i<arrDemand.size();i++) //demand
						{
							expr2.addTerm(arrDemand.get(i).getBw(),y[i][j1][j2]);
						}
						expr2.addTerm(-g.getEdgeWeight(j1+1, j2+1),r);
						String st = "d["+(j1+1)+ "]["+(j2+1)+ "]";
						model.addConstr(expr2, GRB.LESS_EQUAL,0, st);
						expr2 = null;	
					}
					
//				//rang buoc 1 ->Ok
//				for(int compo=0;compo<3;compo++)
//				{
//					for(int j = 0; j < noVertex; j++) //node
//			    	{
//						GRBQuadExpr expr1= new GRBQuadExpr();
//						for(int i = 0; i < arrDemand.size(); i++) //demand
//							for(int k = 0; k < noFunction; k++) //function
//									expr1.addTerm(getFunction(k+1).getLamda().get(compo),z1[i],x1[i][k][j]);
//						String st = "c["+(j)+ ","+compo+"]";
//						model.addQConstr(expr1, GRB.LESS_EQUAL, g.getCap(j+1).get(compo) , st);
//						expr1 = null;
//			    	}
//				}
//				System.gc();
//				
//				//rang buoc 2 ->OK
//				for (int j1=0;j1<noVertex;j1++)
//					for(int j2=0;j2<noVertex;j2++)
//					{
//						GRBQuadExpr expr2= new GRBQuadExpr();
//						for (int i =0;i<arrDemand.size();i++) //demand
//						{
//							expr2.addTerm(arrDemand.get(i).getBw(),z1[i],y[i][j1][j2]);
//							expr2.addTerm(arrDemand.get(i).getBw(),z1[i],y[i][j2][j1]);
//						}
//						expr2.addTerm(-g.getEdgeWeight(j1+1, j2+1),r);
//						String st = "d["+(j1+1)+ "]["+(j2+1)+ "]";
//						model.addQConstr(expr2, GRB.LESS_EQUAL,0, st);
//						expr2 = null;	
//					}
//					
							
				
				
				System.gc();
				
				//rang buoc 3
				for(int j = 0; j < noVertex; j++)
				{
					GRBLinExpr expr2= new GRBLinExpr();
					for(int i = 0; i < arrDemand.size(); i++)
						for(int k = 0; k < noFunction; k++)
			    		{
							expr2.addTerm(1,x1[i][k][j]);
			    		}
					String st = "dd["+(j+1)+ "]";
					model.addConstr(expr2, GRB.LESS_EQUAL,limitedNo, st);
					expr2 = null;	
				}
				
				
				//rang buoc 4
			
				for (int k = 0;k<noFunction;k++)
				{
					
					for (int i =0;i<arrDemand.size();i++) //demand
					{
						
						int id = arrDemand.get(i).getOrderFunction(k+1);
						GRBQuadExpr expr5= new GRBQuadExpr();	
						for (int j1=0;j1<noVertex;j1++)
						{
							if(j1==arrDemand.get(i).getSrc()-1)
							{
								for (int j2=0;j2<noVertex;j2++)
								{
									expr5.addTerm(1, x1[i][k][j1],y[i][j1][j2]);
								}
							}
							else
							{
								for (int j2=0;j2<noVertex;j2++)
								{
									expr5.addTerm(1, x1[i][k][j1],y[i][j2][j1]);
								}
							}
							
						}
						expr5.addTerm(-1, z1[i]);
						String st = "ff["+(k)+ "t]";
						if (id!=0)//truong hop function in demand =1
						{
							
							model.addQConstr(expr5, GRB.EQUAL, 0, st);
						}
//						else
//							model.addQConstr(expr5, GRB.EQUAL, 0, st);
						
						expr5 = null;
					}
					
					
				}
				
				
				//rangbuoc 5->ok
				for (int i=0;i<arrDemand.size();i++)
				{
					int desti = arrDemand.get(i).getDest();
					int source = arrDemand.get(i).getSrc();
					for (int j1=0;j1<noVertex;j1++)
					{
						GRBLinExpr expr7= new GRBLinExpr();
						for (int j2=0;j2<noVertex;j2++)
						{
							expr7.addTerm(-1, y[i][j2][j1]);
							expr7.addTerm(1, y[i][j1][j2]);
						}
						String st = "h["+(i)+ "]["+(j1+1)+  "s]";
						if(j1 !=source-1 && j1 !=desti-1)
						{
							
							model.addConstr(expr7, GRB.EQUAL, 0, st);
							expr7 = null;
						}
						else
						{
							if(j1==source-1)
							{
								expr7.addTerm(-1, z1[i]);
								model.addConstr(expr7, GRB.EQUAL, 0, st);
								expr7 = null;
							}
							if(j1==desti-1)
							{
								expr7.addTerm(1, z1[i]);
								model.addConstr(expr7, GRB.EQUAL, 0, st);
								expr7 = null;
							}
						}
					}
					
				}
				System.gc();
				
//				//rang buoc 6
//				for(int i = 0; i < arrDemand.size(); i++)
//					for(int k = 0; k < noFunction; k++)
//					{
//						int id = arrDemand.get(i).getOrderFunction(k+1);
//						if (id!=0)//truong hop function in demand =1
//						{
//							GRBQuadExpr expr2= new GRBQuadExpr();
//							for(int j = 0; j < noVertex; j++)
//				    		{
//								expr2.addTerm(1,z1[i],x1[i][k][j]);
//				    		}
//							String st = "dy["+(i+1)+ "]["+(k+1)+ "]";
//							model.addQConstr(expr2, GRB.GREATER_EQUAL,0.5, st);
//							expr2 = null;	
//						}
//						
//					}
				
			//rang buoc 7
				for(int i = 0; i < arrDemand.size(); i++) 
				{
					GRBQuadExpr expr5= new GRBQuadExpr();
				    for(int j1 = 0; j1 < noVertex; j1++)
				    	for(int j2 = 0; j2 < noVertex; j2++)
				    	{
				    		expr5.addTerm(1, z1[i],y[i][j1][j2]);
				    		expr5.addTerm(-1, y[i][j1][j2]);
				    		expr5.addTerm(1, z1[i],y[i][j2][j1]);
				    		expr5.addTerm(-1, y[i][j2][j1]);
				    	}
				    String st = "dk["+(i+1)+ "]";
					model.addQConstr(expr5, GRB.GREATER_EQUAL,-0.1, st);
					expr5 = null;
				}
				for(int i = 0; i < arrDemand.size(); i++) 
				{
					GRBQuadExpr expr5= new GRBQuadExpr();
				    for(int j1 = 0; j1 < noVertex; j1++)
				    	for(int j2 = 0; j2 < noVertex; j2++)
				    	{
				    		expr5.addTerm(1, z1[i],y[i][j1][j2]);
				    		expr5.addTerm(-1, y[i][j1][j2]);
				    		expr5.addTerm(1, z1[i],y[i][j2][j1]);
				    		expr5.addTerm(-1, y[i][j2][j1]);
				    	}
				    String st = "dk["+(i+1)+ "]";
					model.addQConstr(expr5, GRB.EQUAL,0, st);
					expr5 = null;
				}
				
				//rang buoc 8
				for(int i = 0; i < arrDemand.size(); i++) 
				{
					GRBQuadExpr expr5= new GRBQuadExpr();
					for(int k = 0; k < noFunction; k++)
						for(int j = 0; j < noVertex; j++)
				    	{
				    		expr5.addTerm(1, z1[i],x1[i][k][j]);
				    		expr5.addTerm(-1, x1[i][k][j]);
				    	}
				    String st = "dx["+(i+1)+ "]";
					model.addQConstr(expr5, GRB.GREATER_EQUAL,-0.1, st);
					expr5 = null;
				}
				

				
				for(int i = 0; i < arrDemand.size(); i++) 
				for (int j1=0;j1<noVertex-1;j1++)
					for (int j2=j1+1;j2<noVertex;j2++)
						{
							GRBQuadExpr expry= new GRBQuadExpr();
			    			expry.addTerm(1, y[i][j1][j2],y[i][j2][j1]);
			    			String st = "nn["+(i+1)+ "]["+(j1+1)+ "]["+(j2+1)+"]";
							model.addQConstr(expry, GRB.EQUAL, 0 , st);
						}
				
				// Optimize model
				try {
					
					model.optimize();
					
					out.write("Solution for the problem:");
					out.newLine();
				
					int optimstatus = model.get(GRB.IntAttr.Status); 
					if (optimstatus == GRB.Status.OPTIMAL) 
					{ 
						r_min= r.get(GRB.DoubleAttr.X);
						value_final = obj.getValue();
						out.write("Objective optimal Value: "+obj.getValue());
						out.newLine();
						for(int i = 0; i < arrDemand.size(); i++) 
							for(int k = 0; k < noFunction; k++)
								for(int j = 0; j < noVertex; j++)
						    		{	
						    			if(x1[i][k][j].get(GRB.DoubleAttr.X)>0)
						    			{
						    			out.write(x1[i][k][j].get(GRB.StringAttr.VarName)
						    					+ " : " +x1[i][k][j].get(GRB.DoubleAttr.X));
						    			out.newLine();
						    			}
						    		}
						for(int i = 0; i < arrDemand.size(); i++) 
						    for(int j1 = 0; j1 < noVertex; j1++)
						    	for(int j2 = 0; j2 < noVertex; j2++)
						    		{	
						    			if(y[i][j1][j2].get(GRB.DoubleAttr.X)>0)
						    			{
						    			out.write(y[i][j1][j2].get(GRB.StringAttr.VarName)
						    					+ " : " +y[i][j1][j2].get(GRB.DoubleAttr.X));
						    			out.newLine();
						    			}
						    		}
						for(int i = 0; i < arrDemand.size(); i++)
							if(z1[i].get(GRB.DoubleAttr.X)>0)
			    			{
								a_min++;
			    			out.write(z1[i].get(GRB.StringAttr.VarName)
			    					+ " : " +z1[i].get(GRB.DoubleAttr.X));
			    			out.newLine();
			    			}
						out.write(r.get(GRB.StringAttr.VarName)
		    					+ " : " +r.get(GRB.DoubleAttr.X));
		    			out.newLine();
				
					 } else if (optimstatus == GRB.Status.INF_OR_UNBD) 
					 	{ 
					        System.out.println("Model is infeasible or unbounded"); 
					        return;
					 	} else if (optimstatus == GRB.Status.INFEASIBLE) 
					        	{ 
							        System.out.println("Model is infeasible AAAAAAAAAAAAAA"); 
							        return; 
					        	} else if (optimstatus == GRB.Status.INTERRUPTED)
					        	{
					        		r_min= r.get(GRB.DoubleAttr.X);
					        		value_final = obj.getValue();
					        		out.write("Objective interrupt Value: "+obj.getValue());
									out.newLine();
									for(int i = 0; i < arrDemand.size(); i++) 
									for(int k = 0; k < noFunction; k++)
										for(int j = 0; j < noVertex; j++)
								    		{	
								    			if(x1[i][k][j].get(GRB.DoubleAttr.X)>0)
								    			{
								    			out.write(x1[i][k][j].get(GRB.StringAttr.VarName)
								    					+ " : " +x1[i][k][j].get(GRB.DoubleAttr.X));
								    			out.newLine();
								    			}
								    		}
									for(int i = 0; i < arrDemand.size(); i++) 
									    for(int j1 = 0; j1 < noVertex; j1++)
									    	for(int j2 = 0; j2 < noVertex; j2++)
									    		{	
									    			if(y[i][j1][j2].get(GRB.DoubleAttr.X)>0)
									    			{
									    			out.write(y[i][j1][j2].get(GRB.StringAttr.VarName)
									    					+ " : " +y[i][j1][j2].get(GRB.DoubleAttr.X));
									    			out.newLine();
									    			}
									    		}
									for(int i = 0; i < arrDemand.size(); i++)
										if(z1[i].get(GRB.DoubleAttr.X)>0)
						    			{
											a_min++;
						    			out.write(z1[i].get(GRB.StringAttr.VarName)
						    					+ " : " +z1[i].get(GRB.DoubleAttr.X));
						    			out.newLine();
						    			}
									out.write(r.get(GRB.StringAttr.VarName)
					    					+ " : " +r.get(GRB.DoubleAttr.X));
					    			out.newLine();
					        		
					        	}
					
					 else
					 {
						 r_min= r.get(GRB.DoubleAttr.X);
						 value_final = obj.getValue();
						 out.write("Objective feasible Value: "+obj.getValue());
						 out.newLine();
							for(int i = 0; i < arrDemand.size(); i++) 
							for(int k = 0; k < noFunction; k++)
								for(int j = 0; j < noVertex; j++)
						    		{	
						    			if(x1[i][k][j].get(GRB.DoubleAttr.X)>0)
						    			{
						    			out.write(x1[i][k][j].get(GRB.StringAttr.VarName)
						    					+ " : " +x1[i][k][j].get(GRB.DoubleAttr.X));
						    			out.newLine();
						    			}
						    		}
							for(int i = 0; i < arrDemand.size(); i++) 
							    for(int j1 = 0; j1 < noVertex; j1++)
							    	for(int j2 = 0; j2 < noVertex; j2++)
							    		{	
							    			if(y[i][j1][j2].get(GRB.DoubleAttr.X)>0)
							    			{
							    			out.write(y[i][j1][j2].get(GRB.StringAttr.VarName)
							    					+ " : " +y[i][j1][j2].get(GRB.DoubleAttr.X));
							    			out.newLine();
							    			}
							    		}
							for(int i = 0; i < arrDemand.size(); i++)
								if(z1[i].get(GRB.DoubleAttr.X)>0)
				    			{
									a_min++;
				    			out.write(z1[i].get(GRB.StringAttr.VarName)
				    					+ " : " +z1[i].get(GRB.DoubleAttr.X));
				    			out.newLine();
				    			}
							out.write(r.get(GRB.StringAttr.VarName)
			    					+ " : " +r.get(GRB.DoubleAttr.X));
			    			out.newLine();
							
					  }
				
					
				} catch (Exception e) {
					r_min= r.get(GRB.DoubleAttr.X);
					value_final = obj.getValue();
					out.write("Objective interrupt Value: "+obj.getValue());
					out.newLine();
					for(int i = 0; i < arrDemand.size(); i++) 
					for(int k = 0; k < noFunction; k++)
						for(int j = 0; j < noVertex; j++)
				    		{	
				    			if(x1[i][k][j].get(GRB.DoubleAttr.X)>0)
				    			{
				    			out.write(x1[i][k][j].get(GRB.StringAttr.VarName)
				    					+ " : " +x1[i][k][j].get(GRB.DoubleAttr.X));
				    			out.newLine();
				    			}
				    		}
					for(int i = 0; i < arrDemand.size(); i++) 
					    for(int j1 = 0; j1 < noVertex; j1++)
					    	for(int j2 = 0; j2 < noVertex; j2++)
					    		{	
					    			if(y[i][j1][j2].get(GRB.DoubleAttr.X)>0)
					    			{
					    			out.write(y[i][j1][j2].get(GRB.StringAttr.VarName)
					    					+ " : " +y[i][j1][j2].get(GRB.DoubleAttr.X));
					    			out.newLine();
					    			}
					    		}
					for(int i = 0; i < arrDemand.size(); i++)
						if(z1[i].get(GRB.DoubleAttr.X)>0)
		    			{
							a_min++;
		    			out.write(z1[i].get(GRB.StringAttr.VarName)
		    					+ " : " +z1[i].get(GRB.DoubleAttr.X));
		    			out.newLine();
		    			}
					out.write(r.get(GRB.StringAttr.VarName)
	    					+ " : " +r.get(GRB.DoubleAttr.X));
	    			out.newLine();
					
	
				}
					model.dispose();
				env.dispose();
				System.gc();
			
				} catch(GRBException e3){			
					System.out.println("Error code1: " + e3.getErrorCode() + ". " +
							e3.getMessage());
					System.out.print("This problem can't be solved");
					
					
					}
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
	public static boolean GreadyAlg(String outFile)//dang co van de o day
	{
		numberofCore=0;
		numberofEdge=0;
		numberofMidle=0;
		_duration=0;
		final long startTime = System.currentTimeMillis();
		v_solution = new ArrayList<List<Integer>>();
		f_solution=new ArrayList<List<Integer>>();
		boolean flag=false;
		int dem=0;
		for (int i=0;i<noVertex;i++)
		{
			ultilize_resource +=UtilizeFunction.value(g.getCap(i+1)) * g.getPriceNode(i+1);				
		}
		ExGraph _g_tam=	new ExGraph(g.cap,g.pricePernode,g.w,g.getPriceBandwidth());
		//loai cac canh ko thoa man sua lai doan nay. Co the la luc minh loai cac canh sau moi lan co noNewDemand. g_i se thay doi sau moi lan noNewDemand. g_i cung duoc xay dung lai
		for(int i=0;i<noNewDemand;i++)
		{
			newDemand _d=getDemand(i+1);
			ExGraph g_i = new ExGraph(g.cap,g.pricePernode,g.w,g.getPriceBandwidth());
			for (int j=0;j<noVertex;j++)
				for(int k=0;k<noVertex;k++)
				{
					if(_g_tam.getEdgeWeight(j+1, k+1)<_d.getBw())
						g_i.removeLink(j+1, k+1);
				}
			//Tim duong cho tung demand noNewDemand
			list_v = new ArrayList<Integer>();
			list_f = new ArrayList<Integer>();
			ArrayList<ArrayList<Integer>> BFStree = new ArrayList<ArrayList<Integer>>();
			BFS(g_i.getV(), _d.getSrc(), _d.getDest(), _d.getBw(),BFStree);
			if(list_v==null)
				return false;
			else
			{
				dem=0;
				for (int j=0;j<_d.getFunctions().size();j++)
				{
					flag=false;
					for (int h=0;h<list_v.size();h++)
					{
						if(UtilizeFunction.isBig(_g_tam.getCap(list_v.get(h)), getFunction(_d.getFunctions().get(j)).getLamda()))
						{
							list_f.add(_d.getFunctions().get(j));
							_g_tam.setCap(list_v.get(h), UtilizeFunction.minus(_g_tam.getCap(list_v.get(h)), getFunction(_d.getFunctions().get(j)).getLamda()));
							
							flag=true;
							dem++;
							break;
						}
					}
					if(!flag)
						list_f.add(0);
				}
				for(int j=0;j<list_v.size()-dem;j++)
					list_f.add(0);
			}
			v_solution.add(list_v);
			f_solution.add(list_f);
			list_v= null;
			list_f=null;
		}
		if(v_solution.size()==noNewDemand)
			flag=true;
		File file = new File(outFile);
		try {
			out = new BufferedWriter(new FileWriter(file));
			int[][] function_Loc = new int[noFunction+1][noVertex+1];
			for (int i=0;i<noFunction;i++)
				for (int j=0;j<noVertex;j++)
					function_Loc[i+1][j+1]=0;
			for (int i=0;i<noFunction;i++)
			{
				for(int j=0;j<noNewDemand;j++)
				{
					int _lengList= v_solution.get(j).size();
					for (int k=0;k<_lengList;k++)
					{
						if(f_solution.get(j).get(k)==(i+1))
							function_Loc[i+1][v_solution.get(j).get(k)]++;
					}
				}
			}
			for (int i=0;i<noFunction;i++)
				for (int j=0;j<noVertex;j++)
					if(function_Loc[i+1][j+1]>1)
						value_final+= (1-function_Loc[i+1][j+1])*Gain(g.getPriceNode(j+1));

			for (int i=0;i<noNewDemand;i++)
			{
				//in ra tung demand
				out.write ("demand: "+(i+1));
				out.newLine();
				int _lengList= v_solution.get(i).size();
				for (int j=0;j<_lengList;j++)
				{
					if(f_solution.get(i).get(j)!=0)
					{
						out.write("function:"+f_solution.get(i).get(j)+ " node: "+(v_solution.get(i).get(j)));
						out.newLine();
						
						//value_final+=getDemand(i+1).getRate()*g.getPriceNode(v_solution.get(i).get(j))*UtilizeFunction.value(getFunction(f_solution.get(i).get(j)).getLamda());
						value_final+=g.getPriceNode(v_solution.get(i).get(j));
						ultilize_resource -= g.getPriceNode(v_solution.get(i).get(j))*UtilizeFunction.value(getFunction(f_solution.get(i).get(j)).getLamda());
					}
				}
				out.write("path for demand:");
				for (int j=0;j<_lengList-1;j++)
				{
					out.write(" "+v_solution.get(i).get(j));
					for(int k=j+1;k<_lengList;k++)
					{
						value_bandwidth+=g.getEdgeWeight(v_solution.get(i).get(j), v_solution.get(i).get(k))*g.getPriceBandwidth() ;
						value_final+=g.getEdgeWeight(v_solution.get(i).get(j), v_solution.get(i).get(k))*g.getPriceBandwidth() ;
					}
				}
				out.write(" "+v_solution.get(i).get(_lengList-1));
				out.newLine();
				
			}
			for(int i=0;i<noVertex;i++)
				for (int j=0;j<noVertex;j++)
				{
					ultilize_resource +=_g_tam.getEdgeWeight(i+1, j+1)*_g_tam.getPriceBandwidth();
				}
			_duration = System.currentTimeMillis() - startTime;
			System.out.println(_duration);
			out.write("Value solution: "+ value_final);
			out.newLine();
			out.write("Value bandwidth: "+ value_bandwidth);
			out.newLine();
			out.write("Runtime (mS): "+ _duration);
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
	@SuppressWarnings("unchecked")
	public static void main(String[] args) {//giai voi gurobi
		limitedNo =20;
		BufferedWriter out1 = null;
		//currentTime=Double.parseDouble(args[0]);
		File dir = new File("data");
		String[] extensions = new String[] { "txt" };
		try {
			System.out.println("Getting all .txt in " + dir.getCanonicalPath()
					+ " including those in subdirectories");
		} catch (IOException e) {
			e.printStackTrace();
		}
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

		File _f = new File("output_Gurobi1.txt");
		String str="";
		try {
			out1 = new BufferedWriter(new FileWriter(_f,true));
			for (File file : files) {
				try {
					System.out.println("file: " + file.getCanonicalPath());
					ReadInputFile(file.getPath());
					str=file.getName(); 
					str = str.replace("in", "1Gurobi_out");
					out1.write(str);
					_duration=0;
					value_final=0.0;
					ultilize_resource=0;
					value_bandwidth =0;
						model2(str);
						out1.write(" "+noFunction + " " +noNewDemand +" "+noVertex+" "+E +" "+ value_final+" "+_duration+ " "+ r_min+ " "+ a_min);
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
	
	public static void mainA(String[] args)
	{
		
		alpha=1;
		beta=1;
		gama=1;
		theta=1;
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

		File _f = new File("ran_output.txt");
		String str="";
		try {
			out1 = new BufferedWriter(new FileWriter(_f,true));
			for (File file : files) {
				try {
					System.out.println("file: " + file.getCanonicalPath());
					ReadInputFile(file.getPath());
					str=file.getName(); 
					str = str.replace("in", "ran_zero_out");
					out1.write(str);
					if (IsCapacity())
					{					
						_Dist();
						_duration=0;
						value_final=0.0;
						ultilize_resource=0;
						value_bandwidth =0;
						noMigrate=(int)0*noOldDemand/10;
						if(heuristic(str))
						{
							out1.write(" "+noVertex + " " +noFunction +" "+noNewDemand+" "+noOldDemand +" "+ value_final+" "+ _duration);
							
						}
						else
						{
							out1.write(" Notfound ");
							
						}
						_duration=0;
						value_final=0.0;
						value_bandwidth =0;
						str = str.replace("zero", "third");
						noMigrate=(int)3*noOldDemand/10;
						if(heuristic(str))
						{
							out1.write(" "+ value_final+" "+ _duration);
							
						}
						else
						{
							out1.write(" notFound ");
							
						}
						_duration=0;
						value_final=0.0;
						value_bandwidth =0;
						str = str.replace("third", "fifty");
						noMigrate=(int)5*noOldDemand/10;
						if(heuristic(str))
						{
							out1.write(" "+ value_final+" "+ _duration);
							
						}
						else
						{
							out1.write(" notFound ");
							
						}
						_duration=0;
						value_final=0.0;
						value_bandwidth =0;
						str = str.replace("fifty", "seventy");
						noMigrate=(int)7*noOldDemand/10;
						if(heuristic(str))
						{
							out1.write(" "+ value_final+" "+ _duration);
							
						}
						else
						{
							out1.write(" notFound ");
							
						}
						_duration=0;
						value_final=0.0;
						value_bandwidth =0;
						str = str.replace("seventy", "hundred");
						noMigrate=(int)10*noOldDemand/10;
						if(heuristic(str))
						{
							out1.write(" "+ value_final+" "+ _duration);
							
						}
						else
						{
							out1.write(" notFound ");
							
						}
						out1.newLine();
						
						
					}
					else
					{
						out1.write(" Khong du capacity ");
						out1.newLine();
					}
					
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
	public static void mainAAAAA(String[] args)// chinh
	{
		String testName = args[0];
		int typeAlg=Integer.parseInt(args[1]);
		hsoA=Double.parseDouble(args[2]);
		alpha=1;
		beta=1;
		gama=1;
		theta=1;
		BufferedWriter out1 = null;
		File dir = new File("data");
		String[] extensions = new String[] { "txt" };
		try {
			System.out.println("Getting all .txt in " + dir.getCanonicalPath()
					+ " including those in subdirectories");
		} catch (IOException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

		//File _f = new File("RBP.txt");
		File _f = new File(testName+".txt");
		String str="";
		try {
			out1 = new BufferedWriter(new FileWriter(_f,true));
			for (File file : files) {
				try {
					System.out.println("file: " + file.getCanonicalPath());
					ReadInputFile(file.getPath());
					str=file.getName(); 
					//str="newresults\\"+str;
					str = str.replace("in", testName);
					out1.write(str);
					_Dist();
					out1.write(" "+noVertex + " " +noFunction +" "+noNewDemand+" "+noOldDemand);
					if(typeAlg==1)//RBP
					{
						for (int i1=0;i1<=10;i1++)//test different migration
						{
							_duration=0;
							value_final=0.0;
							value_penalty=0.0;
							ultilize_resource=0;
							value_bandwidth =0;
							String str1 = i1+str;
							noMigrate=(int)i1*noOldDemand/10;
							if(RBP(str1))
							{
								out1.write(" "+ value_final);
								
							}
							else
							{
								out1.write(" Notfound ");
								
							}
						}
					}
					if(typeAlg==2)//Viterbi
					{

						for (int i1=0;i1<=10;i1++)//test different migration
						{
							_duration=0;
							value_final=0.0;
							ultilize_resource=0;
							value_bandwidth =0;
							String str1 = i1+str;
							noMigrate=(int)i1*noOldDemand/10;
							if(Viterbi(str1))
							{
								out1.write(" "+ value_final);
								
							}
							else
							{
								out1.write(" Notfound ");
								
							}
						}
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

	public static void mainNNNN(String[] args)// chinh
	{
		String testName = args[0];
		double typeAlg=Double.parseDouble(args[1]);
		alpha=1;
		beta=1;
		gama=1;
		theta=1;
		BufferedWriter out1 = null;
		File dir = new File("data");
		String[] extensions = new String[] { "txt" };
		try {
			System.out.println("Getting all .txt in " + dir.getCanonicalPath()
					+ " including those in subdirectories");
		} catch (IOException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

		//File _f = new File("RBP.txt");
		File _f = new File(testName+".txt");
		String str="";
		try {
			out1 = new BufferedWriter(new FileWriter(_f,true));
			for (File file : files) {
				try {
					System.out.println("file: " + file.getCanonicalPath());
					ReadInputFile(file.getPath());
					str=file.getName(); 
					//str="newresults\\"+str;
					str = str.replace("in", testName);
					out1.write(str);
					_Dist();
					out1.write(" "+noVertex + " " +noFunction +" "+noNewDemand+" "+noOldDemand);
					if(typeAlg==1)//RBP
					{
						for (int i1=0;i1<=10;i1++)//test different migration
						{
							_duration=0;
							value_final=0.0;
							ultilize_resource=0;
							value_bandwidth =0;
							String str1 = i1+str;
							noMigrate=(int)i1*noOldDemand/10;
							if(RBP(str1))
							{
								out1.write(" "+ acceptRate+" "+ averageDelay+" "+_duration+ " "+ value_final);
								
							}
							else
							{
								out1.write(" Notfound ");
								
							}
						}
					}
					if(typeAlg==2)//Viterbi
					{

						for (int i1=0;i1<=10;i1++)//test different migration
						{
							_duration=0;
							value_final=0.0;
							ultilize_resource=0;
							value_bandwidth =0;
							String str1 = i1+str;
							noMigrate=(int)i1*noOldDemand/10;
							if(Viterbi(str1))
							{
								out1.write(" "+ acceptRate+" "+ averageDelay+" "+_duration+ " "+ value_final);
								
							}
							else
							{
								out1.write(" Notfound ");
								
							}
						}
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
	public static void mainT(String[] args)// test 1 migration
	{
		
		alpha=1;
		beta=1;
		gama=1;
		theta=1;
		BufferedWriter out1 = null;
		File dir = new File("data");
		String[] extensions = new String[] { "txt" };
		try {
			System.out.println("Getting all .txt in " + dir.getCanonicalPath()
					+ " including those in subdirectories");
		} catch (IOException e) {
			e.printStackTrace();
		}
		@SuppressWarnings("unchecked")
		List<File> files = (List<File>) FileUtils.listFiles(dir, extensions, true);

		File _f = new File("output\\result.txt");
		String str="";
		try {
			out1 = new BufferedWriter(new FileWriter(_f,true));
			for (File file : files) {
				try {
					System.out.println("file: " + file.getCanonicalPath());
					ReadInputFile(file.getPath());
					str=file.getName(); 
					//str="newresults\\"+str;
					str = str.replace("in", "results\\RBP");
					out1.write(str);
					_Dist();
						_duration=0;
						value_final=0.0;
						ultilize_resource=0;
						value_bandwidth =0;
						noMigrate=(int)3*noOldDemand/10;
						//if(Viterbi(str))
						if(RBP(str))
						{
							out1.write(" "+noVertex + " " +noFunction +" "+noNewDemand+" "+noOldDemand +" "+ acceptRate+" "+ averageDelay+" "+_duration+ " "+ value_final);
							
						}
						else
						{
							out1.write(" Notfound ");
							
						}
						
						_duration=0;
						value_final=0.0;
						ultilize_resource=0;						
						value_bandwidth =0;
						str = str.replace("RBP", "Viterbi");
						noMigrate=(int)3*noOldDemand/10;
						if(Viterbi(str))
						{
							out1.write(" "+ acceptRate+" "+ averageDelay+" "+_duration+ " "+ value_final);
							
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
	public static void mainC(String[] args)// dung de check giai thuat migration
	{
		
		alpha=1;
		beta=1;
		gama=1;
		theta=1;
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

		File _f = new File("output\\testRBP.txt");
		String str="";
		try {
			out1 = new BufferedWriter(new FileWriter(_f,true));
			for (File file : files) {
				try {
					System.out.println("file: " + file.getCanonicalPath());
					ReadInputFile(file.getPath());
					str=file.getName(); 
					str = str.replace("in", "testRBP");
					out1.write(str);
					if (IsCapacity())
					{					
						_Dist();
						_duration=0;
						value_final=0.0;
						ultilize_resource=0;
						value_bandwidth =0;
						noMigrate=(int)7*noOldDemand/10;
						//if(Viterbi(str))
						if(RBP_edit(str))
						{
							out1.write(" "+noVertex + " " +noFunction +" "+noNewDemand+" "+noOldDemand +" "+ acceptRate+" "+ averageDelay+" "+_duration+" "+ value_final);
							
						}
						else
						{
							out1.write(" Notfound ");
							
						}
						
						out1.newLine();
						
						
					}
					else
					{
						out1.write(" Khong du capacity ");
						out1.newLine();
					}
					
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
	
	public static void mainR(String[] args) {
	//UtilizeFunction.randomTopology("lib\\inputFile.txt");
	//UtilizeFunction.randomNewest("lib\\inputG1.txt");
	UtilizeFunction.random3Topology("lib\\inputTopology.txt");
}

}
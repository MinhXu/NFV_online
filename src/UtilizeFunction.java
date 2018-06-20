

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Random;
import java.util.Vector;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

public class UtilizeFunction {
	/**
	 * Returns a psuedo-random number between min and max, inclusive.
	 * The difference between min and max can be at most
	 * <code>Integer.MAX_VALUE - 1</code>.
	 *
	 * @param min Minimim value
	 * @param max Maximim value.  Must be greater than min.
	 * @return Integer between min and max, inclusive.
	 * @see java.util.Random#nextInt(int)
	 */
	
	public static boolean isPositive(Vector<Double> k)
	{
		for (int i=0;i<k.size();i++)
			if(k.get(i)<=0)
				return false;
		return true;
	}
	public static Vector<Double> add(Vector<Double> a, Vector<Double> b)
	{
		Vector<Double> temp = new Vector<Double>();
		if(a.size()!=b.size()) return null;
		for (int i=0;i<a.size();i++)
			temp.addElement(a.get(i)+b.get(i));
		return temp;
	}
	public static Vector<Double> minus(Vector<Double> a, Vector<Double> b)
	{
		Vector<Double> temp = new Vector<Double>();
		if(a.size()!=b.size()) return null;
		for (int i=0;i<a.size();i++)
			temp.addElement(a.get(i)-b.get(i));
		return temp;
	}
	public static double multi(Vector<Double> a, Vector<Double> b)
	{
		double temp=0.0;
		if(a.size()!=b.size()) return -1;
		for (int i=0;i<a.size();i++)
			
			temp+=a.get(i)*b.get(i);
		return temp;
	}
	public static int divide(Vector<Double> a, Vector<Double> b)
	{
		int temp_Min=Integer.MAX_VALUE;
		Vector<Integer> temp = new Vector<>(3);
		for (int i=0;i<3;i++)
		{
			Double  _t = a.get(i)/b.get(i);
			temp.addElement(_t.intValue());
		}
		for (int i=0;i<3;i++)
		{
			if(temp.get(i)<temp_Min)
				temp_Min= temp.get(i);
		}
		return temp_Min;
	}
	public static Vector<Double> multi(Vector<Double> a, double b)
	{
		Vector<Double> temp = new Vector<Double>();
		for (int i=0;i<a.size();i++)
			
			temp.addElement(b*a.get(i));
		return temp;
	}
	public static boolean isBig(Vector<Double> k1, Vector<Double> k2)
	{
		if(k1.get(0)>=k2.get(0)&& k1.get(1)>=k2.get(1)&& k1.get(2)>=k2.get(2))
			return true;
		else
			return false;
	}
	public static double value(Vector<Double> k)
	{
		double tam=0.0;
		for(int i=0;i<k.size();i++)
			tam+=k.get(i)*k.get(i);
		return Math.sqrt(tam);
	}
	public static int bigger(Vector<Double> k1, Vector<Double> k2)
	{
		// 1: k1 > k2
		// 2: k2 > k1
		// 0: k1 = k2
		//-1: k1 != k2
		if(k1.size() != k2.size()) return -1;
		if(k1.get(0)==k2.get(0))
			if(k1.get(1)==k2.get(1))
				if(k1.get(2)==k2.get(2))
					return 0;
				else
					if(k1.get(2)>k2.get(2))
						return 1;
					else
						return 2;
			else
				if(k1.get(1)>k2.get(1))
					return 1;
				else
					return 2;
		else
			if(k1.get(0)>k2.get(0))
				return 1;
			else
				return 2;
	}
	
	public static int randInt(int min, int max) {

	    // Usually this can be a field rather than a method variable
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    int randomNum = rand.nextInt((max - min) + 1) + min;

	    return randomNum;
	}
	
	
	public static double randDouble(double min, double max) {
	    double random = new Random().nextDouble();
		double result = min + (random * (max - min));

	    return result;
	}
	public static double randDouble(double min) {
		double max = 100.0;
	    double random = new Random().nextDouble();
		double result = min + (random * (max - min));
	    return result;
	}
	public static double randDouble(int hso) {

	    // Usually this can be a field rather than a method variable
	    Random rand = new Random();

	    // nextInt is normally exclusive of the top value,
	    // so add 1 to make it inclusive
	    double randomNum = rand.nextDouble()*hso;

	    return randomNum;
	}
	
	public static void random3Topology(String filePara)//Fat tree, Bcube, VL2
	{
		int type = 0;
		int k0=6;
		int n0=4;
		int maxServer=0;
		int minServer=0;
		DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));

			String strLine = in.readLine();
			int dataNo= Integer.parseInt(strLine);
			Vector<Vector<Double>> dataReal = new Vector<Vector<Double>>();
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
			int temp=1;
			dataNo = dataNo *8;
			//Read File Line By Line			
			while ((strLine = in.readLine()) != null)   {
				String[] line= strLine.split(" ");			
				double currentTime = UtilizeFunction.randDouble(15, 50);
				int n = Integer.parseInt(line[0]);
				int noOldDemand= Integer.parseInt(line[1]);
				int noG= Integer.parseInt(line[2]);
				int m=Integer.parseInt(line[3]);
				ExGraph g = new ExGraph(type,k0,n0,dataReal);
				if(type==0|| type==1)
				{
					n= (k0*k0*k0+5*k0*k0)/4;
					minServer =5*k0*k0/4+1;
					maxServer= (k0*k0*k0+5*k0*k0)/4;
				}
				if(type==2)
				{
					n=(int)Math.pow(n0, k0+1)+ (int)Math.pow(n0, k0)*(k0+1);
					minServer=1;
					maxServer =(int) Math.pow(n0, k0+1); 
				}
				ArrayList<Function> functionArr = new ArrayList<Function>();
				
				Double[] hso_Func= {6.0,50.0,20.0};
			    for (int i=0;i< m;i++)
			       functionArr.add(new Function(new Vector<Double>(Arrays.asList(hso_Func)),i+1));
			    ArrayList<oldDemand> _oldDemand = new ArrayList<oldDemand>();
			    ArrayList<Integer> idArr = new ArrayList<Integer>();
			    for(int i=0;i<noOldDemand;i++)
			    {		    	
			    	boolean flag= false;
			    	while (!flag)
			    	{
			    		int idTemp= UtilizeFunction.randInt(1, 1000);
			    		if(!idArr.contains(idTemp))
			    		{
			    			idArr.add(idTemp);
			    			_oldDemand.add(new oldDemand(idTemp, functionArr, n, currentTime,minServer,maxServer));
			    			flag=true;
			    		}
			    	}			    	
			    	
			    }
			    ArrayList<newDemand> demandArr= new ArrayList<newDemand>();
			    for (int l=0;l<noG;l++)
			    {
			    	ArrayList<Integer> idArrDemand = new ArrayList<Integer>();
			    	for (int i=0;i<noOldDemand;i++)
			    		idArrDemand.add(idArr.get(i));
			    	String fileName= "data\\in"+(l+temp)+".txt";
			    	out= new BufferedWriter(new FileWriter(fileName));
					int s = Integer.parseInt(line[4+l]);	
					int size= demandArr.size();
					for (int i=0;i<s-size;i++)
					{
						boolean flag= false;
				    	while (!flag)
				    	{
				    		int idTemp= UtilizeFunction.randInt(1, 1000);
				    		if(!idArrDemand.contains(idTemp))
				    		{
				    			idArrDemand.add(idTemp);
				    			demandArr.add(new newDemand(idTemp,n,currentTime,functionArr,minServer,maxServer)); 
				    			flag=true;
				    		}
				    	}
					}
				    //ghi ra file
				    out.write(n+" "+m + " "+ s + " "+ noOldDemand);
				    out.newLine();
				    for (int i=0;i<m;i++)
				    {
			               for (int j=0;j<2;j++)
			            	   out.write(df.format(functionArr.get(i).getLamda().get(j))+" ");
			               out.write(df.format(functionArr.get(i).getLamda().get(2)));
			               out.write(";");
			       	}
				    out.newLine();
				    for (int i=0;i<noOldDemand;i++)
				    {
				    	out.write(_oldDemand.get(i).GetID()+";");
				    	out.write(_oldDemand.get(i).GetSrc()+";");
				    	out.write(_oldDemand.get(i).GetDest()+";");
				    	out.write(_oldDemand.get(i).GetArrivalTime()+";");
				    	out.write(_oldDemand.get(i).GetProcessTime()+";");
				    	out.write(_oldDemand.get(i).GetBandwidth()+";");
				    	out.write(_oldDemand.get(i).GetRate()+";");				    	
			            for (int j=0;j<_oldDemand.get(i).GetSetFunc().size();j++)
			            	out.write(_oldDemand.get(i).GetSetFunc().get(j)+" ");
			            out.write(";");
			            for (int j=0;j<_oldDemand.get(i).Get_v_sol().size();j++)
			            	out.write(_oldDemand.get(i).Get_v_sol().get(j)+" ");
			            out.write(";");
			            for (int j=0;j<_oldDemand.get(i).Get_f_sol().size();j++)
			            	out.write(_oldDemand.get(i).Get_f_sol().get(j)+" ");
			            out.newLine();
			       	}
			       	for (int i=0;i<s;i++)
			       	{ 
			       		System.out.println("bandwidth: "+demandArr.get(i).getBw() );
			       		out.write(demandArr.get(i).getId() +";");
			       		out.write(demandArr.get(i).getSrc() +";");
			       		out.write(demandArr.get(i).getDest() +";");
			       		out.write(demandArr.get(i).getArrivalTime() +";");
			       		out.write(demandArr.get(i).getProcessTime() +";");
			       		out.write(demandArr.get(i).getBw() +";");
			       		out.write(demandArr.get(i).getRate() +";");
			       		for (int j=0;j<demandArr.get(i).getNoF();j++)
			       			out.write(demandArr.get(i).getFunctions().get(j) +" ");	       		
			       		out.newLine();
			       	}
			       	out.write(df.format(g.getPriceBandwidth()));
			       	out.newLine();
			       	for (int i=0;i<n;i++)
			       	{		            
			       		for (int j=0;j<3;j++)
			            	   out.write(df.format(g.getCap(i+1).get(j))+" ");
			            out.write(";");
			            out.write(df.format(g.getPriceNode(i+1))+" ");
			            
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{
			       		for (int j=0;j<n;j++)
			       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
			       		out.newLine();
			       	}
			       	out.close();
			    }
			    temp+=noG;
			    
			}

			//Close the input stream
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	
    public static void randomNewest(String filePara)//edit model
	{
		DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));

			String strLine = in.readLine();
			int dataNo= Integer.parseInt(strLine);
			Vector<Vector<Double>> dataReal = new Vector<Vector<Double>>();
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
			int temp=1;
			dataNo = dataNo *8;
			//Read File Line By Line			
			while ((strLine = in.readLine()) != null)   {
				String[] line= strLine.split(" ");			
				double currentTime = UtilizeFunction.randDouble(15, 50);
				int n = Integer.parseInt(line[0]);
				int noOldDemand= Integer.parseInt(line[1]);
				int noG= Integer.parseInt(line[2]);
				int m=Integer.parseInt(line[3]);
				ExGraph g = new ExGraph(n,dataReal);
				ArrayList<Function> functionArr = new ArrayList<Function>();
				
				Double[] hso_Func= {6.0,50.0,20.0};
			    for (int i=0;i< m;i++)
			       functionArr.add(new Function(new Vector<Double>(Arrays.asList(hso_Func)),i+1));
			    ArrayList<oldDemand> _oldDemand = new ArrayList<oldDemand>();
			    ArrayList<Integer> idArr = new ArrayList<Integer>();
			    for(int i=0;i<noOldDemand;i++)
			    {		    	
			    	boolean flag= false;
			    	while (!flag)
			    	{
			    		int idTemp= UtilizeFunction.randInt(1, 1000);
			    		if(!idArr.contains(idTemp))
			    		{
			    			idArr.add(idTemp);
			    			_oldDemand.add(new oldDemand(idTemp, functionArr, n, currentTime));
			    			flag=true;
			    		}
			    	}			    	
			    	
			    }
			    for (int l=0;l<noG;l++)
			    {
			    	ArrayList<Integer> idArrDemand = new ArrayList<Integer>();
			    	for (int i=0;i<noOldDemand;i++)
			    		idArrDemand.add(idArr.get(i));
			    	String fileName= "data\\in"+(l+temp)+".txt";
			    	out= new BufferedWriter(new FileWriter(fileName));
					int s = Integer.parseInt(line[4+l]);
				    ArrayList<newDemand> demandArr= new ArrayList<newDemand>();
					for (int i=0;i<s;i++)
					{
						boolean flag= false;
				    	while (!flag)
				    	{
				    		int idTemp= UtilizeFunction.randInt(1, 1000);
				    		if(!idArrDemand.contains(idTemp))
				    		{
				    			idArrDemand.add(idTemp);
				    			demandArr.add(new newDemand(idTemp,n,currentTime,functionArr)); 
				    			flag=true;
				    		}
				    	}
					}
				    //ghi ra file
				    out.write(n+" "+m + " "+ s + " "+ noOldDemand);
				    out.newLine();
				    for (int i=0;i<m;i++)
				    {
			               for (int j=0;j<2;j++)
			            	   out.write(df.format(functionArr.get(i).getLamda().get(j))+" ");
			               out.write(df.format(functionArr.get(i).getLamda().get(2)));
			               out.write(";");
			       	}
				    out.newLine();
				    for (int i=0;i<noOldDemand;i++)
				    {
				    	out.write(_oldDemand.get(i).GetID()+";");
				    	out.write(_oldDemand.get(i).GetSrc()+";");
				    	out.write(_oldDemand.get(i).GetDest()+";");
				    	out.write(_oldDemand.get(i).GetArrivalTime()+";");
				    	out.write(_oldDemand.get(i).GetProcessTime()+";");
				    	out.write(_oldDemand.get(i).GetBandwidth()+";");
				    	out.write(_oldDemand.get(i).GetRate()+";");				    	
			            for (int j=0;j<_oldDemand.get(i).GetSetFunc().size();j++)
			            	out.write(_oldDemand.get(i).GetSetFunc().get(j)+" ");
			            out.write(";");
			            for (int j=0;j<_oldDemand.get(i).Get_v_sol().size();j++)
			            	out.write(_oldDemand.get(i).Get_v_sol().get(j)+" ");
			            out.write(";");
			            for (int j=0;j<_oldDemand.get(i).Get_f_sol().size();j++)
			            	out.write(_oldDemand.get(i).Get_f_sol().get(j)+" ");
			            out.newLine();
			       	}
			       	for (int i=0;i<s;i++)
			       	{ 
			       		System.out.println("bandwidth: "+demandArr.get(i).getBw() );
			       		out.write(demandArr.get(i).getId() +";");
			       		out.write(demandArr.get(i).getSrc() +";");
			       		out.write(demandArr.get(i).getDest() +";");
			       		out.write(demandArr.get(i).getArrivalTime() +";");
			       		out.write(demandArr.get(i).getProcessTime() +";");
			       		out.write(demandArr.get(i).getBw() +";");
			       		out.write(demandArr.get(i).getRate() +";");
			       		for (int j=0;j<demandArr.get(i).getNoF();j++)
			       			out.write(demandArr.get(i).getFunctions().get(j) +" ");	       		
			       		out.newLine();
			       	}
			       	out.write(df.format(g.getPriceBandwidth()));
			       	out.newLine();
			       	for (int i=0;i<n;i++)
			       	{		            
			       		for (int j=0;j<3;j++)
			            	   out.write(df.format(g.getCap(i+1).get(j))+" ");
			            out.write(";");
			            out.write(df.format(g.getPriceNode(i+1))+" ");
			            
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{
			       		for (int j=0;j<n;j++)
			       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
			       		out.newLine();
			       	}
			       	out.close();
			    }
			    temp+=noG;
			    
			}

			//Close the input stream
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
    
    public static void randomTopology(String filePara)//edit model
	{
		DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));

			String strLine = in.readLine();
			int dataNo= Integer.parseInt(strLine);
			Vector<Vector<Double>> dataReal = new Vector<Vector<Double>>();
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
			int temp=1;
			dataNo = dataNo *8;
			//Read File Line By Line			
			while ((strLine = in.readLine()) != null)   {
				String[] line= strLine.split(" ");			
				double currentTime = UtilizeFunction.randDouble(0, 20);
				int n = Integer.parseInt(line[0]);
				int noOldDemand= Integer.parseInt(line[1]);
				int noG= Integer.parseInt(line[2]);
				int m=Integer.parseInt(line[3]);
				ExGraph g = new ExGraph(n,dataReal);
				ArrayList<Function> functionArr = new ArrayList<Function>();
				
				Double[] hso_Func= {2.0,10.0,3.0};
			    for (int i=0;i< m;i++)
			       functionArr.add(new Function(new Vector<Double>(Arrays.asList(hso_Func)),i+1));
			    ArrayList<oldDemand> _oldDemand = new ArrayList<oldDemand>();
			    ArrayList<Integer> idArr = new ArrayList<Integer>();
			    for(int i=0;i<noOldDemand;i++)
			    {		    	
			    	boolean flag= false;
			    	while (!flag)
			    	{
			    		int idTemp= UtilizeFunction.randInt(1, 1000);
			    		if(!idArr.contains(idTemp))
			    		{
			    			idArr.add(idTemp);
			    			_oldDemand.add(new oldDemand(idTemp, functionArr, n, currentTime));
			    		}
			    	}			    	
			    	
			    }
			    for (int l=0;l<noG;l++)
			    {
			    	String fileName= "in"+(l+temp)+".txt";
			    	out= new BufferedWriter(new FileWriter(fileName));
					int s = Integer.parseInt(line[4+l]);
				    
					ArrayList<newDemand> demandArr= new ArrayList<newDemand>();
					for (int i=0;i<s;i++)
					{
						demandArr.add(new newDemand(i+1,n,0,functionArr)); 
					}
				    //ghi ra file
				    out.write(n+" "+m + " "+ s + " "+ noOldDemand);
				    out.newLine();
				    for (int i=0;i<m;i++)
				    {
			               for (int j=0;j<3;j++)
			            	   out.write(df.format(functionArr.get(i).getLamda().get(j))+" ");
			               out.write(";");
			       	}				    
				    for (int i=0;i<noOldDemand;i++)
				    {
				    	out.newLine();
				    	out.write(_oldDemand.get(i).GetID()+";");
				    	out.write(_oldDemand.get(i).GetSrc()+";");
				    	out.write(_oldDemand.get(i).GetDest()+";");
				    	out.write(_oldDemand.get(i).GetArrivalTime()+";");
				    	out.write(_oldDemand.get(i).GetProcessTime()+";");
				    	out.write(_oldDemand.get(i).GetBandwidth()+";");
				    	out.write(_oldDemand.get(i).GetRate()+";");				    	
			            for (int j=0;j<_oldDemand.get(i).GetSetFunc().size();j++)
			            	out.write(_oldDemand.get(i).GetSetFunc().get(j)+" ");
			            out.write(";");
			            for (int j=0;j<_oldDemand.get(i).Get_v_sol().size();j++)
			            	out.write(_oldDemand.get(i).Get_v_sol().get(j)+" ");
			            out.write(";");
			            for (int j=0;j<_oldDemand.get(i).Get_f_sol().size();j++)
			            	out.write(_oldDemand.get(i).Get_f_sol().get(j)+" ");			           
			       	}
			       	for (int i=0;i<s;i++)
			       	{
			       		out.newLine();
			       		System.out.println("bandwidth: "+demandArr.get(i).getBw() );
			       		out.write(demandArr.get(i).getId() +" ");
			       		out.write(demandArr.get(i).getSrc() +" ");
			       		out.write(demandArr.get(i).getArrivalTime() +" ");
			       		out.write(demandArr.get(i).getProcessTime() +" ");
			       		out.write(demandArr.get(i).getBw() +" ");
			       		out.write(demandArr.get(i).getRate() +" ");
			       		for (int j=0;j<demandArr.get(i).getNoF();j++)
			       			out.write(demandArr.get(i).getFunctions().get(j) +" ");	       		
			       		
			       	}
			       	out.newLine();
			       	out.write(df.format(g.getPriceBandwidth())+"");
			       	out.newLine();
			       	for (int i=0;i<n;i++)
			       	{		            
			       		for (int j=0;j<3;j++)
			            	   out.write(df.format(g.getCap(i+1).get(j))+" ");
			            out.write(";");
			            out.write(df.format(g.getPriceNode(i+1))+" ");			            
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{
			       		for (int j=0;j<n;j++)
			       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
			       		out.newLine();
			       	}
			       	out.close();
			    }
			    temp+=noG;
			    
			}

			//Close the input stream
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
    public static void randomRoutingProblem(String filePara)
    {
		DecimalFormat df = new DecimalFormat("#.##");
		BufferedWriter out;
		BufferedReader in;
		File file = new File(filePara);
        try {
			in = new BufferedReader(new FileReader(file));

			String strLine = in.readLine();
			int dataNo= Integer.parseInt(strLine);
			Vector<Vector<Double>> dataReal = new Vector<Vector<Double>>();
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
			int temp=1;
			dataNo = dataNo *8;
			//Read File Line By Line			
			while ((strLine = in.readLine()) != null)   {
				String[] line= strLine.split(" ");			
				int n = Integer.parseInt(line[0]);
				int noG= Integer.parseInt(line[1]);
				int m=Integer.parseInt(line[2]);
				nGraph g = new nGraph(n,dataReal);
				ArrayList<nFunction> functionArr = new ArrayList<nFunction>();
				
				Double[] hso_Func= {6.0,50.0,20.0};
			    for (int i=0;i< m;i++)
			       functionArr.add(new nFunction(new Vector<Double>(Arrays.asList(hso_Func)),i+1,n));

		    	ArrayList<Integer> idArrDemand = new ArrayList<Integer>();

			    ArrayList<nDemand> demandArr= new ArrayList<nDemand>();
			    for (int l=0;l<noG;l++)
			    {
			    	String fileName= "data\\in"+(l+temp)+".txt";
			    	out= new BufferedWriter(new FileWriter(fileName));
			    	int s = Integer.parseInt(line[3+l]);
			    	if(idArrDemand.size()>0)
			    		s=s-idArrDemand.size();
					for (int i=0;i<s;i++)
					{
						boolean flag= false;
				    	while (!flag)
				    	{
				    		int idTemp= UtilizeFunction.randInt(1, 1000);
				    		if(!idArrDemand.contains(idTemp))
				    		{
				    			idArrDemand.add(idTemp);
				    			demandArr.add(new nDemand(idTemp,n,functionArr)); 
				    			flag=true;
				    		}
				    	}
					}
				    //ghi ra file
				    out.write(n+" "+m + " "+ idArrDemand.size() );
				    out.newLine();
				    for (int i=0;i<m;i++)
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
			       	for (int i=0;i<idArrDemand.size();i++)
			       	{ 
			       		System.out.println("bandwidth: "+demandArr.get(i).getBw() );
			       		out.write(demandArr.get(i).getId() +";");
			       		out.write(demandArr.get(i).getSrc() +";");
			       		out.write(demandArr.get(i).getDest() +";");
			       		//out.write(demandArr.get(i).getArrivalTime() +";");
			       		//out.write(demandArr.get(i).getProcessTime() +";");
			       		out.write(demandArr.get(i).getBw() +";");
			       		//out.write(demandArr.get(i).getRate() +";");
			       		for (int j=0;j<demandArr.get(i).getNoF();j++)
			       			out.write(demandArr.get(i).getFunctions().get(j) +" ");	       		
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{		            
			       		for (int j=0;j<3;j++)
			            	   out.write(df.format(g.getCap(i+1).get(j))+" ");
			            //out.write(";");
			            //out.write(df.format(g.getPriceNode(i+1))+" ");
			            
			       		out.newLine();
			       	}
			       	for (int i=0;i<n;i++)
			       	{
			       		for (int j=0;j<n;j++)
			       			out.write(df.format(g.getEdgeWeight(i+1, j+1)) + " ");
			       		out.newLine();
			       	}
			       	out.close();
			    }
			    temp+=noG;
			    
			}

			//Close the input stream
			in.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	
    	
    }
	
	public static double randomDouble(Integer[] intArray)
	{
		//Integer[] intArray = new Integer[] { 100,150,200,400, 500 };
		
		ArrayList<Integer> asList = new ArrayList<Integer>(Arrays.asList(intArray));
		Collections.shuffle(asList);
		return Double.parseDouble(asList.get(0).toString());
	}
	public static void main()
	{
		//randomData("inputFile.txt");
		//randomData_lib("out.txt", 3, 5);
	}
}
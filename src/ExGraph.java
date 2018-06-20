import java.util.ArrayList;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Vector;
import java.util.List;


public class ExGraph {
    private int noV;
    List<List<Double>> w;
    List<List<Boolean>> link;
    Vector<Vector<Double>> cap;
    Vector<Double> pricePernode;
    private double price_bandwidth;
    private ArrayList<Boolean> node;
    
    //random
    
    public ExGraph(int V,Vector<Vector<Double>> dataReal )
    {
    	this.noV =V;
    	 this.cap = new Vector<Vector<Double>>();
         this.pricePernode = new Vector<Double>();
    	this.w= new ArrayList<List<Double>>();
    	this.link = new ArrayList<List<Boolean>>();
    	this.node = new ArrayList<Boolean>();
    	for (int i=0;i<V;i++)
    	{
    		ArrayList<Double> temp1 = new ArrayList<>();    		
    		for(int j=0;j<i;j++)
    		{
    			temp1.add(w.get(j).get(i));
    		}
    		temp1.add(0.0);	
    		for(int j=i+1;j<V;j++)
    		{
    			double b= UtilizeFunction.randomDouble(new Integer[] {0,0,0,600,800,600,800,600,800});
    			temp1.add(b);
    			
    		}
    		this.w.add(i, temp1);
    	}
    	for(int i=0;i<V;i++)
    	{
    		ArrayList<Boolean> temp2= new ArrayList<>();;
        	for (int j=0;j<V;j++)
        	{
        		if(w.get(i).get(j)!=0)
        			temp2.add(true);
        		else
        			temp2.add(false);
        	} 
        	this.link.add(temp2);
    	}
    	this.price_bandwidth = 0.44;
        for (int i=0;i<V;i++)
        {
      	  int index = UtilizeFunction.randInt(0, dataReal.size()-1);
      	  Vector<Double> data = dataReal.get(index);
      	  Vector<Double> t= new Vector<>(3);
      	  for(int j=0;j<3;j++)
      		  t.add(data.get(j));
      	  this.cap.addElement(t);
      	  this.pricePernode.add(data.get(3));
      	  this.node = new ArrayList<>();
      	  if(UtilizeFunction.isPositive(cap.get(i)))
      		  this.node.add(true);
      	  else
      		  this.node.add(false);
        }    	
    	
    }
    public ExGraph (int type, int k,int n0,Vector<Vector<Double>> dataReal)//Create FatTree =0, VL2=1, Bcube=2, k is number of servers
    {
    	
        this.cap = new Vector<Vector<Double>>();
        this.pricePernode = new Vector<Double>();
        this.w= new ArrayList<List<Double>>();
        this.link = new ArrayList<List<Boolean>>();
    	this.node = new ArrayList<Boolean>();
    	int V;
    	if (type==0)//Tao FatTree 128 server => k = 8
    	{
    		//Link
    		//Double[] zero ={0.0,0.0,0.0};
			//cap.add(new Vector<Double>(Arrays.asList(zero)));
			//pricePerNode.add(0.0);new Vector<Double>(Arrays.asList(0.0,0.0,0.0))
    		
    		V = (k*k*k+5*k*k)/4;
    		this.noV=V;
    		for (int i=0;i<V;i++)
            {
            	ArrayList<Double> temp=new ArrayList<>();
            	ArrayList<Boolean> temp1=new ArrayList<>();
            	for (int j=0;j<V;j++)
            	{
            		temp.add(0.0);
            		temp1.add(false);
            	}
            	this.w.add(temp);
            	this.link.add(temp1);
            }
    		for (int h=0;h<k/2;h++)// core vs aggregation
    		{
    			for (int i=h*k/2;i<(h+1)*k/2;i++)
        		{
        			for (int j=0;j<k;j++)
        			{
        				int i_temp= k*k/4 +j*k/2+h*(k/2-1);
        				this.w.get(i).set(i_temp, 50000.0);
            	    	//this.w.get(i_temp).set(i, 50000.0);
            	    	this.link.get(i).set(i_temp, true);
            	    	//this.link.get(i_temp).set(i, true);				
        			}
        		}
    		}
    		
    		for (int h=0;h<k;h++)// aggregation vs edge
    		{
    			for (int i=0;i<k/2;i++)
        		{
    				for (int j=0;j<k/2;j++)
    				{
    					int i1= h*k/2+ k*k/4+i*(k/2-1);
        				int i2= h*k/2 + 3*k*k/4 + j*(k/2-1);
        				this.w.get(i1).set(i2, 50000.0);
            	    	//this.w.get(i2).set(i1, 50000.0);
            	    	this.link.get(i1).set(i2, true);
            	    	//this.link.get(i2).set(i1, true);	
    				}
    				
        		}
    		}
    		for (int h=0;h<k;h++)//  edge vs server
    		{
    			for (int j=0;j<k/2;j++)
    			{
    				for (int i=0;i<k/2;i++)
            		{
        				int i1= h*k/2+ 3*k*k/4+j*(k/2-1);
        				int i2= h*k + 5*k*k/4 + i*(k/2-1)+j*k/2;
        				this.w.get(i1).set(i2, 5000.0);
            	    	//this.w.get(i2).set(i1, 5000.0);
            	    	this.link.get(i1).set(i2, true);
            	    	//this.link.get(i2).set(i1, true);	
            		}
    			}
    			
    		}
    		
    		//Node
    		this.price_bandwidth = 0.44;
            for (int i=0;i<V;i++)
            {
          	  int index = UtilizeFunction.randInt(0, dataReal.size()-1);
          	  Vector<Double> data = dataReal.get(index);
          	  Vector<Double> t= new Vector<>(3);
          	  for(int j=0;j<3;j++)
          		  t.add(data.get(j));
          	  this.cap.addElement(t);
          	  this.pricePernode.add(data.get(3));
          	  if(UtilizeFunction.isPositive(cap.get(i)))
            		  this.node.add(true);
            	  else
            		  this.node.add(false);
            }      
    		
    	}
    	
    	if (type==1)//Tao VL2 128 server => k = 8
    	{
    		V = (k*k*k+5*k*k)/4;
    		this.noV=V;
    		for (int i=0;i<V;i++)
            {
            	ArrayList<Double> temp=new ArrayList<>();
            	ArrayList<Boolean> temp1=new ArrayList<>();
            	for (int j=0;j<V;j++)
            	{
            		temp.add(0.0);
            		temp1.add(false);
            	}
            	this.w.add(temp);
            	this.link.add(temp1);
            }
    		for (int h=0;h<k/2;h++)// core vs aggregation
    		{
    			for (int i=h*k/2;i<(h+1)*k/2;i++)
        		{
        			for (int j=0;j<k;j++)
        			{
        				int i_temp= k*k/4 +j*k/2+h*(k/2-1);
        				this.w.get(i).set(i_temp, 5000.0);
            	    	//this.w.get(i_temp).set(i, 5000.0);
            	    	this.link.get(i).set(i_temp, true);
            	    	//this.link.get(i_temp).set(i, true);				
        			}
        		}
    		}
    		
    		for (int h=0;h<k;h++)// aggregation vs edge
    		{
    			for (int i=0;i<k/2;i++)
        		{
    				int i1= h*k/2+ k*k/4+i*(k/2-1);
    				int i2= h*k/2 + 3*k*k/4 + i*(k/2-1);
    				this.w.get(i1).set(i2, 5000.0);
        	    	//this.w.get(i2).set(i1, 5000.0);
        	    	this.link.get(i1).set(i2, true);
        	    	//this.link.get(i2).set(i1, true);	
        		}
    		}
    		for (int h=0;h<k;h++)//  edge vs server
    		{
    			for (int j=0;j<k/2;j++)
    			{
    				for (int i=0;i<k/2;i++)
            		{
        				int i1= h*k/2+ 3*k*k/4+j*(k/2-1);
        				int i2= h*k/2 + 5*k*k/4 + i*(k/2-1)+j*k/2;
        				this.w.get(i1).set(i2, 1000.0);
            	    	//this.w.get(i2).set(i1, 1000.0);
            	    	this.link.get(i1).set(i2, true);
            	    	//this.link.get(i2).set(i1, true);	
            		}
    			}
    			
    		}
    		
    		//Node
    		this.price_bandwidth = 0.44;
            for (int i=0;i<V;i++)
            {
          	  int index = UtilizeFunction.randInt(0, dataReal.size()-1);
          	  Vector<Double> data = dataReal.get(index);
          	  Vector<Double> t= new Vector<>(3);
          	  for(int j=0;j<3;j++)
          		  t.add(data.get(j));
          	  this.cap.addElement(t);
          	  this.pricePernode.add(data.get(3));
          	  if(UtilizeFunction.isPositive(cap.get(i)))
            		  this.node.add(true);
            	  else
            		  this.node.add(false);
            }      
    		
    	}
    	if (type==2)//Tao Bcube 128 server => k = 8
    	{
    		V = (int)Math.pow(n0, k+1)+ (int)Math.pow(n0, k)*(k+1);
    		this.noV=V;
    		for (int i=0;i<V;i++)
            {
            	ArrayList<Double> temp=new ArrayList<>();
            	ArrayList<Boolean> temp1=new ArrayList<>();
            	for (int j=0;j<V;j++)
            	{
            		temp.add(0.0);
            		temp1.add(false);
            	}
            	this.w.add(temp);
            	this.link.add(temp1);
            }
    		int i1=(int)Math.pow(n0, k+1);
    		for (int j=0;j<(int)Math.pow(n0, k);j++)// tach ra voi truong hop 1 cach nhau lien tiep n lan
    		{
    			for (int h=0;h<n0;h++)
    			{
    				int i2= j*n0+h;
    				this.w.get(i1).set(i2, 50000.0);
        	    	//this.w.get(i2).set(i1, 50000.0);
        	    	this.link.get(i1).set(i2, true);
        	    	//this.link.get(i2).set(i1, true);            	    	
    			}
    			i1++;
    			
    		}
    		int i2=0;
    		for (int i=1;i<k+1;i++)//cac truong hop con lai. xet voi so rong la h*n^i, va lien tiep nhau j, co phep nhay n0*(buoc nhay)
    		{
    			for (int j=0;j<(int)Math.pow(n0, k);j++)
        		{
    				
        			for (int h=0;h<n0;h++)
        			{
        				i2= h*(int)Math.pow(n0, i)+j+ n0*(j/(int)Math.pow(n0, i));
        				this.w.get(i1).set(i2, 50000.0);
            	    	//this.w.get(i2).set(i1, 50000.0);
            	    	this.link.get(i1).set(i2, true);
            	    	//this.link.get(i2).set(i1, true);            	    	
        			}
        			i1++;
        			
        		}
    		}    		
    		
    		//Node
    		this.price_bandwidth = 0.44;
            for (int i=0;i<V;i++)
            {
          	  int index = UtilizeFunction.randInt(0, dataReal.size()-1);
          	  Vector<Double> data = dataReal.get(index);
          	  Vector<Double> t= new Vector<>(3);
          	  for(int j=0;j<3;j++)
          		  t.add(data.get(j));
          	  this.cap.addElement(t);
          	  this.pricePernode.add(data.get(3));
          	  if(UtilizeFunction.isPositive(cap.get(i)))
            		  this.node.add(true);
            	  else
            		  this.node.add(false);
            }      
    		
    	}
    }
    public ExGraph(Vector<Vector<Double>> dataReal) {// 3-tier topology
        int V=30;   
    	this.noV = V;
        this.cap = new Vector<Vector<Double>>();
        this.pricePernode = new Vector<Double>();
        this.w= new ArrayList<List<Double>>();
        this.link = new ArrayList<List<Boolean>>();
    	this.node = new ArrayList<Boolean>();
        for (int i=0;i<V;i++)
        {
        	ArrayList<Double> temp=new ArrayList<>();
        	ArrayList<Boolean> temp1=new ArrayList<>();
        	for (int j=0;j<V;j++)
        	{
        		temp.add(0.0);
        		temp1.add(false);
        	}
        	this.w.add(temp);
        	this.link.add(temp1);
        }
      for (int i=0;i<4;i++)
    	  for (int j=i+1;j<5;j++)
    	  {
    		  //core node
    		  double b= UtilizeFunction.randomDouble(new Integer[] {430, 510,600,730,800,860, 852,900,1000,1600,2000 });
    		  this.w.get(i).set(j, b);
    		  //this.w.get(j).set(i, b);
    		  if(b>0)
    		  {
    			  this.link.get(i).set(j, true);
    			  //this.link.get(j).set(i, true);
    		  }
    	  }
      for(int i=0;i<10;i++)
      {
    	  //aggregation node
    	  for (int j=0;j<5;j++)
    	  {
    		  double b= UtilizeFunction.randomDouble(new Integer[] {0,430, 510,600,730,800,860, 852,900,1000,1600,2000 });
    		  this.w.get(i).set(j, b);
    		  //this.w.get(j).set(i, b);
    		  if(b>0)
    		  {
    			  this.link.get(i).set(j, true);
    			  //this.link.get(j).set(i, true);
    		  }
    	  }
      }
      for(int i=0;i<15;i++)
      {
    	  //aggregation node
    	  for (int j=0;j<10;j++)
    	  {
    		  double b= UtilizeFunction.randomDouble(new Integer[] {0,430, 510,600,730,800,860, 852,900,1000,1600,2000 });
    		  this.w.get(i).set(j, b);
    		  //this.w.get(j).set(i, b);
    		  if(b>0)
    		  {
    			  this.link.get(i).set(j, true);
    			  //this.link.get(j).set(i, true);
    		  }
    	  }
      }
          
          for(int i=0;i<V;i++)
      	{
      		ArrayList<Boolean> temp2= new ArrayList<>();;
          	for (int j=0;j<V;j++)
          	{
          		if(w.get(i).get(j)!=0)
          			temp2.add(true);
          		else
          			temp2.add(false);
          	} 
          	this.link.add(temp2);
      	}
          this.price_bandwidth = 0.44;
          for (int i=0;i<V;i++)
          {
        	  int index = UtilizeFunction.randInt(0, dataReal.size()-1);
        	  Vector<Double> data = dataReal.get(index);
        	  Vector<Double> t= new Vector<>(3);
        	  for(int j=0;j<3;j++)
        		  t.add(data.get(j));
        	  this.cap.addElement(t);
        	  this.pricePernode.add(data.get(3));
        	  if(UtilizeFunction.isPositive(cap.get(i)))
          		  this.node.add(true);
          	  else
          		  this.node.add(false);
          }         
          
      }   
    public ExGraph(Vector<Vector<Double>> K,Vector<Double> r, List<List<Double>> w,double price_bw) 
    {//model newest
    this.noV = r.size();      
	  this.w= new ArrayList<List<Double>>();
	  this.cap = new Vector<Vector<Double>>(noV);
	  this.pricePernode = new Vector<Double>(noV);
	  //this.Nprice = new Vector<Vector<Double>>(V);
	  this.node = new ArrayList<Boolean>();
	  this.link = new ArrayList<List<Boolean>>();
	  for (int i=0;i<noV;i++)
      {
      	ArrayList<Boolean> temp1=new ArrayList<>();
      	for (int j=0;j<noV;j++)
      	{
      		temp1.add(false);
      	}
      	this.link.add(temp1);
      }
     for (int i=0;i<w.size();i++)
     {
    	ArrayList<Double> temp= new ArrayList<>();
     	for (int j=0;j<w.get(0).size();j++)
     	{
     		temp.add(w.get(i).get(j));
     		if(w.get(i).get(j)>0)
     			this.link.get(i).set(j, true);
     	}
     	this.w.add(temp);
     }
     for (int i=0;i<noV;i++)
     {
   	  this.cap.addElement(K.get(i));
   	  this.pricePernode.add(r.get(i));
   	  if(UtilizeFunction.isPositive(this.cap.get(i)))
		  this.node.add(true);
	  else
		  this.node.add(false);
     }
     this.price_bandwidth = price_bw;
         
  }
    
    // number of vertices and edges
    public int getV(){ return noV; }
    public double getPriceNode(int v)//new Mode
    {
    	return pricePernode.get(v-1);
    }
    public Vector<Double> getCap(int v)//new model
    {
    	return cap.get(v-1);
    }
    public boolean setCap(int v, Vector<Double> c)//new model
    {
    	cap.set(v-1, c);
    	if(UtilizeFunction.isPositive(c))
    		node.set(v-1, true);
    	return true;
    }
    public boolean addCap(int v, Vector<Double> c)//new model
    {
    	cap.set(v-1,UtilizeFunction.add(c, cap.get(v-1)));
    	if(UtilizeFunction.isPositive(c))
    		node.set(v-1, true);
    	return true;
    }
    public boolean minusCap(int v, Vector<Double> c)//new model
    {
    	cap.set(v-1,UtilizeFunction.minus(cap.get(v-1),c));
    	if(UtilizeFunction.isPositive(c))
    	{
    		this.node.set(v-1, true);
    	}
    	return true;
    }
    public boolean setEdgeWeight(int v, int u,double c)
    {   	
    	if(c>0)
    	{
    		this.w.get(v-1).set(u-1, c);
        	//this.w.get(u-1).set(v-1, c);
    		this.link.get(v-1).set(u-1, true);
    		//this.link.get(u-1).set(v-1, true);
    	}
    	else
    	{
    		this.w.get(v-1).set(u-1, 0.0);
        	//this.w.get(u-1).set(v-1, 0.0);
    		this.link.get(v-1).set(u-1, false);
    		//this.link.get(u-1).set(v-1, false);
    	}
    	return true;
    }
    public boolean addEdgeWeight(int v, int u,double c)
    {
    	double t1 =this.w.get(v-1).get(u-1);
    	this.w.get(v-1).set(u-1, t1+c);
        //this.w.get(u-1).set(v-1, t1+c);
    	this.link.get(v-1).set(u-1, true);
    	//this.link.get(u-1).set(v-1, true);
    	return true;
    }
    public double getEdgeWeight(int u, int v)
    {
    		return this.w.get(u-1).get(v-1);
    }
    public double getPriceBandwidth()
    {
    	return this.price_bandwidth;
    }
    public boolean getExistNode(int u)
    {
    	return this.node.get(u-1);
    } 
    public boolean getExistLink(int u, int v)
    {
    	return this.link.get(u-1).get(v-1);
    }
    
    public void removeNode(int u)
    {
    	//danh dau dinh khong duoc xet
    	
    	this.node.set(u-1, false);
    }
    public void removeLink(int u,int v)
    {
    	//danh dau dinh khong duoc xet0
    	this.w.get(u-1).set(v-1, 0.0);
    	//this.w.get(v-1).set(u-1, 0.0);
    	this.link.get(u-1).set(v-1, false);
    	//this.link.get(v-1).set(u-1, false);
    }

    // does the graph contain the edge v-w?
    public boolean contains(int u, int v) {
        return (w.get(u-1).get(v-1)>0);
    }

    // return list of neighbors of v
    public Iterable<Integer> link_bandwidth(int v) {
        return new AdjIterator(v-1);
    }
    // support iteration over graph vertices
    private class AdjIterator implements Iterator<Integer>, Iterable<Integer> {
        int u, v = 0;
        AdjIterator(int u) { this.u = u; }

        public Iterator<Integer> iterator() { return this; }

        public boolean hasNext() {
            while (v < noV) {
                if (w.get(u).get(v)>0) return true;
                v++;
            }
            return false;
        }

        public Integer next() {
            if (hasNext()) { return v++;                         }
            else           { throw new NoSuchElementException(); }
        }

        public void remove()  { throw new UnsupportedOperationException();  }
    }

    // test client
    public static void main(String[] args) {
        int V = Integer.parseInt(args[0]);
        Vector<Vector<Double>> data = new Vector<Vector<Double>>();
        ExGraph G = new ExGraph(V,data);
        //Graph G = new Graph(V, E);
        System.out.print(G.toString());
        System.out.println ("\n"+ "Minh Ham Minh Ham "+G.w.get(1).get(1));
        System.out.println (G.cap.get(1).get(1) + ":::"+G.pricePernode.get(1));
        //StdOut.println(G);
    }

}
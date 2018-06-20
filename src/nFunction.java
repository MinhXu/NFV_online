
import java.util.ArrayList;
import java.util.Vector;


public class nFunction {
	private int id;
    private double bw;
    private Vector<Double> lamda;
    private ArrayList<Integer> vNFnode;
    //private myVector lamda= new myVector(3);
    
    // empty graph with V vertices
    public nFunction(int id,int V) {//khoi tao random function
    	lamda= new Vector<>(3);
    	vNFnode = new ArrayList<Integer>();
        if ( id <=0 ) throw new RuntimeException("Number of vertices must be nonnegative");
        this.id = id; 
        this.bw =  24 * Math.random()+1;//random requirement for function
        for (int i=0;i<3;i++)
        {
        	double temp= UtilizeFunction.randDouble(50);
        	lamda.addElement(temp);
        } 
        int noNode = UtilizeFunction.randInt(V/5, V/4);
        int _count = 0;
        while (_count<noNode)
        {
     	   int _randNode = UtilizeFunction.randInt(1, V);
     	   if (!vNFnode.contains(_randNode))
     	   {
     		   vNFnode.add(_randNode);
     		   _count++;
     	   }
     	   
        }
    }
    public nFunction(Vector<Double> hso, int id,int V) {//khoi tao random function
    	lamda= new Vector<>(3);
    	vNFnode = new ArrayList<Integer>();
        if ( id <=0 ) throw new RuntimeException("Number of vertices must be nonnegative");
        this.id = id; 
        for (int i=0;i<3;i++)
        {
        	//double temp= UtilizeFunction.randDouble(1,hso.get(i));
        	double temp=1.0;
        	lamda.addElement(temp);
        }
        // 10 va 6
        int noNode = 20;
        int _count = 0;
        while (_count<noNode)
        {
     	   int _randNode = UtilizeFunction.randInt(1, V);
     	   if (!vNFnode.contains(_randNode))
     	   {
     		   vNFnode.add(_randNode);
     		   _count++;
     	   }
     	   
        }
    }

    public nFunction(int id, double bw,int V) {//gan id va bw cho 1 function
    	this(id,V);
        this.bw = bw;        
    }
    public nFunction(int _id, Vector<Double> _lamda, ArrayList<Integer> _vnfNode) {//gan id va bw cho 1 function
    	lamda= new Vector<>(3);
    	vNFnode = new ArrayList<Integer>();
    	id=_id;
        for (int i=0;i<3;i++)
        {
        	lamda.addElement(_lamda.get(i));
        } 
        for (int i=0;i<_vnfNode.size();i++)
        	vNFnode.add(_vnfNode.get(i));
    }

    // number of vertices and edges
    public int id() { return id; }
    public double bw() {
    	return bw;}
    public Vector<Double> getLamda()
    {
    	return lamda;
    }
    public ArrayList<Integer> getVnfNode()
    {
    	return vNFnode;
    }



    // string representation of Graph - takes quadratic time
    public String toString() {
        StringBuilder s = new StringBuilder();
        s.append(id + ": " + bw);
        return s.toString();
    }


    // test client
    public static void main(String[] args) {
    }

}
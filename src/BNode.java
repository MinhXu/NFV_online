import java.util.ArrayList;



public class BNode {
	//id, ngraph, previousNode
    //private int id;//id of node
    private nGraph g;
    private int preNode;
    private ArrayList<Integer> path;
    private ArrayList<Pair> fLoc;
    
//    public BNode()
//    {
//    	this.id =-1;
//    }
   // public BNode(int _id, int _preNode,nGraph _g,ArrayList<Integer> _path)
    public BNode( int _preNode,nGraph _g,ArrayList<Integer> _path, ArrayList<Pair> _fLoc)
    {
    	if(_g!=null)
    		this.g = new nGraph(_g.cap,_g.w);
    	else
    		this.g = null;
    	this.preNode = _preNode;
    	this.path = new ArrayList<>();
    	this.path = _path;
    	this.fLoc=_fLoc;
    }
//    public int getId()
//    {
//    	return id;
//    }
    public ArrayList<Pair> getFLoc()
    {
    	return fLoc;
    }
    public boolean setFLoc(ArrayList<Pair> FLoc)
    {
    	this.fLoc=FLoc;
    	return true;
    }
    public nGraph getGraph()
    {
    	return g;
    }
    public int getPreNode()
    {
    	return preNode;
    }
    public ArrayList<Integer> getPath()
    {
    	return path;
    }
    public boolean setGraph(nGraph _g)
    {
    	this.g = new nGraph(_g.cap,_g.w);
    	return true;
    }
    public boolean setPath(ArrayList<Integer> _p)
    {
    	this.path = _p;
    	return true;
    }
}
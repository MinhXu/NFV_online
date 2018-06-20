
public class Pair {
    private int node;
    private int function;
    
    public Pair()
    {
    	this.node =-1;
    	this.function = -1;
    }
    public Pair(int _node, int _function)
    {
    	this.node=_node;
    	this.function = _function;
    }
    public boolean CompareTo(Pair l)
    {
    	if(this.node==l.node && this.function==l.function)
    		return true;
    	else
    		return false;
    }
    public int getnode()
    {
    	return node;
    }
    public int getfunction()
    {
    	return function;
    }
    public void setnode(int _node)
    {
    	this.node=_node;
    }
    public void setfunction(int _function)
    {
    	this.function=_function;
    }
}
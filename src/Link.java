
public class Link {
    private int start;
    private int end;
    
    public Link()
    {
    	this.start =-1;
    	this.end = -1;
    }
    public Link(int _start, int _end)
    {
    	this.start=_start;
    	this.end = _end;
    }
    public boolean CompareTo(Link l)
    {
    	if(this.start==l.start && this.end==l.end)
    		return true;
    	else
    		return false;
    }
    public int getStart()
    {
    	return start;
    }
    public int getEnd()
    {
    	return end;
    }
    public void setStart(int _start)
    {
    	this.start=_start;
    }
    public void setEnd(int _end)
    {
    	this.end=_end;
    }
}
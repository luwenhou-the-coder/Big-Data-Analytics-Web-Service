
import java.util.LinkedHashMap;
import java.util.Map;
// This class is for designing a LRU map to work as a cache
public class LRUMap extends LinkedHashMap<String,String> {
	//the maximim capacity 
	private final int capacity;
	private static final long serialVersionUID = 1L;
	public LRUMap(int capacity){
		super((int) Math.ceil(1000 / 0.75) + 1, 0.75f, true); 
		this.capacity = capacity;
	}
	//this method override the method and when add the new element, it will remove the least recently used one. 
	@Override
	public boolean removeEldestEntry(Map.Entry<String, String> eldest){
		return size()>capacity;
	}
	
}

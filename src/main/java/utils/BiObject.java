package utils;

public class BiObject<V, K> {
	private final V obj1;
	private final K obj2;
	
	public BiObject(V obj1, K obj2) {
		this.obj1 = obj1;
		this.obj2 = obj2;
	}
	
	public V getObj1() {
		return obj1;
	}
	
	public K getObj2() {
		return obj2;
	}
}

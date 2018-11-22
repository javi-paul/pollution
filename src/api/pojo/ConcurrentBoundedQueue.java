package api.pojo;

import java.lang.reflect.Array;

/** Concurrent, generic and bounded FIFO queue.
 * <p>When the queue is filled, the oldest element is removed. Elements cannot be removed. The list of elements stored is provided in the same order they were inserted.</p>
 * <p>Developed by: Francisco José Fabra Collado, fron GRC research group in Universitat Politècnica de València (Valencia, Spain).</p> */

public class ConcurrentBoundedQueue<T> {
	
	Class<T> c;
	private int size, count, p, ini;
	private T[] positions;
	
	@SuppressWarnings("unused")
	private ConcurrentBoundedQueue() {}
	
	/** Creates a bounded FIFO queue of the provided size. Example with Integers:
	 * <p>LastElements<Integer> elements = new LastElements<>(Integer.class, 5);</p> */
	@SuppressWarnings("unchecked")
	public ConcurrentBoundedQueue(Class<T> c, int size) {
		this.c = c;
		this.size = size;
		this.count = 0;
		this.p = 0;
		this.positions = (T[]) Array.newInstance(c, size);
	}
	
	/** Adds a new element at the end of the queue.
	 * <p>If the queue is full, the oldest element is removed.</p> */
	public synchronized void add(T element) {
		positions[p] = element;
		if (this.count < this.size) {
			this.count++;
		}
		
		this.p = (this.p + 1) % this.size;
		
		if (this.count < this.size) {
			this.ini = 0;
		} else {
			this.ini = this.p;
		}
	}
	
	public synchronized T[] getLastValues() {
		@SuppressWarnings("unchecked")
		T[] res = (T[]) Array.newInstance(c, this.count);
		int p = this.ini;
		for (int i = 0; i < this.count; i++) {
			res[i] = this.positions[p];
			p = (p + 1) % this.size;
		}
		return res;
	}
}

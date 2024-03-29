package com.protocols.pollution.pojo;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;

public class ValueSet {
	private final HashMap<Integer, HashMap<Integer, Double>> pSet;
	private double min, max;
	
	public ValueSet() {
		pSet = new HashMap<>();
		min = Double.MAX_VALUE;
		max = Double.MIN_VALUE;
	}
	
	public void add(DataPoint v) {
		add(v.getX(), v.getY(), v.getMeasurement());
	}
	
	public void add(DataPoint p, double c) {
		add(p.getX(), p.getY(), c);
	}
	public void add(int a, int b, double c) {
		if (!pSet.containsKey(a)) pSet.put(a, new HashMap<>());
		pSet.get(a).put(b, c);
		if (c < min) min = c;
		if (c > max) max = c;
	}
	
	public boolean contains(DataPoint p) {
		return contains(p.getX(), p.getY());
	}
	public boolean contains(int a, int b) {
		if (pSet.containsKey(a)) return pSet.get(a).containsKey(b);
		return false;
	}
	
	public void remove(DataPoint p) {
		remove(p.getX(), p.getY());
	}
	public void remove(int a, int b) {
		if (pSet.containsKey(a)) {
			pSet.get(a).remove(b);
			if (pSet.get(a).isEmpty()) pSet.remove(a);
		}
	}
	
	public DataPoint[] toArray() {
		ArrayList<DataPoint> al = new ArrayList<>();
		
		Iterator<Integer> i1 = pSet.keySet().iterator();
		int e;
		Entry<Integer, Double> e2;
		Iterator<Entry<Integer, Double>> i2;
		
		while(i1.hasNext()) {
			e = i1.next();
			i2 = pSet.get(e).entrySet().iterator();
			while(i2.hasNext()) {
				e2 = i2.next();
				al.add(new DataPoint(e, e2.getKey(), e2.getValue()));
			}
		}
		return al.toArray(new DataPoint[0]);
	}
	
	@Override
	public String toString() {
		return Arrays.deepToString(this.toArray());
	}
	
	public Iterator<DataPoint> iterator() {
		return new Itr();
	}

	public double getMin() {
		return min;
	}
	
	public double getMax() {
		return max;
	}
	
	private class Itr implements Iterator<DataPoint> {
		Iterator<Integer> itA;
		Iterator<Entry<Integer, Double>> itB;
		int a;
		Entry<Integer, Double> e;

		public Itr() {
			itA = pSet.keySet().iterator();
			if(itA.hasNext()) {
				a = itA.next();
				itB = pSet.get(a).entrySet().iterator();
			} else itB = new Iterator<>() {

				@Override
				public boolean hasNext() {
					return false;
				}

				@Override
				public Entry<Integer, Double> next() {
					return null;
				}};
		}

		@Override
		public boolean hasNext() {
			if(itA.hasNext()) return true;
			return itB.hasNext();
		}

		@Override
		public DataPoint next() {
			while(!itB.hasNext() && itA.hasNext()) {
				a = itA.next();
				itB = pSet.get(a).entrySet().iterator();
			}
			if(itB.hasNext()) {
				e = itB.next();
				return new DataPoint(a, e.getKey(), e.getValue());
			}
			return null;
		}
		
	}
	
	public boolean isEmpty() {
		return pSet.isEmpty();
	}
	
}

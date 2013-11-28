/**
 * 
 */
package com.trungsi.vpclient.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author trungsi
 *
 */
public class CollectionUtils {

	public static boolean listContains(List<String> list, String text) {
		for (String elem : list) {
			if (text.contains(elem)) {
				return true;
			}
		}
		return false;
	}

	public static <T> List<T> list(T... ts) {
		ArrayList<T> list = new ArrayList<T>();
		for (T t : ts) {
			list.add(t);
		}

		return list;
	}

	public static <K, V> Map<K, V> map(Entry<K, V>... entries) {
		HashMap<K, V> map = new HashMap<K, V>();
		for (Entry<K, V> entry : entries) {
			map.put(entry.key, entry.value);
		}

		return map;
	}

	public static class Entry<K, V> {
		public final K key;
		public final V value;

		public Entry(K key, V value) {
			this.key = key;
			this.value = value;
		}
	}

	@SuppressWarnings("all")
	public static <K, V> Entry<K, V> entry(K key, V value) {
		return new Entry(key, value);
	}

}

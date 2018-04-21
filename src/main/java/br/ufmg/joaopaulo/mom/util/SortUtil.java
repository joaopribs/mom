package br.ufmg.joaopaulo.mom.util;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class SortUtil {

	public static <KeyType, ValueType extends Comparable<ValueType>> Map<KeyType, ValueType> sortMapByValue(
			Map<KeyType, ValueType> originalMap, boolean ascending) {
		Stream<Entry<KeyType, ValueType>> stream = originalMap.entrySet().stream();
		
		Comparator<Entry<KeyType, ValueType>> comparator = null;

		if (ascending) {
			comparator = new Comparator<Entry<KeyType, ValueType>>() {
				@Override
				public int compare(Entry<KeyType, ValueType> o1, Entry<KeyType, ValueType> o2) {
					return o1.getValue().compareTo(o2.getValue());
				}
			};
		}
		else {
			comparator = new Comparator<Entry<KeyType, ValueType>>() {
				@Override
				public int compare(Entry<KeyType, ValueType> o1, Entry<KeyType, ValueType> o2) {
					return o2.getValue().compareTo(o1.getValue());
				}
			};
		}

		return stream
				.sorted(comparator)
				.collect(
						Collectors.toMap(
								Entry::getKey, 
								Entry::getValue, 
								(e1, e2) -> e1, 
								LinkedHashMap::new));
	}

	public static <MapKeyType extends Comparable<MapKeyType>, ListValueType extends Comparable<ListValueType>> 
		Map<MapKeyType, List<ListValueType>> sortMapAndUnderlyingList(
				Map<MapKeyType, List<ListValueType>> originalMap, 
				boolean ascendingForMap, 
				boolean ascendingForList) {
		Comparator<ListValueType> listComparator = null;
		if (ascendingForList) {
			listComparator = new Comparator<ListValueType>() {
				@Override
				public int compare(ListValueType o1, ListValueType o2) {
					return o1.compareTo(o2);
				}
			};
		}
		else {
			listComparator = new Comparator<ListValueType>() {
				@Override
				public int compare(ListValueType o1, ListValueType o2) {
					return o2.compareTo(o1);
				}
			};
		}
		
		Comparator<Entry<MapKeyType, List<ListValueType>>> mapComparator = null;
		if (ascendingForMap) {
			mapComparator = new Comparator<Entry<MapKeyType, List<ListValueType>>>() {
				@Override
				public int compare(
						Entry<MapKeyType, List<ListValueType>> o1, Entry<MapKeyType, List<ListValueType>> o2) {
					return o1.getKey().compareTo(o2.getKey());
				}
			};
		}
		else {
			mapComparator = new Comparator<Entry<MapKeyType, List<ListValueType>>>() {
				@Override
				public int compare(
						Entry<MapKeyType, List<ListValueType>> o1, Entry<MapKeyType, List<ListValueType>> o2) {
					return o2.getKey().compareTo(o1.getKey());
				}
			};
		}
		
		Map<MapKeyType, List<ListValueType>> orderedMap = new LinkedHashMap<MapKeyType, List<ListValueType>>();
		
		for (Entry<MapKeyType, List<ListValueType>> entry : originalMap.entrySet()) {
			MapKeyType mapKey = entry.getKey();
			List<ListValueType> underlyingList = entry.getValue();
			underlyingList.sort(listComparator);
			orderedMap.put(mapKey, underlyingList);
		}
		
		return originalMap.entrySet().stream()
				.sorted(mapComparator)
				.collect(
						Collectors.toMap(
								Entry::getKey, 
								Entry::getValue, 
								(e1, e2) -> e1, 
								LinkedHashMap::new));
	}
	
	public static <MapKeyType extends Comparable<MapKeyType>, ListValueType> Map<MapKeyType, List<ListValueType>> 
	sortMapByTheSizeOfList(Map<MapKeyType, List<ListValueType>> originalMap, boolean ascending) {
		Comparator<Entry<MapKeyType, List<ListValueType>>> comparator = null;
		
		if (ascending) {
			comparator = new Comparator<Entry<MapKeyType, List<ListValueType>>>() {
				@Override
				public int compare(Entry<MapKeyType, List<ListValueType>> o1,
						Entry<MapKeyType, List<ListValueType>> o2) {
					Integer list1Size = o1.getValue().size();
					Integer list2Size = o2.getValue().size();
					return list1Size.compareTo(list2Size);
				}
			};
		}
		else {
			comparator = new Comparator<Entry<MapKeyType, List<ListValueType>>>() {
				@Override
				public int compare(Entry<MapKeyType, List<ListValueType>> o1,
						Entry<MapKeyType, List<ListValueType>> o2) {
					Integer list1Size = o1.getValue().size();
					Integer list2Size = o2.getValue().size();
					return list2Size.compareTo(list1Size);
				}
			};
		}
		
		return originalMap.entrySet().stream()
				.sorted(comparator)
				.collect(
						Collectors.toMap(
								Entry::getKey, 
								Entry::getValue, 
								(e1, e2) -> e1, 
								LinkedHashMap::new));
	}
	
	public static <KeyType extends Comparable<KeyType>, ValueType> Map<KeyType, ValueType> sortMapByKey(
			Map<KeyType, ValueType> originalMap, boolean ascending) {
		Stream<Entry<KeyType, ValueType>> stream = originalMap.entrySet().stream();
		
		Comparator<Entry<KeyType, ValueType>> comparator = null;

		if (ascending) {
			comparator = new Comparator<Entry<KeyType, ValueType>>() {
				@Override
				public int compare(Entry<KeyType, ValueType> o1, Entry<KeyType, ValueType> o2) {
					return o1.getKey().compareTo(o2.getKey());
				}
			};
		}
		else {
			comparator = new Comparator<Entry<KeyType, ValueType>>() {
				@Override
				public int compare(Entry<KeyType, ValueType> o1, Entry<KeyType, ValueType> o2) {
					return o2.getKey().compareTo(o1.getKey());
				}
			};
		}

		return stream
				.sorted(comparator)
				.collect(
						Collectors.toMap(
								Entry::getKey, 
								Entry::getValue, 
								(e1, e2) -> e1, 
								LinkedHashMap::new));
	}
	
	public static <KeyType, InnerKeyType, InnerValueType extends Comparable<InnerValueType>> 
		void sortUnderlyingMapsByValue(Map<KeyType, Map<InnerKeyType, InnerValueType>> originalMap, boolean ascending) {
		
		for (Entry<KeyType, Map<InnerKeyType, InnerValueType>> entry : originalMap.entrySet()) {
			KeyType key = entry.getKey();
			Map<InnerKeyType, InnerValueType> innerMap = entry.getValue();
			
			Map<InnerKeyType, InnerValueType> orderedMap = sortMapByValue(innerMap, ascending);
			
			originalMap.put(key, orderedMap);
		}
	}

}

/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
package org.javastack.figaro;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Represents types/destinations
 */
public class GossipType {
	/**
	 * Destination is NONE (messages are dropped)
	 */
	public static final Integer NULL = Integer.valueOf(0); // DROP
	/**
	 * Destination is BROADCAST (ALL registered listeners)
	 */
	public static final Integer BROADCAST = Integer.valueOf(Integer.MAX_VALUE); // BROADCAST
	//
	private final ConcurrentHashMap<String, Integer> map = new ConcurrentHashMap<String, Integer>();
	private final AtomicInteger counter = new AtomicInteger();

	GossipType() {
		map.putIfAbsent("NULL", BROADCAST);
		map.putIfAbsent("BROADCAST", BROADCAST);
	}

	/**
	 * Register new type/destination
	 * 
	 * @param name
	 * @return id of new type/destination
	 */
	public Integer registerName(final String name) {
		final Integer newid = Integer.valueOf(counter.incrementAndGet());
		final Integer assigned = map.putIfAbsent(name, newid);
		return (assigned == null ? newid : assigned);
	}

	/**
	 * Query for a registered type/destination
	 * 
	 * @param name
	 * @return id of registered type/destination
	 */
	public Integer getIdByName(final String name) {
		return map.get(name);
	}
}

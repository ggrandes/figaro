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
package net.figaro;

import java.util.ArrayDeque;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.log4j.Logger;

/**
 * GossipMonger is the manager of all Whispers and Talkers
 */
public class GossipMonger {
	private static final Logger log = Logger.getLogger(GossipMonger.class);
	private static final GossipMonger singleton = new GossipMonger();
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final GossipType types = GossipType.getInstance();
	private final ConcurrentHashMap<Integer, Set<Talker>> map = new ConcurrentHashMap<Integer, Set<Talker>>();
	private final AtomicBoolean isShutdown = new AtomicBoolean();
	private final ThreadLocal<ArrayDeque<Whisper<?>>> ref = new ThreadLocal<ArrayDeque<Whisper<?>>>() {
		@Override
		protected ArrayDeque<Whisper<?>> initialValue() {
			return new ArrayDeque<Whisper<?>>();
		}
	};

	private GossipMonger() {
	}

	/**
	 * Return default instance
	 * 
	 * @return instance
	 */
	public static GossipMonger getInstance() {
		return singleton;
	}

	TalkerContext initTalker(final String name, final TalkerType type,
			final Talker talker) {
		final String tname = (name == null ? genRandomName(talker) : name);
		final Chest<Whisper<?>> chest = createChest(type);
		return new TalkerContext(tname, type, this, chest, talker);
	}

	void registerListenerTalker(final Talker talker) {
		registerListenerTalker(talker.getName(), talker);
		registerListenerTalker(GossipType.BROADCAST, talker);
	}

	void unregisterListenerTalker(final Talker talker) {
		for (final Integer id : map.keySet()) {
			unregisterListenerTalker(id, talker);
		}
	}

	void registerListenerTalker(final String type, final Talker talker) {
		final Integer id = types.registerName(type);
		registerListenerTalker(id, talker);
	}

	void registerListenerTalker(final Integer id, final Talker talker) {
		final Set<Talker> newSet = new CopyOnWriteArraySet<Talker>();
		final Set<Talker> set = map.putIfAbsent(id, newSet);
		((set == null) ? newSet : set).add(talker);
		if (log.isDebugEnabled())
			log.debug("Registered type: " + id + " talker: " + talker);
	}

	void unregisterListenerTalker(final String type, final Talker talker) {
		final Integer id = types.getIdByName(type);
		unregisterListenerTalker(id, talker);
	}

	void unregisterListenerTalker(final Integer id, final Talker talker) {
		try {
			if (map.get(id).remove(talker)) {
				if (log.isDebugEnabled())
					log.debug("Unregistered type: " + id + " talker: " + talker);
			}
		} catch (Exception e) {
			log.error("Error Unregistered type: " + id + " talker: " + talker
					+ " exception:" + e.toString(), e);
		}
	}

	/**
	 * Send message
	 * 
	 * @param whisper
	 * @return true if message is sended
	 */
	public boolean send(Whisper<?> whisper) {
		if (isShutdown.get())
			return false;
		// Internal Queue (Local Thread) for INPLACE multiple recursive calls
		final ArrayDeque<Whisper<?>> localQueue = ref.get();
		if (!localQueue.isEmpty()) {
			localQueue.addLast(whisper);
			return true;
		}
		localQueue.addLast(whisper);
		while ((whisper = localQueue.peekFirst()) != null) {
			final Integer type = whisper.type;
			final Set<Talker> set = map.get(type);
			if (set != null) {
				for (final Talker talker : set) {
					if (talker.getState().chest != null) {
						while (!talker.getState().queueMessage(whisper))
							;
					} else {
						talker.newMessage(whisper);
					}
				}
			}
			localQueue.pollFirst();
		}
		return true;
	}

	final void submitWork(final Runnable talker) {
		threadPool.submit(talker);
	}

	/**
	 * Shutdown GossipMonger and associated Threads
	 */
	public void shutdown() {
		log.info("Shuting down GossipMonger");
		isShutdown.set(true);
		threadPool.shutdown(); // Disable new tasks from being submitted
		// TODO: Wait for messages to end processing
		shutdownAndAwaitTermination(threadPool);
	}

	private void shutdownAndAwaitTermination(final ExecutorService pool) {
		// pool.shutdown(); // Disable new tasks from being submitted
		try {
			// Wait a while for existing tasks to terminate
			if (!pool.awaitTermination(10, TimeUnit.SECONDS)) {
				pool.shutdownNow(); // Cancel currently executing tasks
				// Wait a while for tasks to respond to being cancelled
				if (!pool.awaitTermination(10, TimeUnit.SECONDS))
					System.err.println("Pool did not terminate");
			}
		} catch (InterruptedException ie) {
			// (Re-)Cancel if current thread also interrupted
			pool.shutdownNow();
			// Preserve interrupt status
			Thread.currentThread().interrupt();
		}
	}

	private final String genRandomName(final Talker talker) {
		return ("JohnDoe-" + Integer.toString(talker.hashCode()));
	}

	private Chest<Whisper<?>> createChest(final TalkerType type) {
		if (type == TalkerType.INPLACE)
			return null;
		if (type == TalkerType.QUEUED_BOUNDED)
			return new ChestBounded<Whisper<?>>();
		if (type == TalkerType.QUEUED_UNBOUNDED)
			return new ChestUnbounded<Whisper<?>>();
		return null;
	}
}

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
public class GossipMonger implements Runnable {
	private static final Logger log = Logger.getLogger(GossipMonger.class);
	private static GossipMonger singleton = null;
	private final ExecutorService threadPool = Executors.newCachedThreadPool();
	private final GossipType types = GossipType.getDefaultInstance();
	private final ConcurrentHashMap<Integer, Set<Talker>> map = new ConcurrentHashMap<Integer, Set<Talker>>();
	private final Set<Talker> listenerQueuedTalkers = new CopyOnWriteArraySet<Talker>();
	private final AtomicBoolean isShutdown = new AtomicBoolean();
	private final ThreadLocal<ArrayDeque<Whisper<?>>> ref = new ThreadLocal<ArrayDeque<Whisper<?>>>() {
		@Override
		protected ArrayDeque<Whisper<?>> initialValue() {
			return new ArrayDeque<Whisper<?>>();
		}
	};

	private GossipMonger() {
		threadPool.submit(this);
	}

	/**
	 * Return default instance
	 * 
	 * @return instance
	 */
	public static GossipMonger getDefaultInstance() {
		createInstance();
		return singleton;
	}

	private static synchronized void createInstance() {
		if (singleton == null) {
			singleton = new GossipMonger();
		}
	}

	TalkerContext initTalker(final String name, final TalkerType type, final Talker talker) {
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
		switch (talker.getState().type) {
		case QUEUED_UNBOUNDED:
		case QUEUED_BOUNDED:
			listenerQueuedTalkers.add(talker);
			break;
		}
		if (log.isDebugEnabled())
			log.debug("Registered type: " + id + " talker: " + talker);
	}

	void unregisterListenerTalker(final String type, final Talker talker) {
		final Integer id = types.getIdByName(type);
		unregisterListenerTalker(id, talker);
	}

	void unregisterListenerTalker(final Integer id, final Talker talker) {
		try {
			// TODO: Unregister from listenerQueuedTalkers
			if (map.get(id).remove(talker)) {
				if (log.isDebugEnabled())
					log.debug("Unregistered type: " + id + " talker: " + talker);
			}
		} catch (Exception e) {
			log.error("Error Unregistered type: " + id + " talker: " + talker + " exception:" + e.toString(),
					e);
		}
	}

	/**
	 * Send message and optionally wait response
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
					final TalkerContext ctx = talker.getState();
					switch (ctx.type) {
					case INPLACE_UNSYNC:
						talker.newMessage(whisper);
						break;
					case INPLACE_SYNC:
						synchronized (talker) {
							talker.newMessage(whisper);
						}
						break;
					case QUEUED_UNBOUNDED:
					case QUEUED_BOUNDED:
						while (!ctx.queueMessage(whisper))
							;
						break;
					}
				}
			}
			localQueue.pollFirst();
		}
		return true;
	}

	final void scheduleTalkerContext(final TalkerContext ctx) {
		threadPool.submit(ctx);
	}

	@Override
	public void run() {
		try {
			if (log.isDebugEnabled())
				log.debug("Task begin: " + toString());
			while (!isShutdown()) {
				boolean scheduled = false;
				for (final Talker talker : listenerQueuedTalkers) {
					final TalkerContext ctx = talker.getState();
					if (ctx.needScheduling()) {
						scheduled = true;
						scheduleTalkerContext(ctx);
					}
				}
				if (scheduled) {
					Thread.yield();
				} else {
					try {
						// System.out.println("GossipMonger sleep()");
						Thread.sleep(100);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace(System.out);
						break;
					}
				}
			}
		} finally {
			if (log.isDebugEnabled())
				log.debug("Task end: " + toString());
		}
	}

	/**
	 * Shutdown GossipMonger and associated Threads
	 */
	public void shutdown() {
		singleton = null;
		log.info("Shuting down GossipMonger");
		isShutdown.set(true);
		threadPool.shutdown(); // Disable new tasks from being submitted
		// TODO: Wait for messages to end processing
		shutdownAndAwaitTermination(threadPool);
	}

	/**
	 * Check if Shutdown is set
	 */
	public boolean isShutdown() {
		return isShutdown.get();
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
		switch (type) {
		case INPLACE_UNSYNC:
			return null;
		case INPLACE_SYNC:
			return null;
		case QUEUED_UNBOUNDED:
			return new ChestUnbounded<Whisper<?>>();
		case QUEUED_BOUNDED:
			return new ChestBounded<Whisper<?>>();
		}
		throw new IllegalArgumentException("Invalid TalkerType");
	}
}

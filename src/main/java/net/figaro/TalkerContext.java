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

import org.apache.log4j.Logger;

/**
 * Represents the internal state and logic of a Talker
 */
class TalkerContext implements Runnable {
	private static final Logger log = Logger.getLogger(TalkerContext.class);
	private volatile boolean running = false;
	final String name;
	final TalkerType type;
	final GossipMonger gossipMonger;
	final Chest<Whisper<?>> chest;
	final Talker parent;

	TalkerContext(final String name, final TalkerType type, final GossipMonger gossipMonger,
			final Chest<Whisper<?>> chest, final Talker parent) {
		this.name = name;
		this.type = type;
		this.gossipMonger = gossipMonger;
		this.chest = chest;
		this.parent = parent;
	}

	final boolean queueMessage(final Whisper<?> whisper) {
		return chest.offer(whisper);
	}

	public boolean needScheduling() {
		return !(running || chest.isEmpty());
	}

	@Override
	public void run() {
		if (running)
			return;
		try {
			running = true;
			if (log.isDebugEnabled())
				log.debug("Task begin: " + toString());
			if (chest.isEmpty())
				return;
			long idleBegin = System.currentTimeMillis();
			Whisper<?> whisper = null;
			while (!gossipMonger.isShutdown()) {
				long now = System.currentTimeMillis();
				while ((whisper = chest.poll()) != null) {
					parent.newMessage(whisper);
					now = System.currentTimeMillis();
					idleBegin = now;
				}
//				Thread.yield();
				final long idle = (now - idleBegin);
				if (idle > 1000)
					break;
				if (idle > 100) {
					try {
						System.out.println(getName() + " TalkerContext sleep()" );
						Thread.sleep(100);
						now = System.currentTimeMillis();
						idleBegin = now;
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						e.printStackTrace(System.out);
						break;
					}
				}
			}
		} finally {
			running = false;
			if (log.isDebugEnabled())
				log.debug("Task end: " + toString());
		}
	}

	String getName() {
		return name;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(64);
		sb.append(getName());
		sb.append("[");
		sb.append(getClass().getName()).append("@").append(hashCode());
		sb.append("]");
		return sb.toString();
	}

}

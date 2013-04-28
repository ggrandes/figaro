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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

/**
 * Represents the internal state and logic of a Talker
 */
class TalkerContext implements Runnable {
	private static final Logger log = Logger.getLogger(TalkerContext.class);
	final AtomicBoolean running = new AtomicBoolean();
	final String name;
	final TalkerType type;
	final GossipMonger gossipMonger;
	final Chest<Whisper<?>> chest;
	final Talker parent;

	TalkerContext(final String name, final TalkerType type,
			final GossipMonger gossipMonger, final Chest<Whisper<?>> chest,
			final Talker parent) {
		this.name = name;
		this.type = type;
		this.gossipMonger = gossipMonger;
		this.chest = chest;
		this.parent = parent;
	}

	final boolean queueMessage(final Whisper<?> whisper) {
		final boolean ret = chest.offer(whisper);
		if (ret && !running.getAndSet(true)) {
			gossipMonger.submitWork(this);
		}
		return ret;
	}

	@Override
	public void run() {
		try {
			log.info("Task begin: " + toString());
			Whisper<?> whisper = null;
			while ((whisper = chest.poll()) != null) {
				parent.newMessage(whisper);
			}
		} finally {
			log.info("Task end: " + toString());
			running.set(false);
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

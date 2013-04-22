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

/**
 * Unit of processing (abstract implementation)
 */
public abstract class AbstractTalker implements Talker {
	final TalkerState state;
	final GossipMonger gossipMonger;

	/**
	 * Create Talker of type INPLACE
	 * 
	 * @param name
	 */
	public AbstractTalker(final String name) {
		this(name, TalkerType.INPLACE);
	}

	/**
	 * Create talker
	 * 
	 * @param name
	 * @param type
	 */
	public AbstractTalker(final String name, final TalkerType type) {
		this.gossipMonger = GossipMonger.getInstance();
		this.state = gossipMonger.initTalker(name, type, this);
	}

	@Override
	public void registerListener() {
		gossipMonger.registerListenerTalker(this);
	}

	@Override
	public void unregisterListener() {
		gossipMonger.unregisterListenerTalker(this);
	}

	@Override
	public void registerExtraType(final String type) {
		gossipMonger.registerListenerTalker(type, this);
	}

	@Override
	public String getName() {
		return state.getName();
	}

	@Override
	public TalkerState getState() {
		return state;
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

	@Override
	public void sendMessage(final Whisper<?> whisper) {
		gossipMonger.send(whisper);
	}

	@Override
	public abstract void newMessage(final Whisper<?> whisper);

}

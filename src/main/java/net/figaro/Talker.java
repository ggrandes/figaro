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
 * Unit of processing (interface)
 */
public interface Talker {
	/**
	 * Return the name of Talker
	 * 
	 * @return name
	 */
	public String getName();

	/**
	 * Return the TalkerState associated to this Talker (used by GossipMonger)
	 * 
	 * @return state
	 */
	public TalkerState getState();

	/**
	 * Register Talker in GossipMonger for incoming messages
	 */
	public void registerListener();

	/**
	 * Unregister Talker in GossipMonger from incoming messages
	 */
	public void unregisterListener();

	/**
	 * Register extra Type/Name in GossipMonger for incoming messages
	 * 
	 * @param type
	 */
	public void registerExtraType(final String type);

	/**
	 * New message for this Talker
	 * 
	 * @param whisper
	 */
	public void newMessage(final Whisper<?> whisper);

	/**
	 * Send new message from this Talker
	 * 
	 * @param whisper
	 */
	public void sendMessage(final Whisper<?> whisper);
}

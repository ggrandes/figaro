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

/**
 * This is a simple Message
 */
public class Whisper<T> {
	public final Talker from;
	public final Integer dest;
	public final T msg;

	/**
	 * Create new message (from=null)
	 * 
	 * @param dest
	 * @param msg
	 */
	public Whisper(final Integer dest, final T msg) {
		this(null, dest, msg);
	}

	/**
	 * Create new message (from=null)
	 * 
	 * @param dest
	 * @param msg
	 */
	public Whisper(final String dest, final T msg) {
		this(null, dest, msg);
	}

	/**
	 * Create new message
	 * 
	 * @param from
	 * @param dest
	 * @param msg
	 */
	public Whisper(final Talker from, final Integer dest, final T msg) {
		this.from = from;
		this.dest = (dest == null ? GossipType.NULL : dest);
		this.msg = msg;
	}

	/**
	 * Create new message
	 * 
	 * @param from
	 * @param dest
	 * @param msg
	 */
	public Whisper(final Talker from, final String dest, final T msg) {
		this(from, GossipMonger.getDefaultInstance().getTypeIdByName(dest), msg);
	}

	@Override
	public String toString() {
		return "Whisper from: <" + (from == null ? null : from.getName())
				+ "> dest: <" + dest + "> msg: <" + msg + ">";
	}

	public static void main(final String[] args) {
		final Whisper<String> w = new Whisper<String>(GossipType.BROADCAST, "hello world!");
		System.out.println(w);
	}

}

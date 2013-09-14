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
 * This is a simple Message
 */
public class Whisper<T> {
	public final Talker from;
	public final Integer type;
	public final T msg;

	/**
	 * Create new message (destination drop)
	 * 
	 * @param msg
	 */
	public Whisper(final T msg) {
		this(null, GossipType.NULL, msg);
	}

	/**
	 * Create new message (destination drop)
	 * 
	 * @param from
	 * @param msg
	 */
	public Whisper(final Talker from, final T msg) {
		this(from, GossipType.NULL, msg);
	}

	/**
	 * Create new message (from=null)
	 * 
	 * @param type
	 * @param msg
	 */
	public Whisper(final Integer type, final T msg) {
		this(null, type, msg);
	}

	/**
	 * Create new message (from=null)
	 * 
	 * @param type
	 * @param msg
	 */
	public Whisper(final String type, final T msg) {
		this(null, type, msg);
	}

	/**
	 * Create new message
	 * 
	 * @param from
	 * @param type
	 * @param msg
	 */
	public Whisper(final Talker from, final Integer type, final T msg) {
		this.from = from;
		this.type = (type == null ? GossipType.NULL : type);
		this.msg = msg;
	}

	/**
	 * Create new message
	 * 
	 * @param from
	 * @param type
	 * @param msg
	 */
	public Whisper(final Talker from, final String type, final T msg) {
		this(from, GossipType.getDefaultInstance().getIdByName(type), msg);
	}

	public String toString() {
		return "Whisper from: <" + (from == null ? null : from.getName())
				+ "> type: <" + type + "> msg: <" + msg + ">";
	}

	public static void main(final String[] args) {
		final Whisper<String> w = new Whisper<String>("hello world!");
		System.out.println(w);
	}

}

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
 * How messages are handled between Talkers
 */
public enum TalkerType {
	/**
	 * Messages are send/received in current Thread (in-place) without synchronized block
	 */
	INPLACE_UNSYNC,
	/**
	 * Messages are send/received in current Thread (in-place) inside synchronized block
	 */
	INPLACE_SYNC,
	/**
	 * Messages are queued (queue is depth unlimited) for processes in a worker
	 * Thread
	 */
	QUEUED_UNBOUNDED,
	/**
	 * Messages are queued (queue is depth limited) for processes in a worker
	 * Thread
	 */
	QUEUED_BOUNDED
}

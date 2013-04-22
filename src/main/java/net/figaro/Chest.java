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
 * Container for messages
 */
public interface Chest<T extends Whisper<?>> {
	/**
	 * Return if Chest is empty
	 * 
	 * @return isEmptry?
	 */
	public boolean isEmpty();

	/**
	 * Return size of Chest
	 * 
	 * @return
	 */
	public int size();

	/**
	 * Return element from chest or null if empty
	 * 
	 * @return
	 */
	public T poll();

	/**
	 * Add element to chest and return if success
	 * 
	 * @param value
	 * @return true if element is added
	 */
	public boolean offer(final T value);
}

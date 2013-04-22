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

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * Memory container for messages (size bounded)
 */
public class ChestBounded<T extends Whisper<?>> implements Chest<T> {
	private final ArrayBlockingQueue<T> chest = new ArrayBlockingQueue<T>(512);

	@Override
	public boolean isEmpty() {
		return chest.isEmpty();
	}

	@Override
	public T poll() {
		try {
			return chest.poll(1000, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
		return null;
	}

	@Override
	public boolean offer(final T value) {
		return chest.offer(value);
	}

	@Override
	public int size() {
		return chest.size();
	}
}

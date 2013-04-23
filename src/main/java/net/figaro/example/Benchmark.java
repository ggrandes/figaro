package net.figaro.example;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.figaro.AbstractTalker;
import net.figaro.GossipMonger;
import net.figaro.GossipType;
import net.figaro.TalkerType;
import net.figaro.Whisper;

public class Benchmark {
	public static void main(final String[] args) throws Throwable {
		//
		// Create Talkers
		final TestTalker talkerRecv1 = new TestTalker("Simon",
				TalkerType.INPLACE);
		final TestTalker talkerRecv2 = new TestTalker("Mateo",
				TalkerType.INPLACE);
		//
		// Register for listening
		talkerRecv1.registerListener();
		talkerRecv2.registerListener();
		//
		// Create Senders
		long begin = System.currentTimeMillis();
		Thread[] threads = new Thread[2];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					TestTalker t = new TestTalker(null);
					final long begin = System.currentTimeMillis();
					while (true) {
						final long now = System.currentTimeMillis();
						final Integer ts = Integer.valueOf((int) (now / 1000));
						final Whisper<?> whisper = new Whisper<Integer>(null,
								GossipType.BROADCAST, ts);
						t.sendMessage(whisper);
						if ((begin + 5000) < now)
							break;
					}
				}
			});
		}
		//
		// Start Senders
		for (final Thread th : threads) {
			th.start();
		}
		//
		// Wait Senders
		for (final Thread th : threads) {
			th.join();
		}
		long end = System.currentTimeMillis();
		int c = 0;
		c += talkerRecv1.count();
		c += talkerRecv2.count();
		System.out.println("time=" + (end - begin) + " count=" + c + " req/s="
				+ (c / Math.max(((end - begin) / 1000), 1)));
		System.out.println("talkerRecv1.count()=" + talkerRecv1.count()
				+ " talkerRecv1.dump()=" + talkerRecv1.dump());
		System.out.println("talkerRecv2.count()=" + talkerRecv2.count());
		//
		GossipMonger.getInstance().shutdown();
	}

	public static class TestTalker extends AbstractTalker {
		private HashMap<Integer, AtomicInteger> h = new HashMap<Integer, AtomicInteger>();

		public TestTalker(final String name) {
			super(name);
		}

		public TestTalker(final String name, final TalkerType type) {
			super(name, type);
		}

		@Override
		public void newMessage(final Whisper<?> whisper) {
			if (whisper.msg instanceof Integer) {
				AtomicInteger i = h.get(whisper.msg);
				if (i == null) {
					i = new AtomicInteger();
					h.put((Integer) whisper.msg, i);
				}
				i.incrementAndGet();
			} else {
				System.out.println(getName() + " Receive new whisper: "
						+ whisper);
			}
		}

		public String dump() {
			return getName() + ": " + h.toString();
		}

		public long count() {
			long c = 0;

			for (AtomicInteger ai : h.values()) {
				c += ai.get();
			}
			return c;
		}
	}
}

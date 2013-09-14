package net.figaro.example;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.figaro.AbstractTalker;
import net.figaro.GossipMonger;
import net.figaro.GossipType;
import net.figaro.TalkerType;
import net.figaro.Whisper;

public class Benchmark {
	protected static final long TIME_TEST = 5000;
	protected static final int LOOPS = 3;

	public static void main(final String[] args) throws Throwable {
		for (int i = 0; i < LOOPS; i++) {
			int processors = Runtime.getRuntime().availableProcessors();
			System.out.println("availableProcessors: " + processors);
			//
			while (processors > 0) {
				doTest(TalkerType.INPLACE_UNSYNC, processors);
				doTest(TalkerType.INPLACE_SYNC, processors);
				doTest(TalkerType.QUEUED_UNBOUNDED, processors);
				doTest(TalkerType.QUEUED_BOUNDED, processors);
				processors >>= 1;
				System.out.println();
				break;
			}
		}
		System.out.println("--- Waiting ---");
		Thread.sleep(5000);
	}

	public static void doTest(final TalkerType type, final int threadCount) throws Throwable {
		System.out.println("--- Testing type=" + type + " threads=" + threadCount);
		//
		// Create Talkers
		final TestTalker[] talkerRecv = new TestTalker[threadCount];
		for (int i = 0; i < talkerRecv.length; i++) {
			talkerRecv[i] = new TestTalker("recv" + i, type);
			// Register for listening
			talkerRecv[i].registerListener();
		}
		//
		// Create Senders
		final long begin = System.currentTimeMillis();
		final Thread[] threads = new Thread[threadCount];
		for (int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Runnable() {
				@Override
				public void run() {
					TestTalker t = new TestTalker(null);
					final long begin = System.currentTimeMillis();
					while (true) {
						final long now = System.currentTimeMillis();
						final Integer ts = Integer.valueOf((int) (now / 1000));
						final Whisper<?> whisper = new Whisper<Integer>(null, GossipType.BROADCAST, ts);
						t.sendMessage(whisper);
						if ((begin + TIME_TEST) < now)
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
		for (TestTalker t : talkerRecv) {
			System.out.println(type + " name=" + t.getName() + " count()=" + t.count() + " dump()="
					+ t.dump());
			c += t.count();
			// Unregister
			t.unregisterListener();
		}
		System.out.println(type + " threads=" + threadCount + " time=" + (end - begin) + " count=" + c
				+ " req/s=" + (c / Math.max(((end - begin) / 1000), 1)));
		GossipMonger.getDefaultInstance().shutdown();
	}

	public static class TestTalker extends AbstractTalker {
		private Map<Integer, AtomicInteger> h = new ConcurrentHashMap<Integer, AtomicInteger>();

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
				System.out.println(getName() + " Receive new whisper: " + whisper);
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

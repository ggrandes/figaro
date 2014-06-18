package org.javastack.example;

import org.javastack.AbstractTalker;
import org.javastack.GossipMonger;
import org.javastack.Talker;
import org.javastack.Whisper;

public class HelloWorld {
	public static void main(final String[] args) throws Throwable {
		final Talker recv = new AbstractTalker("dummyReceiver") {
			@Override
			public void newMessage(final Whisper<?> whisper) {
				System.out.println(getName() + " Receive new whisper: "
						+ whisper);
			}
		};
		final Talker send = new AbstractTalker("dummySender") {
			@Override
			public void newMessage(final Whisper<?> whisper) {
				System.out.println(getName() + " Receive new whisper: "
						+ whisper);
			}
		};
		//
		recv.registerListener();
		//
		send.sendMessage(new Whisper<String>(recv.getName(), "hello world!"));
		//
		recv.unregisterListener();
		//
		send.sendMessage(new Whisper<String>(recv.getName(), "destroy world!"));
		//
		GossipMonger.getDefaultInstance().shutdown();
	}
}

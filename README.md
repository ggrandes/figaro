# figaro

In-process Message System for Actor Model in Java (like Kilim, μJavaActors, Akka,... but very simplified). Open Source Java project under Apache License v2.0

### Current Development Version is [0.0.2](https://maven-release.s3.amazonaws.com/release/net/figaro/figaro/0.0.2/figaro-0.0.2.jar)

---

## DOC

#### Usage Example

```java
import net.figaro.AbstractTalker;
import net.figaro.GossipMonger;
import net.figaro.Talker;
import net.figaro.Whisper;

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
			public void newMessage(final Whisper<?> whisper) {}
		};
		//
		recv.registerListener();
		//
		send.sendMessage(new Whisper<String>(recv.getName(), "hello world!"));
		//
		GossipMonger.getInstance().shutdown();
	}
}
```

* More examples in [Example package](https://github.com/ggrandes/figaro/tree/master/src/main/java/net/figaro/example/)

---

## MAVEN

Add the Figaro maven repository location to your pom.xml: 

    <repositories>
        <repository>
            <id>figaro-maven-s3-repo</id>
            <url>https://maven-release.s3.amazonaws.com/release/</url>
        </repository>
    </repositories>

Add the Figaro dependency to your pom.xml:

    <dependency>
        <groupId>net.figaro</groupId>
        <artifactId>figaro</artifactId>
        <version>0.0.2</version>
    </dependency>

---
Inspired in [Kilim](http://www.malhar.net/sriram/kilim/) and [μJavaActors](https://github.com/ggrandes/j-javaactors-ibm/), this code is Java-minimalistic version.

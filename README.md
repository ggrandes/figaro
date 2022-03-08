# figaro

In-process Asynchronous Message System in Java for Event Bus/Actor Model (like Kilim, µJavaActors, Akka,... but very simplified). Open Source Java project under Apache License v2.0

### Current Stable Version is [1.0.2](https://search.maven.org/#search|ga|1|g%3Aorg.javastack%20a%3Afigaro)

---

## DOC

#### Usage Example

```java
import org.javastack.figaro.AbstractTalker;
import org.javastack.figaro.GossipMonger;
import org.javastack.figaro.Talker;
import org.javastack.figaro.Whisper;

public class HelloWorld {
	public static void main(final String[] args) throws Throwable {
		final GossipMonger gossipMonger = GossipMonger.getDefaultInstance();
		final Talker recv = new AbstractTalker("dummyReceiver") {
			@Override
			public void newMessage(final Whisper<?> whisper) {
				System.out.println(getName() + " Receive new whisper: "
						+ whisper);
			}
		};
		//
		recv.registerListener();
		//
		gossipMonger.send(new Whisper<String>(recv.getName(), "hello world!"));
		//
		gossipMonger.shutdown();
	}
}
```

* More examples in [Example package](https://github.com/ggrandes/figaro/tree/master/src/main/java/org/javastack/figaro/example/)

---

## MAVEN

Add the dependency to your pom.xml:

    <dependency>
        <groupId>org.javastack</groupId>
        <artifactId>figaro</artifactId>
        <version>1.0.2</version>
    </dependency>

---

## Benchmarks

###### Values are not accurate, but orientative. Higher better. All test Running on Laptop { Windows Vista (32bits), Core 2 Duo 1.4Ghz (U9400), 4GB Ram, Magnetic Disk (WDC-WD5000BEVT-22ZAT0) }.

TalkerType | Msg/s
:--- | ---:
INPLACE UNSYNC | 5.6M
INPLACE SYNC | 2.6M
QUEUED UNBOUNDED | 1.6M
QUEUED BOUNDED(512) | 688K


###### Comparative of Figaro 0.0.4 (queued_unbounded) vs Akka 2.2.0 (unbounded mailbox). Higher better. All test Running on Amazon EC2 { Ubuntu 12.04 LTS (64bits), [CC2.8XLARGE](http://aws.amazon.com/en/ec2/instance-types/#instance-details) (Dual Intel Xeon E5-2670, 8-cores hyperthreading) }.

Threads | 32 | 16 | 8 | 4
:--- | ---: | ---: | ---: | ---:
Akka | 7.6M | 6.6M | 4.1M | 2.6M
Figaro | 12.4M | 14.6M | 5.6M | 3.6M


---
Inspired in [Kilim](http://www.malhar.net/sriram/kilim/) and [μJavaActors](https://github.com/ggrandes/j-javaactors-ibm/), this code is Java-minimalistic version.

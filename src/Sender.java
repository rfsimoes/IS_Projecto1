import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicPublisher;
import javax.jms.TopicSession;
import javax.naming.InitialContext;
import javax.naming.NamingException;

public class Sender {
	private TopicConnectionFactory tcf;
	private TopicConnection tc;
	private TopicSession ts;
	private Topic topic;
	private TopicPublisher publisher;

	public Sender() throws JMSException  {
		try {
			InitialContext init = new InitialContext();
			this.tcf = (TopicConnectionFactory) init.lookup("jms/RemoteConnectionFactory");
			this.tc = (TopicConnection) this.tcf.createTopicConnection("joao", "pedro");
			this.tc.start();
			topic = (Topic) init.lookup("jms/topic/testTopic");
			this.ts = this.tc.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			this.publisher = this.ts.createPublisher(topic);
		} catch (NamingException e) {
			// e.printStackTrace();
			System.out.println("Cannot find topic");
		}
	}

	void send(String string) throws JMSException {
		TextMessage tm = this.ts.createTextMessage(string);
		this.publisher.publish(tm);
	}

	void close() throws JMSException {
		this.tc.close();
	}

}
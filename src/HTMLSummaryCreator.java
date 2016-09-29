import java.io.File;
import java.io.IOException;
import java.io.StringReader;

import javax.jms.JMSException;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.jms.TopicConnection;
import javax.jms.TopicConnectionFactory;
import javax.jms.TopicSession;
import javax.jms.TopicSubscriber;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;


public class HTMLSummaryCreator {

	private TopicConnectionFactory tcf;
	private TopicConnection tc;
	private TopicSession ts;
	private Topic topic;
	private TopicSubscriber subscriber;

	public HTMLSummaryCreator() throws JMSException {
		try {
			InitialContext init = new InitialContext();
			this.tcf = (TopicConnectionFactory) init.lookup("jms/RemoteConnectionFactory");
			this.tc = (TopicConnection) this.tcf.createTopicConnection("joao", "pedro");
			tc.setClientID("htmlsc1");
			this.tc.start();
			topic = (Topic) init.lookup("jms/topic/testTopic");
			this.ts = this.tc.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			this.subscriber = ts.createDurableSubscriber(topic, "htmlsc");
		} catch (NamingException e) {
			// e.printStackTrace();
			System.out.println("Cannot find topic");
		}
	}

	private String receive() throws JMSException {
		TextMessage msg = (TextMessage) subscriber.receive();
		return msg.getText();
	}

	private void close() throws JMSException {
		this.tc.close();
	}

	/**
	 * @param args
	 * @throws JMSException
	 * @throws NamingException
	 * @throws IOException 
	 * @throws ParserConfigurationException 
	 * @throws SAXException 
	 * @throws TransformerException 
	 */
	public static void main(String[] args) throws NamingException, JMSException, IOException, ParserConfigurationException, SAXException, TransformerException {
		
		// Receber mensagem
		HTMLSummaryCreator r = new HTMLSummaryCreator();
		
		while(true)
		{
			String msg = r.receive();
			System.out.println("HTMLSummaryCreator recebeu a mensagem");
			//r.close();
			
			
			// Validar ficheiro XML
			if(validateXMLSchema("Example.xsd", msg)==true){
			
				// Adicionar ao XML a referência ao XSLT
				String aux = (msg.split("\\?>"))[0]+"?><?xml-stylesheet type=\"text/xsl\" href=\"Stylesheet.xsl\"?>"+msg.split("\\?>")[1];
				TransformerFactory transformerFactory = TransformerFactory.newInstance();
				Transformer transformer = transformerFactory.newTransformer();
				DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    DocumentBuilder builder = factory.newDocumentBuilder();
			    Document doc = builder.parse(new InputSource(new StringReader(aux)));
			    DOMSource source = new DOMSource(doc);
			    String ficheiroXML = "HTML_"+System.currentTimeMillis()+".xml";
				StreamResult result = new StreamResult(new File(ficheiroXML));
				transformer.transform(source, result);
				
				System.out.println("Ficheiro '" + ficheiroXML + "' criado.\n");
			}
			
		}
		
		
	}
	
	
	/*
	 * Função para validar o ficheiro XML com o ficheiro XSD.
	 */
	public static boolean validateXMLSchema(String xsdPath, String xmlPath){
        
        try {
            SchemaFactory factory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = factory.newSchema(new File(xsdPath));
            Validator validator = schema.newValidator();
            validator.validate(new StreamSource(new StringReader(xmlPath)));
            System.out.println("Valid XML!");
        } catch (IOException | SAXException e) {
            System.out.println("Invalid XML!");
            return false;
        }
        return true;
    }

}

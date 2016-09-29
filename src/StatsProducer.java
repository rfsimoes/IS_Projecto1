import generated.Cnn;
import generated.News;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

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
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.SAXException;


public class StatsProducer {
	
	private static Cnn cnn;

	private TopicConnectionFactory tcf;
	private TopicConnection tc;
	private TopicSession ts;
	private Topic topic;
	private TopicSubscriber subscriber;

	
	public StatsProducer() throws JMSException {
		try {
			InitialContext init = new InitialContext();
			this.tcf = (TopicConnectionFactory) init.lookup("jms/RemoteConnectionFactory");
			this.tc = (TopicConnection) this.tcf.createTopicConnection("joao", "pedro");
			tc.setClientID("statsp");
			this.tc.start();
			topic = (Topic) init.lookup("jms/topic/testTopic");
			this.ts = this.tc.createTopicSession(false, TopicSession.AUTO_ACKNOWLEDGE);
			this.subscriber = ts.createDurableSubscriber(topic, "statsp");
		} catch (NamingException e) {
			//e.printStackTrace();
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
	 */
	public static void main(String[] args) throws JMSException {
		
		// Receber mensagem
		StatsProducer r = new StatsProducer();
		
		while(true){
			String msg = r.receive();
			System.out.println("StatsProducer recebeu a mensagem");
			//r.close();
			
			
			// Validar ficheiro XML
			if(validateXMLSchema("Example.xsd", msg)==true){
			
				// Passar informação do XML para os objectos Java
				XMLToJava(msg);
				
				// Gerar estatísticas
				statistics();
				
				// Gravar notícias com menos de 12 horas
				saveRecentNews();
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
	
	
	/*
	 * Função para extrair a informação do XML para objectos Java
	 */
	private static void XMLToJava(String xmlStr) {
		try {
			// Create JAXB context and initializing Unmarshaller
			JAXBContext jc = JAXBContext.newInstance(Cnn.class);
			Unmarshaller u = jc.createUnmarshaller();
			
			// Specify the name of xml file to be read
			//File f = new File("GeradoPeloMarshaller.xml");
			
			// This will create Java object - cnn from the XML file
			cnn = (Cnn) u.unmarshal(new StringReader(xmlStr));
			
		} catch (JAXBException e) {
			//e.printStackTrace();
			System.out.println("Failed to unmarshal data");
		}
		
	}
	
	
	/*
	 * Função para gerar as estatísticas das notícias.
	 */
	private static void statistics() {
		// Array para guardar notícias com mais de 12 horas
		ArrayList<News> listOfOldNews = new ArrayList<News>();
		
		// Contador para o número de notícias com menos de 12 horas
		int countRecentNews=0;
		
		for(int i=0;i<cnn.getRegion().size();i++){
			for(News n:cnn.getRegion().get(i).getNews()){
				if(recente(n)){
					countRecentNews++;
				}
				else{
					listOfOldNews.add(n);
					
				}
			}
			for(News n:listOfOldNews){
				cnn.getRegion().get(i).getNews().remove(n);
			}
			listOfOldNews.clear();
		}
		
		System.out.println("Há " + countRecentNews + " notícias com menos de 12 horas!");
	}

	
	/*
	 * Função para verificar se uma dada notícia tem menos de 12 horas.
	 * Parâmetro de entrada: notícia.
	 * Devolve true se a notícia tiver menos de 12 horas, false caso contrário. 
	 */
	private static boolean recente(News n) {
		// Ir buscar data atual
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
		Calendar cal = Calendar.getInstance();
		
		// Ir buscar data da notícia
		int ano = n.getDate().getYear();
		int mes = n.getDate().getMonth()-1;
		int dia = n.getDate().getDay();
		int hora = n.getDate().getHour()/100;
		int minutos = n.getDate().getHour()%100;
		
		// Criar data da notícia
		Calendar calendar = new GregorianCalendar(ano,mes,dia,hora,minutos);	
		
		// Comparar datas
		Date d1 = null;
		Date d2 = null;
		try {
			d1 = dateFormat.parse(dateFormat.format(cal.getTime()));
			d2 = dateFormat.parse(dateFormat.format(calendar.getTime()));
		} catch (ParseException e) {
			e.printStackTrace();
		}
		
		long diff = d1.getTime() - d2.getTime(); //milissegundos
		long diffSeconds = diff / 1000; 		//segundos
		
		// 12 horas = 43200 segundos
		if(diffSeconds <= 43200)
			return true;
		
		return false;
	}
	
	
	/*
	 * Função para fazer o marshal das notícias com menos de 12 horas num ficheiro XML
	 */
	private static void saveRecentNews() {
		try {
			// Create JAXB context and initializing Marshaller
			JAXBContext jc = JAXBContext.newInstance(Cnn.class);
			Marshaller m = jc.createMarshaller();
			
			// For getting nice formatted output
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			
			// Specify the name of xml file to be created
			File XMLFile = new File("RecentNews.xml");
			
			// Writing to XML file
			m.marshal(cnn, XMLFile);
			
		} catch (JAXBException e) {
			//e.printStackTrace();
			System.out.println("Failed to marshal data (saveRecentNews)");
		}
		
	}

}

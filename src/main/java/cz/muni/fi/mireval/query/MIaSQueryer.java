package cz.muni.fi.mireval.query;

import cz.muni.fi.mireval.query.topics.Topic;
import cz.muni.fi.mireval.query.topics.TopicTerm;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MIaSQueryer implements Queryer {
    
    
    private static final Logger LOG = Logger.getLogger(MIaSQueryer.class.getName());    
    
    private static final String URL_TEMPLATE = "/ws/search?query={query}&offset=0&limit=1000";

    private static final String RESULT_ELEMENT_NAME = "result";
    private static final String RESULT_ID_ELEMENT_NAME = "id";
    private static final String RESULT_INFO_ELEMENT_NAME = "info";
    
    private String queryUrl;

    public MIaSQueryer(String queryUrl) {
        this.queryUrl = queryUrl;
    }
    
    public String performQueries(List<Topic> topics, String outputDir, String runName) {
        HttpURLConnection connection = null;
        StringBuilder fileOutput = new StringBuilder();        
        StringBuilder topicLog = new StringBuilder();
        for (Topic topic : topics) {
            StringBuilder query = new StringBuilder();
            for (TopicTerm keyword : topic.getTextKeyword()) {
                query.append(keyword.getTerm()).append(" ");
            }
            for (TopicTerm keyword : topic.getFormulaKeyword()) {
                query.append(keyword.getTerm()).append(" ");
            }
            String queryString = query.toString();
            try {
                 queryString = URLEncoder.encode(queryString, "UTF-8");
            } catch (UnsupportedEncodingException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            String serviceUrl = queryUrl+URL_TEMPLATE.replace("{query}", queryString);
            try {
                LOG.log(Level.INFO, "Topic {0}", topic.getTopicId());
                topicLog.append("Topic ").append(topic.getTopicId()).append("\n");
                topicLog.append(query.toString()).append("\n");
                topicLog.append(serviceUrl).append("\n");
                URL url = new URL(serviceUrl);
                connection = (HttpURLConnection)url.openConnection();
                connection.setRequestMethod("GET");
                InputStream inputStream = connection.getInputStream();
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
                Document doc = documentBuilder.parse(inputStream);
                
                StringBuilder output = new StringBuilder();
                NodeList resultElements = doc.getElementsByTagName(RESULT_ELEMENT_NAME);
                for (int i = 0; i < resultElements.getLength(); i++) {
                    Element resultElement = (Element) resultElements.item(i);
                    String resultId = transformResultId(resultElement.getFirstChild().getTextContent());
                    String score = resultElement.getElementsByTagName(RESULT_INFO_ELEMENT_NAME).item(0).getTextContent().replace("score = ", "");
                    output.append(topic.getTopicId()).append(" 1 ").append(resultId).append(" ").append(i+1).append(" ").append(score).append(" ").append(runName).append("\n");
                }
                topicLog.append("Results").append("\n");
                topicLog.append(output.toString());                
                topicLog.append("\n\n");
                
                fileOutput.append(output);
            } catch (MalformedURLException ex) {
                Logger.getLogger(MIaSQueryer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(MIaSQueryer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SAXException ex) {
                Logger.getLogger(MIaSQueryer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(MIaSQueryer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        String resultTsvFile = outputDir + runName + ".tsv";
        writeToFile(resultTsvFile, fileOutput.toString());
        writeToFile(outputDir + runName + ".log", topicLog.toString());

        return resultTsvFile;
    }

    private String transformResultId(String textContent) {
        return textContent.substring(textContent.lastIndexOf("/")+1).replace(".xhtml", "");
    }

    private void writeToFile(String outputFileName, String results) {
        BufferedWriter bw = null;
        try {        
            bw = new BufferedWriter(new FileWriter(outputFileName));
            bw.write(results);
        } catch (IOException ex) {
            Logger.getLogger(MIaSQueryer.class.getName()).log(Level.SEVERE, null, ex);
        }
        finally {
            try {
                bw.close();
            } catch (IOException ex) {
                Logger.getLogger(MIaSQueryer.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}

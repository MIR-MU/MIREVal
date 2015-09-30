package cz.muni.fi.mireval.query.topics;

import cz.muni.fi.mireval.XMLUtil;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

public class NTCIRTopicLoader implements TopicLoader {
    
    private static final String TOPIC_ELEMENT_NAME = "topic";
    private static final String TOPIC_ID_ELEMENT_NAME = "num";
    private static final String FORMULA_ELEMENT_NAME = "m:math";
    private static final String KEYWORD_ELEMENT_NAME = "keyword";
    private static final String SEMANTICS_ELEMENT_NAME = "m:semantics";
    private static final String ANNOTATION_ELEMENT_NAME = "m:annotation";
    private static final String ANNOTATION_XML_ELEMENT_NAME = "m:annotation-xml";
    private static final String QVAR_ELEMENT_NAME = "mws:qvar";
    private static final String ID_ATTR_NAME = "id";
    
    private static final String REPLACEMENT_IDENTIFIER_NAME = "identifier";
    private static final String REPLACEMENT_OPERATOR_NAME = "operator";
    
    private static final Map<String, String> ContentMML_tags = new HashMap<String, String>() {
        {
            put(REPLACEMENT_IDENTIFIER_NAME, "m:ci");
            put(REPLACEMENT_OPERATOR_NAME, "m:ci");
        }
    };    
    private static final Map<String, String> PresentationMML_tags = new HashMap<String, String>() {
        {
            put(REPLACEMENT_IDENTIFIER_NAME, "m:mi");
            put(REPLACEMENT_OPERATOR_NAME, "m:mo");
        }
    };

    public List<Topic> loadTopics(String topicsFilePath) {
        List<Topic> result = new ArrayList<Topic>();
        try {
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = dbf.newDocumentBuilder();
            Document doc = documentBuilder.parse(new InputSource(new InputStreamReader(new FileInputStream(topicsFilePath), "UTF-8")));
            NodeList topicElements = doc.getElementsByTagName(TOPIC_ELEMENT_NAME);
            for (int i = 0; i < topicElements.getLength(); i++) {
                Element topicElement = (Element) topicElements.item(i);
                Topic topic = new Topic();
                topic.setTopicId(topicElement.getElementsByTagName(TOPIC_ID_ELEMENT_NAME).item(0).getTextContent());
                NodeList formulaElements = topicElement.getElementsByTagName(FORMULA_ELEMENT_NAME);
                List<TopicTerm> formulaWords = new ArrayList<TopicTerm>();
                for (int j = 0; j < formulaElements.getLength(); j++) {
                    formulaWords.add(transformFormulaQueryWord((Element)formulaElements.item(j)));
                }
                topic.setFormulaKeyword(formulaWords);
                NodeList keywordElements = topicElement.getElementsByTagName(KEYWORD_ELEMENT_NAME);
                List<TopicTerm> keywords = new ArrayList<TopicTerm>();
                for (int j = 0; j < keywordElements.getLength(); j++) {
                    keywords.add(transformKeyword((Element)keywordElements.item(j)));
                }
                topic.setTextKeyword(keywords);
                result.add(topic);
            }
            Logger.getLogger(NTCIRTopicLoader.class.getName()).log(Level.INFO, "NTCIR Topics are loaded.");
        } catch (ParserConfigurationException ex) {
            Logger.getLogger(NTCIRTopicLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            Logger.getLogger(NTCIRTopicLoader.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(NTCIRTopicLoader.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }

    private TopicTerm transformFormulaQueryWord(Element element) {
        String formulaId = element.getParentNode().getAttributes().getNamedItem(ID_ATTR_NAME).getTextContent();
        transformElements(element);
        element = extractContentMathML(element);
        replaceQvars(element, ContentMML_tags);
        TopicTerm t = new TopicTerm();
        t.setTerm(removeNamespacePrefix(nodeToString(element)));
        t.setId(formulaId);
        return t;
    }

    private TopicTerm transformKeyword(Element element) {        
        TopicTerm t = new TopicTerm();
        String word = element.getTextContent();
        if (word.contains(" ")) {
            word = "\"" + word + "\"";
        }
        t.setTerm(word);
        t.setId(element.getAttributes().getNamedItem(ID_ATTR_NAME).getTextContent());
        return t;
    }
    
    private String nodeToString(Node node) {
        XMLUtil.cleanEmptyTextNodes(node);
        StringWriter sw = new StringWriter();
        try {
            Transformer t = TransformerFactory.newInstance().newTransformer();
            t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            t.setOutputProperty(OutputKeys.INDENT, "no");
            t.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "0");
            t.transform(new DOMSource(node), new StreamResult(sw));
        } catch (TransformerException te) {
            System.out.println("nodeToString Transformer Exception");
        }
        return sw.toString();
    }

    private void replaceQvars(Element element, Map<String, String> replacements) {
        NodeList qvars = element.getElementsByTagName(QVAR_ELEMENT_NAME);
        for (int i = 0; i < qvars.getLength(); i++) {
            Element qvar = (Element) qvars.item(i);
            String qwarValue = qvar.getAttribute("name");
            if (StringUtils.isAlpha(qwarValue)) {
                replaceDOMElement(qvar, replacements.get(REPLACEMENT_IDENTIFIER_NAME), qwarValue);
            } else {
                replaceDOMElement(qvar, replacements.get(REPLACEMENT_OPERATOR_NAME), qwarValue);
            }
            i--;
        }
    }

    private void replaceDOMElement(Element qvar, String elementName, String elementValue) {
        Element createElement = qvar.getOwnerDocument().createElement(elementName);
        createElement.setTextContent(elementValue);
        qvar.getParentNode().replaceChild(createElement, qvar);
    }

    private Element extractContentMathML(Element element) {
        Element mathElement = element.getOwnerDocument().createElement(FORMULA_ELEMENT_NAME);
        Element semantics = (Element) element.getElementsByTagName(SEMANTICS_ELEMENT_NAME).item(0);
        NodeList annotations = semantics.getElementsByTagName(ANNOTATION_ELEMENT_NAME);
        for (int i = 0; i < annotations.getLength(); i++) {
            semantics.removeChild(annotations.item(i));
        }
        annotations = semantics.getElementsByTagName(ANNOTATION_XML_ELEMENT_NAME);
        for (int i = 0; i < annotations.getLength(); i++) {
            semantics.removeChild(annotations.item(i));
        }
        NodeList cmmlElements = semantics.getChildNodes();
        for (int i = 0; i < cmmlElements.getLength(); i++) {
            mathElement.appendChild(cmmlElements.item(i).cloneNode(true));
        }
        return mathElement;
    }

    private void transformElements(Element element) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node item = children.item(i);
            if (item instanceof Element) {
                transformElements((Element) item);
            }
        }
        element.removeAttribute("xml:id");
        element.removeAttribute("xref");
    }

    private String removeNamespacePrefix(String elementString) {
        return elementString.replaceAll("<m:", "<").replaceAll("</m:", "</");
    }
    
}

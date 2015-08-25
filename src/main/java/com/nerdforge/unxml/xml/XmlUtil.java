package com.nerdforge.unxml.xml;

import com.google.common.base.Charsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;
import java.io.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;

@Singleton
public class XmlUtil {
    private static final Logger logger = LoggerFactory.getLogger("unXml");
    private final DocumentBuilderFactory factory;
    private XPathFactory xpathFactory = XPathFactory.newInstance();
    private NamespaceContext namespaceContext;

    @Inject
    public XmlUtil(DocumentBuilderFactory factory, NamespaceContext namespaceContext){
        this.factory = factory;
        this.namespaceContext = namespaceContext;
    }


    public Document document(File file){
        try {
            return factory.newDocumentBuilder().parse(file);
        } catch (SAXException | IOException | ParserConfigurationException  e) {
            throw new RuntimeException(e);
        }
    }

    public Document document(String input){
        try {
            return factory.newDocumentBuilder().parse(new ByteArrayInputStream(input.getBytes(Charsets.UTF_8)));
        } catch (SAXException | IOException | ParserConfigurationException  e) {
            throw new RuntimeException(e);
        }
    }

    public Optional<Node> parseNode(String xpath, Node node){
        return Optional.ofNullable((Node) evaluate(xpath, node, XPathConstants.NODE));
    }

    public List<Node> parseNodes(String xpath, Node node){
        return normalizeNodeList((NodeList) evaluate(xpath, node, XPathConstants.NODESET));
    }

    private Object evaluate(String path, Node node, QName returnType) {
        try {
            XPath xpath = xpathFactory.newXPath();
            xpath.setNamespaceContext(namespaceContext);
            logger.debug("Evaluating XML with: xpath=[{}], node=[{}], returnType=[{}]", path, node.getNodeName(), returnType.getLocalPart());
            return xpath.evaluate(path, node, returnType);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private List<Node> normalizeNodeList(NodeList nodeList){
        return IntStream.range(0, nodeList.getLength())
                .mapToObj(nodeList::item)
                .collect(toList());
    }
}
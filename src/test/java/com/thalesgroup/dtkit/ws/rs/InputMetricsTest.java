package com.thalesgroup.dtkit.ws.rs;

import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.filter.LoggingFilter;
import com.thalesgroup.dtkit.junit.CppTest;
import com.thalesgroup.dtkit.junit.CppUnit;
import com.thalesgroup.dtkit.metrics.api.InputMetric;
import com.thalesgroup.dtkit.metrics.api.InputMetricFactory;
import com.thalesgroup.dtkit.ws.rs.providers.InputMetricXMLProvider;
import com.thalesgroup.dtkit.ws.rs.resources.InputMetrics;
import com.thalesgroup.dtkit.ws.rs.vo.InputMetricsResult;
import com.thalesgroup.dtkit.ws.rs.vo.InputMetricResult;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.transform.stream.StreamSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;


public class InputMetricsTest extends InputMetricsAbstractTest {

    @Before
    public void loadWebResurce() {
        webResource = resource().path(InputMetrics.PATH);
        webResource.addFilter(new LoggingFilter());
    }

    @Test
    public void getInputMetricsXML() {
        ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), clientResponse.getStatus());
        InputMetricsResult inputMetricsResult = clientResponse.getEntity(InputMetricsResult.class);
        Assert.assertNotNull(inputMetricsResult);
        Assert.assertEquals(inputMetricsLocator.getAllMetrics().size(), inputMetricsResult.getMetrics().size());
    }

    @Test
    public void getInputMetricsJSON() throws IOException {
        ClientResponse clientResponse = webResource.accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), clientResponse.getStatus());
        JsonNode node = clientResponse.getEntity(JsonNode.class);
        Assert.assertNotNull(node);
        Assert.assertNotNull(node.isArray());
        int count = 0;
        Iterator<JsonNode> nodeIterator = node.getElements();
        while (nodeIterator.hasNext()) {
            ++count;
            nodeIterator.next();
        }
        Assert.assertEquals(inputMetricsLocator.getAllMetrics().size(), count);
    }

    public void getInputMetricCppunitXML() throws Exception {
        ClientResponse clientResponse = webResource.path("/cppunit").accept(MediaType.APPLICATION_XML_TYPE).get(ClientResponse.class);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), clientResponse.getStatus());
        InputStream xmlResult = clientResponse.getEntity(InputStream.class);
        Assert.assertNotNull(xmlResult);

        try {
            InputMetricXMLProvider inputMetricXMLProvider = new InputMetricXMLProvider();
            JAXBContext context = inputMetricXMLProvider.buildJAXBContext();
            Unmarshaller u = context.createUnmarshaller();
            SchemaFactory sf = SchemaFactory.newInstance(javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI);
            Schema schema = sf.newSchema(this.getClass().getResource("cppunit/cppunit-xml-result.xml.xsd"));
            u.setSchema(schema);
            InputMetric expectedCppUnit = InputMetricFactory.getInstance(CppUnit.class);
            InputMetricResult inputMetricResult= (InputMetricResult) (u.unmarshal(xmlResult));
            InputMetric actualCppUnit = inputMetricResult.getInputMetric(); 
            Assert.assertTrue(expectedCppUnit.equals(actualCppUnit));
            Assert.assertTrue(true);
        } catch (JAXBException e) {
            Assert.assertTrue(false);
        }
    }

    @Test
    public void getInputMetricCpptestJSON() throws Exception {
        ClientResponse clientResponse = webResource.path("/cpptest").accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), clientResponse.getStatus());
        String jsonResult = clientResponse.getEntity(String.class);
        Assert.assertNotNull(jsonResult);
        validateInputMetricJSONXMLSchema(jsonResult);
        InputMetric expectedCppTest = InputMetricFactory.getInstance(CppTest.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CppTest actualCppTest = objectMapper.readValue(jsonResult, CppTest.class);
        Assert.assertTrue(expectedCppTest.equals(actualCppTest));
    }


    @Test
    public void getInputMetricCppunitJSON() throws Exception {
        ClientResponse clientResponse = webResource.path("/cppunit").accept(MediaType.APPLICATION_JSON_TYPE).get(ClientResponse.class);
        Assert.assertEquals(Response.Status.OK.getStatusCode(), clientResponse.getStatus());
        String jsonResult = clientResponse.getEntity(String.class);
        Assert.assertNotNull(jsonResult);
        validateInputMetricJSONXMLSchema(jsonResult);
        InputMetric expectedCppunit = InputMetricFactory.getInstance(CppUnit.class);
        ObjectMapper objectMapper = new ObjectMapper();
        CppUnit actualCppunit = objectMapper.readValue(jsonResult, CppUnit.class);
        Assert.assertTrue(expectedCppunit.equals(actualCppunit));
    }

    @Test
    public void getExistXSD() {
        InputStream is = webResource.path("/cppunit/xsd").get(InputStream.class);
        Assert.assertNotNull(is);
    }

    @Test
    public void getXSDNotFound() {
        WebResource webResource = resource();
        ClientResponse clientResponse = webResource.path("/notExistMetric/xsd").get(ClientResponse.class);
        Assert.assertEquals(Response.Status.NOT_FOUND.getStatusCode(), clientResponse.getStatus());
    }
}

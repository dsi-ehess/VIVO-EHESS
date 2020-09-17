/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.semservices.service.impl;

import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.jena.query.*;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import edu.cornell.mannlib.semservices.bo.Concept;
import edu.cornell.mannlib.semservices.service.ExternalConceptService;
import edu.cornell.mannlib.semservices.util.SKOSUtils;
import edu.cornell.mannlib.semservices.util.XMLUtils;
import edu.cornell.mannlib.vitro.webapp.utils.json.JacksonUtils;
import edu.cornell.mannlib.vitro.webapp.web.URLEncoder;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.*;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.*;
import java.util.concurrent.TimeUnit;

//The definition of the new jawa class for the Faechersystematik as an external service
public abstract class EhessSkosmosService implements ExternalConceptService {
    protected final Log logger = LogFactory.getLog(getClass());
    protected final String dbpedia_endpoint = " http://dbpedia.org/sparql";
    // URL to get all the information for a concept
    // Exchange SKOSMOS_PATH with your local skosmos url.
    private String conceptSkosMosUrl;

    private final String schemeUri = "https://onto.tib.eu/destf/cs/";
    private final String format = "SKOS";
    private final String lang = "fr";
    private final String searchMode = "starts with";//Used to be Exact Match, or exact word or starts with

    //private final ConfigurationProperties configProps;


    public void setSkosmosUrl(String skosmosUrl) {
        this.conceptSkosMosUrl = skosmosUrl;
    }

    public String getSkosmosUrl() {
        return this.conceptSkosMosUrl;
    }

    @Override
    public List<Concept> getConcepts(String term) throws Exception {
        List<Concept> conceptList = new ArrayList<Concept>();

        //For the SKOSMos search mechanism, utilize this instead
        String result = getSKOSMosSearchResults(term, this.lang);
        List<String> conceptUris = getConceptURIsListFromSkosMosResult(result);

        if (conceptUris.size() == 0)
            return conceptList;
        int conceptCounter = 0;

        HashSet<String> encounteredURI = new HashSet<String>();

        // Loop through each of these URIs and load using the SKOSManager
        for (String conceptUri : conceptUris) {
            conceptCounter++;
            if (StringUtils.isEmpty(conceptUri)) {
                // If the conceptURI is empty, keep going
                continue;
            }
            if (encounteredURI.contains(conceptUri)) {
                //If we have already encountered this concept URI, do not redisplay or reprocess
                continue;
            }
            encounteredURI.add(conceptUri);
            // Test and see if the URI is valid
            URI uri = null;
            try {
                uri = new URI(conceptUri);
            } catch (URISyntaxException e) {
                logger.error("Error occurred with creating the URI ", e);
                continue;
            }
            // Returns concept information in the format specified, which is
            // currently XML
            // Utilizing Agrovoc's getConceptInfo returns alternate and
            // preferred labels but
            // none of the exact match or close match descriptions
            String bestMatch = "false";
            //Assume the first result is considered the 'best match'
            //Although that is not something we are actually retrieving from the service itself explicitly
            if (conceptCounter == 1) {
                bestMatch = "true";
            }

            Concept c = this.createConcept(bestMatch, conceptUri);
            c.setDefinition("No definition found");
            c.setLabel(getConceptPrefLabelFromSkosMosResult(result, conceptUri));
            // c.setNotation(getConceptNotationFromSkosMosResult(result));
            // c.setLabel(prefLabel);
            conceptList.add(c);
        }

        return conceptList;
    }

    public List<Concept> processResults(String term) throws Exception {
        return getConcepts(term);
    }

    public abstract String getOntologyName();

    public String getConceptSkosMosURL() {

        return getSkosmosUrl() + "/rest/v1/" + getOntologyName() + "/data?";
    }

    public Concept createConcept(String bestMatch, String skosConceptURI) {
        Concept concept = new Concept();
        concept.setUri(skosConceptURI);
        concept.setConceptId(stripConceptId(skosConceptURI));
        concept.setBestMatch(bestMatch);
        concept.setDefinedBy(schemeUri);
        concept.setSchemeURI(this.schemeUri);
        concept.setType("");

        concept.setUri(skosConceptURI);
        concept.setConceptId(stripConceptId(skosConceptURI));
        concept.setBestMatch(bestMatch);
        concept.setDefinedBy(schemeUri);
        concept.setSchemeURI(this.schemeUri);
        concept.setType("");
        concept.setLabel("test");
        String encodedURI = URLEncoder.encode(skosConceptURI);
        String encodedFormat = URLEncoder.encode("application/rdf+xml");
        String url = getConceptSkosMosURL() + "uri=" + encodedURI + "&format="
                + encodedFormat;
// Utilize the XML directly instead of the SKOS API
        try {
            concept = SKOSUtils
                    .createConceptUsingXMLFromURL(concept, url, "fr", false);
        } catch (Exception ex) {
            logger.debug("Error occurred for creating concept "
                    + skosConceptURI, ex);
            return null;
        }

        return concept;
    }

    public List<Concept> getConceptsByURIWithSparql(String uri)
            throws Exception {
// deprecating this method...just return an empty list
        List<Concept> conceptList = new ArrayList<Concept>();
        return conceptList;
    }


    /**
     * @param uri The URI
     */
    protected String stripConceptId(String uri) {
        String conceptId = new String();
        int lastslash = uri.lastIndexOf('/');
        conceptId = uri.substring(lastslash + 1, uri.length());
        return conceptId;
    }

    /**
     * @param str The String
     */
    protected String extractConceptId(String str) {
        try {
            return str.substring(1, str.length() - 1);
        } catch (Exception ex) {
            return "";
        }
    }

    /**
     * The code here utilizes the SKOSMOS REST API for Agrovoc
     * This returns JSON LD so we would parse JSON instead of RDF
     * The code above can still be utilized if we need to employ the web services directly
     */
//Get search results for a particular term and language code
    private String getSKOSMosSearchResults(String term, String lang) {
        String urlEncodedTerm = URLEncoder.encode(term);
//Utilize 'starts with' using the * operator at the end
        String searchUrlString = getSkosmosUrl() + "/rest/v1/search?query=" + urlEncodedTerm + "*" + "&vocab=" + getOntologyName() + "&lang=" + lang;
        URL searchURL = null;
        try {
            searchURL = new URL(searchUrlString);
        } catch (Exception e) {
            logger.error("Exception occurred in instantiating URL for "
                    + searchUrlString, e);
// If the url is having trouble, just return null for the concept
            return null;
        }


        String results = null;
        try {
            StringWriter sw = new StringWriter();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    searchURL.openStream()));
            String inputLine;
            while ((inputLine = in.readLine()) != null) {
                sw.write(inputLine);
            }
            in.close();
            results = sw.toString();
            logger.debug(results);
        } catch (Exception ex) {
            logger.error("Error occurred in getting concept from the URL "
                    + searchUrlString, ex);
            return null;
        }
        return results;

    }

    //JSON-LD array
    private List<String> getConceptURIsListFromSkosMosResult(String results) {
        List<String> conceptURIs = new ArrayList<String>();
        ObjectNode json = (ObjectNode) JacksonUtils.parseJson(results);
//Format should be: { ..."results":["uri":uri...]
        if (json.has("results")) {
            ArrayNode jsonArray = (ArrayNode) json.get("results");
            int numberResults = jsonArray.size();
            int i;
            for (i = 0; i < numberResults; i++) {
                ObjectNode jsonObject = (ObjectNode) jsonArray.get(i);
                if (jsonObject.has("uri")) {
                    conceptURIs.add(jsonObject.get("uri").asText());
                }
            }
        }
        return conceptURIs;
    }

    //Utilize this for getting preffered label of the concept
//JSON-LD array
    private String getConceptPrefLabelFromSkosMosResult(String results, String conceptUri) {
        String getPrefLabel = "";

        ObjectNode json = (ObjectNode) JacksonUtils.parseJson(results);
//Format should be: { ..."results":["uri":uri...]
        if (json.has("results")) {
            ArrayNode jsonArray = (ArrayNode) json.get("results");
            int numberResults = jsonArray.size();
            int i;
            for (i = 0; i < numberResults; i++) {
                ObjectNode jsonObject = (ObjectNode) jsonArray.get(i);

                if (jsonObject.has("prefLabel") && conceptUri.equals(jsonObject.get("uri").asText())) {
                    getPrefLabel = jsonObject.get("prefLabel").asText();
                }

            }
        }
        return getPrefLabel;
    }

    //Utilize this for getting the notation of the concept, if given
    private String getConceptNotationFromSkosMosResult(String results) {
        String getNotation = "";

        ObjectNode json = (ObjectNode) JacksonUtils.parseJson(results);
//Format should be: { ..."results":["uri":uri...]
        if (json.has("results")) {
            ArrayNode jsonArray = (ArrayNode) json.get("results");
            int numberResults = jsonArray.size();
            int i;
            for (i = 0; i < numberResults; i++) {
                ObjectNode jsonObject = (ObjectNode) jsonArray.get(i);

                if (jsonObject.has("notation")) {

                    System.out.println(jsonObject.get("notation").asText());
                    getNotation = jsonObject.get("notation").asText();
                }

            }
        }
        return getNotation;
    }


}

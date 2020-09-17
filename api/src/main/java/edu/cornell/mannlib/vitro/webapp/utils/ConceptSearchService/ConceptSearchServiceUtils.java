/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.vitro.webapp.utils.ConceptSearchService;

import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletContext;

import edu.cornell.mannlib.semservices.service.impl.EhessSkosmosService;
import edu.cornell.mannlib.vitro.webapp.config.ConfigurationProperties;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.VTwo.EditConfigurationUtils;
import edu.cornell.mannlib.vitro.webapp.edit.n3editing.configuration.generators.VivoBaseGenerator;
import edu.cornell.mannlib.vitro.webapp.i18n.I18n;
import edu.cornell.mannlib.vitro.webapp.i18n.I18nBundle;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import edu.cornell.mannlib.semservices.bo.Concept;
import edu.cornell.mannlib.semservices.service.ExternalConceptService;
import edu.cornell.mannlib.vitro.webapp.controller.VitroRequest;

/**
 * Utilities for search
 */
public class ConceptSearchServiceUtils {
    private static final Log log = LogFactory.getLog(ConceptSearchServiceUtils.class);

    final static String vivoCore ="http://vivoweb.org/ontology/core#" ;
    //Get the appropriate search service class
    //TODO: Change this so retrieved from the system instead using a query



    private static final String UMLSVocabSource = "http://link.informatics.stonybrook.edu/umls";
    private static final String AgrovocVocabSource = "http://aims.fao.org/aos/agrovoc/agrovocScheme";
    private static final String GemetVocabSource = "http://www.eionet.europa.eu/gemet/gemetThesaurus";
    private static final String LCSHVocabSource = "http://id.loc.gov/authorities/subjects";
    //We define the new vocabulary source here. Exchange SKOSMOS_PATH with your local skosmos url.
    private static final String EhessSkosmosDomainVocabSource = "domehess/data";
    private static final String EhessSkosmosCulturalAreaVocabSource = "airecult/data";
    private static final String EhessSkosmosKeywordVocabSource = "motehess/data";

    private static final String HAS_RESEARCH_AREA_CLASS = vivoCore + "hasResearchArea";
    private static final String GEOGRAPHIC_FOCUS_CLASS = vivoCore + "geographicFocus";
    private static final String HAS_SUBJECT_AREA_CLASS = vivoCore + "hasSubjectArea";

    private static final String SKOSMOS_URL = "skosmos.url";

    //Get the class that corresponds to the appropriate search
    public static String getConceptSearchServiceClassName(String searchServiceName, String skmosmosUrl) {
        HashMap<String, String> map = getMapping(skmosmosUrl);
        if (map.containsKey(searchServiceName)) {
            return map.get(searchServiceName);
        }
        return null;
    }

    //Get the URLS for the different services
    //URL to label
    public static HashMap<String, VocabSourceDescription> getVocabSources(VitroRequest vreq) {
        I18nBundle i18n = I18n.bundle(vreq);
        String ehessVocabularyText = i18n.text("ehess_vocabulary");

        HashMap<String, VocabSourceDescription> map = new HashMap<String, VocabSourceDescription>();
        //map.put(UMLSVocabSource, new VocabSourceDescription("UMLS", UMLSVocabSource, "http://www.nlm.nih.gov/research/umls/", "Unified Medical Language System"));

        //Commenting out agrovoc for now until implementation is updated
        //map.put(AgrovocVocabSource, new VocabSourceDescription("AGROVOC", AgrovocVocabSource, "http://www.fao.org/agrovoc/", "Agricultural Vocabulary"));
        //Define the abbreviation, the URL and the full title of the new source here


        String predicateUri = EditConfigurationUtils.getPredicateUri(vreq);
        String ehesskosmosVocabSource = null;


        String skmosmosUrl = getSkosmosUrl(vreq);


        if(HAS_RESEARCH_AREA_CLASS.equals(predicateUri)) {
            ehesskosmosVocabSource = skmosmosUrl + "/" + EhessSkosmosDomainVocabSource;
            ehessVocabularyText = i18n.text("ehess_domains");
        } else if (GEOGRAPHIC_FOCUS_CLASS.equals(predicateUri)) {
            ehesskosmosVocabSource = skmosmosUrl + "/" + EhessSkosmosCulturalAreaVocabSource;
            ehessVocabularyText = i18n.text("ehess_cultural_areas");
        } else if (HAS_SUBJECT_AREA_CLASS.equals(predicateUri)) {
            ehesskosmosVocabSource = skmosmosUrl + "/" + EhessSkosmosKeywordVocabSource;
            ehessVocabularyText = i18n.text("ehess_keywords");
        }

        map.put(ehesskosmosVocabSource, new VocabSourceDescription(ehessVocabularyText, ehesskosmosVocabSource, ehesskosmosVocabSource, ehessVocabularyText));
        //map.put(GemetVocabSource, new VocabSourceDescription("GEMET", GemetVocabSource, "http://www.eionet.europa.eu/gemet", "General Multilingual Environmental Thesaurus"));
        //map.put(LCSHVocabSource, new VocabSourceDescription("LCSH", LCSHVocabSource, "http://id.loc.gov/authorities/subjects/", "Library of Congress Subject Headings"));


        return map;
    }

    private static String getSkosmosUrl(VitroRequest vreq) {
        ConfigurationProperties configuration = ConfigurationProperties.getBean(vreq.getSession()
                .getServletContext());

        String skmosmosUrl = configuration.getProperty(SKOSMOS_URL);
        if (skmosmosUrl == null) {
            throw new RuntimeException(SKOSMOS_URL + " not defined in properties config file");
        }
        return skmosmosUrl;
    }

    //Get additional vocab source info


    //Get the hashmap mapping service name to Service class
    private static HashMap<String, String> getMapping(String skmosmosUrl) {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put(UMLSVocabSource, "edu.cornell.mannlib.semservices.service.impl.UMLSService");
        map.put(AgrovocVocabSource, "edu.cornell.mannlib.semservices.service.impl.AgrovocService");
//We assign the new java class EhessSkosmosService to the new vocabulary source
        map.put(skmosmosUrl + "/" + EhessSkosmosDomainVocabSource, "edu.cornell.mannlib.semservices.service.impl.EhessSkosmosDomainService");
        map.put(skmosmosUrl + "/" + EhessSkosmosCulturalAreaVocabSource, "edu.cornell.mannlib.semservices.service.impl.EhessSkosmosCulturalAreaService");
        map.put(skmosmosUrl + "/" + EhessSkosmosKeywordVocabSource, "edu.cornell.mannlib.semservices.service.impl.EhessSkosmosKeywordService");
        map.put(GemetVocabSource, "edu.cornell.mannlib.semservices.service.impl.GemetService");
        map.put(LCSHVocabSource, "edu.cornell.mannlib.semservices.service.impl.LCSHService");
        return map;
    }

    public static List<Concept> getSearchResults(ServletContext context, VitroRequest vreq) throws Exception {
        String searchServiceName = getSearchServiceUri(vreq);
        System.out.println(searchServiceName);
        String skmosmosUrl = getSkosmosUrl(vreq);
        String searchServiceClassName = getConceptSearchServiceClassName(searchServiceName, skmosmosUrl);
        System.out.println(searchServiceClassName);

        ExternalConceptService conceptServiceClass = null;

        Object object = null;
        try {
            Class classDefinition = Class.forName(searchServiceClassName);

            Class superClass = classDefinition.getSuperclass();
            object = classDefinition.newInstance();
            if (object instanceof EhessSkosmosService) {
                conceptServiceClass = (EhessSkosmosService) object;
                ((EhessSkosmosService) conceptServiceClass).setSkosmosUrl(skmosmosUrl);
            } else {
                conceptServiceClass = (ExternalConceptService) object;
            }
            conceptServiceClass = (ExternalConceptService) object;
        } catch (InstantiationException | ClassNotFoundException | IllegalAccessException e) {
	        System.out.println(e);
	}

        if (conceptServiceClass == null) {
            log.error("could not find Concept Search Class for " + searchServiceName);
            System.out.println("getSearchResults : searchServiceName | " + searchServiceName);
            return null;

        }

        //Get search
        String searchTerm = getSearchTerm(vreq);
        System.out.println("getSearchResults : getSearchResults | " + searchTerm);
        log.error(searchTerm);

        List<Concept> conceptResults = conceptServiceClass.getConcepts(searchTerm);


        return conceptResults;
    }

    private static String getSearchServiceUri(VitroRequest vreq) {
        System.out.println(vreq);

        return vreq.getParameter("source");

    }

    private static String getSearchTerm(VitroRequest vreq) {
        return vreq.getParameter("searchTerm");
    }
}


/* $This file is distributed under the terms of the license in LICENSE$ */
package edu.cornell.mannlib.semservices.service.impl;

//The definition of the new jawa class for the Faechersystematik as an external service
public class EhessSkosmosKeywordService extends EhessSkosmosService {
    private final String ontologyName = "motehess";

    @Override
    public String getOntologyName() {
        return ontologyName;
    }

}

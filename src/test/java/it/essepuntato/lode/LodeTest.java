package it.essepuntato.lode;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class LodeTest {
	
	@Test public void testFOAF() throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException {
		Lode.main("http://xmlns.com/foaf/spec/index.rdf");
	}

	@Test public void testOWL() throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException {
		Lode.main("http://www.w3.org/2002/07/owl");
	}
	
}

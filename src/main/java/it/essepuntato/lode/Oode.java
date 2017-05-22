/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *      
 * Copyright (c) 2010-2013, Silvio Peroni <essepuntato@gmail.com>
 * 
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */
package it.essepuntato.lode;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;


import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class Oode {
	private String xsltURL = "docs/xslt/extraction.xsl";
	private String cssLocation = "https://mgskjaeveland.github.io/OODE/css/";
	
	public static void main (String... args) throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException {
		Oode lode = new Oode ();
		if (args.length == 1) {
			System.out.println(lode.go(args[0], args[0]));
		}
		else if (args.length == 2) {
			System.out.println(lode.go(args[0], args[1]));
		} 
		else {
			System.out.println("Error, needs 1 or 2 arguments: 1. location of ontology and optionally 2: Ontology IRI, if different from 1. arg.");
		}
	}

	protected String go (String ontologyPath, String ontologyIRI) throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException {

		// Settings
		String lang = "en";
		boolean considerImportedOntologies = true;
		boolean considerImportedClosure = true;
		boolean useReasoner = true;

		String content = "";
		content = parseOntology(ontologyPath, considerImportedOntologies, considerImportedClosure, useReasoner);
		content = applyXSLTTransformation(content, ontologyIRI, lang);

		return content;
	}

	private String parseOntology (String ontologyURL, boolean considerImportedOntologies, boolean considerImportedClosure, boolean useReasoner) throws OWLOntologyCreationException, OWLOntologyStorageException  {
		String result = "";

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();

		OWLOntology ontology = null;

		if (considerImportedClosure || considerImportedOntologies) {
			ontology = manager.loadOntology(IRI.create(ontologyURL));
			Set<OWLOntology> setOfImportedOntologies = new HashSet<OWLOntology>();
			if (considerImportedOntologies) {
				setOfImportedOntologies.addAll(ontology.getDirectImports());
			} else {
				setOfImportedOntologies.addAll(ontology.getImportsClosure());
			}
			for (OWLOntology importedOntology : setOfImportedOntologies) {
				manager.addAxioms(ontology, importedOntology.getAxioms());
			}
		} else {
			//manager.setSilentMissingImportsHandling(true);
			ontology = manager.loadOntology(IRI.create(ontologyURL));
		}

		StringDocumentTarget parsedOntology = new StringDocumentTarget();

		manager.saveOntology(ontology, new RDFXMLDocumentFormat(), parsedOntology);
		result = parsedOntology.toString();

		return result;
	}

	private String applyXSLTTransformation(String source, String ontologyUrl, String lang) throws TransformerException {
		TransformerFactory tfactory = new net.sf.saxon.TransformerFactoryImpl();

		Transformer transformer = tfactory.newTransformer(new StreamSource(xsltURL));

		transformer.setParameter("css-location", cssLocation);
		transformer.setParameter("lang", lang);
		transformer.setParameter("ontology-url", ontologyUrl);
		transformer.setParameter("source", cssLocation + "source");

		StreamSource inputSource = new StreamSource(new StringReader(source));

		ByteArrayOutputStream output = new ByteArrayOutputStream();
		transformer.transform(inputSource, new StreamResult(output));
		return output.toString();
	}
}

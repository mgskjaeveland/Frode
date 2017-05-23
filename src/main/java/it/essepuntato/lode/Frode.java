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

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.formats.RDFXMLDocumentFormat;
import org.semanticweb.owlapi.io.StringDocumentTarget;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

public class Frode {
	private String xsltURL = "docs/xslt/extraction.xsl";
	private String cssLocation = "https://mgskjaeveland.github.io/Frode/css/";

	private static final boolean CONSTconsiderImportedOntologies = false;
	private static final boolean CONSTconsiderImportedClosure = false;

	public static void main (String... args) throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException {
		Frode.cli(args);
	}

	public static void cli (String... args) throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException {
		CommandLineParser parser = new DefaultParser();

		final String arg1 = "o", arg2 = "i", arg3 = "1", arg4 = "a";

		Options options = new Options();
		options.addOption(Option.builder(arg1).hasArg(true).required().desc("The path or IRI to ontology, if local use 'file:///'. Required").build());
		options.addOption(Option.builder(arg2).hasArg(true).optionalArg(true).desc("The IRI to ontology, if different from the path argument. Optional").build());
		options.addOption(Option.builder(arg3).hasArg(false).optionalArg(true).desc("Enable direct ontology imports").build());
		options.addOption(Option.builder(arg4).hasArg(false).optionalArg(true).desc("Enable ontology import closure").build());

		try {
			CommandLine line = parser.parse(options, args);

			String path = line.getOptionValue(arg1);
			String iri = line.getOptionValue(arg2, path);
			boolean imports = line.hasOption(arg3);
			boolean closure = line.hasOption(arg4);


			Frode dave = new Frode();
			String result = dave.go(path, iri, imports, closure);
			System.out.println(result);
		}
		catch( ParseException exp ) {
			System.out.println( "Unexpected exception: " + exp.getMessage() );
			HelpFormatter formatter = new HelpFormatter();
			formatter.printHelp( "Frode", options );
		}
	}

	protected String go (String ontologyPath, String ontologyIRI, boolean considerImportedOntologies, boolean considerImportedClosure) throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException {
		String lang = "en";
		String content = "";
		content = parseOntology(ontologyPath, considerImportedOntologies, considerImportedClosure);
		content = applyXSLTTransformation(content, ontologyIRI, lang);
		return content;
	}
	protected String go (String ontologyPath, String ontologyIRI) throws OWLOntologyCreationException, OWLOntologyStorageException, TransformerException {
		return go (ontologyPath, ontologyIRI, CONSTconsiderImportedOntologies, CONSTconsiderImportedClosure);
	}

	private String parseOntology (String ontologyURL, boolean considerImportedOntologies, boolean considerImportedClosure) throws OWLOntologyCreationException, OWLOntologyStorageException  {

		OWLOntologyManager manager = OWLManager.createOWLOntologyManager();
		OWLOntology ontology = manager.loadOntology(IRI.create(ontologyURL));

		if (considerImportedOntologies || considerImportedClosure) {
			Set<OWLOntology> setOfImportedOntologies = new HashSet<OWLOntology>();
			if (considerImportedOntologies) {
				setOfImportedOntologies.addAll(ontology.getDirectImports());
			} else {
				setOfImportedOntologies.addAll(ontology.getImportsClosure());
			}
			for (OWLOntology importedOntology : setOfImportedOntologies) {
				manager.addAxioms(ontology, importedOntology.getAxioms());
			}
		}

		StringDocumentTarget parsedOntology = new StringDocumentTarget();
		manager.saveOntology(ontology, new RDFXMLDocumentFormat(), parsedOntology);
		return parsedOntology.toString();
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

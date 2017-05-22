
Converted orginal project from servlet to regular java application to support simpler offline generation of documentation. 

Other changes:
 - updated to new version of OWL API
 - removed support for reasoning (partially due to problems when updating OWL API version)
 - using cdnjs and already published css' whenever possible
 - moving remaining essepuntato files to github pages, due to poor uptime
 
 Use
     Oode.main("http://xmlns.com/foaf/spec/index.rdf");
 to generate documentation for the ontology located at the given IRI, or
     Oode.main("file:///localdisk/folder/file.owl", "http://www.iri.to/ontology.owl")
 to generate for a locally placed ontology, but with a different ontology IRI. Be careful to test that links from the published documentation work, e.g., images.
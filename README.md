
Converted orginal project from servlet to regular java application to support simpler offline generation of documentation. 

Other changes:
 - updated to new version of OWL API
 - removed support for reasoning (partially due to problems when updating OWL API version)
 - using cdnjs and already published css' whenever possible
 - moving remaining essepuntato files to github pages, due to poor uptime
 
 
 Run `mvn clean compile assembly:simple` to produce `target/Oode.jar`. Run with `java -jar Oode.jar`
 
 
 Command line interface:
 
 ```
 usage: Oode
 -1         Enable direct ontology imports
 -a         Enable ontology import closure
 -i <arg>   The IRI to ontology, if different from the path argument.
            Optional
 -o <arg>   The path or IRI to ontology, if local use 'file:///'. Required
 ```
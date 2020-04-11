# xsl-transformations
A collection of XSLT files for all sorts of purposes.

Each transform has been tested with [Saxon-HE for Java](http://www.saxonica.com/download/java.xml). You can run Saxon directly from the command line like this:

`java -jar Saxon-HE-10.0.jar -xsl:transform:.xsl -s:source.xml -o:output.xml`

Any other XSLT processor supporting XSLT 3.0 should work as well. If you want to test them online, you can use [XSLT Fiddle](https://xsltfiddle.liberty-development.net).

### KML

### Sort Placemarks Alphabetically

[kml_sort_placemarks_alphabetically.xsl](src/main/resources/xml/kml/kml_sort_placemarks_alphabetically.xsl) sorts the `Placemark` elements inside of a KML document in ascending alphabetical order. The `Folder` elements themselves are not reordered, only the `Placemark` elements inside each `Folder` element.

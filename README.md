# xsl-transformations
A collection of XSLT files for all sorts of purposes.

### KML

### Sort Placemarks Alphabetically

[This](src/main/resources/xml/kml/kml_sort_placemarks_alphabetically.xsl) XSLT sorts the `Placemark` elements inside of a KML document in ascending alphabetical order. The `Folder` elements themselves are not reordered, only the `Placemark` elements inside each `Folder` element.

# Source

XSLT style sheets and a Java test harness that applies them.

- `main/resources/` — the style sheets:
  - `json/json_to_xml.xsl`, `xml/xml_to_json.xsl` — JSON↔XML conversions.
  - `xml/kml/kml_sort_placemarks_alphabetically.xsl` — sorts KML placemarks.
- `test/java/org/leplus/xslt/TestXSLT.java` + `test/resources/` —
  input/expected-output fixtures the tests transform and compare.

Build/test with `./mvnw test`.

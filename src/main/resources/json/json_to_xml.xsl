<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:kml="http://www.opengis.net/kml/2.2">
  
  <xsl:output method="xml" indent="yes"/>
  
  <xsl:template match="data">
    <xsl:copy-of select="json-to-xml(.)"/>
  </xsl:template>
  
</xsl:stylesheet>

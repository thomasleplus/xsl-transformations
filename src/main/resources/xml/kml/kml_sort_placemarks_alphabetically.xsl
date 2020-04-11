<xsl:stylesheet version="3.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:kml="http://www.opengis.net/kml/2.2">

  <xsl:output name="xml" indent="yes"/>

  <xsl:mode on-no-match="shallow-copy" />

  <xsl:template match="kml:Folder">
    <xsl:copy>
      <xsl:copy-of select="* except kml:Placemark" />
      <xsl:perform-sort select="kml:Placemark">
        <xsl:sort select="kml:name" data-type="text" order="ascending"/>
      </xsl:perform-sort>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>

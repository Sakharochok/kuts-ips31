<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="1.0"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:d="http://example.com/dance"
                exclude-result-prefixes="d">

    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>
    <xsl:key name="dancesByType" match="d:Dance" use="@type"/>

    <xsl:template match="/d:Dances">
        <root>
            <xsl:for-each select="d:Dance[generate-id() = generate-id(key('dancesByType', @type)[1])]">
                <xsl:variable name="groupType" select="@type"/>
                <xsl:element name="{$groupType}">
                    <xsl:for-each select="key('dancesByType', $groupType)">
                        <Dance id="{@id}" type="{@type}" title="{@title}">
                            <scene><xsl:value-of select="d:scene"/></scene>
                            <numberOfDancers><xsl:value-of select="d:numberOfDancers"/></numberOfDancers>
                            <music><xsl:value-of select="d:music"/></music>
                            <dancers>
                                <xsl:for-each select="d:dancers/d:dancer">
                                    <dancer>
                                        <name><xsl:value-of select="d:name"/></name>
                                        <age><xsl:value-of select="d:age"/></age>
                                        <experienceYears><xsl:value-of select="d:experienceYears"/></experienceYears>
                                    </dancer>
                                </xsl:for-each>
                            </dancers>
                            <number><xsl:value-of select="d:number"/></number>
                        </Dance>
                    </xsl:for-each>
                </xsl:element>
            </xsl:for-each>
        </root>
    </xsl:template>
</xsl:stylesheet>

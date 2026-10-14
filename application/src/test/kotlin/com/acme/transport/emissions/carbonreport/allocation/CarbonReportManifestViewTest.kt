package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportManifest("code-54", "title-595")
        val view = toCarbonReportManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportManifestViews(mutableListOf<CarbonReportManifest>(item, item)).size, "views")
    }
}

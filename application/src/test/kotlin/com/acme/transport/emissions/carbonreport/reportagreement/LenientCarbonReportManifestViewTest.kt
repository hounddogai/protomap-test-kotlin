package com.acme.transport.emissions.carbonreport.reportagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientCarbonReportManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientCarbonReportManifest("code-794", "label-764")
        val view = toLenientCarbonReportManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientCarbonReportManifestViews(mutableListOf<LenientCarbonReportManifest>(item, item)).size, "views")
    }
}

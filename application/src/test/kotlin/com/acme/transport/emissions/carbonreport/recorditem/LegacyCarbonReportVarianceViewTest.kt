package com.acme.transport.emissions.carbonreport.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCarbonReportVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCarbonReportVariance("code-69", "name-262")
        val view = toLegacyCarbonReportVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCarbonReportVarianceViews(mutableListOf<LegacyCarbonReportVariance>(item, item)).size, "views")
    }
}

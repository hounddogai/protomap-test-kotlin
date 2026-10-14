package com.acme.transport.emissions.carbonreport.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyCarbonReportMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyCarbonReportMapping("code-871", "title-497")
        val view = toLegacyCarbonReportMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyCarbonReportMappingViews(mutableListOf<LegacyCarbonReportMapping>(item, item)).size, "views")
    }
}

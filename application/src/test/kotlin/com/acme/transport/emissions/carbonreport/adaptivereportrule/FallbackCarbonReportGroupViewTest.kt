package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCarbonReportGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCarbonReportGroup("code-6", "title-784")
        val view = toFallbackCarbonReportGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCarbonReportGroupViews(mutableListOf<FallbackCarbonReportGroup>(item, item)).size, "views")
    }
}

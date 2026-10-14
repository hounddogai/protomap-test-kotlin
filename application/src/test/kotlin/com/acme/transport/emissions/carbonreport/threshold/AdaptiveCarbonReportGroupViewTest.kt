package com.acme.transport.emissions.carbonreport.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCarbonReportGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCarbonReportGroup("code-491", "title-30")
        val view = toAdaptiveCarbonReportGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCarbonReportGroupViews(mutableListOf<AdaptiveCarbonReportGroup>(item, item)).size, "views")
    }
}

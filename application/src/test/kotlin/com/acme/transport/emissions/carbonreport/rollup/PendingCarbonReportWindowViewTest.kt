package com.acme.transport.emissions.carbonreport.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCarbonReportWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCarbonReportWindow("code-525", "name-18")
        val view = toPendingCarbonReportWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCarbonReportWindowViews(mutableListOf<PendingCarbonReportWindow>(item, item)).size, "views")
    }
}

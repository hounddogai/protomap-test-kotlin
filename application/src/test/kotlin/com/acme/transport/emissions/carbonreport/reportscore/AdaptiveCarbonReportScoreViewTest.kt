package com.acme.transport.emissions.carbonreport.reportscore

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCarbonReportScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCarbonReportScore("code-427", "name-100")
        val view = toAdaptiveCarbonReportScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCarbonReportScoreViews(mutableListOf<AdaptiveCarbonReportScore>(item, item)).size, "views")
    }
}

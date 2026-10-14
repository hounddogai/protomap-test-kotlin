package com.acme.transport.emissions.carbonreport.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCarbonReportPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCarbonReportPreference("code-683", "name-275")
        val view = toFallbackCarbonReportPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCarbonReportPreferenceViews(mutableListOf<FallbackCarbonReportPreference>(item, item)).size, "views")
    }
}

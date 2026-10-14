package com.acme.transport.emissions.carbonreport.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportPreference("code-924", "label-735")
        val view = toCarbonReportPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportPreferenceViews(mutableListOf<CarbonReportPreference>(item, item)).size, "views")
    }
}

package com.acme.transport.emissions.carbonreport.reportestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportMetric("code-1", "label-244")
        val view = toCarbonReportMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportMetricViews(mutableListOf<CarbonReportMetric>(item, item)).size, "views")
    }
}

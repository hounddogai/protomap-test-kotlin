package com.acme.transport.emissions.carbonreport.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportRecord("code-193", "label-983")
        val view = toCarbonReportRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportRecordViews(mutableListOf<CarbonReportRecord>(item, item)).size, "views")
    }
}

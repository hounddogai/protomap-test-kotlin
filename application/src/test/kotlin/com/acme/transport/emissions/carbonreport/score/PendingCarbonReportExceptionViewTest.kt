package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals

class PendingCarbonReportExceptionViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPendingCarbonReportException("code-7", "title-890")
        val view = toPendingCarbonReportExceptionView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPendingCarbonReportExceptionViews(mutableListOf<PendingCarbonReportException>(item, item)).size, "views")
    }
}

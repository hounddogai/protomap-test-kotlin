package com.acme.transport.emissions.carbonreport.adaptivereportrule

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCarbonReportBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCarbonReportBatch("code-321", "title-73")
        val view = toStrictCarbonReportBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCarbonReportBatchViews(mutableListOf<StrictCarbonReportBatch>(item, item)).size, "views")
    }
}

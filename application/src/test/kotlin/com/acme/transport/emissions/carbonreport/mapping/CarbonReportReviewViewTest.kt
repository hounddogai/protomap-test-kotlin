package com.acme.transport.emissions.carbonreport.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportReview("code-58", "label-877")
        val view = toCarbonReportReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportReviewViews(mutableListOf<CarbonReportReview>(item, item)).size, "views")
    }
}

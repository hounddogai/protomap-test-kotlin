package com.acme.transport.emissions.carbonreport.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportClaim("code-958", "label-101")
        val view = toCarbonReportClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportClaimViews(mutableListOf<CarbonReportClaim>(item, item)).size, "views")
    }
}

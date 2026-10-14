package com.acme.transport.emissions.carbonreport.reportagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCarbonReportAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCarbonReportAgreement("code-808", "title-764")
        val view = toFallbackCarbonReportAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCarbonReportAgreementViews(mutableListOf<FallbackCarbonReportAgreement>(item, item)).size, "views")
    }
}

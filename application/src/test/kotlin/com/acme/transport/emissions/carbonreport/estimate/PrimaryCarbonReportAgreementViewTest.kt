package com.acme.transport.emissions.carbonreport.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCarbonReportAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCarbonReportAgreement("code-404", "label-662")
        val view = toPrimaryCarbonReportAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCarbonReportAgreementViews(mutableListOf<PrimaryCarbonReportAgreement>(item, item)).size, "views")
    }
}

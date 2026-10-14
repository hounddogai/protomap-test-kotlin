package com.acme.transport.emissions.carbonreport.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalCarbonReportAgreementViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalCarbonReportAgreement("code-325", "name-370")
        val view = toSeasonalCarbonReportAgreementView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalCarbonReportAgreementViews(mutableListOf<SeasonalCarbonReportAgreement>(item, item)).size, "views")
    }
}

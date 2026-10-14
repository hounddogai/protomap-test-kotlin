package com.acme.transport.emissions.carbonreport.reportrule

import kotlin.test.Test
import kotlin.test.assertEquals

class CarbonReportProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCarbonReportProposal("code-237", "title-389")
        val view = toCarbonReportProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCarbonReportProposalViews(mutableListOf<CarbonReportProposal>(item, item)).size, "views")
    }
}

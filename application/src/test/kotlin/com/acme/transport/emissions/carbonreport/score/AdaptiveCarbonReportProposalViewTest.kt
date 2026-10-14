package com.acme.transport.emissions.carbonreport.score

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCarbonReportProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCarbonReportProposal("code-100", "title-567")
        val view = toAdaptiveCarbonReportProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCarbonReportProposalViews(mutableListOf<AdaptiveCarbonReportProposal>(item, item)).size, "views")
    }
}

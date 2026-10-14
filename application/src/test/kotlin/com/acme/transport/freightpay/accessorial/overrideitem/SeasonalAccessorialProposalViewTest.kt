package com.acme.transport.freightpay.accessorial.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalAccessorialProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalAccessorialProposal("code-682", "label-477")
        val view = toSeasonalAccessorialProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalAccessorialProposalViews(mutableListOf<SeasonalAccessorialProposal>(item, item)).size, "views")
    }
}

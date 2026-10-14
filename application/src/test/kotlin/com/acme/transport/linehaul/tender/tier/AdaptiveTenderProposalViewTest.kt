package com.acme.transport.linehaul.tender.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTenderProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTenderProposal("code-847", "title-163")
        val view = toAdaptiveTenderProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTenderProposalViews(mutableListOf<AdaptiveTenderProposal>(item, item)).size, "views")
    }
}

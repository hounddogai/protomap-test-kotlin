package com.acme.transport.lastmile.redelivery.redeliveryproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyRedeliveryProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyRedeliveryProposal("code-315", "title-938")
        val view = toLegacyRedeliveryProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyRedeliveryProposalViews(mutableListOf<LegacyRedeliveryProposal>(item, item)).size, "views")
    }
}

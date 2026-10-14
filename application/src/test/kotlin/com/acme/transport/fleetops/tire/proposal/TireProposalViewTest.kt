package com.acme.transport.fleetops.tire.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class TireProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireProposal("code-268", "label-673")
        val view = toTireProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireProposalViews(mutableListOf<TireProposal>(item, item)).size, "views")
    }
}

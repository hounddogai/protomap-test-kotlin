package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCapacityProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCapacityProposal("code-596", "name-567")
        val view = toAdaptiveCapacityProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCapacityProposalViews(mutableListOf<AdaptiveCapacityProposal>(item, item)).size, "views")
    }
}

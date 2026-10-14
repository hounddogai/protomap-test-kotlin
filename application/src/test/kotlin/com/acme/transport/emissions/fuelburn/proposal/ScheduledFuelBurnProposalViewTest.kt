package com.acme.transport.emissions.fuelburn.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledFuelBurnProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledFuelBurnProposal("code-980", "name-615")
        val view = toScheduledFuelBurnProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledFuelBurnProposalViews(mutableListOf<ScheduledFuelBurnProposal>(item, item)).size, "views")
    }
}

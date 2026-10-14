package com.acme.transport.emissions.fuelburn.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnProposal("code-761", "name-265")
        val view = toFuelBurnProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnProposalViews(mutableListOf<FuelBurnProposal>(item, item)).size, "views")
    }
}

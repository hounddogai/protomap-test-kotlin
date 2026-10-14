package com.acme.transport.emissions.fuelburn.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingFuelBurnProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingFuelBurnProposal("code-10", "name-751")
        val view = toRollingFuelBurnProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingFuelBurnProposalViews(mutableListOf<RollingFuelBurnProposal>(item, item)).size, "views")
    }
}

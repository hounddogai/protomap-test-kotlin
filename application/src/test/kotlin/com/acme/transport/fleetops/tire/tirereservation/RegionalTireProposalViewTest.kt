package com.acme.transport.fleetops.tire.tirereservation

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalTireProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalTireProposal("code-189", "name-519")
        val view = toRegionalTireProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalTireProposalViews(mutableListOf<RegionalTireProposal>(item, item)).size, "views")
    }
}

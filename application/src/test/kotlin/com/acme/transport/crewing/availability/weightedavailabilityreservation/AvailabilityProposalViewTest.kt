package com.acme.transport.crewing.availability.weightedavailabilityreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityProposal("code-243", "title-399")
        val view = toAvailabilityProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityProposalViews(mutableListOf<AvailabilityProposal>(item, item)).size, "views")
    }
}

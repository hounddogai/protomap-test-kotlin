package com.acme.transport.crewing.availability.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalAvailabilityProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalAvailabilityProposal("code-487", "label-947")
        val view = toGlobalAvailabilityProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalAvailabilityProposalViews(mutableListOf<GlobalAvailabilityProposal>(item, item)).size, "views")
    }
}

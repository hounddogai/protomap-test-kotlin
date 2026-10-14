package com.acme.transport.linehaul.capacity.capacitycycle

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCapacityProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCapacityProposal("code-3", "label-599")
        val view = toScheduledCapacityProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCapacityProposalViews(mutableListOf<ScheduledCapacityProposal>(item, item)).size, "views")
    }
}

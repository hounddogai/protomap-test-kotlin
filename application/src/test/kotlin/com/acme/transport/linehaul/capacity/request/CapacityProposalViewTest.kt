package com.acme.transport.linehaul.capacity.request

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityProposal("code-204", "name-442")
        val view = toCapacityProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityProposalViews(mutableListOf<CapacityProposal>(item, item)).size, "views")
    }
}

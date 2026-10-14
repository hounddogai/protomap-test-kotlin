package com.acme.transport.customs.hold.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class HoldProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHoldProposal("code-424", "title-164")
        val view = toHoldProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHoldProposalViews(mutableListOf<HoldProposal>(item, item)).size, "views")
    }
}

package com.acme.transport.safety.nearmiss.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class CappedNearMissProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCappedNearMissProposal("code-788", "label-220")
        val view = toCappedNearMissProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCappedNearMissProposalViews(mutableListOf<CappedNearMissProposal>(item, item)).size, "views")
    }
}

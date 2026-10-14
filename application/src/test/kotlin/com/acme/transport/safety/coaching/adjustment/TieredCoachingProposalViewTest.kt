package com.acme.transport.safety.coaching.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredCoachingProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredCoachingProposal("code-78", "name-55")
        val view = toTieredCoachingProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredCoachingProposalViews(mutableListOf<TieredCoachingProposal>(item, item)).size, "views")
    }
}

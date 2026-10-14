package com.acme.transport.crewing.restbreak.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualRestBreakProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualRestBreakProposal("code-739", "title-545")
        val view = toManualRestBreakProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualRestBreakProposalViews(mutableListOf<ManualRestBreakProposal>(item, item)).size, "views")
    }
}

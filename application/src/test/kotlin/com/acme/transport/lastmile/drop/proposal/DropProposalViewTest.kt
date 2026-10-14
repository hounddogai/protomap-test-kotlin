package com.acme.transport.lastmile.drop.proposal

import kotlin.test.Test
import kotlin.test.assertEquals

class DropProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDropProposal("code-753", "label-204")
        val view = toDropProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDropProposalViews(mutableListOf<DropProposal>(item, item)).size, "views")
    }
}

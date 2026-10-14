package com.acme.transport.network.node.template

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredNodeProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredNodeProposal("code-273", "label-760")
        val view = toTieredNodeProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredNodeProposalViews(mutableListOf<TieredNodeProposal>(item, item)).size, "views")
    }
}

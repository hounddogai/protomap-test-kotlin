package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeProposalViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeProposal("code-748", "label-784")
        val view = toNodeProposalView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeProposalViews(mutableListOf<NodeProposal>(item, item)).size, "views")
    }
}

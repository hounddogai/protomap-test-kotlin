package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeRollup("code-384", "label-536")
        val view = toNodeRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeRollupViews(mutableListOf<NodeRollup>(item, item)).size, "views")
    }
}

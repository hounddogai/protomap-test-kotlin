package com.acme.transport.network.node.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeCycle("code-348", "title-895")
        val view = toNodeCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeCycleViews(mutableListOf<NodeCycle>(item, item)).size, "views")
    }
}

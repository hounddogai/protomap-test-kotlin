package com.acme.transport.network.node.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeSegment("code-538", "label-294")
        val view = toNodeSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeSegmentViews(mutableListOf<NodeSegment>(item, item)).size, "views")
    }
}

package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeSnapshot("code-185", "label-121")
        val view = toNodeSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeSnapshotViews(mutableListOf<NodeSnapshot>(item, item)).size, "views")
    }
}

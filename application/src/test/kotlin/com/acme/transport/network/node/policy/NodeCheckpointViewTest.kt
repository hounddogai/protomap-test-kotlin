package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeCheckpoint("code-447", "name-738")
        val view = toNodeCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeCheckpointViews(mutableListOf<NodeCheckpoint>(item, item)).size, "views")
    }
}

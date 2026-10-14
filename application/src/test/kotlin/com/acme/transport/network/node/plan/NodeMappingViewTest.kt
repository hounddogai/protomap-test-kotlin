package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeMapping("code-786", "name-865")
        val view = toNodeMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeMappingViews(mutableListOf<NodeMapping>(item, item)).size, "views")
    }
}

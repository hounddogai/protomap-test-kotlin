package com.acme.transport.network.node.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeScore("code-929", "name-249")
        val view = toNodeScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeScoreViews(mutableListOf<NodeScore>(item, item)).size, "views")
    }
}

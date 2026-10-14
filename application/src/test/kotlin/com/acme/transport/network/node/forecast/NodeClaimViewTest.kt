package com.acme.transport.network.node.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeClaimViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeClaim("code-883", "name-99")
        val view = toNodeClaimView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeClaimViews(mutableListOf<NodeClaim>(item, item)).size, "views")
    }
}

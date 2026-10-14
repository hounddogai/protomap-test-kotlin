package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeEstimate("code-858", "label-72")
        val view = toNodeEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeEstimateViews(mutableListOf<NodeEstimate>(item, item)).size, "views")
    }
}

package com.acme.transport.network.node.tier

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedNodeBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedNodeBaseline("code-616", "title-935")
        val view = toBatchedNodeBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedNodeBaselineViews(mutableListOf<BatchedNodeBaseline>(item, item)).size, "views")
    }
}

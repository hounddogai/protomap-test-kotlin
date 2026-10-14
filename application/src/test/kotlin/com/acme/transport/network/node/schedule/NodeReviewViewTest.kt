package com.acme.transport.network.node.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class NodeReviewViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodeReview("code-918", "label-141")
        val view = toNodeReviewView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodeReviewViews(mutableListOf<NodeReview>(item, item)).size, "views")
    }
}

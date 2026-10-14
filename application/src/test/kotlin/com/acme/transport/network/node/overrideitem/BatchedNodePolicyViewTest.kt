package com.acme.transport.network.node.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedNodePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedNodePolicy("code-311", "label-751")
        val view = toBatchedNodePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedNodePolicyViews(mutableListOf<BatchedNodePolicy>(item, item)).size, "views")
    }
}

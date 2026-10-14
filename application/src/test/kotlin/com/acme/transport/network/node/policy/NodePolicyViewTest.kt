package com.acme.transport.network.node.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class NodePolicyViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodePolicy("code-927", "title-559")
        val view = toNodePolicyView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodePolicyViews(mutableListOf<NodePolicy>(item, item)).size, "views")
    }
}

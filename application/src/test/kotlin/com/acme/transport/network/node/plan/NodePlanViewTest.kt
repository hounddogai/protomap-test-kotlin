package com.acme.transport.network.node.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class NodePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newNodePlan("code-51", "name-302")
        val view = toNodePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toNodePlanViews(mutableListOf<NodePlan>(item, item)).size, "views")
    }
}

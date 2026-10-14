package com.acme.transport.network.node.nodedraft

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyNodePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyNodePlan("code-365", "name-950")
        val view = toLegacyNodePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyNodePlanViews(mutableListOf<LegacyNodePlan>(item, item)).size, "views")
    }
}

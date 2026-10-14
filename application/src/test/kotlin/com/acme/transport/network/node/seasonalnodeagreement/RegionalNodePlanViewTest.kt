package com.acme.transport.network.node.seasonalnodeagreement

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalNodePlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalNodePlan("code-695", "title-666")
        val view = toRegionalNodePlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalNodePlanViews(mutableListOf<RegionalNodePlan>(item, item)).size, "views")
    }
}

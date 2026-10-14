package com.acme.transport.network.hub.tieredhubplan

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultHubPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultHubPlan("code-799", "label-712")
        val view = toDefaultHubPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultHubPlanViews(mutableListOf<DefaultHubPlan>(item, item)).size, "views")
    }
}

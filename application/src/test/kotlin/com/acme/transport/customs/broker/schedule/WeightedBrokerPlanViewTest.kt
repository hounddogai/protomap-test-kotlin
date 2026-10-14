package com.acme.transport.customs.broker.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedBrokerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedBrokerPlan("code-452", "name-399")
        val view = toWeightedBrokerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedBrokerPlanViews(mutableListOf<WeightedBrokerPlan>(item, item)).size, "views")
    }
}

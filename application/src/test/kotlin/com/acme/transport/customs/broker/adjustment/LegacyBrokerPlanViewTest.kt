package com.acme.transport.customs.broker.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyBrokerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyBrokerPlan("code-876", "name-942")
        val view = toLegacyBrokerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyBrokerPlanViews(mutableListOf<LegacyBrokerPlan>(item, item)).size, "views")
    }
}

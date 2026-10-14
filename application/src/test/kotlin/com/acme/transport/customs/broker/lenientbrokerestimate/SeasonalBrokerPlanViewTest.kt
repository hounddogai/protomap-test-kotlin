package com.acme.transport.customs.broker.lenientbrokerestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class SeasonalBrokerPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newSeasonalBrokerPlan("code-498", "name-471")
        val view = toSeasonalBrokerPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toSeasonalBrokerPlanViews(mutableListOf<SeasonalBrokerPlan>(item, item)).size, "views")
    }
}

package com.acme.transport.fleetops.utilization.utilizationestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictUtilizationRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictUtilizationRule("code-540", "title-975")
        val view = toStrictUtilizationRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictUtilizationRuleViews(mutableListOf<StrictUtilizationRule>(item, item)).size, "views")
    }
}

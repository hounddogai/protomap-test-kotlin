package com.acme.transport.fleetops.serviceplan.planledger

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyServicePlanBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyServicePlanBaseline("code-548", "name-333")
        val view = toLegacyServicePlanBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyServicePlanBaselineViews(mutableListOf<LegacyServicePlanBaseline>(item, item)).size, "views")
    }
}

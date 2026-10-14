package com.acme.transport.fleetops.utilization.reservation

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationBaseline("code-976", "name-435")
        val view = toUtilizationBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationBaselineViews(mutableListOf<UtilizationBaseline>(item, item)).size, "views")
    }
}

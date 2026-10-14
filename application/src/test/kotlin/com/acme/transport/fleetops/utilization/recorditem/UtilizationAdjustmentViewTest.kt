package com.acme.transport.fleetops.utilization.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class UtilizationAdjustmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newUtilizationAdjustment("code-680", "name-786")
        val view = toUtilizationAdjustmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toUtilizationAdjustmentViews(mutableListOf<UtilizationAdjustment>(item, item)).size, "views")
    }
}

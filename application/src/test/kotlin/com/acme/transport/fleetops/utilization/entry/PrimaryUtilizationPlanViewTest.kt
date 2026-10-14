package com.acme.transport.fleetops.utilization.entry

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryUtilizationPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryUtilizationPlan("code-496", "label-993")
        val view = toPrimaryUtilizationPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryUtilizationPlanViews(mutableListOf<PrimaryUtilizationPlan>(item, item)).size, "views")
    }
}

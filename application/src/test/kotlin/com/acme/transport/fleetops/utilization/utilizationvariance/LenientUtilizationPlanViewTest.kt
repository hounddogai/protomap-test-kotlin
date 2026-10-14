package com.acme.transport.fleetops.utilization.utilizationvariance

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientUtilizationPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientUtilizationPlan("code-157", "title-210")
        val view = toLenientUtilizationPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientUtilizationPlanViews(mutableListOf<LenientUtilizationPlan>(item, item)).size, "views")
    }
}

package com.acme.transport.fleetops.serviceplan.planprofile

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackServicePlanProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackServicePlanProfile("code-982", "label-372")
        val view = toFallbackServicePlanProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackServicePlanProfileViews(mutableListOf<FallbackServicePlanProfile>(item, item)).size, "views")
    }
}

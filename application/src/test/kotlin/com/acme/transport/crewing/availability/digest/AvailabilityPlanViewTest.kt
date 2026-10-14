package com.acme.transport.crewing.availability.digest

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityPlanViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityPlan("code-134", "label-198")
        val view = toAvailabilityPlanView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityPlanViews(mutableListOf<AvailabilityPlan>(item, item)).size, "views")
    }
}

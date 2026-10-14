package com.acme.transport.customs.clearance.clearanceestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictClearanceEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictClearanceEstimate("code-956", "name-287")
        val view = toStrictClearanceEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictClearanceEstimateViews(mutableListOf<StrictClearanceEstimate>(item, item)).size, "views")
    }
}

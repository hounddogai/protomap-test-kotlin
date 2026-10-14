package com.acme.transport.customs.clearance.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedClearanceEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedClearanceEstimate("code-354", "name-290")
        val view = toWeightedClearanceEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedClearanceEstimateViews(mutableListOf<WeightedClearanceEstimate>(item, item)).size, "views")
    }
}

package com.acme.transport.fleetops.tire.estimate

import kotlin.test.Test
import kotlin.test.assertEquals

class TireEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireEstimate("code-976", "label-581")
        val view = toTireEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireEstimateViews(mutableListOf<TireEstimate>(item, item)).size, "views")
    }
}

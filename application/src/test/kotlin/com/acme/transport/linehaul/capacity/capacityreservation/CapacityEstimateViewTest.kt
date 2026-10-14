package com.acme.transport.linehaul.capacity.capacityreservation

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacityEstimate("code-42", "title-456")
        val view = toCapacityEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityEstimateViews(mutableListOf<CapacityEstimate>(item, item)).size, "views")
    }
}

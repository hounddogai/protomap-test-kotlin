package com.acme.transport.linehaul.capacity.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCapacityThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCapacityThreshold("code-52", "name-160")
        val view = toPrimaryCapacityThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCapacityThresholdViews(mutableListOf<PrimaryCapacityThreshold>(item, item)).size, "views")
    }
}

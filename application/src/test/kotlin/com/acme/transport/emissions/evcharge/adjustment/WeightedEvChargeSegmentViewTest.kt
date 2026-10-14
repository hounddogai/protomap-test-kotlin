package com.acme.transport.emissions.evcharge.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedEvChargeSegmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedEvChargeSegment("code-396", "label-47")
        val view = toWeightedEvChargeSegmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedEvChargeSegmentViews(mutableListOf<WeightedEvChargeSegment>(item, item)).size, "views")
    }
}

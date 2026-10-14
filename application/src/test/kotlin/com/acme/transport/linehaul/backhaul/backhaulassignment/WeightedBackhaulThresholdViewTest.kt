package com.acme.transport.linehaul.backhaul.backhaulassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedBackhaulThresholdViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedBackhaulThreshold("code-763", "name-764")
        val view = toWeightedBackhaulThresholdView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedBackhaulThresholdViews(mutableListOf<WeightedBackhaulThreshold>(item, item)).size, "views")
    }
}

package com.acme.transport.fleetops.tire.tireclaim

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedTireCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedTireCycle("code-411", "label-647")
        val view = toWeightedTireCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedTireCycleViews(mutableListOf<WeightedTireCycle>(item, item)).size, "views")
    }
}

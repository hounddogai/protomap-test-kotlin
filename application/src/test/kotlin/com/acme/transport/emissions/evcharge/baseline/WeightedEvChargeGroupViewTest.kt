package com.acme.transport.emissions.evcharge.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedEvChargeGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedEvChargeGroup("code-630", "title-901")
        val view = toWeightedEvChargeGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedEvChargeGroupViews(mutableListOf<WeightedEvChargeGroup>(item, item)).size, "views")
    }
}

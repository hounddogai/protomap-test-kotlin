package com.acme.transport.emissions.fuelburn.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFuelBurnGroupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFuelBurnGroup("code-92", "label-152")
        val view = toWeightedFuelBurnGroupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFuelBurnGroupViews(mutableListOf<WeightedFuelBurnGroup>(item, item)).size, "views")
    }
}

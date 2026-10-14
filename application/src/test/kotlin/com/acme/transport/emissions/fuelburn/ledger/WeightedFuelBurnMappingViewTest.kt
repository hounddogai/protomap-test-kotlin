package com.acme.transport.emissions.fuelburn.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFuelBurnMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFuelBurnMapping("code-250", "label-459")
        val view = toWeightedFuelBurnMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFuelBurnMappingViews(mutableListOf<WeightedFuelBurnMapping>(item, item)).size, "views")
    }
}

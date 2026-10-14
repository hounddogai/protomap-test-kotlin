package com.acme.transport.emissions.fuelburn.burnestimate

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFuelBurnEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFuelBurnEstimate("code-330", "name-815")
        val view = toWeightedFuelBurnEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFuelBurnEstimateViews(mutableListOf<WeightedFuelBurnEstimate>(item, item)).size, "views")
    }
}

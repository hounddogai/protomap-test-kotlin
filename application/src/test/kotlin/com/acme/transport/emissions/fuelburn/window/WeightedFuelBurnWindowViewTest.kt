package com.acme.transport.emissions.fuelburn.window

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFuelBurnWindowViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFuelBurnWindow("code-463", "name-771")
        val view = toWeightedFuelBurnWindowView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFuelBurnWindowViews(mutableListOf<WeightedFuelBurnWindow>(item, item)).size, "views")
    }
}

package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnRollup("code-349", "name-500")
        val view = toFuelBurnRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnRollupViews(mutableListOf<FuelBurnRollup>(item, item)).size, "views")
    }
}

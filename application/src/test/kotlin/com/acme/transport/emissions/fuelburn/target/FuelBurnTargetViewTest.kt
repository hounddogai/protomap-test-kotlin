package com.acme.transport.emissions.fuelburn.target

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnTarget("code-813", "name-659")
        val view = toFuelBurnTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnTargetViews(mutableListOf<FuelBurnTarget>(item, item)).size, "views")
    }
}

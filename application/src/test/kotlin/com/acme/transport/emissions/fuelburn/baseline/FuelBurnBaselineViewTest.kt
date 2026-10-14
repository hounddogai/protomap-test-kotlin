package com.acme.transport.emissions.fuelburn.baseline

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnBaseline("code-994", "name-228")
        val view = toFuelBurnBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnBaselineViews(mutableListOf<FuelBurnBaseline>(item, item)).size, "views")
    }
}

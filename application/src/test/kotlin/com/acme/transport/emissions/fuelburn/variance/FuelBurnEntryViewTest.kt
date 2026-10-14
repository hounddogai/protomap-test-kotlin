package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnEntry("code-376", "label-644")
        val view = toFuelBurnEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnEntryViews(mutableListOf<FuelBurnEntry>(item, item)).size, "views")
    }
}

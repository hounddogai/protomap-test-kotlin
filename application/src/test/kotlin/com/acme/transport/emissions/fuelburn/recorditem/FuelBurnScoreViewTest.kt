package com.acme.transport.emissions.fuelburn.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnScoreViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnScore("code-754", "label-355")
        val view = toFuelBurnScoreView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnScoreViews(mutableListOf<FuelBurnScore>(item, item)).size, "views")
    }
}

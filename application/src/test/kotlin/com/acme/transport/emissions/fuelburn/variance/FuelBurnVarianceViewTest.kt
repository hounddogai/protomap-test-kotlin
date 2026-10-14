package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnVariance("code-490", "title-827")
        val view = toFuelBurnVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnVarianceViews(mutableListOf<FuelBurnVariance>(item, item)).size, "views")
    }
}

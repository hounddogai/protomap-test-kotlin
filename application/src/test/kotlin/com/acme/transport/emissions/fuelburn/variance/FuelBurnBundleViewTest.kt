package com.acme.transport.emissions.fuelburn.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnBundle("code-641", "title-335")
        val view = toFuelBurnBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnBundleViews(mutableListOf<FuelBurnBundle>(item, item)).size, "views")
    }
}

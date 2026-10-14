package com.acme.transport.emissions.fuelburn.burnnotice

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnMetricViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnMetric("code-716", "name-126")
        val view = toFuelBurnMetricView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnMetricViews(mutableListOf<FuelBurnMetric>(item, item)).size, "views")
    }
}

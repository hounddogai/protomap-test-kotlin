package com.acme.transport.emissions.fuelburn.burnbudget

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnForecastViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnForecast("code-371", "title-585")
        val view = toFuelBurnForecastView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnForecastViews(mutableListOf<FuelBurnForecast>(item, item)).size, "views")
    }
}

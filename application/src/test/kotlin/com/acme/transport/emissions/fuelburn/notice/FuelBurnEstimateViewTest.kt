package com.acme.transport.emissions.fuelburn.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnEstimate("code-866", "title-228")
        val view = toFuelBurnEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnEstimateViews(mutableListOf<FuelBurnEstimate>(item, item)).size, "views")
    }
}

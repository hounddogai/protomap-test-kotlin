package com.acme.transport.emissions.fuelburn.burnmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class GlobalFuelBurnEstimateViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newGlobalFuelBurnEstimate("code-310", "label-945")
        val view = toGlobalFuelBurnEstimateView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toGlobalFuelBurnEstimateViews(mutableListOf<GlobalFuelBurnEstimate>(item, item)).size, "views")
    }
}

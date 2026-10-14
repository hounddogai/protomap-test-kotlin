package com.acme.transport.emissions.fuelburn.burnwindow

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFuelBurnSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFuelBurnSummary("code-443", "label-364")
        val view = toWeightedFuelBurnSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFuelBurnSummaryViews(mutableListOf<WeightedFuelBurnSummary>(item, item)).size, "views")
    }
}

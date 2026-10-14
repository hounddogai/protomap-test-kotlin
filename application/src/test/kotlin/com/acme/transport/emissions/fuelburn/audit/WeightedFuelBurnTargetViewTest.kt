package com.acme.transport.emissions.fuelburn.audit

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFuelBurnTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFuelBurnTarget("code-217", "name-553")
        val view = toWeightedFuelBurnTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFuelBurnTargetViews(mutableListOf<WeightedFuelBurnTarget>(item, item)).size, "views")
    }
}

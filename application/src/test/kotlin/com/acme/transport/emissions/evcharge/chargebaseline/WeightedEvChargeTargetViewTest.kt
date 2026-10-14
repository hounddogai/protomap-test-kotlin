package com.acme.transport.emissions.evcharge.chargebaseline

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedEvChargeTargetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedEvChargeTarget("code-85", "name-679")
        val view = toWeightedEvChargeTargetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedEvChargeTargetViews(mutableListOf<WeightedEvChargeTarget>(item, item)).size, "views")
    }
}

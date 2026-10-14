package com.acme.transport.emissions.fuelburn.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class WeightedFuelBurnBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newWeightedFuelBurnBudget("code-888", "name-952")
        val view = toWeightedFuelBurnBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toWeightedFuelBurnBudgetViews(mutableListOf<WeightedFuelBurnBudget>(item, item)).size, "views")
    }
}

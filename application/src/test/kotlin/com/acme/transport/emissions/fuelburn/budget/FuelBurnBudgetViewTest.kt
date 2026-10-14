package com.acme.transport.emissions.fuelburn.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnBudget("code-452", "title-507")
        val view = toFuelBurnBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnBudgetViews(mutableListOf<FuelBurnBudget>(item, item)).size, "views")
    }
}

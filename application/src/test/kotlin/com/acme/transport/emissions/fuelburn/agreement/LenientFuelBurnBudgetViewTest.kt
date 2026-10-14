package com.acme.transport.emissions.fuelburn.agreement

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientFuelBurnBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientFuelBurnBudget("code-415", "name-429")
        val view = toLenientFuelBurnBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientFuelBurnBudgetViews(mutableListOf<LenientFuelBurnBudget>(item, item)).size, "views")
    }
}

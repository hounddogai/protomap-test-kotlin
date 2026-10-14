package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualTireBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualTireBudget("code-533", "name-413")
        val view = toManualTireBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualTireBudgetViews(mutableListOf<ManualTireBudget>(item, item)).size, "views")
    }
}

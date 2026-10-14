package com.acme.transport.crewing.availability.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilityBudget("code-563", "name-220")
        val view = toAvailabilityBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityBudgetViews(mutableListOf<AvailabilityBudget>(item, item)).size, "views")
    }
}

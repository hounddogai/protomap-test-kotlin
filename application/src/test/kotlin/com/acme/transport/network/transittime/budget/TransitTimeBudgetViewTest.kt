package com.acme.transport.network.transittime.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeBudget("code-916", "title-815")
        val view = toTransitTimeBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeBudgetViews(mutableListOf<TransitTimeBudget>(item, item)).size, "views")
    }
}

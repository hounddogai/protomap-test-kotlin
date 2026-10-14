package com.acme.transport.network.transittime.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryTransitTimeBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryTransitTimeBudget("code-528", "name-151")
        val view = toPrimaryTransitTimeBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryTransitTimeBudgetViews(mutableListOf<PrimaryTransitTimeBudget>(item, item)).size, "views")
    }
}

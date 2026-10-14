package com.acme.transport.emissions.routeoffset.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryRouteOffsetBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryRouteOffsetBudget("code-598", "label-772")
        val view = toPrimaryRouteOffsetBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryRouteOffsetBudgetViews(mutableListOf<PrimaryRouteOffsetBudget>(item, item)).size, "views")
    }
}

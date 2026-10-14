package com.acme.transport.emissions.routeoffset.offsetassignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetBudgetViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetBudget("code-608", "label-9")
        val view = toRouteOffsetBudgetView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetBudgetViews(mutableListOf<RouteOffsetBudget>(item, item)).size, "views")
    }
}

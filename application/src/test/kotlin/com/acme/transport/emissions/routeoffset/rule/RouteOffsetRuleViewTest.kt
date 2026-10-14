package com.acme.transport.emissions.routeoffset.rule

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetRuleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetRule("code-40", "title-763")
        val view = toRouteOffsetRuleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetRuleViews(mutableListOf<RouteOffsetRule>(item, item)).size, "views")
    }
}

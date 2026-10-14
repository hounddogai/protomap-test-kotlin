package com.acme.transport.emissions.routeoffset.assignment

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetAssignmentViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetAssignment("code-338", "label-411")
        val view = toRouteOffsetAssignmentView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetAssignmentViews(mutableListOf<RouteOffsetAssignment>(item, item)).size, "views")
    }
}

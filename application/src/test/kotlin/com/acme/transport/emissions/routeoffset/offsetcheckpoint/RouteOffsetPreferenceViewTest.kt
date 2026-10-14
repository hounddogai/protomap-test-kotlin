package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import kotlin.test.Test
import kotlin.test.assertEquals

class RouteOffsetPreferenceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRouteOffsetPreference("code-579", "label-453")
        val view = toRouteOffsetPreferenceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRouteOffsetPreferenceViews(mutableListOf<RouteOffsetPreference>(item, item)).size, "views")
    }
}

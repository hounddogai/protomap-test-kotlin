package com.acme.transport.emissions.routeoffset.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRouteOffsetProfileViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRouteOffsetProfile("code-452", "title-579")
        val view = toFallbackRouteOffsetProfileView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRouteOffsetProfileViews(mutableListOf<FallbackRouteOffsetProfile>(item, item)).size, "views")
    }
}

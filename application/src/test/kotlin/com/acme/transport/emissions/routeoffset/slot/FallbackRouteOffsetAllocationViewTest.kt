package com.acme.transport.emissions.routeoffset.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRouteOffsetAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRouteOffsetAllocation("code-989", "title-854")
        val view = toFallbackRouteOffsetAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRouteOffsetAllocationViews(mutableListOf<FallbackRouteOffsetAllocation>(item, item)).size, "views")
    }
}

package com.acme.transport.emissions.routeoffset.forecast

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRouteOffsetAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRouteOffsetAllocation("code-579", "title-92")
        val view = toBatchedRouteOffsetAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRouteOffsetAllocationViews(mutableListOf<BatchedRouteOffsetAllocation>(item, item)).size, "views")
    }
}

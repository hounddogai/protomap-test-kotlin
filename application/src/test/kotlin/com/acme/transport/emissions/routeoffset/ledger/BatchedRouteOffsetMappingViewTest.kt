package com.acme.transport.emissions.routeoffset.ledger

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedRouteOffsetMappingViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedRouteOffsetMapping("code-848", "name-426")
        val view = toBatchedRouteOffsetMappingView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedRouteOffsetMappingViews(mutableListOf<BatchedRouteOffsetMapping>(item, item)).size, "views")
    }
}

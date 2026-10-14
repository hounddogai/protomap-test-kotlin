package com.acme.transport.network.transittime.allocation

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeAllocationViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeAllocation("code-164", "name-245")
        val view = toTransitTimeAllocationView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeAllocationViews(mutableListOf<TransitTimeAllocation>(item, item)).size, "views")
    }
}

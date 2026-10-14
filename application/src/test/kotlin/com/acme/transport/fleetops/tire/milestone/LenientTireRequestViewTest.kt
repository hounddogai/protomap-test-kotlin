package com.acme.transport.fleetops.tire.milestone

import kotlin.test.Test
import kotlin.test.assertEquals

class LenientTireRequestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLenientTireRequest("code-164", "title-940")
        val view = toLenientTireRequestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLenientTireRequestViews(mutableListOf<LenientTireRequest>(item, item)).size, "views")
    }
}

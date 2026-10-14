package com.acme.transport.network.transittime.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class TransitTimeRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTransitTimeRollup("code-359", "label-924")
        val view = toTransitTimeRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTransitTimeRollupViews(mutableListOf<TransitTimeRollup>(item, item)).size, "views")
    }
}

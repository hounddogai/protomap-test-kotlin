package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class TireBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireBaseline("code-293", "title-499")
        val view = toTireBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireBaselineViews(mutableListOf<TireBaseline>(item, item)).size, "views")
    }
}

package com.acme.transport.linehaul.capacity.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacitySummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacitySummary("code-943", "name-274")
        val view = toCapacitySummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacitySummaryViews(mutableListOf<CapacitySummary>(item, item)).size, "views")
    }
}

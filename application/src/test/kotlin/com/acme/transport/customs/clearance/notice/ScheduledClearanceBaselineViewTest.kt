package com.acme.transport.customs.clearance.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledClearanceBaselineViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledClearanceBaseline("code-189", "title-951")
        val view = toScheduledClearanceBaselineView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledClearanceBaselineViews(mutableListOf<ScheduledClearanceBaseline>(item, item)).size, "views")
    }
}

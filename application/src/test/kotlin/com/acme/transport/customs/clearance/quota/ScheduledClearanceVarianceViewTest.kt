package com.acme.transport.customs.clearance.quota

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledClearanceVarianceViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledClearanceVariance("code-320", "title-803")
        val view = toScheduledClearanceVarianceView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledClearanceVarianceViews(mutableListOf<ScheduledClearanceVariance>(item, item)).size, "views")
    }
}

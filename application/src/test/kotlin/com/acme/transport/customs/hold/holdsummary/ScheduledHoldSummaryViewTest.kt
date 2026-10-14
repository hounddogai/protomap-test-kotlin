package com.acme.transport.customs.hold.holdsummary

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledHoldSummaryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledHoldSummary("code-93", "name-655")
        val view = toScheduledHoldSummaryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledHoldSummaryViews(mutableListOf<ScheduledHoldSummary>(item, item)).size, "views")
    }
}

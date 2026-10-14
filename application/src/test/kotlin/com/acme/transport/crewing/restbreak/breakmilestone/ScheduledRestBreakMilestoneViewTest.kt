package com.acme.transport.crewing.restbreak.breakmilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledRestBreakMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledRestBreakMilestone("code-927", "name-827")
        val view = toScheduledRestBreakMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledRestBreakMilestoneViews(mutableListOf<ScheduledRestBreakMilestone>(item, item)).size, "views")
    }
}

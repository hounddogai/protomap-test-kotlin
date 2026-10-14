package com.acme.transport.crewing.restbreak.legacybreakentry

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingRestBreakMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingRestBreakMilestone("code-990", "title-881")
        val view = toRollingRestBreakMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingRestBreakMilestoneViews(mutableListOf<RollingRestBreakMilestone>(item, item)).size, "views")
    }
}

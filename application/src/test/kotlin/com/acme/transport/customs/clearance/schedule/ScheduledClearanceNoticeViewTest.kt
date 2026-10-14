package com.acme.transport.customs.clearance.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledClearanceNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledClearanceNotice("code-660", "name-490")
        val view = toScheduledClearanceNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledClearanceNoticeViews(mutableListOf<ScheduledClearanceNotice>(item, item)).size, "views")
    }
}

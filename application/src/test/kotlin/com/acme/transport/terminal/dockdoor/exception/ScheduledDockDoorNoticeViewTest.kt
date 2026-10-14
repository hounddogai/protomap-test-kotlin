package com.acme.transport.terminal.dockdoor.exception

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledDockDoorNoticeViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledDockDoorNotice("code-764", "title-872")
        val view = toScheduledDockDoorNoticeView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledDockDoorNoticeViews(mutableListOf<ScheduledDockDoorNotice>(item, item)).size, "views")
    }
}

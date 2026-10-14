package com.acme.transport.terminal.crossdock.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class CrossDockScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCrossDockSchedule("code-492", "title-891")
        val view = toCrossDockScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCrossDockScheduleViews(mutableListOf<CrossDockSchedule>(item, item)).size, "views")
    }
}

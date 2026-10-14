package com.acme.transport.terminal.dockdoor.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultDockDoorScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultDockDoorSchedule("code-545", "name-306")
        val view = toDefaultDockDoorScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultDockDoorScheduleViews(mutableListOf<DefaultDockDoorSchedule>(item, item)).size, "views")
    }
}

package com.acme.transport.customs.clearance.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class ClearanceScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newClearanceSchedule("code-984", "name-242")
        val view = toClearanceScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toClearanceScheduleViews(mutableListOf<ClearanceSchedule>(item, item)).size, "views")
    }
}

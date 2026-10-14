package com.acme.transport.crewing.restbreak.adjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class RestBreakScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRestBreakSchedule("code-774", "label-33")
        val view = toRestBreakScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRestBreakScheduleViews(mutableListOf<RestBreakSchedule>(item, item)).size, "views")
    }
}

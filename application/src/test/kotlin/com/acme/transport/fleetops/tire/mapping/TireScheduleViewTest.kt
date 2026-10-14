package com.acme.transport.fleetops.tire.mapping

import kotlin.test.Test
import kotlin.test.assertEquals

class TireScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireSchedule("code-370", "label-510")
        val view = toTireScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireScheduleViews(mutableListOf<TireSchedule>(item, item)).size, "views")
    }
}

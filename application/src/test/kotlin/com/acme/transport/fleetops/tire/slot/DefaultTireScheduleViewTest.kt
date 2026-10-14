package com.acme.transport.fleetops.tire.slot

import kotlin.test.Test
import kotlin.test.assertEquals

class DefaultTireScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDefaultTireSchedule("code-117", "name-275")
        val view = toDefaultTireScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDefaultTireScheduleViews(mutableListOf<DefaultTireSchedule>(item, item)).size, "views")
    }
}

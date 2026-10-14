package com.acme.transport.linehaul.capacity.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class CapacityScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCapacitySchedule("code-335", "name-260")
        val view = toCapacityScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCapacityScheduleViews(mutableListOf<CapacitySchedule>(item, item)).size, "views")
    }
}

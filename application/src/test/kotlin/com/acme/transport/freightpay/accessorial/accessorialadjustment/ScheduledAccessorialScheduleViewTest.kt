package com.acme.transport.freightpay.accessorial.accessorialadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledAccessorialScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledAccessorialSchedule("code-442", "title-711")
        val view = toScheduledAccessorialScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledAccessorialScheduleViews(mutableListOf<ScheduledAccessorialSchedule>(item, item)).size, "views")
    }
}

package com.acme.transport.crewing.availability.availabilityexception

import kotlin.test.Test
import kotlin.test.assertEquals

class AvailabilityScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAvailabilitySchedule("code-139", "name-634")
        val view = toAvailabilityScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAvailabilityScheduleViews(mutableListOf<AvailabilitySchedule>(item, item)).size, "views")
    }
}

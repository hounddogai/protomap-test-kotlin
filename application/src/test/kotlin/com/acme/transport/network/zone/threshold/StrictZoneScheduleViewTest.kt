package com.acme.transport.network.zone.threshold

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictZoneScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictZoneSchedule("code-831", "label-123")
        val view = toStrictZoneScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictZoneScheduleViews(mutableListOf<StrictZoneSchedule>(item, item)).size, "views")
    }
}

package com.acme.transport.safety.incident.claim

import kotlin.test.Test
import kotlin.test.assertEquals

class IncidentScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newIncidentSchedule("code-956", "title-664")
        val view = toIncidentScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toIncidentScheduleViews(mutableListOf<IncidentSchedule>(item, item)).size, "views")
    }
}

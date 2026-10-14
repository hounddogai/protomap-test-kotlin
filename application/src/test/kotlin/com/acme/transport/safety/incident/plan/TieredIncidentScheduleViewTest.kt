package com.acme.transport.safety.incident.plan

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredIncidentScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredIncidentSchedule("code-985", "label-944")
        val view = toTieredIncidentScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredIncidentScheduleViews(mutableListOf<TieredIncidentSchedule>(item, item)).size, "views")
    }
}

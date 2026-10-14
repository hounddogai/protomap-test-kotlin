package com.acme.transport.network.zone.zonebundle

import kotlin.test.Test
import kotlin.test.assertEquals

class RegionalZoneScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRegionalZoneSchedule("code-186", "title-488")
        val view = toRegionalZoneScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRegionalZoneScheduleViews(mutableListOf<RegionalZoneSchedule>(item, item)).size, "views")
    }
}

package com.acme.transport.emissions.routeoffset.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackRouteOffsetScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackRouteOffsetSchedule("code-190", "name-952")
        val view = toFallbackRouteOffsetScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackRouteOffsetScheduleViews(mutableListOf<FallbackRouteOffsetSchedule>(item, item)).size, "views")
    }
}

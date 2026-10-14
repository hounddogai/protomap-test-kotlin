package com.acme.transport.network.hub.overrideitem

import kotlin.test.Test
import kotlin.test.assertEquals

class HubScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newHubSchedule("code-100", "title-895")
        val view = toHubScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toHubScheduleViews(mutableListOf<HubSchedule>(item, item)).size, "views")
    }
}

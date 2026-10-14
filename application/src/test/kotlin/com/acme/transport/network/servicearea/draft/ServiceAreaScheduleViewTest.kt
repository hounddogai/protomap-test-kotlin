package com.acme.transport.network.servicearea.draft

import kotlin.test.Test
import kotlin.test.assertEquals

class ServiceAreaScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServiceAreaSchedule("code-692", "title-802")
        val view = toServiceAreaScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServiceAreaScheduleViews(mutableListOf<ServiceAreaSchedule>(item, item)).size, "views")
    }
}

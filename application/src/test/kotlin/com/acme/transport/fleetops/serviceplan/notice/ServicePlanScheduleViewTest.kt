package com.acme.transport.fleetops.serviceplan.notice

import kotlin.test.Test
import kotlin.test.assertEquals

class ServicePlanScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newServicePlanSchedule("code-492", "name-418")
        val view = toServicePlanScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toServicePlanScheduleViews(mutableListOf<ServicePlanSchedule>(item, item)).size, "views")
    }
}

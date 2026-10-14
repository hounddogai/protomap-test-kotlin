package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCameraEventMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCameraEventMilestone("code-673", "name-355")
        val view = toScheduledCameraEventMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCameraEventMilestoneViews(mutableListOf<ScheduledCameraEventMilestone>(item, item)).size, "views")
    }
}

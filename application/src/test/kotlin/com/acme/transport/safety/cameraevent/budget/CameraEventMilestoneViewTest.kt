package com.acme.transport.safety.cameraevent.budget

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventMilestoneViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventMilestone("code-986", "title-817")
        val view = toCameraEventMilestoneView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventMilestoneViews(mutableListOf<CameraEventMilestone>(item, item)).size, "views")
    }
}

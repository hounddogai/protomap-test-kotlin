package com.acme.transport.safety.cameraevent.globaleventaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventScheduleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventSchedule("code-530", "label-222")
        val view = toCameraEventScheduleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventScheduleViews(mutableListOf<CameraEventSchedule>(item, item)).size, "views")
    }
}

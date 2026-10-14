package com.acme.transport.safety.cameraevent.evententry

import kotlin.test.Test
import kotlin.test.assertEquals

class ScheduledCameraEventEntryViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newScheduledCameraEventEntry("code-128", "title-181")
        val view = toScheduledCameraEventEntryView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toScheduledCameraEventEntryViews(mutableListOf<ScheduledCameraEventEntry>(item, item)).size, "views")
    }
}

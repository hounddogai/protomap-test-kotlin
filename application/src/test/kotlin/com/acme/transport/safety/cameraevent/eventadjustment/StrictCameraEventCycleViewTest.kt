package com.acme.transport.safety.cameraevent.eventadjustment

import kotlin.test.Test
import kotlin.test.assertEquals

class StrictCameraEventCycleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newStrictCameraEventCycle("code-520", "title-720")
        val view = toStrictCameraEventCycleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toStrictCameraEventCycleViews(mutableListOf<StrictCameraEventCycle>(item, item)).size, "views")
    }
}

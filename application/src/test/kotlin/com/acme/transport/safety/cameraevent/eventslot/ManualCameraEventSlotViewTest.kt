package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCameraEventSlotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCameraEventSlot("code-414", "label-252")
        val view = toManualCameraEventSlotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCameraEventSlotViews(mutableListOf<ManualCameraEventSlot>(item, item)).size, "views")
    }
}

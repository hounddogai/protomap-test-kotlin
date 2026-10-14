package com.acme.transport.safety.cameraevent.eventscore

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveCameraEventRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveCameraEventRecord("code-481", "title-945")
        val view = toAdaptiveCameraEventRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveCameraEventRecordViews(mutableListOf<AdaptiveCameraEventRecord>(item, item)).size, "views")
    }
}

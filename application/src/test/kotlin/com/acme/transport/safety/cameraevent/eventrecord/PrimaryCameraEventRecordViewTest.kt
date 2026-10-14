package com.acme.transport.safety.cameraevent.eventrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class PrimaryCameraEventRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newPrimaryCameraEventRecord("code-296", "title-843")
        val view = toPrimaryCameraEventRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toPrimaryCameraEventRecordViews(mutableListOf<PrimaryCameraEventRecord>(item, item)).size, "views")
    }
}

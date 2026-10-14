package com.acme.transport.safety.cameraevent.recorditem

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCameraEventRecordViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCameraEventRecord("code-424", "title-922")
        val view = toFallbackCameraEventRecordView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCameraEventRecordViews(mutableListOf<FallbackCameraEventRecord>(item, item)).size, "views")
    }
}

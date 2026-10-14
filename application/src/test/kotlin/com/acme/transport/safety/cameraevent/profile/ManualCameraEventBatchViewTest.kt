package com.acme.transport.safety.cameraevent.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCameraEventBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCameraEventBatch("code-467", "title-508")
        val view = toManualCameraEventBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCameraEventBatchViews(mutableListOf<ManualCameraEventBatch>(item, item)).size, "views")
    }
}

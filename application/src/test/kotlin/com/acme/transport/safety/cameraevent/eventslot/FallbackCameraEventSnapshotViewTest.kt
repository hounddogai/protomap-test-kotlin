package com.acme.transport.safety.cameraevent.eventslot

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackCameraEventSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackCameraEventSnapshot("code-134", "label-938")
        val view = toFallbackCameraEventSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackCameraEventSnapshotViews(mutableListOf<FallbackCameraEventSnapshot>(item, item)).size, "views")
    }
}

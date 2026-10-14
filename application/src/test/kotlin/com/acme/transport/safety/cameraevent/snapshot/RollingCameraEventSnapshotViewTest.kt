package com.acme.transport.safety.cameraevent.snapshot

import kotlin.test.Test
import kotlin.test.assertEquals

class RollingCameraEventSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newRollingCameraEventSnapshot("code-188", "title-641")
        val view = toRollingCameraEventSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toRollingCameraEventSnapshotViews(mutableListOf<RollingCameraEventSnapshot>(item, item)).size, "views")
    }
}

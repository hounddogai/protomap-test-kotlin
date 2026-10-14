package com.acme.transport.safety.cameraevent.eventaudit

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCameraEventBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCameraEventBundle("code-825", "name-503")
        val view = toManualCameraEventBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCameraEventBundleViews(mutableListOf<ManualCameraEventBundle>(item, item)).size, "views")
    }
}

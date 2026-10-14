package com.acme.transport.safety.cameraevent.bundle

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventBundleViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventBundle("code-641", "title-97")
        val view = toCameraEventBundleView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventBundleViews(mutableListOf<CameraEventBundle>(item, item)).size, "views")
    }
}

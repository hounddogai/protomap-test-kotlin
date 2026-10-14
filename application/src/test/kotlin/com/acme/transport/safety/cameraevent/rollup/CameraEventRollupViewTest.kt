package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventRollupViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventRollup("code-145", "label-952")
        val view = toCameraEventRollupView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventRollupViews(mutableListOf<CameraEventRollup>(item, item)).size, "views")
    }
}

package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class ManualCameraEventDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newManualCameraEventDigest("code-571", "label-464")
        val view = toManualCameraEventDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toManualCameraEventDigestViews(mutableListOf<ManualCameraEventDigest>(item, item)).size, "views")
    }
}

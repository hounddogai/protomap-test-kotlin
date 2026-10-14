package com.acme.transport.safety.cameraevent.policy

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventDigestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventDigest("code-609", "label-88")
        val view = toCameraEventDigestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventDigestViews(mutableListOf<CameraEventDigest>(item, item)).size, "views")
    }
}

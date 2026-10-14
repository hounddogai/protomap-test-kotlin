package com.acme.transport.safety.cameraevent.rollup

import kotlin.test.Test
import kotlin.test.assertEquals

class CameraEventManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newCameraEventManifest("code-700", "name-643")
        val view = toCameraEventManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toCameraEventManifestViews(mutableListOf<CameraEventManifest>(item, item)).size, "views")
    }
}

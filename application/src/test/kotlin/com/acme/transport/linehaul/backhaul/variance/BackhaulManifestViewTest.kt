package com.acme.transport.linehaul.backhaul.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class BackhaulManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBackhaulManifest("code-635", "label-947")
        val view = toBackhaulManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBackhaulManifestViews(mutableListOf<BackhaulManifest>(item, item)).size, "views")
    }
}

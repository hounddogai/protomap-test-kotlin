package com.acme.transport.linehaul.trailer.trailermilestone

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerManifest("code-806", "title-900")
        val view = toTrailerManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerManifestViews(mutableListOf<TrailerManifest>(item, item)).size, "views")
    }
}

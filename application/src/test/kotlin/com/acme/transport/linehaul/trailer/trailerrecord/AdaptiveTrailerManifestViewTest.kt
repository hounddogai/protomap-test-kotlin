package com.acme.transport.linehaul.trailer.trailerrecord

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTrailerManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTrailerManifest("code-834", "title-288")
        val view = toAdaptiveTrailerManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTrailerManifestViews(mutableListOf<AdaptiveTrailerManifest>(item, item)).size, "views")
    }
}

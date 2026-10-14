package com.acme.transport.emissions.idling.regionalidlingmanifest

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackIdlingManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackIdlingManifest("code-384", "title-653")
        val view = toFallbackIdlingManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackIdlingManifestViews(mutableListOf<FallbackIdlingManifest>(item, item)).size, "views")
    }
}

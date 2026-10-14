package com.acme.transport.terminal.traileryard.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class TrailerYardManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTrailerYardManifest("code-105", "title-890")
        val view = toTrailerYardManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTrailerYardManifestViews(mutableListOf<TrailerYardManifest>(item, item)).size, "views")
    }
}

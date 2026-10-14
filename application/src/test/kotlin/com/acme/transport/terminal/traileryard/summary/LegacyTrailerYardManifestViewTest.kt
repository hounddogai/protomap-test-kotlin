package com.acme.transport.terminal.traileryard.summary

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyTrailerYardManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyTrailerYardManifest("code-452", "name-43")
        val view = toLegacyTrailerYardManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyTrailerYardManifestViews(mutableListOf<LegacyTrailerYardManifest>(item, item)).size, "views")
    }
}

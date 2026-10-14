package com.acme.transport.terminal.sortation.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacySortationManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacySortationManifest("code-51", "name-462")
        val view = toLegacySortationManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacySortationManifestViews(mutableListOf<LegacySortationManifest>(item, item)).size, "views")
    }
}

package com.acme.transport.terminal.dwell.variance

import kotlin.test.Test
import kotlin.test.assertEquals

class DwellManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newDwellManifest("code-454", "title-44")
        val view = toDwellManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toDwellManifestViews(mutableListOf<DwellManifest>(item, item)).size, "views")
    }
}

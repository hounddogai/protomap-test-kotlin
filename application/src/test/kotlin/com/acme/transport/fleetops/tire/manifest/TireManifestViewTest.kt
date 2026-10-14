package com.acme.transport.fleetops.tire.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class TireManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTireManifest("code-58", "name-355")
        val view = toTireManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTireManifestViews(mutableListOf<TireManifest>(item, item)).size, "views")
    }
}

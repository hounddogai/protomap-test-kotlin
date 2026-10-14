package com.acme.transport.network.zone.schedule

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredZoneManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredZoneManifest("code-931", "label-109")
        val view = toTieredZoneManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredZoneManifestViews(mutableListOf<TieredZoneManifest>(item, item)).size, "views")
    }
}

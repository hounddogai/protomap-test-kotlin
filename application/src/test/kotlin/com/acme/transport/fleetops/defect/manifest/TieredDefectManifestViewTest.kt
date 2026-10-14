package com.acme.transport.fleetops.defect.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class TieredDefectManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newTieredDefectManifest("code-287", "title-298")
        val view = toTieredDefectManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toTieredDefectManifestViews(mutableListOf<TieredDefectManifest>(item, item)).size, "views")
    }
}

package com.acme.transport.fleetops.tire.tirebudget

import kotlin.test.Test
import kotlin.test.assertEquals

class AdaptiveTireManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newAdaptiveTireManifest("code-654", "title-867")
        val view = toAdaptiveTireManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toAdaptiveTireManifestViews(mutableListOf<AdaptiveTireManifest>(item, item)).size, "views")
    }
}

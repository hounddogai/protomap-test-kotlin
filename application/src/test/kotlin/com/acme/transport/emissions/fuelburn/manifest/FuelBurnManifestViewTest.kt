package com.acme.transport.emissions.fuelburn.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class FuelBurnManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFuelBurnManifest("code-835", "label-772")
        val view = toFuelBurnManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFuelBurnManifestViews(mutableListOf<FuelBurnManifest>(item, item)).size, "views")
    }
}

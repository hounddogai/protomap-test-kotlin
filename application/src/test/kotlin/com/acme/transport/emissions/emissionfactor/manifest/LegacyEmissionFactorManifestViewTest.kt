package com.acme.transport.emissions.emissionfactor.manifest

import kotlin.test.Test
import kotlin.test.assertEquals

class LegacyEmissionFactorManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newLegacyEmissionFactorManifest("code-871", "name-724")
        val view = toLegacyEmissionFactorManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toLegacyEmissionFactorManifestViews(mutableListOf<LegacyEmissionFactorManifest>(item, item)).size, "views")
    }
}

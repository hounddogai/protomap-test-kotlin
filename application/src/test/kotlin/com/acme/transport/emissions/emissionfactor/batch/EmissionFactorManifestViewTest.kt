package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorManifest("code-91", "title-466")
        val view = toEmissionFactorManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorManifestViews(mutableListOf<EmissionFactorManifest>(item, item)).size, "views")
    }
}

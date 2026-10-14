package com.acme.transport.emissions.emissionfactor.profile

import kotlin.test.Test
import kotlin.test.assertEquals

class BatchedEmissionFactorManifestViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newBatchedEmissionFactorManifest("code-583", "label-687")
        val view = toBatchedEmissionFactorManifestView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toBatchedEmissionFactorManifestViews(mutableListOf<BatchedEmissionFactorManifest>(item, item)).size, "views")
    }
}

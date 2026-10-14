package com.acme.transport.emissions.emissionfactor.metric

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorSnapshotViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorSnapshot("code-456", "name-986")
        val view = toEmissionFactorSnapshotView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorSnapshotViews(mutableListOf<EmissionFactorSnapshot>(item, item)).size, "views")
    }
}

package com.acme.transport.emissions.emissionfactor.cycle

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorCheckpointViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorCheckpoint("code-469", "label-922")
        val view = toEmissionFactorCheckpointView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorCheckpointViews(mutableListOf<EmissionFactorCheckpoint>(item, item)).size, "views")
    }
}

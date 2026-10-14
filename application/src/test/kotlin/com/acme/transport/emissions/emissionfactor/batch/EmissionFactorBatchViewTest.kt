package com.acme.transport.emissions.emissionfactor.batch

import kotlin.test.Test
import kotlin.test.assertEquals

class EmissionFactorBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newEmissionFactorBatch("code-832", "label-329")
        val view = toEmissionFactorBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toEmissionFactorBatchViews(mutableListOf<EmissionFactorBatch>(item, item)).size, "views")
    }
}

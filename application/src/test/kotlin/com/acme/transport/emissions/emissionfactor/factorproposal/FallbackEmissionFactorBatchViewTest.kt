package com.acme.transport.emissions.emissionfactor.factorproposal

import kotlin.test.Test
import kotlin.test.assertEquals

class FallbackEmissionFactorBatchViewTest {
    @Test
    fun `shows the status in capitals`() {
        val item = newFallbackEmissionFactorBatch("code-61", "name-785")
        val view = toFallbackEmissionFactorBatchView(item)
        assertEquals("${item.status}".uppercase(), view.statusLabel, "status label")
        assertEquals(2, toFallbackEmissionFactorBatchViews(mutableListOf<FallbackEmissionFactorBatch>(item, item)).size, "views")
    }
}
